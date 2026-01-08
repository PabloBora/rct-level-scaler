package com.example.rctlevelscaler.mixin;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import com.example.rctlevelscaler.config.ConfigState;
import com.example.rctlevelscaler.config.ScalerConfig;
import com.example.rctlevelscaler.scaling.TeamScaler;

import com.gitlab.srcmc.rctapi.api.battle.BattleFormat;
import com.gitlab.srcmc.rctapi.api.battle.BattleRules;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer;

import net.minecraft.server.level.ServerPlayer;

@Mixin(value = com.gitlab.srcmc.rctapi.api.battle.BattleManager.class, remap = false)
public class BattleManagerMixin {

    // Firma exacta de startBattle (evita remapeo / lookup por nombre)
    private static final String START_BATTLE =
            "startBattle(Ljava/util/List;Ljava/util/List;Lcom/gitlab/srcmc/rctapi/api/battle/BattleFormat;Lcom/gitlab/srcmc/rctapi/api/battle/BattleRules;)Ljava/util/UUID;";

    // Contexto del startBattle actual (ThreadLocal = seguro por hilo)
    private static final ThreadLocal<Integer> RCT_BASE_LEVEL = new ThreadLocal<>();
    private static final ThreadLocal<Long> RCT_SEED = new ThreadLocal<>();

    @Inject(method = START_BATTLE, at = @At("HEAD"), remap = false)
    private void rctLevelScaler_captureContext(
            List<Trainer> participants1,
            List<Trainer> participants2,
            BattleFormat battleFormat,
            BattleRules battleRules,
            CallbackInfoReturnable<UUID> cir
    ) {
        ScalerConfig cfg = ConfigState.get();

        int baseLevel = 0;
        Long seed = null;

        if (participants1 != null && !participants1.isEmpty() && participants1.get(0) instanceof TrainerPlayer tp) {
            var sp = tp.getPlayer();
            if (sp instanceof ServerPlayer serverPlayer) {

                // baseMode: max | avg2
                if ("avg2".equals(cfg.baseMode)) {
                    baseLevel = TeamScaler.getTop2AvgPartyLevel(serverPlayer);
                } else {
                    baseLevel = TeamScaler.getMaxPartyLevel(serverPlayer);
                }

                // seedMode: none | real_day
                if ("real_day".equals(cfg.seedMode)) {
                    // día real estable (UTC) -> mismo día = mismos resultados
                    long day = LocalDate.now(ZoneOffset.UTC).toEpochDay();

                    // mezclamos con UUID del jugador para que no sea igual para todos
                    long playerMix = serverPlayer.getUUID().getMostSignificantBits()
                            ^ serverPlayer.getUUID().getLeastSignificantBits();

                    seed = day ^ playerMix;
                }
            }
        }

        RCT_BASE_LEVEL.set(baseLevel);
        RCT_SEED.set(seed);

        if (cfg.enableLogs) {
            System.out.println("[RCT-Level-Scaler] BattleManager.startBattle HEAD"
                    + " | baseMode=" + cfg.baseMode
                    + " | baseLevel=" + baseLevel
                    + " | seedMode=" + cfg.seedMode
                    + (seed != null ? (" | seed=" + seed) : ""));
        }
    }

    @Inject(method = START_BATTLE, at = @At("RETURN"), remap = false)
    private void rctLevelScaler_clearContext(
            List<Trainer> participants1,
            List<Trainer> participants2,
            BattleFormat battleFormat,
            BattleRules battleRules,
            CallbackInfoReturnable<UUID> cir
    ) {
        RCT_BASE_LEVEL.remove();
        RCT_SEED.remove();
    }

    @Redirect(
            method = "toBattleActor(Lcom/gitlab/srcmc/rctapi/api/trainer/TrainerNPC;)Lcom/cobblemon/mod/common/api/battles/model/actor/BattleActor;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gitlab/srcmc/rctapi/api/trainer/TrainerNPC;getTeam()[Lcom/cobblemon/mod/common/pokemon/Pokemon;"
            ),
            remap = false
    )
    private static Pokemon[] rctLevelScaler_scaleNpcTeam(TrainerNPC npc) {
        ScalerConfig cfg = ConfigState.get();

        // cachear una sola vez
        Pokemon[] original = npc.getTeam();

        Integer base = RCT_BASE_LEVEL.get();
        if (base == null || base <= 0) {
            return original;
        }

        Long seed = RCT_SEED.get();

        if (seed != null) {
            try {
                // algo estable del NPC (nombre visible). Alternativa: identity si existe.
                String npcName = npc.getName().getComponent().getString();
                seed = seed ^ (long) npcName.hashCode();
            } catch (Throwable ignored) {
                // si algo raro pasa, no rompemos nada: dejamos el seed original
            }
        }

        Pokemon[] scaled;
        try {
            scaled = TeamScaler.scaleNpcPokemonTeam(original, base, seed);
        } catch (Throwable t) {
            if (cfg.enableLogs) {
                System.out.println("[RCT-Level-Scaler] scaleNpcPokemonTeam FAILED: " + t);
            }
            return original;
        }

        if (scaled == null) {
            if (cfg.enableLogs) {
                System.out.println("[RCT-Level-Scaler] scaleNpcPokemonTeam returned null, using original");
            }
            return original;
        }

        if (cfg.enableLogs && original != null && original.length > 0 && scaled.length > 0) {
            try {
                Pokemon a = original[0];
                Pokemon b = scaled[0];
                System.out.println("[RCT-Level-Scaler] NPC team scaled"
                        + " | first=" + a.getSpecies().getName() + " " + a.getLevel()
                        + " -> " + b.getLevel());
            } catch (Throwable ignored) {}
        }

        return scaled;
    }
}
