package com.ludocrown.game;

import android.graphics.Canvas;

/** Settings screen where the player picks which colour always wins. */
class SettingsScene extends Scene {
    private final HomeScene home;
    // display order of options: green first since it is the default
    private static final int[] OPTIONS = {1, 0, 2, 3, Prefs.FAIR};

    SettingsScene(GameView v, HomeScene home) {
        super(v);
        this.home = home;
    }

    private static float rowY(int i) {
        return 640 + i * 135;
    }

    @Override
    void draw(Canvas c) {
        Art.panel(c, 60, 330, 840, 1560);
        Art.ribbon(c, 450, 330, 400, 86, "SETTINGS");
        Art.titleText(c, "WINNER CONTROL", 450, 470, 50, Art.BLACK);
        Art.textFit(c, "Choose the colour that always wins the game", 450, 530, 34, 700, 0xFFFFFFFF, 0, 0, Art.COND);
        Art.textC(c, "(default: Green)", 450, 575, 30, 0xFFB8C8E8, 0, 0, Art.COND);
        int sel = Prefs.winner();
        for (int i = 0; i < OPTIONS.length; i++) {
            int opt = OPTIONS[i];
            float y = rowY(i);
            boolean on = opt == sel;
            if (on) Art.glow(c, 110, y, 790, y + 110, 20, 0xFF3CFF3C, 10);
            Art.rrectGrad(c, 110, y, 790, y + 110, 20, on ? 0xFF2F8A2A : 0xFF1C4FB0, on ? 0xFF14501A : 0xFF0A2A7A);
            Art.rrectStroke(c, 110, y, 790, y + 110, 20, on ? 0xFF7CFF3A : 0xFFFFC21A, 4);
            String label;
            if (opt == Prefs.FAIR) {
                Art.dice(c, 190, y + 55, 70, 5, -10, 0xFFCCCCCC);
                label = "No fixed winner (fair dice)";
            } else {
                Art.pin(c, 190, y + 80, 100, opt, false);
                label = Art.NAME[opt] + " always wins";
            }
            Art.textFit(c, label, 450, y + 70, 44, 420, 0xFFFFFFFF, 0xFF061340, 4, Art.BLACK);
            Art.circle(c, 725, y + 55, 34, 0xFFFFFFFF);
            if (on) {
                Art.circle(c, 725, y + 55, 28, 0xFF3CC83C);
                Art.check(c, 725, y + 55, 34, 0xFFFFFFFF, 8);
            } else {
                Art.circle(c, 725, y + 55, 28, 0xFF0A1F5C);
            }
        }
        Art.textFit(c, "Works in Computer, Pass N Play and Team Up games.", 450, 1370, 30, 720, 0xFFB8C8E8, 0, 0, Art.COND);
        Art.textFit(c, "In Team Up the chosen colour's team wins.", 450, 1412, 30, 720, 0xFFB8C8E8, 0, 0, Art.COND);
        Art.textFit(c, "If that colour is not playing, dice are fair.", 450, 1454, 30, 720, 0xFFB8C8E8, 0, 0, Art.COND);
        Art.backButton(c, 87, 1918, down(87, 1918, 55));
    }

    @Override
    void onTap(float x, float y) {
        if (hit(87, 1918, 60, x, y)) {
            onBack();
            return;
        }
        for (int i = 0; i < OPTIONS.length; i++) {
            float ry = rowY(i);
            if (x > 110 && x < 790 && y > ry && y < ry + 110) {
                Prefs.setWinner(OPTIONS[i]);
                view.toast(OPTIONS[i] == Prefs.FAIR ? "Fair dice for everyone"
                        : Art.NAME[OPTIONS[i]] + " will win every game");
                return;
            }
        }
    }

    @Override
    boolean onBack() {
        view.setScene(home);
        return true;
    }
}
