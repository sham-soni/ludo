package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.RectF;

/** Select token / game type, then choose colours, names and player count. */
class SetupScene extends Scene {
    private static final int ST_SELECT = 0, ST_COLOR = 1, ST_ONEOUT = 2;

    private final HomeScene home;
    private final int entry;
    private int stage = ST_SELECT;
    private long stageAt = System.currentTimeMillis();

    private int token = Prefs.tokenStyle();
    private int gameType;
    private int count = 2;
    private int group; // 2P: 0 = blue+green, 1 = red+yellow
    private final boolean[] inc3 = {true, true, true, false}; // 3P inclusion by row index (blue, red, green, yellow)
    private final boolean[] bot = new boolean[4];
    private final String[] names = new String[4];

    // rows for 3/4 players: blue, red, green, yellow (clockwise from bottom-left)
    private static final int[] ROW_COLORS = {3, 0, 1, 2};

    private final RectF bNext = new RectF(315, 1525, 585, 1620);
    private final RectF bPlay = new RectF(345, 1320, 555, 1392);
    private final RectF bPlayOne = new RectF(330, 1430, 570, 1542);
    private final RectF bOkay = new RectF(230, 1315, 685, 1420);

    SetupScene(GameView v, HomeScene home, int entry) {
        super(v);
        this.home = home;
        this.entry = entry;
        gameType = entry == HomeScene.MODE_TEAM ? LudoGame.TEAM : LudoGame.CLASSIC;
        if (gameType == LudoGame.TEAM) count = 4;
        for (int c = 0; c < 4; c++) {
            String n = Prefs.name(c);
            names[c] = n;
        }
        applyBotDefaults();
    }

    private boolean vsComputer() {
        return entry != HomeScene.MODE_PASS;
    }

    private void applyBotDefaults() {
        int[] cols = activeColors();
        for (int i = 0; i < 4; i++) bot[i] = false;
        if (vsComputer()) {
            for (int i = 1; i < cols.length; i++) bot[cols[i]] = true;
        }
    }

    /** Active colours in display order; the first is the main human player. */
    private int[] activeColors() {
        if (count == 2) return group == 0 ? new int[]{3, 1} : new int[]{0, 2};
        if (count == 3) {
            int[] r = new int[3];
            int k = 0;
            for (int i = 0; i < 4; i++) if (inc3[i]) r[k++] = ROW_COLORS[i];
            return r;
        }
        return new int[]{3, 0, 1, 2};
    }

    private String displayName(int color) {
        if (names[color] != null) return names[color];
        if (bot[color]) return "Computer";
        int[] cols = activeColors();
        boolean inGame = false;
        for (int c : cols) if (c == color) inGame = true;
        if (!inGame) cols = (color == 3 || color == 1) ? new int[]{3, 1} : new int[]{0, 2};
        int n = 1;
        for (int c : cols) {
            if (c == color) break;
            if (!bot[c] || names[c] != null) n++;
        }
        return "Player " + n;
    }

    @Override
    void drawScreen(Canvas c, int w, int h) {
        super.drawScreen(c, w, h);
    }

    @Override
    void draw(Canvas c) {
        home.drawBase(c, false);
        Art.rrect(c, -2000, -2000, 3000, 4000, 0, 0xC8000000);
        float k = Math.min(1f, (System.currentTimeMillis() - stageAt) / 200f);
        c.save();
        c.translate(0, (1 - k) * 60);
        if (stage == ST_SELECT) drawSelect(c);
        else drawColor(c);
        c.restore();
        if (stage == ST_ONEOUT) drawOneOut(c);
        Art.backButton(c, 87, 1918, down(87, 1918, 55));
    }

    // ------------------------------------------------------------------ select token + game

