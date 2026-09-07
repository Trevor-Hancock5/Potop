package com.hancockt.potop.level;

import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Platform;

import java.util.ArrayList;
import java.util.List;

public class ModuleLibrary {
    public static final List<Module> ALL_MODULES = new ArrayList<>();
        static {
            ALL_MODULES.add(new Module(
                "walk_and_run",
                new Vector2(250, 20), //end offset
                new Platform[]{
                    new Platform(0,0,250,20)
                } // no obstacles, just flat ground handled separately
            ));

            ALL_MODULES.add(new Module(
                "small_jump",
                new Vector2(250,85),
                new Platform[]{
                    new Platform(150,65,100,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "variable_jumps",
                new Vector2(1500,160),
                new Platform[]{
                    new Platform(300,0,100,20),
                    new Platform(750,0,100,20),
                    new Platform(1000,80,100,20),
                    new Platform(1400,140,100,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "momentum",
                new Vector2(1665,20),
                new Platform[]{
                    new Platform(150,0,50,20),
                    new Platform(350,0,70,20),
                    new Platform(700,0,90,20),
                    new Platform(1150,0,60,20),
                    new Platform(1545,0,120,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "vert_jumping_series",
                new Vector2(640,390),
                new Platform[]{
                    new Platform(25,60,30,30),
                    new Platform(0,175,85,15),
                    new Platform(15,265,90,15),
                    new Platform(440,220,150,80),
                    new Platform(540,375,100,15)
                }
            ));

            ALL_MODULES.add(new Module(
                "falls",
                new Vector2(1350,440),
                new Platform[]{
                    new Platform(30, 70, 130, 15),
                    new Platform(30, 165, 130, 15),
                    new Platform(30, 260, 130, 15),
                    new Platform(30, 350, 130, 15),
                    new Platform(30, 445, 130, 15),
                    new Platform(30, 535, 130, 15),
                    new Platform(30, 630, 130, 15),
                    new Platform(30, 725, 130, 15),
                    new Platform(30, 820, 130, 15),
                    new Platform(30, 915, 130, 15),
                    new Platform(30, 1010, 130, 15),
                    new Platform(30, 1105, 130, 15),
                    new Platform(30, 1200, 130, 15),
                    new Platform(30, 1295, 130, 15),
                    new Platform(30, 1390, 130, 15),
                    new Platform(30, 1485, 130, 15),
                    new Platform(30, 1580, 130, 15),
                    new Platform(400,1000,150,150),
                    new Platform(1150,410,200,30)
                }
            ));

            ALL_MODULES.add(new Module(
                "wall_jumping",
                new Vector2(725,670),
                new Platform[]{
                    new Platform(325,0,400,20),
                    new Platform(425,170,25,600),
                    new Platform(575,20,150,650)
                }
            ));

            ALL_MODULES.add(new Module(
                "danger_boost_vertical",
                new Vector2(100,125),
                new Platform[]{
                    new Platform(50,110,50,15)
                }
            ));

            ALL_MODULES.add(new Module(
                "danger_boost_horizontal",
                new Vector2(440,88),
                new Platform[]{
                    new Platform(365,68,75,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "precision_jumps", //to teach holding down for higher friction
                new Vector2(1250,165),
                new Platform[]{
                    new Platform(0, 85, 50, 15),
                    new Platform(0,185,50,15),
                    new Platform(350,200,50,15),
                    new Platform(650,0,65,15),
                    new Platform(850,65,50,15),
                    new Platform(975,80,50,15),
                    new Platform(1200,150,50,15)
                }
            ));
        }
}
