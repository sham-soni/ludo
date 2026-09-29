package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/** Studio splash (coloured letter blocks) followed by the game logo splash. */
class SplashScene extends Scene {
    private static final long STUDIO = 1800, LOGO = 2000;

    SplashScene(GameView v) {
        super(v);
    }

    @Override
    void drawScreen(Canvas c, int w, int h) {
        if (age() < STUDIO) {
            Art.reset();
            Art.P.setShader(new RadialGradient(w / 2f, h * 0.42f, Math.max(w, h) * 0.6f,
                    new int[]{0xFF0452C4, 0xFF033A94, 0xFF02173F}, new float[]{0, 0.45f, 1}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, Art.P);
            Art.P.setShader(null);
        } else {
            super.drawScreen(c, w, h);
        }
    }

    @Override
    void draw(Canvas c) {
        long t = age();
        if (t < STUDIO) {
            drawStudio(c, Math.min(1f, t / 400f));
        } else if (t < STUDIO + LOGO) {
            float k = Math.min(1f, (t - STUDIO) / 350f);
            float s = 0.7f + 0.3f * k;
            Art.logo(c, 450, 800, 1.18f * s);
        } else {
            view.setScene(new HomeScene(view));
        }
    }

    private void drawStudio(Canvas c, float alpha) {
        String word = "ludocraft";
        int[] cols = {0xFFFF1A1A, 0xFF9CFF00, 0xFFFF00E6, 0xFFC6FF00, 0xFF3399FF, 0xFFFF9900, 0xFF00F000,
                0xFFFFCC00, 0xFFFF3D7F};
        float[] rot = {-3, 2, 0, -2, 3, -1, 2, -3, 1};
        float bw = 80, total = bw * word.length();
        float x0 = 450 - total / 2;
        int a = (int) (255 * alpha);
        for (int i = 0; i < word.length(); i++) {
            float cx = x0 + i * bw + bw / 2;
            float cy = 1000 + (i % 2 == 0 ? -4 : 4);
            c.save();
            c.rotate(rot[i], cx, cy);
            Art.rrect(c, cx - bw / 2, cy - 55, cx + bw / 2 + 2, cy + 55, 2, (a << 24) | (cols[i] & 0xFFFFFF));
            Art.text(c, String.valueOf(word.charAt(i)), cx, cy + 28, 96, (a << 24), 0, 0, Art.MONO,
                    Paint.Align.CENTER);
            c.restore();
        }
    }

    @Override
    void onTap(float x, float y) {
        if (age() > 600) view.setScene(new HomeScene(view));
    }
}