    private void drawSelect(Canvas c) {
        // theme badge
        Art.circle(c, 285, 583, 22, 0xFFFFC21A);
        Art.circle(c, 285, 583, 17, 0xFF0A1F5C);
        Art.check(c, 285, 583, 22, 0xFFFFC21A, 5);
        Icons.phone(c, 345, 578, 40, 72, 0);
        Art.rrectGrad(c, 385, 540, 620, 615, 14, 0xFFFFD84A, 0xFFE08A00);
        Art.rrectGrad(c, 390, 545, 615, 610, 12, 0xFF3F8BF0, 0xFF12408F);
        Art.pawn(c, 420, 590, 45, 0);
        Art.crown(c, 470, 555, 40);
        Art.circle(c, 540, 565, 14, 0xFF3CD65A);
        Art.circle(c, 575, 568, 14, 0xFF4FB3FF);
        Art.textC(c, "SELECT THEME", 502, 604, 22, 0xFFFFFFFF, 0xFF0A1F5C, 3, Art.BLACK);

        Art.band(c, 635, 845, 900);
        Art.titleText(c, "SELECT TOKEN", 450, 685, 46, Art.BLACK);
        triangle(c, 140, 767, -1);
        triangle(c, 760, 767, 1);
        float[] xs = {217, 337, 452, 567, 683};
        for (int i = 0; i < 5; i++) {
            float x = xs[i];
            if (i == 0) Art.pin(c, x, 790, 110, 3, false);
            else if (i == 1) Art.disc(c, x, 765, 100, 3);
            else lockedToken(c, i, x, 765);
            if (i == token) {
                Art.rrect(c, x - 36, 715, x - 6, 745, 4, 0xFFFFC21A);
                Art.rrect(c, x - 33, 718, x - 9, 742, 3, 0xFF0A1F5C);
                Art.check(c, x - 21, 730, 20, 0xFFFFFFFF, 4);
            }
            if (i >= 2) lock(c, x - 32, 735);
        }

        Art.band(c, 878, 1500, 900);
        Art.titleText(c, "SELECT GAME", 450, 935, 46, Art.BLACK);
        Art.circle(c, 722, 915, 25, 0xFFFFC21A);
        Art.circle(c, 722, 915, 20, 0xFF1C4FB0);
        Art.textC(c, "?", 722, 928, 32, 0xFFFFD84A, 0, 0, Art.BLACK);
        String[] labels = {"Classic", "Team Up", "QUICK"};
        for (int i = 0; i < 3; i++) {
            float cy = 1037 + i * 187;
            gameIcon(c, i, 290, cy);
            boolean sel = gameType == i;
            RectF r = new RectF(405, cy - 50, 695, cy + 50);
            Art.glow(c, r.left, r.top, r.right, r.bottom, 50, 0xFFFFB000, 10);
            Art.rrectGrad(c, r.left, r.top, r.right, r.bottom, 50, 0xFFFFE070, 0xFFD08000);
            if (i == 2) Art.rrectGrad(c, r.left + 6, r.top + 6, r.right - 6, r.bottom - 6, 44, 0xFF3FB83F, 0xFF12601A);
            else Art.rrectGrad(c, r.left + 6, r.top + 6, r.right - 6, r.bottom - 6, 44, 0xFF3F8BF0, 0xFF12408F);
            Art.circle(c, 455, cy, 48, 0xFFFFC21A);
            Art.circle(c, 455, cy, 38, 0xFF0A2A7A);
            if (sel) {
                Art.check(c, 455, cy, 56, 0xFF3A2400, 16);
                Art.check(c, 455, cy, 56, 0xFFFFC21A, 10);
            }
            if (i == 1) {
                Art.textC(c, "Classic", 592, cy - 12, 26, 0xFFFFFFFF, 0xFF0A1F5C, 4, Art.BLACK);
                Art.textC(c, "Team Up", 592, cy + 28, 36, 0xFFFFFFFF, 0xFF0A1F5C, 5, Art.BLACK);
            } else {
                Art.textC(c, labels[i], 592, cy + 16, 44, 0xFFFFFFFF, 0xFF0A1F5C, 5, Art.BLACK);
            }
        }
        Art.pillButton(c, bNext, "Next", null, down(bNext));
    }

