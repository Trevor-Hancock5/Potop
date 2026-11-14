# Potop

**Play the Demo:** [afrobonk.itch.io/potop-demo](https://afrobonk.itch.io/potop-demo)

**Potop** is a 2D precision platformer roguelite built from scratch in Java and libGDX. You play as **Spark**, a self-aware matchstick trying to climb an endless vertical shaft to outrun a freezing flood—collecting matches, spinning faucets to buy time, and trying very hard not to get extinguished.

---

## Core Mechanics & Features

* **High-Inertia Precision Physics:** Custom 2D physics simulation built from the ground up with committed jumps (minimal mid-air control), variable jump heights, fast-falling, and three distinct wall-jump variants (wall climb, neutral diagonal, and horizontal launch).
* **Dynamic Risk/Reward (Danger Boost):** The closer the rising water gets to Spark, the faster his movement speed and higher his jump velocity scale.
* **Procedural Vertical Generation:** Seeded, band-based infinite level generation featuring dynamic difficulty scaling, spatial reachability validation, checkpoints, water-slowing faucets, and collectible currency.
* **Modular Systems Architecture:** Decoupled game loop isolating state management (`RunData`, `PlayerData`, `PersistentData`), rendering, physics, and feature subsystems (`WaterSystem`, `FaucetSystem`, `CurrencySystem`, `CheckpointSystem`).

---

## Controls

| Action | Keys |
| :--- | :--- |
| **Move Left / Right** | `A` / `D` or `Left` / `Right` |
| **Jump / Wall Jump** | `W`, `Up`, or `Space` |
| **Fast Fall / Ground Brake** | `S` or `Down` |
| **Interact (Faucets / Checkpoints)** | `E` / `Shift` |
| **Quick Restart / Full Reset** | `R` / `Q` |
| **Pause / Menu** | `P` / `M` |

---

## Project Structure

* `core/`: Main game logic, custom physics engine, procedural generation, state containers, and isolated subsystem drivers.
* `core/src/test/`: JUnit 5 automated test suites validating spatial collision avoidance, collectible reachability, and generation performance benchmarks.
* `lwjgl3/`: Desktop launcher configuration using LWJGL3.
* `assets/`: Sprites, animations, and UI assets.
