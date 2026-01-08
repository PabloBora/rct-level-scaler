package com.example.rctlevelscaler.scaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.example.rctlevelscaler.config.ConfigState;
import com.example.rctlevelscaler.config.ScalerConfig;

import com.gitlab.srcmc.rctapi.api.models.BagItemModel;
import com.gitlab.srcmc.rctapi.api.models.PokemonModel;
import com.gitlab.srcmc.rctapi.api.models.PokemonModel.StatsModel;
import com.gitlab.srcmc.rctapi.api.util.JTO;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerTeam;

import net.minecraft.server.level.ServerPlayer;

public final class TeamScaler {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;

    private TeamScaler() {}

    // ✅ Base MAX
    public static int getMaxPartyLevel(ServerPlayer player) {
        int max = 0;
        for (var pk : Cobblemon.INSTANCE.getStorage().getParty(player)) {
            max = Math.max(max, pk.getLevel());
        }
        return max;
    }

    // ✅ Base TOP2_AVG (nuevo)
    public static int getTop2AvgPartyLevel(ServerPlayer player) {
        int top1 = 0;
        int top2 = 0;

        for (var pk : Cobblemon.INSTANCE.getStorage().getParty(player)) {
            int lvl = pk.getLevel();
            if (lvl >= top1) {
                top2 = top1;
                top1 = lvl;
            } else if (lvl > top2) {
                top2 = lvl;
            }
        }

        if (top1 <= 0) return 0;
        if (top2 <= 0) return top1;
        return (top1 + top2) / 2;
    }

    // =========================
    //  (LEGACY) TrainerTeam API
    //  Lo dejamos intacto
    // =========================
    public static TrainerTeam scaleTrainerTeam(TrainerTeam original, int maxPlayerLevel) {
        if (original == null) return null;
        if (maxPlayerLevel <= 0) return original;

        ScalerConfig cfg = ConfigState.get();
        int minus = cfg.minus;
        int plus  = cfg.plus;

        int min = clamp(maxPlayerLevel - minus, MIN_LEVEL, MAX_LEVEL);
        int max = clamp(maxPlayerLevel + plus,  MIN_LEVEL, MAX_LEVEL);

        List<PokemonModel> src = original.getTeam();
        if (src == null || src.isEmpty()) return original;

        List<PokemonModel> scaled = new ArrayList<>(src.size());
        for (PokemonModel pm : src) {
            if (pm == null) continue;

            int newLevel = randomInclusive(min, max);
            scaled.add(cloneWithLevel(pm, newLevel));
        }

        String identity = original.getIdentity();
        String name = extractNameLiteral(original);

        JTO<BattleAI> ai = original.getAI();
        List<BagItemModel> bag = original.getBag();

        return new TrainerTeam(
                identity,
                name,
                original.getBattleFormat(),
                original.getBattleRules(),
                ai,
                bag,
                scaled
        );
    }

    private static String extractNameLiteral(TrainerTeam team) {
        if (team.getName() == null) return "Trainer";
        String lit = team.getName().getLiteral();
        return (lit != null && !lit.isBlank()) ? lit : "Trainer";
    }

    private static PokemonModel cloneWithLevel(PokemonModel pm, int newLevel) {
        List<String> heldItems = (pm.getHeldItems() != null) ? List.of(pm.getHeldItems()) : List.of();

        StatsModel ivs = (pm.getIVs() != null)
                ? new StatsModel(pm.getIVs().getHP(), pm.getIVs().getAtk(), pm.getIVs().getDef(),
                pm.getIVs().getSpA(), pm.getIVs().getSpD(), pm.getIVs().getSpe())
                : new StatsModel();

        StatsModel evs = (pm.getEVs() != null)
                ? new StatsModel(pm.getEVs().getHP(), pm.getEVs().getAtk(), pm.getEVs().getDef(),
                pm.getEVs().getSpA(), pm.getEVs().getSpD(), pm.getEVs().getSpe())
                : new StatsModel();

        return new PokemonModel(
                pm.getSpecies(),
                pm.getNickname(),
                pm.getGender(),
                newLevel,
                pm.getNature(),
                pm.getAbility(),
                Set.copyOf(pm.getMoveset()),
                ivs,
                evs,
                pm.isShiny(),
                heldItems,
                Set.copyOf(pm.getAspects()),
                pm.getGimmicks()
        );
    }

    // =========================
    // ✅ NPC Pokemon[] (en uso)
    // =========================

    // Mantengo tu firma original (random normal)
    public static Pokemon[] scaleNpcPokemonTeam(Pokemon[] original, int maxPlayerLevel) {
        return scaleNpcPokemonTeam(original, maxPlayerLevel, null);
    }

    // ✅ Nuevo overload con seed determinista (si seed == null usa random normal)
    public static Pokemon[] scaleNpcPokemonTeam(Pokemon[] original, int maxPlayerLevel, Long seed) {
        if (original == null) return null;
        if (maxPlayerLevel <= 0) return original;

        ScalerConfig cfg = ConfigState.get();
        int minus = cfg.minus;
        int plus  = cfg.plus;

        int min = clamp(maxPlayerLevel - minus, MIN_LEVEL, MAX_LEVEL);
        int max = clamp(maxPlayerLevel + plus,  MIN_LEVEL, MAX_LEVEL);

        final SplittableRandom seededRng = (seed != null) ? new SplittableRandom(seed) : null;

        Pokemon[] out = new Pokemon[original.length];

        for (int i = 0; i < original.length; i++) {
            Pokemon src = original[i];
            if (src == null) {
                out[i] = null;
                continue;
            }

            int newLevel = randomInclusive(min, max, seededRng);

            Pokemon copy = new Pokemon();
            copy.copyFrom(src);
            copy.setLevel(newLevel);


            out[i] = copy;
        }

        return out;
    }

    // =========================
    // Helpers
    // =========================

    private static int randomInclusive(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static int randomInclusive(int min, int max, SplittableRandom seeded) {
        if (min >= max) return min;
        if (seeded == null) return ThreadLocalRandom.current().nextInt(min, max + 1);
        return seeded.nextInt(min, max + 1);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
