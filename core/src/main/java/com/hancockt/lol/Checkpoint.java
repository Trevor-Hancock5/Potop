/*
 * Course: CSC1110A-111
 * Fall 2024
 * Assignment
 * Name: Trevor Hancock
 * Last Updated: 6/16/2026
 */
package com.hancockt.lol;

public class Checkpoint {
    float x,y;
    boolean activated;

    public Checkpoint(float x, float y, boolean activated){
        this.x = x;
        this.y = y;
        this.activated = activated;
    }

    public Checkpoint(float x, float y){
        this(x, y, false);
    }
}
