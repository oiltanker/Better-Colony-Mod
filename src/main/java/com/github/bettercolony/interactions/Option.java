package com.github.bettercolony.interactions;


public class Option {

    private static long prevId = -1;

    private Option() {}

    public static String newOption() {
        prevId++;
        return "colonizer_option_" + prevId;
    }

}