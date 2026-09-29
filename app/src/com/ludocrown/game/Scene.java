package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.RectF;

/** A full screen of the game drawn in the 900x2000 virtual coordinate space. */
abstract class Scene {
    final GameView view;
    final long start = System.currentTimeMillis();

    Scene(GameView view) {
        this.view = view;
    }

    long age() {
        return System.currentTimeMillis() - start;
    }

    /** Draws in real screen pixels before the virtual transform (backgrounds, dimming). */
    void drawScreen(Canvas c, int w, int h) {
        c.drawBitmap(view.pattern(), 0, 0, null);
    }

    abstract void draw(Canvas c);

    void onTap(float x, float y) {}

    /** @return true if handled */
    boolean onBack() { return false; }

    boolean down(RectF r) {
        return view.pressing && r.contains(view.downX, view.downY);
    }

    boolean down(float cx, float cy, float rad) {
        return view.pressing && Math.hypot(view.downX - cx, view.downY - cy) < rad;
    }

    static boolean hit(RectF r, float x, float y) {
        return r.contains(x, y);
    }

    static boolean hit(float cx, float cy, float rad, float x, float y) {
        return Math.hypot(x - cx, y - cy) < rad;
    }
}
