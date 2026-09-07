# Code Style Guide

This guide prioritizes maintainability, testability, and making new features easy to add.

These are strict guidelines. Break a rule only when there is a clear reason.

## 1. One Clear Responsibility

Each class must have one clear responsibility.

Test: Describe the class in one short sentence without using "and."

Bad: A GameScreen handles physics, rendering, spawning, input, and currency.

Good: A CurrencySystem manages currency.

---

## 2. Methods Must Be 50 Lines or Fewer

A method must not exceed 50 lines.

If a method becomes longer, split it into smaller methods with meaningful names.

Do not create methods such as updatePart1 or updatePart2.

---

## 3. Classes Must Be 300 Lines or Fewer

A class must not exceed 300 lines.

If it becomes larger, split responsibilities into separate classes.

---

## 4. Important State Has One Owner

Every important piece of game state must have one clear owner.

Other classes may read the state or request changes, but multiple classes must not own separate versions of the same state.

Example:

Good:

* PlayerStats owns player speed.
* PhysicsSystem reads player speed.
* UpgradeSystem modifies PlayerStats.

Bad:

* Player stores speed.
* PhysicsSystem stores speed.
* UpgradeSystem stores another speed value.

---

## 5. Persistent State and Runtime State Must Be Separate

Saved data must not contain temporary runtime data.

Persistent state includes:

* Currency
* Upgrades
* High scores
* Settings

Runtime state includes:

* Velocity
* Active platforms
* Current run score
* Water position

---

## 6. No Tunable Magic Numbers

Values likely to be adjusted during balancing must have a meaningful name and be centralized.

Bad:
A movement calculation contains the value 37.5 directly.

Good:
The movement calculation uses a named value such as WATER_ACCELERATION.

---

## 7. Base Values Must Be Separate From Modifiers

Do not permanently overwrite a base value when applying an upgrade or temporary effect.

Current values should be calculated from:

* Base value
* Permanent modifiers
* Temporary modifiers

Example:

Base Speed × Upgrade Modifier × Temporary Modifier = Current Speed

---

## 8. Feature Rules Belong to Their Feature

A feature's rules must live in the system responsible for that feature.

Do not add feature-specific logic to unrelated systems.

Bad:
PhysicsSystem checks whether the player has a Magnet upgrade.

Good:
CurrencySystem checks whether the Magnet affects currency.

---

## 9. UI Must Not Own Game Logic

UI displays information and requests actions.

Gameplay systems validate and perform actions.

Bad:
A shop button checks currency, subtracts currency, and unlocks an upgrade.

Good:
The shop button requests a purchase.
The UpgradeSystem validates the purchase and performs it.

---

## 10. Update and Rendering Must Be Separate

Rendering must not change gameplay state.

Update changes game state.

Rendering reads and displays game state.

---

## 11. Maximum Nesting Depth of 3

For if and loop statements, they must not be nested more than 3 deep. Otherwise, separate using methods!

---

## 12. Use Seeded RNG for Reproducible Gameplay Randomness

Gameplay randomness that may need to be reproduced must use a controllable RNG with a known seed.

This is especially important for procedural generation.

A seed should allow the same gameplay generation to be reproduced.

---

## 13. Avoid Boolean Explosion

Do not use multiple booleans to represent mutually exclusive states.

Use an enum or state object instead.

Bad:

* isPlaying
* isDead
* isPaused
* isRestarting

Good:
A GameState enum containing:

* PLAYING
* DEAD
* PAUSED
* RESTARTING

---

## 14. Add Tests When They Are Simple and Valuable

Add an automated test when:

* The behavior is simple to test.
* The test is reasonably easy to write.
* The test could prevent a worthwhile bug.

Example:
Generate many platform bands and verify that platforms and collectibles do not overlap.

---

## 15. For any new art/music/assets, ensure to add to appropriate class and load at start!

When adding new music, art, or whatever asset, make sure to add it to the asset manager class
and that they are all loaded and prepared BEFORE starting the game!
