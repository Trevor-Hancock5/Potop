# The MC — Design Doc

---

## Mantra
+ Make the simple stuff GREAT! 

---

## Elevator Pitch
+ A Fast-Moving 2D minimalistic platformer where you escape rising water by climbing higher and higher, and turning 
  faucets off as you go!

---

## Core Gameplay Loop
1. Advance through levels, hitting chechkpoints and turning off faucets
2. Complete the tutorial to unlock infinite mode
3. Finish all other levels to unlock underwater mode
4. Re-attempt levels in UW mode and do the new UW levels

---

## Player Mechanics
+ Move
+ Jump
+ Friction-stop or fall-fast
+ Turn faucets
+ Wall-jump
  + Jump into wall
  + Jump diagonally away from wall
  + Jump directly away from wall
+ Closer to water, higher jump

---

## Goal
Get to the end of the level and turn the winning faucet!

---

## Mechanic
AW = above water stuff
BW = below water stuff

So you are stick man and you are trying to avoid the rising water, going up and up and up.
- eventually, the water (probably) catches you and you turn into 'lol' and look like youre sinking.
  you need to take a breath and hold it before you get dragged under. You hit, turn to lol, float for a second
  then a shark or something drags you under the water. And now you have to float back to the surface (which is
  still rising) to continue the level. But the floating part is hard. There are air gaps you have to stop at and use
  but if you knock into anything you lose air. The less air, the slower you float; the more air, the fast you float
  up. Once on surface, you have a short time to reach a platform before you are dragged back down, also if you float up
  too slowly, the sharks drag you further down (where you already used the air).
- You have a hold breath, breath in, breath out keys. You want to breathe out then in, and hold before going under
  the water. Then you hold your breath the whole time and let it out to slow down if you need to navigate to an
  air pocket, then breathe in and hold. If you accidently stop holding your breath, you slowly leak air out.
  If you accidently breathe in while under water, you cough it out along with all your air. very short time to get
  air.
- After being in the water for the first time, you move slower and jump not as high. Why? Because you are soaking wet
  and exhausted. so falling into the water, makes it all harder!
- !!! - But this does mean, that after you fall, the only way to get past a section may be to fall into the water and
  rise above it! Some levels should be impossible without going under water (with maybe one like expert jump or play or
  smth - like super hard or the water)

---

















## What It Is
Stick man climbs through a Noah's flood. Water always rises. He always has something to say about it.

---

## V1 - AW Game Loop
- Route choice for player expression, water rising for risk, and checkpoints (built with memories?) for rewards
- Infinite mode: Increase water speed as player gets higher with a max speed. Have a random seed, so other players 
  can replicate and beat it. High scores! Section 1 rewards you with infinite mode. (Maybe a secret way to get 
  infinite mode without beating section 1, but with a skill check)
- Make modules to build levels with (wall jumps, big gaps, height, climbing sections, ...). Then use whiteboard to 
  build the levels with those module numbers

Future (not MVP) in AW section
- Moving platforms, water surges, crumbling platforms
- Memories could be glowing orbs representing places MC has been, where MC reflects on his memories there. A funny one,
would be one where he just keeps talking and talking until the water gets right up to him and the player has just a 
few seconds to escape lol. Could open up secret level challenges for hidden memories
- Things cause water to rise faster (faucets, pipe bursts, etc.) that MC must clog up to slow down water.

Note:
player size = 20 x 50
pixels/seconds horizontal: 400
pixels/seconds falling: 1200 (max cap!)
comfortable jump gap (no vert): 300
comfortable jump gap (diag): 200 x 80
max jump gap (no vert): 600
max jump gap (diag): 350 x 60
comfortable jump height: 75
max jump height: 108
danger boost: 1.25x

---

## The Stickman's Voice
THICK LEGS for stickman and super skinny upperbody (only ever hits leg day)!!

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
- could make a level that has a frame perfect jump, so it basically forces underwater. But theoretically, you can do it.
  Use the speed forces and everything to get max speed and max distance you can possibly jump. - physics ... yay
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
