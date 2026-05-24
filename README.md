# ⚔ GloriousBattle

![GloriousBattle README](src/main/resources/assests/gloriousbattle_readme.png)

> A fully offline turn-based strategy game built with Java 21 + JavaFX. Choose your character, pick a map, and battle AI opponents on a 10×10 grid.

## 🚀 Getting Started

**Option 1 — Clone & run (dev)**
```bash
git clone https://github.com/NCKBill/Turn-Based-Final
cd Turn-Based-Final
mvn clean javafx:run
```

**Option 2 — Run JAR file**
```bash
java -jar GloriousBattle.jar
```
> Requires Java 21 + JavaFX 21

## 🎮 Game Modes

| Mode | Description |
|------|-------------|
| Player vs AI | Control one unit, allies are AI-driven |
| AI vs AI | Fully automated spectator match |

## 🧙 Unit Classes

| Class | HP | STR | POW | DEF | MDEF | MP | Role |
|-------|----|-----|-----|-----|------|----|------|
| Tank | 100 | 5 | 0 | 10 | 10 | 3 | Frontline bruiser |
| Rogue | 70 | 30 | 0 | 5 | 5 | 4 | High-damage melee |
| Mage | 60 | 2 | 10 | 5 | 5 | 2 | Ranged magic attacker |
| Healer | 60 | 2 | 20 | 4 | 15 | 2 | Support caster |

## 🗺 Maps

| Grassland | Forest | Mixed terrain | Wall map |
|-----------|--------|---------------|----------|
| Open field, no obstacles | Tree border walls | Water rivers + trees | Mountain choke points |

## 🌿 Terrain

| Terrain | Move cost |
|---------|-----------|
| Grass | 1 — free |
| Water | 2 — slows movement |
| Trees | 2 — slows movement |
| Mountain | Impassable |

## ⚔ Actions

| Action | Formula |
|--------|---------|
| Physical attack | `max(0, val + STR − DEF)` |
| Magic attack | `max(0, val + POW − MDEF)` |
| Defense attack | `max(0, val + DEF − DEF)` |
| Heal | `val + POW (healer)`, capped at max HP |

## 📖 How to Play

1. **Select character** — pick Tank, Rogue, Mage, or Healer. Remaining allies are AI-controlled.
2. **Pick a map** — choose from 4 preset maps with different terrain layouts.
3. **Initiative roll** — each round, units roll dice + stats to determine acting order.
4. **Move** — click a highlighted cell. Dijkstra's algorithm finds the shortest path within your MP.
5. **Act** — select a skill from the action bar, then click a valid target. Each action costs AP.
6. **Win** — eliminate all enemy units. The game resets to the menu after each match.

## 🛠 Tech Stack

- **Language**: Java 21
- **GUI**: JavaFX 21.0.2
- **Build**: Apache Maven 3
- **IDE**: IntelliJ IDEA / VS Code

## 👥 Team GloriousBattle — COMP1020 Spring 2026

| Name | Role |
|------|------|
| Phung Gia Khanh | Leader |
| Nguyen Canh Ky | Main Developer |
| Pham Bui My Linh | Co-Developer |
| Tran Chi Minh | Member |
| Ninh Thi Viet Ha | Member |
