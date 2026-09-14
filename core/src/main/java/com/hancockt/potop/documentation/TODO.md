# POTOP
- Focus on getting players to play it rather than earning money, focus on feedback!
    - goal: 100 people to play it
- Need better conversion from impression to view to download!

- 25k high score!



  ***Go top to bottom for milestones but then any order w/i a milestone!***

- Added top, right, getRect for features. As you add new stuff use that and then replace as you go!
- Trim parameters when possible

# Milestones:
## Bug/code fix

- improve performance! seems slow! 
    - Make an active objects list (platforms, checkpoints, faucets, ...)
      - Make it once and then store it and update it - no streams!
      - Only update every few seconds (not every update!) and if the player has moved more than 500 px up
      - Make sure current lists are sorted?
      - If sorted, then clear active lists when updated and then loop through all (continue when too low, break when too high)
      - If not sorted, then don't bother sorting, just loop through all of it with no break! (i think use continue!)
-  don't use streams or for each loops!

## Necessary Improvements!
- less dense higher? more dense lower?
- less platforms by bounding walls?
- improve wall buffer (when you jump up through a tiny platform, don't nudge them onto the edge of the wall if they have
  - big veloY and low veloX (absolute value). Kinda sketchy and annoying!
- setting to turn off screen shake!
- nerf faucets? (probably less of them as you go higher!) 
- Denser level generation
- improve infinite mode generation: guarenteed way up in each half of the playing width!
- fix regular jump animation
- update platform art so good for wide blocks!
- make wall jump animations (climb/jump away)
- add logic for mirroring animations/art to match which way it's facing!
- add a very very secret easter egg, for only the most devout players that says I'm single lol
    - like bought all upgrades, and like beat thresholds for PR and height.
    - do it: basically add a chance for the golden faucet in infinite mode. After 20k or whatever is super high, you add
      a .0001% chance for the faucet to be gold. And then increase up to a max of 1%. "Spark has discovered the most important
      secret, His developer is single lol"
- Make a learning mode: infinite mode with adjusted settings.
    - limited currency (no prestige), none after set height
    - reset high score after beating learning mode
    - Add brief prompt with minimal settings (just left right and jump, and a note about settings)
    - when within range of wall jump, add a prompt for that (pause and prompt the user)
    - prompt for flare/restart/checkpoint/faucet. Basically prompt on first interaction
    - slower water
    - higher density platforms but also higher variety!
    - more checkpoints and faucets (less currency from these though!)
    - Remove tutorial? and thus remove 'INF' or space for tutorial on starter page. Also water fix zone and stuff
      - Make it so that beginner mode is the default and once you reach a set y, you earn standard mode and beginner mode 
        is disabled on the next run and just auto standard. You should earn lots of currency for that gap! 
      - Once set y is reached, it notifies them and tells them to start a new run to start on standard mode! (and to earn more matches)
      - speed up water to influence them into standard (no option to re-enter beginner)
      - And the end of beginner mode should feel similar to the start/middle of standard mode! 
      - No option to skip beginner mode, make players prove they are ready for standard mode
        - Must be able to complete in under 1 minute for me?
- How to teach controls b/c people don't read!
    - More clear how to use checkpoints
- better tutorial or how to better do it? maybe just a set seed for tutorial and then overlay instructions
    - maybe just note that all controls are found in pause menu!



## Features
- start marketing (just a little)
- record as you add features for marketing
- keep track of details - number of total deaths in campaign, speed of completing. Faucets and checkpoints in infinite
    mode, score, time, score/minute.
- Implement persistent currency system (matches saved between runs)
- Save/load system for all persistent progress (matches, firewood, upgrades, factory state, high scores, flare stock)
- Build upgrade shop UI screen
- Implement upgrades:
    - mechanical upgrades (speed, jump, friction - more friction; and reduce starting values);
      - check infinite mode difficulty cap and check guarenteed reachability at various heights what level of upgrades you need
        or maybe even you have a guarenteed reachability at half upgrades?
    - tools (magnet, auto checkpoint and faucet, bigger platforms for a run, sodium tip - one safety from water during
      run, pro start - up high with set distance from water);
- Make restart better or infinite mode cycle better (flares!)
    - Implement flare system — 3 free restarts per run, upgradeable to 10 via permanent upgrade, two-tap confirm to prevent accidents, prompt to buy on empty
    - Implement flare purchase with matches (cheap, hoverboard equivalent)
- Factory system: unlocks once set height reached (10k) or you found one rare firewood, and then big cost of matches to get.
  Firewood rate is based on tutorial PB + infinite high score. Upgrade factory using R&D, spend matches to start project
  then you need to pass a cumulative score threshold (reduced each run) to complete it. This also unlocks different
  upgrade routes - upgrade tree!! remove firewood chance in run when factory is unlocked
    - permanent upgrades (very expensive, platform size, start height, and maybe match spawn rate. These use
      firewood not matches)
- add sound effects: audacity (tool). Water sound when closer and heartbeat when really close, collectibles!
    - check copyright!
- practice music!
    - Read about getting good at bosca ceoil blue and making that type of music!
    - Listen to game sound tracks and just learn what makes it good
    - Practice rhythm: 8 bars, get good at making a beat! Try different rhythm patterns
      - instruments: 2 square waves, triangle wave, and noise(drums)
    - take a bass line you liked from rhythm practice, then practice melody by adding a second
      instrument on top of the bass.
    - practice with a third instrument to fill space and 'add texture'
- LOTS OF MARKETING


## Polish
- goal: ensure that even with knowing exactly what you're doing AND all upgrades, that you still can mess up and no climb effortlessly
  because that's where replayability comes from. 
- practice how to do the looping? - music
- Make final music: Either do thematic (so each screen is different music with clean transition), or a single track that repeats
- art for buttons/UI components
- Add juice: Add animations for feedback and responsiveness, small things (like when landing, or jumping, or
  going through a platform). basically add animation/sound effect for every conditional
- LOTS OF MARKETING
- bigger player so they can see animations? (test out like a slight increase, so like 60x24)
- add background
- water!
- For art, separate fire from body (for skins and animations) (head must always be in same location ALWAYS)
    - test this for just the run and jump animation to check it is good first!
    - see about combining animations, so like combining fire afro of running with body of jump. Like a priority list
- Add boxes for instructions or pop-ups
- simple controls: allow for custom control changes.
- If user is trying to move cursor, show cursor, if not moving for x seconds dissappear
- Improve UI everywhere: menu, how to move between screens, HUD!!!


## Prep for release
- MARKET HARDCORE
- Start drafting itch page, steam page, etc. 
- Get playtesters for final testing before full release!
- Make it so that the built gradle has the name Potop and the logo is Spark (or another one)
- sign installation? steam?
- look through feedback and analyze what needs to change
- Potop animation (waves)
- draw afrobonk, must have profile picture ready before final release!
- Final game: add analytics tracking and sending. Send update (playtime, deaths, restarts, currency, # times opened, etc
    .) that sends when 'checkpoints' are reached, so game open, game close, death, interaction. This way I can have accurate
    and useful data to use for improving this game and knowledge for future games.
    - could track, save to file then ask playtesters to manually send that to you!
    - There are services that can handle the analytics set up for you!



## Dream features
- add functionality to see random seed, and be able to add random seed to use (surround in try/catch)
    - It is the same every time (could make test!)
- Look further in front???
- Give spark personality! (maybe non-cooperation, with a lawyer tool that disables non-cooperation)
- New mechanic! - make it so that when you hit down with 0 y velocity (standing), you flip upside down (no art, just programming?)
  and then you fall down through the top of the platform?
- Add enemies/moving platforms (or maybe simple is better?)
- Leaderboard (inspiration from subway surfers)
- Android/apple functionality, web functionality
- Make the currency "point" (rotate) to where it's heading, so that it looks like it's going towards Spark flame!
- After death when you use the flare, show Spark relighting his flame using the flare
  of body/fire afro animations and states
- use spark spinning as loading animation = )
- Try making 3 different flame sizes. so the closer you are to the water, the smaller it is, then if far enough it's the
  biggest size. But then animate all! (could be the layers yellow, orange, and then red too)
