# RCT Level Scaler

**RCT Level Scaler** is a small addon mod for **Cobblemon** + **Radical Cobblemon Trainers (RCT)** that dynamically scales trainer Pokémon levels to match the player’s current progression.

This mod is designed for players who want more freedom to explore, survive, and progress at their own pace — without trainers becoming trivial or impossible due to level gaps.

---

## ✨ Why this mod exists

In my playthrough, I disabled RCT’s level cap because I wanted a **true survival experience** with Cobblemon:

- Hunger enabled  
- Hostile mobs enabled  
- Exploration (Nether, caves, structures) before gyms  
- Pokémon progression happening naturally alongside Minecraft progression  

### The problem

- I could easily **outlevel gyms** just by playing survival normally.  
- If I rushed gyms, I had to **pause exploration** to stay in range.  
- Trainers either became **too weak** or **would not battle me at all**.  

So instead of forcing a strict progression order, I built this mod so that:

> **Trainers adapt to you, not the other way around.**

Gyms, NPCs, and trainers always stay within a fair, configurable range of your current team — no matter when you decide to fight them.

---

## 🧠 What the mod does

When a battle starts against an RCT trainer:

- The trainer’s Pokémon levels are **recalculated dynamically**
- Levels are scaled to a configurable range around your party level
- **Stats are recalculated correctly** (not just the level number)
- Scaling can be **deterministic per day** (optional), so retrying a battle feels consistent

This applies to:

- Regular trainers  
- Gym leaders  
- Any RCT NPC that starts a battle  

---

## ⚙️ Features

### Base level modes
- `max` — strongest Pokémon in your party  
- `avg2` — average of your two strongest Pokémon  

### Configurable level range
- Example: `baseLevel ± 3`

### Deterministic daily scaling (optional)
- Same trainer, same day → same levels  
- New day → fresh roll  

### Per-player seeding
- Different players get different trainer teams  

### Fully configurable
- Enable / disable logs  
- Change base mode, range, and seed behavior  

---

## 🧩 Configuration

A config file is generated automatically on first launch:

```json
{
  "enableLogs": true,
  "baseMode": "max",
  "minus": 3,
  "plus": 2,
  "seedMode": "real_day"
}
```

### Config options

| Option      | Description                 |
|------------|-----------------------------|
| enableLogs | Enable debug logs           |
| baseMode   | `"max"` or `"avg2"`         |
| minus      | Levels below base           |
| plus       | Levels above base           |
| seedMode   | `"real_day"` or `"none"`    |

---

## ⚠️ Important: RCT config interaction

This mod **does not override RCT’s spawn or battle restrictions**.

RCT still controls:

- Whether trainers spawn near you  
- Whether trainers allow or force battles  
- Maximum allowed level difference  

If you want full freedom to battle trainers at any time, you may need to adjust RCT’s own config, for example:

- `Spawning.maxLevelDiff`  
- `Trainers.forceBattleMaxLevelDiff`  

Setting these values higher (e.g. `100`) allows trainers to spawn and battle regardless of level difference.

**This mod handles battle scaling, not spawn permission logic.**

---

## 🎯 Intended use case

This mod is **not** meant to replicate classic Pokémon progression.

It is meant for players who want:

- Open-ended exploration  
- Survival-first gameplay  
- No grinding just to stay “in range”  
- Gyms and trainers that remain relevant at any point  

If you prefer strict routes, level caps, and ordered gym progression, this mod may not be for you — and that’s okay.

---

## 🛠 Compatibility

- **Minecraft**: Fabric  
- **Cobblemon**: tested with `1.7.x`  
- **Radical Cobblemon Trainers**: required  

Works in singleplayer and multiplayer servers.  
Built as a clean Fabric addon — no datapacks required.

---

## 📦 Installation

1. Install **Cobblemon**
2. Install **Radical Cobblemon Trainers (RCT)**
3. Drop `rct-level-scaler-<version>.jar` into your `mods/` folder
4. Launch the game once to generate the config
5. Adjust the config if desired

---

## 📄 License

MIT License  
Free to use, modify, and include in modpacks.

---

## 💬 Final notes

This is a very specific QoL mod built for a particular playstyle — but if that playstyle matches yours, it makes **Cobblemon + RCT feel far more natural in a survival world**.
