package com.hancockt.lol;

import java.util.Arrays;
import java.util.List;

// TODO this is just a template/example from AI, change EVERYTHING
public class UpgradeLibrary {
    public final Upgrade speedUpgrade = new Upgrade(
        "speed",
        "Movement Speed",
        5, // max level
        new int[]{50, 100, 200, 400, 800}, // cost per level
        new float[]{350f, 375f, 400f, 425f, 450f, 475f} // value at each level (index 0 = unupgraded)
    );

    public final Upgrade jumpUpgrade = new Upgrade(
        "jump",
        "Jump Height",
        5,
        new int[]{50, 100, 200, 400, 800},
        new float[]{350f, 365f, 380f, 400f, 420f, 440f}
    );

    public final Upgrade flareUpgrade = new Upgrade(
        "flares",
        "Free Flares",
        7, // upgrades from 3 to 10
        new int[]{100, 200, 300, 400, 500, 600, 700},
        new float[]{3, 4, 5, 6, 7, 8, 9, 10}
    );

    // collect all into a list for iteration
    public final List<Upgrade> all = Arrays.asList(
        speedUpgrade, jumpUpgrade, flareUpgrade
        // add more here
    );
}
