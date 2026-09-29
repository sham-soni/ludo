package com.ludocrown.game;

/** Settings chosen on the setup screens. Indices are colours (0 red, 1 green, 2 yellow, 3 blue). */
final class GameConfig {
    int mode = LudoGame.CLASSIC;
    boolean oneOut;
    int tokenStyle;
    final boolean[] active = new boolean[4];
    final boolean[] bot = new boolean[4];
    final String[] names = new String[4];
}
