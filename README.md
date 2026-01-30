# 🧨 Creeper Apocalypse Escalation

**A Minecraft Fabric mod that turns your world into a creeper-infested nightmare!**

Every mob spawn becomes a creeper. Every day, more spawn. Can you survive?

![Minecraft 1.21.11](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)

---

## 📋 Features

### 🔄 Mob Replacement System
- **ALL mobs spawn as Creepers!** (with exceptions)
- Exceptions that remain normal:
  - ⚡ Blazes (you still need blaze rods!)
  - 🏠 Villagers (for trading)
  - 🐉 Ender Dragon (final boss remains)
  - 🤖 Iron Golems (optional, configurable)

### 📈 Auto-Escalation System
- **Day 1**: Normal spawn rates (1x)
- **Each day**: Spawn multiplier increases by 1
- **Configurable cap**: Default 10x (max 20x)
- **Automatic progression**: No manual triggers needed!

```
Day 1:  1x spawns
Day 2:  2x spawns
Day 3:  3x spawns
...
Day 10: 10x spawns (chaos!)
```

### 🎯 Milestone Events

#### Day 5: 🩸 Blood Moon
- All creepers glow with a crimson aura
- Visible through walls (glowing effect)
- Maximum creepiness!

#### Day 7: ⚡ Charged Day
- 25% of spawned creepers are CHARGED
- Percentage increases each day after
- Watch out for massive explosions!

#### Day 10: 💀 The Swarm
- Special announcement and effects
- +50% spawn rate bonus
- Creepers get speed boost
- True survival horror!

### 🧬 Special Creeper Variants

9 unique creeper variants with different abilities!

| Variant | Size | Speed | Special Ability |
|---------|------|-------|-----------------|
| 🐁 **Mini Creeper** | 50% | Fast | Tiny, hard to spot |
| 🦖 **Giant Creeper** | 200% | Slow | Massive explosion radius |
| 🕷️ **Spider Creeper** | 100% | 120% | Can climb walls! |
| 🥷 **Ninja Creeper** | 90% | Fast | Invisible when not moving |
| 🌈 **Rainbow Creeper** | 100% | Normal | Drops random loot |
| 🏀 **Bouncy Creeper** | 100% | Normal | Bounces super high |
| 🤠 **Jockey Creeper** | 110% | Normal | Rides other creepers |
| 💖 **Happy Creeper** | 90% | Normal | Friendly, buffs players |
| ⚡ **Lightning Creeper** | 140% | Fast | Always charged, boss-tier |

**Lightning Creeper** - Day 7+ Special:
- **Super Charged** variant has PURPLE AURA (instead of blue) (not working lol)
- Spawns purple particles
- Increased speed and deadlier explosion

All variants spawn from Day 3+, with spawn chances configurable in settings!

### 📊 Statistics Tracking

Track your survival with detailed stats:
- ☠️ Creeper death counter
- 🗡️ Creepers killed
- 💥 Explosions survived
- ⚠️ Near misses (survived explosion < 3 blocks away)
- 🏆 Survival streak (days without creeper death)
- 📈 Global statistics (total spawned, killed, explosions)

### 🎮 GUI Features

**Pause Menu Button**: "Creeper Challenge Settings"
- ✅ Enable/Disable challenge
- 🎚️ Max spawn multiplier slider (1-20x)
- ⏱️ Escalation speed (0.5x/1x/2x)
- 🧬 Special variants toggle + per-variant chances
- 🔄 Reset challenge button
- 📅 Current day display

**Stats Overlay** (Press J):
- Real-time stats display
- Personal and global statistics
- Milestone status

### 📺 Streaming/Content Features
- Chat stats command
- Auto-screenshot on death
- Death replay system (last 5 seconds)

---

## 🔧 Installation

### Requirements
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.16.0+
- Fabric API

### Steps
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download Creeper Apocalypse Escalation
4. Place both `.jar` files in your `mods` folder
5. Launch Minecraft with Fabric profile

---

## ⚙️ Configuration

Config file: `config/creeper-apocalypse.json`