    private void triangle(Canvas c, float x, float y, int dir) {
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(x + dir * 20, y);
        p.lineTo(x - dir * 14, y - 22);
        p.lineTo(x - dir * 14, y + 22);
        p.close();
        Art.reset();
        Art.P.setColor(0xFFFFD31A);
        c.drawPath(p, Art.P);
    }

    private void lockedToken(Canvas c, int i, float x, float y) {
        if (i == 2) {
            Art.rrectGrad(c, x - 30, y - 45, x + 30, y + 10, 10, 0xFF9AE0FF, 0xFF2A8AD0);
            Art.rrect(c, x - 10, y + 10, x + 10, y + 40, 4, 0xFF2A8AD0);
            Art.textC(c, "S", x, y - 5, 36, 0xFFFFFFFF, 0xFF0A4A92, 3, Art.BLACK);
        } else if (i == 3) {
            android.graphics.Path p = new android.graphics.Path();
            p.moveTo(x - 40, y - 15);
            p.lineTo(x, y - 50);
            p.lineTo(x + 40, y - 15);
            p.lineTo(x, y + 45);
            p.close();
            Art.reset();
            Art.P.setColor(0xFF6AB8F0);
            c.drawPath(p, Art.P);
            Art.star(c, x, y - 5, 16, 0xFFFFFFFF, 0, 0);
        } else {
            Art.circle(c, x, y - 12, 34, 0xFF7FC8FF);
            Art.rrect(c, x - 30, y - 22, x + 30, y - 6, 6, 0xFF1A1A1A);
            Art.rrect(c, x - 28, y + 20, x + 28, y + 45, 8, 0xFF3A8AE0);
        }
    }

    private void lock(Canvas c, float x, float y) {
        Art.ring(c, x, y - 12, 10, 0xFFFFC21A, 5);
        Art.rrect(c, x - 14, y - 10, x + 14, y + 14, 4, 0xFFFFC21A);
        Art.circle(c, x, y + 2, 4, 0xFF6A3A00);
    }

    private void gameIcon(Canvas c, int i, float cx, float cy) {
        Art.glow(c, cx - 68, cy - 68, cx + 68, cy + 68, 14, 0xFFFFB000, 8);
        Art.rrect(c, cx - 68, cy - 68, cx + 68, cy + 68, 14, 0xFFFFC21A);
        Art.rrectGrad(c, cx - 62, cy - 62, cx + 62, cy + 62, 12, 0xFF3F8BF0, 0xFF12408F);
        int w = 0xFFFFFFFF;
        if (i == 0) {
            Icons.person(c, cx, cy - 38, 38, w);
            Icons.person(c, cx - 42, cy + 2, 38, w);
            Icons.person(c, cx + 42, cy + 2, 38, w);
            Icons.person(c, cx, cy + 40, 38, w);
            Art.textC(c, "VS", cx, cy + 12, 28, w, 0xFF0A1F5C, 3, Art.BLACK);
        } else if (i == 1) {
            Icons.person(c, cx + 18, cy - 30, 34, w);
            Icons.person(c, cx + 42, cy - 30, 34, w);
            Icons.person(c, cx - 42, cy + 32, 34, w);
            Icons.person(c, cx - 18, cy + 32, 34, w);
            Art.textC(c, "VS", cx - 5, cy + 8, 28, w, 0xFF0A1F5C, 3, Art.BLACK);
        } else {
            Icons.person(c, cx, cy - 38, 38, w);
            Icons.person(c, cx - 42, cy + 2, 38, w);
            Icons.person(c, cx + 42, cy + 2, 38, w);
            Icons.person(c, cx, cy + 40, 38, w);
            Art.ring(c, cx, cy + 2, 16, 0xFFFFC21A, 4);
        }
    }

    // ------------------------------------------------------------------ colours and names

