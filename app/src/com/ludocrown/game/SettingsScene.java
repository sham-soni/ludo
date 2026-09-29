package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.RectF;

/** Settings screen where the player picks which colour always wins. Changes need the PIN once one is set. */
class SettingsScene extends Scene {
    private final HomeScene home;
    // display order of options: green first since it is the default
    private static final int[] OPTIONS = {1, 0, 2, 3, Prefs.FAIR};
    private final RectF bPin = new RectF(250, 1440, 650, 1530);
    /** PIN entered correctly during this visit to the screen. */
    private boolean unlocked;

    SettingsScene(GameView v, HomeScene home) {
        super(v);
        this.home = home;
    }

    private static float rowY(int i) {
        return 640 + i * 135;
    }

    @Override
    void draw(Canvas c) {
        Art.panel(c, 60, 330, 840, 1670);
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
        Art.textFit(c, "Works in Computer, Pass N Play and Team Up games.", 450, 1350, 28, 720, 0xFFB8C8E8, 0, 0, Art.COND);
        Art.textFit(c, "In Team Up the chosen colour's team wins.", 450, 1390, 28, 720, 0xFFB8C8E8, 0, 0, Art.COND);
        boolean locked = Prefs.hasPin() && !unlocked;
        Art.pillButton(c, bPin, Prefs.hasPin() ? "Change PIN" : "Set PIN", null, down(bPin));
        String status = !Prefs.hasPin() ? "Not locked: anyone can change this"
                : locked ? "Locked: PIN needed to change" : "Unlocked";
        Art.reset();
        Art.P.setTypeface(Art.COND);
        Art.P.setTextSize(32);
        float tw = Math.min(600, Art.P.measureText(status));
        float left = 450 - (tw + 56) / 2;
        lockIcon(c, left + 20, 1612, locked);
        Art.textFit(c, status, left + 56 + tw / 2, 1622, 32, 600, locked ? 0xFFFFD31A : 0xFFFFFFFF, 0, 0, Art.COND);
        speaker(c, 770, 420, Prefs.sound());
        Art.backButton(c, 87, 1918, down(87, 1918, 55));
    }

    /** Sound on/off button in the panel's corner. */
    private void speaker(Canvas c, float x, float y, boolean on) {
        Art.circle(c, x, y, 36, 0xFFFFC21A);
        Art.circle(c, x, y, 30, 0xFF1C4FB0);
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(x - 17, y - 8);
        p.lineTo(x - 7, y - 8);
        p.lineTo(x + 5, y - 19);
        p.lineTo(x + 5, y + 19);
        p.lineTo(x - 7, y + 8);
        p.lineTo(x - 17, y + 8);
        p.close();
        Art.reset();
        Art.P.setColor(0xFFFFFFFF);
        c.drawPath(p, Art.P);
        Art.P.setStyle(android.graphics.Paint.Style.STROKE);
        Art.P.setStrokeWidth(4);
        Art.P.setStrokeCap(android.graphics.Paint.Cap.ROUND);
        if (on) {
            Art.R.set(x - 6, y - 12, x + 18, y + 12);
            c.drawArc(Art.R, -50, 100, false, Art.P);
        } else {
            Art.P.setColor(0xFFFF5A5A);
            c.drawLine(x + 10, y - 9, x + 22, y + 9, Art.P);
            c.drawLine(x + 22, y - 9, x + 10, y + 9, Art.P);
        }
    }

    private void lockIcon(Canvas c, float x, float y, boolean closed) {
        if (closed) {
            Art.ring(c, x, y - 18, 14, 0xFFFFC21A, 6);
        } else {
            Art.ring(c, x + 14, y - 22, 14, 0xFFFFC21A, 6);
        }
        Art.rrect(c, x - 20, y - 16, x + 20, y + 16, 5, 0xFFFFC21A);
        Art.circle(c, x, y - 2, 5, 0xFF6A3A00);
    }

    /** Runs the action now if unlocked, otherwise after the correct PIN is entered. */
    private void withPin(final Runnable action) {
        if (!Prefs.hasPin() || unlocked) {
            action.run();
            return;
        }
        view.askPin("Enter PIN", new GameView.PinCallback() {
            @Override
            public void onPin(String pin) {
                if (Prefs.checkPin(pin)) {
                    unlocked = true;
                    action.run();
                } else {
                    view.toast("Wrong PIN");
                }
            }
        });
    }

    private void choosePin() {
        view.askPin("New PIN (4-8 digits)", new GameView.PinCallback() {
            @Override
            public void onPin(final String pin) {
                if (pin.length() < 4) {
                    view.toast("PIN must be at least 4 digits");
                    return;
                }
                view.askPin("Enter the new PIN again", new GameView.PinCallback() {
                    @Override
                    public void onPin(String again) {
                        if (!again.equals(pin)) {
                            view.toast("PINs did not match");
                            return;
                        }
                        Prefs.setPin(pin);
                        unlocked = false;
                        view.toast("PIN saved. Winner control is locked");
                    }
                });
            }
        });
    }

    @Override
    void onTap(float x, float y) {
        if (hit(87, 1918, 60, x, y)) {
            onBack();
            return;
        }
        if (hit(770, 420, 50, x, y)) {
            Prefs.setSound(!Prefs.sound());
            view.toast(Prefs.sound() ? "Sound on" : "Sound off");
            return;
        }
        if (hit(bPin, x, y)) {
            withPin(new Runnable() {
                @Override
                public void run() {
                    choosePin();
                }
            });
            return;
        }
        for (int i = 0; i < OPTIONS.length; i++) {
            float ry = rowY(i);
            if (x > 110 && x < 790 && y > ry && y < ry + 110) {
                final int opt = OPTIONS[i];
                if (opt == Prefs.winner()) return;
                withPin(new Runnable() {
                    @Override
                    public void run() {
                        Prefs.setWinner(opt);
                        view.toast(opt == Prefs.FAIR ? "Fair dice for everyone" : Art.NAME[opt] + " will win every game");
                    }
                });
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