### General Settings
```json
{
  "enabled": true,
  "showWelcomeMessage": true
}
```

### Escalation Settings
```json
{
  "baseSpawnMultiplier": 1.0,
  "maxSpawnMultiplier": 10.0,
  "escalationRate": 1.0,
  "escalationSpeed": 1.0
}
```

Spawn rate formula: `base + (day - 1) * rate * speed`

### Mob Replacement
```json
{
  "replaceHostileMobs": true,
  "replacePassiveMobs": false,
  "replaceNeutralMobs": true,
  "keepBlazes": true,
  "keepVillagers": true,
  "keepEnderDragon": true,
  "keepIronGolems": true
}
```

### Milestones
```json
{
  "milestonesEnabled": true,
  "bloodMoonEnabled": true,
  "chargedDayEnabled": true,
  "chargedDayChance": 0.25,
  "swarmDayEnabled": true
}
```

### Special Variants
```json
{
  "specialVariantsEnabled": true,
  "miniCreeperChance": 0.1,
  "giantCreeperChance": 0.05,
  "spiderCreeperChance": 0.08,
  "ninjaCreeperChance": 0.05,
  "rainbowCreeperChance": 0.03,
  "bouncyCreeperChance": 0.06,
  "jockeyCreeperChance": 0.04,
  "happyCreeperChance": 0.02,
  "lightningCreeperChance": 0.01
}
```

### Quality of Life
```json
{
  "deathCounterEnabled": true,
  "statsTrackingEnabled": true,
  "nearMissCounterEnabled": true,
  "nearMissDistance": 3.0,
  "keepInventoryOnDeath": false
}
```

### Streaming Features
```json
{
  "autoScreenshotOnDeath": false,
  "deathReplayEnabled": false
}
```

---

## ⌨️ Keybindings

| Key | Action |
|-----|--------|
| K | Open Challenge Settings |
| J | Toggle Stats Overlay |
| ESC > "Creeper Challenge Settings" | Full settings GUI |

---

## 🏗️ Building from Source

### Requirements
- JDK 21
- Gradle 8.0+

### Build Steps
```bash
# Clone the repository
git clone https://github.com/Kabirsharmasans/creeper-apocalypse.git
cd creeper-apocalypse

# Build the mod
./gradlew build

# Find the output
# => build/libs/creeper-apocalypse-escalation-1.0.0.jar
```

### Development
```bash
# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer
```

---

## 🤝 Compatibility

### Tested With
- Fabric API ✅
- Mod Menu ✅
- Sodium ✅
- Iris Shaders ✅

### Known Issues
- None currently reported

---

## 📜 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/creeper stats` | Show current challenge stats | All |
| `/creeper reset` | Reset challenge to Day 1 | OP |
| `/creeper day [number]` | Set current day | OP |
| `/creeper toggle` | Toggle challenge on/off | OP |

---

## 📝 Changelog

### v1.0.0 - Initial Release
- Mob replacement system (all mobs become creepers)
- Auto-escalation system (spawn rates increase daily)
- Milestone events (Blood Moon, Charged Day, The Swarm)
- 9 special creeper variants (Mini, Giant, Spider, Ninja, Rainbow, Bouncy, Jockey, Happy, Lightning)
- Lightning Creeper with Super Charged variant (purple aura)
- Full statistics tracking
- GUI settings screen with configurable spawn limits and "No Day" mode
- Networking for multiplayer support
- Streaming features (death replay, auto-screenshot)

---

## 🐛 Bug Reports

Found a bug? Please report it on our [GitHub Issues](https://github.com/Kabirsharmasans/creeper-apocalypse/issues) with:
- Minecraft version
- Mod version
- Steps to reproduce
- Crash log (if applicable)

---

## 📄 License

MIT License - Feel free to use, modify, and distribute!

---

## 💚 Credits

- Inspired by challenge runs and content creators
- Built with Fabric and love for chaos
- Special thanks to the Fabric community

---

**Good luck surviving the Creeper Apocalypse! 🧨💀**