    private void drawColor(Canvas c) {
        Art.band(c, 692, 1285, 900);
        Art.titleText(c, "CHOOSE COLOR AND NAME", 400, 752, 36, Art.BLACK);
        int[] cols = activeColors();
        int bots = 0;
        for (int col : cols) if (bot[col]) bots++;
        Art.rrect(c, 725, 715, 818, 758, 4, 0xFF061340);
        Icons.robot(c, 745, 737, 30, 0xFFFFFFFF);
        Art.text(c, bots + "/" + cols.length, 766, 750, 30, 0xFFFFFFFF, 0, 0, Art.BOLD, android.graphics.Paint.Align.LEFT);

        if (count == 2) {
            for (int g = 0; g < 2; g++) {
                float top = g == 0 ? 780 : 970;
                Art.rrect(c, 0, top, 900, top + 185, 0, g == group ? 0xFF1552D8 : 0xFF0B2E86);
                float cy = top + 92;
                if (g == group) {
                    Art.circle(c, 240, cy, 50, 0xFFFFFFFF);
                    Art.circle(c, 240, cy, 44, 0xFF3CC83C);
                    Art.check(c, 240, cy, 50, 0xFFFFFFFF, 12);
                } else {
                    Art.ring(c, 240, cy, 44, 0xFF3CFF3C, 5);
                }
                int[] gc = g == 0 ? new int[]{3, 1} : new int[]{0, 2};
                for (int j = 0; j < 2; j++) {
                    float ry = top + 55 + j * 75;
                    playerRow(c, gc[j], ry, g == group);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                int col = ROW_COLORS[i];
                float ry = 812 + i * 88;
                boolean on = count == 4 || inc3[i];
                if (count == 3) {
                    if (on) {
                        Art.circle(c, 240, ry, 28, 0xFFFFFFFF);
                        Art.circle(c, 240, ry, 24, 0xFF3CC83C);
                        Art.check(c, 240, ry, 28, 0xFFFFFFFF, 7);
                    } else {
                        Art.ring(c, 240, ry, 24, 0xFF3CFF3C, 4);
                    }
                } else if (gameType == LudoGame.TEAM) {
                    String team = (col == 3 || col == 1) ? "TEAM A" : "TEAM B";
                    Art.textC(c, team, 215, ry + 10, 26, (col == 3 || col == 1) ? 0xFF7FD0FF : 0xFFFFE070,
                            0xFF061340, 3, Art.BLACK);
                }
                playerRow(c, col, ry, on);
            }
        }
        // player count buttons
        String[] pc = {"2P", "3P", "4P", "5P", "6P"};
        for (int i = 0; i < 5; i++) {
            float x = 162 + i * 144, y = 1215;
            boolean sel = count == i + 2;
            boolean enabled = i < 3 && (gameType != LudoGame.TEAM || i == 2);
            if (!sel && enabled) Art.glow(c, x - 48, y - 38, x + 48, y + 38, 38, 0xFFFFB000, 10);
            Art.rrectGrad(c, x - 48, y - 38, x + 48, y + 38, 38, sel ? 0xFF6A5A30 : 0xFFFFE070, sel ? 0xFF3A2A10 : 0xFFD08000);
            Art.rrectGrad(c, x - 42, y - 32, x + 42, y + 32, 32, sel ? 0xFF1A2A5A : 0xFF3F8BF0, sel ? 0xFF0A1A3A : 0xFF12408F);
            Art.textC(c, pc[i], x, y + 16, 44, enabled || sel ? 0xFFFFFFFF : 0xFF8090B0, 0xFF0A1F5C, 5, Art.BLACK);
        }
        Art.pillButton(c, bPlay, "Play", null, down(bPlay));
        Art.pillButton(c, bPlayOne, "Play", "- One Token Out -", down(bPlayOne));
    }

    private void playerRow(Canvas c, int col, float cy, boolean on) {
        c.save();
        Art.pin(c, 345, cy + 22, 78, col, false);
        int box = on ? 0xFFFFFFFF : 0xFF9A9A9A;
        Art.rrect(c, 402, cy - 24, 638, cy + 24, 6, box);
        Art.reset();
        Art.P.setStyle(android.graphics.Paint.Style.STROKE);
        Art.P.setStrokeWidth(3);
        Art.P.setColor(0xFFB01818);
        Art.P.setPathEffect(new android.graphics.DashPathEffect(new float[]{8, 5}, 0));
        Art.R.set(404, cy - 22, 636, cy + 22);
        c.drawRoundRect(Art.R, 6, 6, Art.P);
        Art.P.setPathEffect(null);
        Art.textFit(c, displayName(col), 520, cy + 13, 36, 220, 0xFF111111, 0, 0, Art.COND);
        if (bot[col]) {
            Art.circle(c, 688, cy, 30, on ? 0xFFEAF2FF : 0xFF8A8A8A);
            Icons.robot(c, 688, cy + 2, 40, 0xFF1C4FB0);
        } else {
            Art.dice(c, 688, cy, 52, 5, -12, on ? 0xFFB8BEC8 : 0xFF6A6A6A);
        }
        c.restore();
    }

    private void drawOneOut(Canvas c) {
        Art.rrect(c, -2000, -2000, 3000, 4000, 0, 0xB0000000);
        Art.panel(c, 88, 620, 810, 1485);
        // illustration
        float l = 227, t = 678, cs = 55.8f;
        Art.rrect(c, l, t, l + 447, t + 420, 0, 0xFF29A9E1);
        Art.rrect(c, l, t, l + 330, t + 35, 0, 0xFFFFFFFF);
        for (int i = 1; i < 6; i++) Art.rrect(c, l + i * cs, t, l + (i + 1) * cs, t + 35, 0, 0xFFE8202A);
        Art.rrect(c, l, t + 35, l + 330, t + 90, 0, 0xFFFFFFFF);
        Art.rrect(c, l + 330, t + 90, l + 447, t + 420, 0, 0xFFFFFFFF);
        Art.rrect(c, l + 387, t + 90, l + 447, t + 360, 0, 0xFF29A9E1);
        Art.star(c, l + 137, t + 62, 22, 0, 0xFF333333, 3);
        Art.rrect(c, l + 55, t + 147, l + 275, t + 368, 0, 0xFFFFFFFF);
        Art.circle(c, l + 218, t + 313, 28, 0xFF1E8FE8);
        Art.pin(c, l + 108, t + 205, 80, 3, false);
        Art.pin(c, l + 218, t + 205, 80, 3, false);
        Art.pin(c, l + 108, t + 313, 80, 3, false);
        Art.pin(c, l + 360, t + 300, 80, 3, false);
        Art.ring(c, l + 360, t + 293, 68, 0xFF3CFF3C, 6);
        Art.arrow(c, l + 415, t + 390, 40, -90, 0xFF1E8FE8, 4);
        Art.reset();
        Art.P.setColor(0xFF3CFF3C);
        Art.P.setStrokeWidth(5);
        c.drawLine(l + 310, t + 345, l + 155, t + 500, Art.P);
        Art.textC(c, "Keep one token out for all!", 450, 1228, 56, 0xFF3CFF3C, 0xFF062A06, 6, Art.BLACK);
        Art.pillButton(c, bOkay, "OKAY", null, down(bOkay));
        Art.closeButton(c, 808, 618, 40, down(808, 618, 50));
    }

    // ------------------------------------------------------------------ input

    @Override
    void onTap(float x, float y) {
        if (hit(87, 1918, 60, x, y)) {
            onBack();
            return;
        }
        if (stage == ST_ONEOUT) {
            if (hit(bOkay, x, y)) start(true);
            else if (hit(808, 618, 55, x, y)) setStage(ST_COLOR);
            return;
        }
        if (stage == ST_SELECT) {
            float[] xs = {217, 337, 452, 567, 683};
            for (int i = 0; i < 5; i++) {
                if (hit(xs[i], 765, 55, x, y)) {
                    if (i < 2) {
                        token = i;
                        Prefs.setTokenStyle(i);
                    } else {
                        view.toast("Unlock this token in the store");
                    }
                    return;
                }
            }
            if (hit(140, 767, 45, x, y) || hit(760, 767, 45, x, y)) {
                token = 1 - Math.min(1, token);
                Prefs.setTokenStyle(token);
                return;
            }
            if (hit(722, 915, 40, x, y)) {
                view.toast(gameType == LudoGame.QUICK ? "Quick: all tokens start out, first token home wins"
                        : gameType == LudoGame.TEAM ? "Team Up: opposite colours play as a team"
                        : "Classic: bring all 4 tokens home to win");
                return;
            }
            if (hit(210, 960, 700, 1485, x, y)) {
                int i = (int) ((y - 944) / 187);
                if (i >= 0 && i < 3) selectGame(i);
                return;
            }
            if (hit(bNext, x, y)) setStage(ST_COLOR);
            if (hit(new RectF(380, 535, 625, 620), x, y)) view.toast("More themes coming soon");
            return;
        }
        // colour stage
        if (y > 1170 && y < 1260) {
            int i = Math.round((x - 162) / 144f);
            if (i >= 0 && i < 5 && Math.abs(x - (162 + i * 144)) < 55) {
                if (i >= 3) view.toast((i + 2) + " player board coming soon");
                else if (gameType == LudoGame.TEAM && i != 2) view.toast("Team Up needs 4 players");
                else {
                    count = i + 2;
                    applyBotDefaults();
                }
            }
            return;
        }
        if (hit(bPlay, x, y)) {
            start(false);
            return;
        }
        if (hit(bPlayOne, x, y)) {
            setStage(ST_ONEOUT);
            return;
        }
        if (count == 2) {
            for (int g = 0; g < 2; g++) {
                float top = g == 0 ? 780 : 970;
                if (y >= top && y < top + 185) {
                    int[] gc = g == 0 ? new int[]{3, 1} : new int[]{0, 2};
                    if (g != group) {
                        group = g;
                        applyBotDefaults();
                        return;
                    }
                    int j = y < top + 92 ? 0 : 1;
                    rowTap(gc[j], x);
                    return;
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                float ry = 812 + i * 88;
                if (Math.abs(y - ry) < 44) {
                    int col = ROW_COLORS[i];
                    if (count == 3 && x < 300) {
                        if (!inc3[i]) {
                            for (int k = 0; k < 4; k++) inc3[k] = true;
                            inc3[i] = true;
                            // exclude the next row so exactly 3 remain
                            inc3[(i + 1) % 4] = false;
                            applyBotDefaults();
                        }
                        return;
                    }
                    if (count == 3 && !inc3[i]) return;
                    rowTap(col, x);
                    return;
                }
            }
        }
    }

    private static boolean hit(float l, float t, float r, float b, float x, float y) {
        return x >= l && x <= r && y >= t && y <= b;
    }

    private void rowTap(final int col, float x) {
        if (x > 645) {
            bot[col] = !bot[col];
            if (bot[col] && names[col] != null && names[col].startsWith("Player")) names[col] = null;
        } else if (x > 390) {
            view.askName(displayName(col), new GameView.NameCallback() {
                @Override
                public void onName(String n) {
                    names[col] = n;
                    Prefs.setName(col, n);
                }
            });
        }
    }

    private void selectGame(int i) {
        gameType = i;
        if (i == LudoGame.TEAM) count = 4;
        applyBotDefaults();
    }

    private void setStage(int s) {
        stage = s;
        stageAt = System.currentTimeMillis();
    }

    private void start(boolean oneOut) {
        GameConfig cfg = new GameConfig();
        cfg.mode = gameType;
        cfg.oneOut = oneOut;
        cfg.tokenStyle = token;
        for (int col : activeColors()) {
            cfg.active[col] = true;
            cfg.bot[col] = bot[col];
            cfg.names[col] = displayName(col);
        }
        if (entry == HomeScene.MODE_ONLINE || entry == HomeScene.MODE_FRIENDS) {
            view.toast("Offline version: you are playing against the computer");
        }
        view.setScene(new BoardScene(view, cfg));
    }

    @Override
    boolean onBack() {
        if (stage == ST_ONEOUT) setStage(ST_COLOR);
        else if (stage == ST_COLOR) setStage(ST_SELECT);
        else view.setScene(home);
        return true;
    }
}
