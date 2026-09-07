# POTOP — Design Doc

---

## Mantra

Make the simple stuff GREAT.

---

## Elevator Pitch

A precision platformer roguelite where Spark, a self-aware matchstick, climbs endlessly from a freezing flood —
collecting matches, upgrading his abilities, and trying very hard not to get extinguished.

---

## Core Gameplay Loop

**Tutorial** — teaches mechanics, light story/dialogue, no rising water until the end? Completing it unlocks Infinite
Mode.

**Infinite Mode** — the main game. Procedurally generated, rising water, collectibles throughout. Die, upgrade, climb
higher. The loop:

- Climb as high as possible
- Collect matches along the way
- Die (water extinguishes Spark)
- Spend matches on upgrades and tools
- Climb higher next run

**Factory** — unlocked at 15k (?) height or bought using firewood and matches from rare in-run fin do of a firewood. 
Passively generates firewood (prestige currency) based on tutorial PB and infinite mode high score. Firewood is used 
only for permanent upgrades. Research and development unlocks upgrade routes — each R&D project costs matches to start 
and requires a cumulative score threshold to complete, reduced each run.

---

## Player Mechanics

- Move (WASD / arrow keys)
- Jump — committed, no mid-air direction change
- Hold jump for variable height
- Fast fall / ground brake (hold down) — ice skates upgrade (not available on mobile)
- Wall jump — three variants:
    - Into wall (climb slowly)
    - Away from wall (fling horizontally)
    - Neutral (diagonal)
- Danger boost — closer to water = faster movement and higher jump
- Turn faucets (hold E/Shift [change?]) — slows water rise speed
- Flares — consumable restart from last checkpoint (start with 3 free, can upgrade for more to start, or buy as needed)

---

## Goal

Climb as high as possible before the flood extinguishes Spark. Collect matches. Find checkpoints. Spin faucets for
breathing room. Die eventually. Come back stronger.

Tutorial goal: reach the top and spin the master faucet to stop the flood.

---

## Setting and Thematic Logic

Spark is a matchstick with a flame afro running from a freezing flood. The flood is cold enough to flash-freeze surfaces
above it — which is why platforms are icy. The closer to the water, the icier and more slippery the platforms. Spark
doesn't drown — he gets extinguished. Fire vs. ice vs. water. The flood (Potop) is the antagonist. Spark is the
protagonist. The irony of a matchstick on ice running from a freezing flood is the whole joke.

Potop = Polish word for flood. Also a biblical reference (Noah's flood). Also the lol joke when he dies.

Death = Spark's flame goes out. Arms pop up. Turns into a floating "lol."

---

## Meta-Progression

**Matches** (active currency — earned in run):

- Upgrade movement speed, jump height, ice skates (friction control)
- Upgrade collectible spawn rate and value
- Buy tools (consumable per run)
- Buy flares (checkpoint restarts)
- Start R&D projects

**Firewood** (prestige currency — earned only by factory, very rare chance in run):

- Permanent upgrades only
- Platform size increase
- Start height/water offset (bypass easy section): pro upgrade, can get one time use of this too!
- Unlock cosmetic tree branches (?)
- more free flares

**Tools (consumable, purchased with matches):**

- Sodium tip — if water touches Spark, chemical reaction launches him upward. One use.
- Magnet — expands collectible pickup radius
- Auto-checkpoint — automatically saves checkpoint when passing a Y height
- Auto-spin faucets — spins nearby faucets without player input (no collectible bonus)
- Bigger platforms — one run only
- Lawyer — disables Spark's random refusal to cooperate for one run (?)

**Flares:**

- 3 free per run to start, upgradeable to 10
- Used on death (automatic animation) or intentional restart at checkpoint
- Two-tap to confirm (hold out flare, then ignite) to prevent accidental use
- After free flares used: buy one, use pre-purchased stock, or start fresh run with new seed

**Collectible bonuses:**

- Manual faucet spin = large match bonus
- Manual checkpoint activation = large match bonus
- Auto tools give no bonus (or limited)
- More matches spawn higher up

---

## Cosmetics

All cosmetics are code-based tinting (batch.setColor) — no extra art required for most.

Cosmetic tree structure — each purchase unlocks connected options:

- Default orange flame → blue flame → bunsen burner skin → science student outfit → ...
- Body color variants
- Wood grain skin (static sprite, not animated)

Spark has opinions about his cosmetics. He approves of some, disapproves of others. This is some of his best dialogue.

---

## Spark's Character

Self-aware. Knows he's in a game. Knows you're sitting there doing nothing while he does all the work. Complains
constantly. Breaks the fourth wall freely. Makes fun of you when you fail. Humor comes from his suffering vs. your
comfort.

Key scripted moments — everything else is improvised complaint:

- "I'm the MC. Of course I survive this." — then immediately drowns
- "Mr. Stick Man if you're nasty."
- "Do you know who I am? I am THE main character." — right before a humiliating death
- After buying blue flame: "Blue? Really? I'm a matchstick, not a bunsen burner. Fine."
- After setting cosmetic back to default: "Finally. Back to basics. This is who I am."
- Randomly refuses to move: crosses arms, won't cooperate until player uses Lawyer tool or he decides he's done with his
  break. This is never done in danger zone or held long enough to be dangerous. 

Dialogue also reacts to:

- Tools being used
- Upgrades purchased
- Cosmetics equipped
- Deaths (especially repeated deaths in the same spot)
- Faucets spun manually vs auto
- High scores beaten

---

## Audience

Players who want more skill expression than Subway Surfers or Jetpack Joyride but don't want the punishing tone of
Celeste or Getting Over It. Precision platformer fans who don't mind dying a lot as long as they're making meta-progress
between runs. Age roughly 16-28. Also anyone who thinks a self-aware matchstick complaining about a flood is funny,
which it is. Also for people who think idle games are too passive! 

---

## Genre / Tags

Precision platformer, roguelite, endless climber, meta-progression
Tags: 2D, Platformer, Roguelite, Endless, Precision Platformer, Difficult, Procedural Generation, Upgrades,
Meta-progression, Funny, Pixel Art

---

## Platform

Desktop first (Windows, itch.io). Android later if there's demand — requires separate physics config and touch control
design (joystick left side for direction, jump button right side).
