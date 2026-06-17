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
        static {
            ALL_MODULES.add(new Module(
                "walk_and_run",
                new Vector2(1500, 0), //end offset
                new Platform[]{} // no obstacles, just flat ground handled separately
            ));

            ALL_MODULES.add(new Module(
                "small_jump",
                new Vector2(250,65),
                new Platform[]{
                    new Platform(150,45,100,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "variable_jumps",
                new Vector2(1750,140),
                new Platform[]{
                    new Platform(300,0,100,20),
                    new Platform(900,0,100,20),
                    new Platform(1200,80,100,20),
                    new Platform(1650,140,100,20)
                }
            ));

            ALL_MODULES.add(new Module(
                "momentum",
                new Vector2(1770,0),
                new Platform[]{
                    new Platform(50,0,30,20),
                    new Platform(230,0,50,20),
                    new Platform(580,0,70,20),
                    new Platform(1100,0,40,20),
                    new Platform(1670,0,100,20)
                }
            ));

            //TODO - fix
            ALL_MODULES.add(new Module(
                "vert_jumping_series",
                new Vector2(600,345),
                new Platform[]{
                    new Platform(300,95,50,50),
                    new Platform(-50,155,100,25),
                    new Platform(-450,215,75,35),
                    new Platform(-230,325,50,10),
                    new Platform(420,325,30,10),
                    new Platform(500,325,100,20)
                }
            ));

            //TODO - fix
            ALL_MODULES.add(new Module(
                "falls",
                new Vector2(1760,-1600),
                new Platform[]{
                    new Platform(400,-550,100,150),
                    new Platform(1500,-710,60,10),
                    new Platform(1560,-1650,200,50)
                }
            ));

            ALL_MODULES.add(new Module(
                "wall_jumping",
                new Vector2(800,850),
                new Platform[]{
                    new Platform(450,0,350,20),
                    new Platform(500,170,25,800),
                    new Platform(650,20,150,850)
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
                new Vector2(950,-170),
                new Platform[]{
                    new Platform(0, 100, 35, 15),
                    new Platform(0,200,35,15),
                    new Platform(400,200,35,15),
                    new Platform(650,0,35,15),
                    new Platform(800,65,35,15),
                    new Platform(925,80,35,15)
                }
            ));
        }
}
