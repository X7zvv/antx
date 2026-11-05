# Axiom Client (Fabric, Minecraft 1.21.11)

Client-side Fabric mod. Mojang mappings, Loom 1.14, Fabric API 0.141.1+1.21.11.

## Build (GitHub Actions does this for you, see below)
1. Install JDK 21.
2. Add the Gradle wrapper (not included, no network when this was generated):
   `gradle wrapper --gradle-version 9.2.1` (or copy gradlew/gradle/ from the Fabric example mod).
3. `./gradlew build` -> jar in `build/libs/axiom-client-1.0.0.jar`, drop in `mods/`.

## Commands (chat)
- `/axiom gui` opens a click GUI. `/axiom friend add|remove|list <name>`
- `/axiom list` | `/axiom toggle <module>` | `/axiom info <module>`
- `/axiom set <module> <key> <value>`
- `/axiom build <file>` (reads `.minecraft/schematics/<file>.litematic`), `/axiom build stop`

## Modules
PlayerESP, Fullbright, AutoSprint, Fly, TriggerBot, AimAssist, AutoTotem, AutoCrystal, CrystalPvP, SwordPvP, MacePvP, BaseAlert, AutoTPA, AutoSell, AutoBuy, AutoBuilder, AreaMiner, ElytraPilot, BlockFinder

## Notes
- AutoSell/AutoBuy read generic container GUIs (price from item lore, "Confirm" button by name). Tune `command`, `item`, `maxPrice` per server.
- Builder ignores block states (stairs/slab orientation) and needs materials in the hotbar.
- Settings reset on restart.

## Not included
SafeAnchor, AnchorHunter, PearlPrediction, CustomSky, AutoSpear, DeepPlayerFinder, ChunkFinder.
Settings (not on/off states) and friends persist in `config/axiom.json`.
