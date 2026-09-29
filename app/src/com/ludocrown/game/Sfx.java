package com.ludocrown.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Vibrator;

/** Sound effects (synthesised WAVs in assets/sfx) and short vibrations. */
final class Sfx {
    private Sfx() {}

    static final int ROLL = 0, STEP = 1, CAPTURE = 2, HOME = 3, SIX = 4, WIN = 5, CLICK = 6, TURN = 7;
    private static final String[] FILES = {"roll", "step", "capture", "home", "six", "win", "click", "turn"};
    private static final int[] ids = new int[FILES.length];
    private static SoundPool pool;
    private static Vibrator vibrator;

    static void init(Context ctx) {
        try {
            pool = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            for (int i = 0; i < FILES.length; i++) {
                ids[i] = pool.load(ctx.getAssets().openFd("sfx/" + FILES[i] + ".wav"), 1);
            }
        } catch (Exception e) {
            pool = null;
        }
        vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    static void play(int s) {
        play(s, 1f);
    }

    static void play(int s, float rate) {
        if (pool != null && Prefs.sound()) pool.play(ids[s], 1f, 1f, 1, 0, rate);
    }

    static void vibrate(long ms) {
        try {
            if (vibrator != null && Prefs.sound()) vibrator.vibrate(ms);
        } catch (Exception ignored) {
        }
    }
}
