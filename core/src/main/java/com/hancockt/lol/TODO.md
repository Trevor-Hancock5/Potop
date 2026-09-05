# POTOP
- Focus on getting players to play it rather than earning money, focus on feedback!
    - goal: 100 people to play it
- Need better conversion from impression to view to download!

- 25k high score!

---
## Roguelite
- Add collectibles (matches) spawning throughout infinite mode — more frequent higher up (and slight chance of firewood
  collectible)
    - checkpoints and faucets (manually used) get large bonus
      - So currency needs to splash out and then gravitate into Spark
    - make it so that if there is a match nearby there is a higher likelihood for another to spawn, and less likely if
      not. This makes it so that they are in patches!
- Implement persistent currency system (matches saved between runs)
- Make restart better or infinite mode cycle better (flares!)
    - Implement flare system — 3 free restarts per run, upgradeable to 10 via permanent upgrade, two-tap confirm to prevent accidents, prompt to buy on empty
    - Implement flare purchase with matches (cheap, hoverboard equivalent)
- Save/load system for all persistent progress (matches, firewood, upgrades, factory state, high scores, flare stock)
- Build upgrade shop UI screen
- Implement mechanical upgrades (speed, jump, friction - more friction; and reduce starting values); tools (magnet, auto checkpoint and
  faucet, bigger platforms for a run, sodium tip - one safety from water during run, pro start - up high with set distance
  from water); and permanent upgrades (very expensive, platform size, start height, and maybe match spawn rate. These use
  firewood not matches)
- implement prestige currency - firewood. Very rare in run, factory makes it. Remove chance in-run after factory unlocked
- Factory system: unlocks once set height reached (10k) or you found one rare firewood, and then big cost of matches to get.
  Firewood rate is based on tutorial PB + infinite high score. Upgrade factory using R&D, spend matches to start project
  then you need to pass a cumulative score threshold (reduced each run) to complete it. This also unlocks different
  upgrade routes - upgrade tree!!


## Music
- practice music!
  - Read about getting good at bosca ceoil blue and making that type of music!
  - Listen to game sound tracks and just learn what makes it good
  - Practice rhythm: 8 bars, get good at making a beat! Try different rhythm patterns
  - take a bass line you liked from rhythm practice, then practice melody by adding a second
    instrument on top of the bass. 
  - practice with a third instrument to fill space and 'add texture'
- sound effects: audacity (tool). Water sound when closer and heartbeat when really close
- practice how to do the looping?
- Either do thematic (so each screen is different music with clean transition), or a single track that repeats


## Art
- draw afrobonk, must have profile picture ready before final release!
- Potop animation
- Different platforms with themes (like swinging - jungle, more friction - sand, lava spouts - volcano, etc.)
- bigger player so they can see animations?
- celebration animatoin (spin), and idle roasting marshmallow spin!
- make wall jump animations (climb/jump away)
- make one version, then add logic for mirroring to match which way it's facing!
- use spark spinning as loading animation = )
- For art, separate fire from body (for skins and animations)
- fix regular jump animation
- see about combining animations, so like combining fire afro of running with body of jump. Like a priority list
  of body/fire afro animations and states
- Add juice: Add animations for feedback and responsiveness, small things (like when landing, or jumping, or 
  going through a platform). basically add animation/sound effect for every conditional
- Implement cosmetic tinting system — separate batch.draw calls for body and flame so each can be tinted independently
- Build cosmetic unlock tree — purchases unlock adjacent options (blue flame → bunsen burner → science student → ...)
- Try to make idle animation, roasting a marshmallow
- Try making 3 different flame sizes. so the closer you are to the water, the smaller it is, then if far enough it's the
    biggest size. But then animate all!
- After death when you use the flare, show Spark relighting his flame using the flare
- update platform art so good for wide or tall blocks!
- add background
- art for buttons/UI components

