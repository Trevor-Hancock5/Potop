/*
 * Course: CSC1110A-111
 * Fall 2024
 * Assignment
 * Name: Trevor Hancock
 * Last Updated: 6/16/2026
 */
package com.hancockt.lol;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class ModuleLibrary {
    public static List<Module> ALL_MODULES = new ArrayList<>();
    //TODO - test all w/ and w/o danger boost. Maybe adjust jumping w/o danger boost
        //set water level to <-500 in restart for none. Or 0 for max danger boost.
    //TODO - update all end offsets!
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
                new Vector2(1650,160),
                new Platform[]{
                    new Platform(300,0,100,20),
                    new Platform(800,0,100,20),
                    new Platform(1100,80,100,20),
                    new Platform(1550,140,100,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "momentum",
                new Vector2(1835,20),
                new Platform[]{
                    new Platform(150,0,50,20),
                    new Platform(350,0,70,20),
                    new Platform(700,0,90,20),
                    new Platform(1220,0,60,20),
                    new Platform(1715,0,120,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "vert_jumping_series",
                new Vector2(640,390),
                new Platform[]{
                    new Platform(25,60,30,30),
                    new Platform(0,175,85,15),
                    new Platform(40,285,75,15),
                    new Platform(440,220,150,80),
                    new Platform(540,375,100,15)
                }
            ));

            ALL_MODULES.add(new Module(
                "falls",
                new Vector2(1350,440),
                new Platform[]{
                    new Platform(50, 85, 50, 15),
                    new Platform(50, 185, 50, 15),
                    new Platform(50, 285, 50, 15),
                    new Platform(50, 385, 50, 15),
                    new Platform(50, 485, 50, 15),
                    new Platform(50, 585, 50, 15),
                    new Platform(50, 685, 50, 15),
                    new Platform(50, 785, 50, 15),
                    new Platform(50, 885, 50, 15),
                    new Platform(50, 985, 50, 15),
                    new Platform(50, 1085, 50, 15),
                    new Platform(50, 1185, 50, 15),
                    new Platform(50, 1285, 50, 15),
                    new Platform(50, 1385, 50, 15),
                    new Platform(50, 1485, 50, 15),
                    new Platform(50, 1585, 50, 15),
                    new Platform(50, 1685, 50, 15),
                    new Platform(400,1000,150,150),
                    new Platform(1150,410,200,30)
                }
            ));

            ALL_MODULES.add(new Module(
                "wall_jumping",
                new Vector2(750,670),
                new Platform[]{
                    new Platform(350,0,400,20),
                    new Platform(450,170,25,600),
                    new Platform(600,20,150,650)
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
                new Vector2(450,88),
                new Platform[]{
                    new Platform(375,68,75,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "precision_jumps", //to teach holding down for higher friction
                new Vector2(1235,135),
                new Platform[]{
                    new Platform(0, 85, 35, 15),
                    new Platform(0,185,35,15),
                    new Platform(350,200,35,15),
                    new Platform(650,0,50,15),
                    new Platform(850,65,35,15),
                    new Platform(925,80,35,15),
                    new Platform(1200,150,35,15)
                }
            ));
        }
}
