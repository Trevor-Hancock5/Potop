## The MC — Design Doc

---

## What It Is
Stick man climbs through a Noah's flood. Water always rises. He always has something to say about it.

---

## The Stickman's Voice
His legal name may be Mr. Stick Man, but he knows he is THE main character, so he goes by MC. hence the game name, 
the MC. 
Self aware and bitter about it. He knows he's in a game. He knows YOU'RE sitting there doing nothing while HE does all the athletic work. He complains constantly, breaks the fourth wall freely, and makes fun of you when you fail. The humor is his suffering vs your comfort.

Key moments are scripted — everything else is improvised complaint.

Have scripts or a set of dialogue he uses at certain sections, like harder parts he will be overconfident. Also to 
progress the story

Could definitely do a character arc, where he turns from a seriously arrogant and confident stick man to one of more 
of a relaxed and better spot (or whatever)! 

---

## Core Movement Rules
Gameplay feel beats realism. Realism guides decisions.
- Committed jumps — no mid-air direction change
- Momentum → jump height conversion
- Danger boost near water
- Wall jump = one redirect, not a free pass
- Platforms passable from below

---

## The World
Flooding Noah's Arc. Mostly unexplained. Hints over exposition.
Progression: small town → city → mountain → open water → boat → skyscraper → Everest → space (where everything breaks)
Each level is long. Falling far is part of the game.

---

## Above/Below Water
Two states. Above = escape. Below = survive.
The transition is scripted the first time — stickman breaks fourth wall, dives in himself.
The drag-down creature changes per setting. Shark for most of the game until he complains about it always being a shark. Black hole in space.

---

## Underwater
Breath is your resource. Air pockets live under platforms — semicircle of trapped air, single use.
Float speed = breath held. Obstacles cost air. Breathe in underwater = lose everything.
Drown completely = checkpoint, not full restart.

---

## Difficulty
Early levels: no water mechanic. Learn to climb.
Mid game: water introduced via 4th wall break.
Late game: some sections only passable via UW. Some only passable dry.
Always your fault. Never unfair.

---

## Game Jam Target (27th)
Ship a polished AW section. One complete level. Tight movement. Funny stickman. Rising water. Death and restart loop. BW is a stretch goal.

---

## Accessibility — "Fine, I'll Do It Myself"
After X deaths on the same section, stickman breaks the fourth wall: "really? you can't do this?" then "fine, I'll do it myself."
He then completes the section using the developer's recorded inputs — real positions, real timings, looks authentically human. Once through the hard section, control returns to the player.
Player can decline it. Stickman is annoyed if they do.

A few things to think about for later:
Recording — you'd save your position, velocity, and inputs at each frame timestamp when you do a successful run. Replay just sets position/velocity directly each frame instead of calculating physics.
Triggering — what's X? Mario uses 8 deaths. Yours could be more, fits a harder game. Or make it the stickman's decision, seemingly random, which is funnier.
Skipping — does using it give you a badge of shame? A counter somewhere showing how many times the stickman had to bail you out? That's very on brand.
This also solves your "I quit if I can't beat level one" concern perfectly. Good design solving two problems at once.

---

## MC Dialogue Examples
"I'm the MC. Of course I survive this." — then immediately drowns
"Mr. Stick Man if you're nasty"
"Do you know who I am? I am THE main character." — right before a humiliating death
When the stickman takes over for a bad player: "MC does not miss. Watch and learn."
After getting dragged by a shark: "Nobody drags the MC. NOBODY."