- heat color coming off spark, so changes to background somehow!
- Implement cosmetic tinting system — separate batch.draw calls for body and flame so each can be tinted independently
- Build cosmetic unlock tree — purchases unlock adjacent options (blue flame → bunsen burner → science student → ...)
- Different platforms with themes (like swinging - jungle, more friction - sand, lava spouts - volcano, etc.)
- celebration animatoin (spin), and idle roasting marshmallow spin!
- Dialogue
- transitions between screens
- add settings for everything!
- credits screen
- Fix the paramter drilling/deep coupling/dependency! - future
  - Better state machine!




## Marketing Reference
- Sites:
  - discord
  - reddit (experiment most with this) communities.
  - for each subreddit, understand your audience and then tailor towards that
  - get people interested
  - build credibility/relationships (when answering, DO NOT mention your game, just answer from experience)
  - game dev club
  - short form content - post to youtube, tiktok, and other platforms (none on phone!), use either narration or text
      overlay but focus on gameplay
  - itch devlogs (worth it?): reddit should be the interesting parts, itch should have the lengthier description
  - consider substack
- what to talk about:
  - show your progression (game jam, to demo, to final)
  - constant code organization
  - the skill is how to make people curious (how high can you climb)
  - when adding something interesting, record it and save for later
  - show difference from game jam to demo as proof of polish
  - talk about how you focused on the mechanic polish/tuning first - making everything feel fair
  - write substack, summarize for itch, then use interesting parts for reddit and short form content
  - music is hard
  - sketching transfers to pixel art
  - why my game felt great even though it looked terrible
  - EVERYTHING MUST LEAD TO ITCH PAGE - update all of it to final release when done
  - prototype - demo - full release