## UI/UX
- sign installation?
- Improve UI everywhere.
- Improve loading/creation of infinite mode to improve it
- simple controls: allow for custom control changes. Remove wall jump sideways
- Dialogue
- Add boxes for instructions or pop-ups
- Add enemies/moving platforms (or maybe simple is better?)
- Leaderboard (inspiration from subway surfers)
- More movement mechanics
- transitions between screens
- more feedback to the faucet (remove?)
- add functionality to see random seed, and be able to add random seed to use (surround in try/catch)
    - check it's same everytime!
- add settings for everything!
- credits screen

## Marketing
- don't start marketing until starting polish!
- learn marketing! yay. start right after demo to have time for a little traction before full release
    - don't share marketing with fam or friends, this is to get strangers interested
    - discord
    - reddit (experiment most with this) communities.
      - for each subreddit, understand your audience and then tailor towards that
      - get people interested
      - build credibility/relationships (when answering, DO NOT mention your game, just answer from experience)
    - game dev club
    - short form content - post to youtube, tiktok, and other platforms (none on phone!), use either narration or text
      overlay but focus on gameplay
    - show your progression (game jam, to demo, to final)
    - the skill is how to make people curious (how high can you climb)
    - when adding something interesting, record it and save for later
    - itch devlogs (worth it?): reddit should be the interesting parts, itch should have the lengthier description
    - consider substack
- what to talk about:
  -   (show difference from game jam to demo as proof of polish)
      (talk about how you focused on the mechanic polish/tuning first - making everything feel fair)
      (write substack, summarize for itch, then use interesting parts for reddit and short form content)
      (music is hard)
      (sketching transfers to pixel art)
      (why my game felt great even though it looked terrible)
      (EVERYTHING MUST LEAD TO ITCH PAGE - update all of it to final release when done)
      (prototype - demo - full release)


## Misc
- BUGS: collectibles can spawn inside platforms??? DO a base check that there isn't any overlap with any platforms!
- improve performance! seems slow! - Make an active platforms/collectibles that is within like 2k above and
  enough platforms to the previous 2 checkpoints
- Split game Screen class
- move runtime state into it's own class!
- make separate systems (physics, water, spawn, collision, etc.) for maintainability! - each has update()
- separate update (game state) from render (drawing)
- separate constants out for easier tuning (maybe even a file!)
- better tutorial
- How to teach controls b/c people don't read!
- More clear how to use checkpoints
- Have easy, normal, and hard modes
    - easy: more checkpoints, faucets, slower water, etc
    - Maybe for normal, something like this but slightly easier max difficulty for platforms!
    - hard mode: trick faucets (speed up water), and water faster, and platforms
- improve infinite mode generation: guarenteed way up in each half of the playing width!
- No water rising UNTIL input has been received!
- look through feedback and analyze what needs to change
- Make it so that the built gradle has the name Potop and the logo is Spark (or another one)
- add a very very secret easter egg, for only the most devout players that says I'm single lol
    - like bought all upgrades, and like beat thresholds for PR and height.
    - do it: basically add a chance for the golden faucet in infinite mode. After 20k or whatever is super high, you add
      a .0001% chance for the faucet to be gold. And then increase up to a max of 1%. "Spark has discovered the most important
      secret, His developer is single lol"
- Fix so that it works with different hz monitors
- Final game: add analytics tracking and sending. Send update (playtime, deaths, restarts, currency, # times opened, etc
  .) that sends when 'checkpoints' are reached, so game open, game close, death, interaction. This way I can have accurate
  and useful data to use for improving this game and knowledge for future games.
- Give spark personality! (maybe non-cooperation, with a lawyer tool that disables non-cooperation)
- Android/apple functionality, web functionality
- Use an asset manager to load all music and art at the very start of the game
- keep track of details - number of total deaths in campaign, speed of completing. Faucets and checkpoints in inifinite
  mode, score, time, score/minute.
- When you wall jump but stuck inside another wall, it shifts to the other wall's inside. Weird
- Look further in front?
