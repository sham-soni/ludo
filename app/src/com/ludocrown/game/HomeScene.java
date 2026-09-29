package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Locale;
import java.util.Random;

/** Main menu with the startup popups (daily bonus, reminder prompt, offer). */
class HomeScene extends Scene {
    private static boolean popupsShown;

    static final int MODE_COMPUTER = 0, MODE_PASS = 1, MODE_ONLINE = 2, MODE_TEAM = 3, MODE_FRIENDS = 4;

    private static final int POP_NONE = 0, POP_DAILY = 1, POP_NOTIFY = 2, POP_OFFER = 3;
    private int popup = POP_NONE;
    private long popupAt;

    private final RectF bOnline = new RectF(82, 855, 315, 1035);
    private final RectF bTeam = new RectF(335, 855, 568, 1035);
    private final RectF bFriends = new RectF(588, 855, 820, 1035);
    private final RectF bComputer = new RectF(188, 1103, 427, 1285);
    private final RectF bPass = new RectF(475, 1103, 714, 1285);
    private final RectF bTournament = new RectF(320, 1370, 580, 1510);
    private final RectF bSeason = new RectF(275, 1545, 625, 1665);
    private final RectF bGear = new RectF(125, 115, 180, 185);
    private final RectF bFree = new RectF(55, 1630, 145, 1830);

    // daily bonus
    private final RectF bClaim = new RectF(175, 1570, 405, 1648);
    private final RectF bClaim2x = new RectF(447, 1570, 745, 1648);
    // notification
    private final RectF bYes = new RectF(310, 1188, 590, 1275);
    // offer
    private final RectF bPrice = new RectF(350, 1180, 548, 1245);

    private final int[] players = {268481, 5016, 20315};
    private final Random rnd = new Random();
    private long lastTick;

    HomeScene(GameView v) {
        super(v);
        if (!popupsShown) {
            popupsShown = true;
            if (Prefs.bonusAvailable()) showPopup(POP_DAILY);
            else nextPopup(POP_DAILY);
        }
    }

    private void showPopup(int p) {
        popup = p;
        popupAt = System.currentTimeMillis();
    }

    private void nextPopup(int after) {
        if (after < POP_NOTIFY && !Prefs.notifyAsked()) showPopup(POP_NOTIFY);
        else if (after < POP_OFFER) showPopup(POP_OFFER);
        else popup = POP_NONE;
    }

    @Override
    void drawScreen(Canvas c, int w, int h) {
        super.drawScreen(c, w, h);
    }

    @Override
    void draw(Canvas c) {
        drawBase(c, popup == POP_NONE);
        if (popup != POP_NONE) {
            Art.rrect(c, -2000, -2000, 3000, 4000, 0, 0xB0000000);
            float k = Math.min(1f, (System.currentTimeMillis() - popupAt) / 220f);
            float s = 0.6f + 0.4f * (float) Math.sin(k * Math.PI / 2) + (k < 1 ? 0.05f * (float) Math.sin(k * Math.PI) : 0);
            c.save();
            c.scale(s, s, 450, 1000);
            if (popup == POP_DAILY) drawDaily(c);
            else if (popup == POP_NOTIFY) drawNotify(c);
            else drawOffer(c);
            c.restore();
        }
    }

    /** Draws the menu itself. Also used by the setup scenes underneath their overlays. */
    void drawBase(Canvas c, boolean interactive) {
        long now = System.currentTimeMillis();
        if (now - lastTick > 2500) {
            lastTick = now;
            for (int i = 0; i < 3; i++) players[i] += rnd.nextInt(41) - 20;
        }
        drawTopBar(c);
        // side widgets
        Icons.chest(c, 835, 285, 80, 0xFFD08A30, 0xFF3FAF3F);
        Art.coin(c, 810, 300, 14);
        Art.text(c, "FREE", 835, 327, 30, 0xFFFFD31A, 0xFF6A2A00, 5, Art.BLACK, Paint.Align.CENTER);
        Icons.target(c, 832, 398, 72);
        mascot(c, 835, 535, 95);
        Icons.help(c, 840, 670, 90);
        // left widgets
        Icons.chest(c, 55, 305, 70, 0xFFE0508A, 0xFFFFC21A);
        Art.text(c, "STARTER", 55, 318, 20, 0xFFFFE070, 0xFF6A0A2A, 4, Art.BLACK, Paint.Align.CENTER);
        Art.text(c, "PACK", 55, 338, 20, 0xFFFFE070, 0xFF6A0A2A, 4, Art.BLACK, Paint.Align.CENTER);
        Art.circle(c, 58, 458, 34, 0xFFFFC21A);
        Art.circle(c, 58, 458, 28, 0xFF222222);
        Art.textC(c, "K", 58, 474, 40, 0xFFFFC21A, 0, 0, Art.SERIF);
        Art.crown(c, 58, 412, 40);
        Art.rrect(c, 18, 520, 92, 570, 6, 0xFFE8202A);
        Art.textC(c, "LUDO", 55, 552, 22, 0xFFFFFFFF, 0xFF1C4FB0, 3, Art.BLACK);
        Art.rrect(c, 18, 578, 88, 606, 3, 0xFFFFE000);
        Art.textC(c, "LIVE", 53, 602, 26, 0xFF000000, 0, 0, Art.BLACK);

        float bob = (float) Math.sin(now / 700.0) * 6;
        Art.logo(c, 450, 390 + bob, 1.0f);

        Art.modeButton(c, bOnline, "ONLINE", Icons.ONLINE, interactive && down(bOnline));
        Art.modeButton(c, bTeam, "TEAM UP", Icons.TEAM, interactive && down(bTeam));
        Art.modeButton(c, bFriends, "FRIENDS", Icons.FRIENDS, interactive && down(bFriends));
        playerCount(c, bOnline.centerX(), players[0]);
        playerCount(c, bTeam.centerX(), players[1]);
        playerCount(c, bFriends.centerX(), players[2]);
        Art.modeButton(c, bComputer, "COMPUTER", Icons.COMPUTER, interactive && down(bComputer));
        Art.modeButton(c, bPass, "PASS N PLAY", Icons.PASS, interactive && down(bPass));

        Icons.cup(c, 178, 1453, 125);
        Icons.snake(c, 730, 1453, 125);
        Art.crown(c, 450, 1395, 80);
        Art.rrect(c, 330, 1415, 570, 1500, 30, 0x33000000);
        Art.titleText(c, "TOURNAMENT", 450, 1480, 62, Art.COND);

        // season claim
        Art.rrectGrad(c, 330, 1560, 620, 1650, 12, 0xFFFFD84A, 0xFFE08A00);
        Art.rrectStroke(c, 330, 1560, 620, 1650, 12, 0xFFFFF2A0, 4);
        Art.titleText(c, "CLAIM", 500, 1630, 70, Art.BLACK);
        shield(c, 338, 1600, 110);

        // free coins (left bottom)
        Art.text(c, "FREE", 97, 1683, 34, 0xFFFFC21A, 0xFF6A2A00, 5, Art.BLACK, Paint.Align.CENTER);
        Art.rrectGrad(c, 55, 1693, 140, 1780, 10, 0xFFFFE070, 0xFFD08000);
        Art.rrect(c, 62, 1700, 133, 1740, 6, 0xFF1C4FB0);
        Art.textC(c, "AD x5", 97, 1730, 24, 0xFFFFFFFF, 0, 0, Art.BLACK);
        Art.rrect(c, 62, 1745, 133, 1772, 6, 0xFF6A3A00);
        Art.textC(c, "1000", 104, 1767, 21, 0xFFFFFFFF, 0, 0, Art.BOLD);
        Art.coin(c, 75, 1759, 9);
        Art.coin(c, 80, 1805, 20);
        Art.coin(c, 115, 1808, 20);
        Art.gem(c, 97, 1795, 18);

        Icons.wheel(c, 805, 1760, 130);

        drawBottomNav(c);
    }

    private void playerCount(Canvas c, float cx, int n) {
        String s = "Players: " + String.format(Locale.US, "%,d", n);
        Art.reset();
        Art.P.setTypeface(Art.COND);
        Art.P.setTextSize(26);
        float size = Math.min(26, 26 * 200 / Art.P.measureText(s));
        Art.P.setTextSize(size);
        float w = Art.P.measureText(s);
        Art.circle(c, cx - w / 2 - 6, 1063, 6, 0xFF1ED81E);
        Art.text(c, s, cx - w / 2 + 6, 1072, size, 0xFF7CFF3A, 0xFF0A2A00, 3, Art.COND, Paint.Align.LEFT);
    }

    private void drawTopBar(Canvas c) {
        Art.reset();
        Art.P.setShader(new LinearGradient(0, 105, 0, 195, 0xFF0B2A78, 0xFF0A1F5C, Shader.TileMode.CLAMP));
        c.drawRect(-1000, 105, 1900, 195, Art.P);
        Art.P.setShader(null);
        Art.rrect(c, -1000, 103, 1900, 107, 0, 0xFFFFC21A);
        Art.reset();
        Art.P.setStyle(Paint.Style.STROKE);
        Art.P.setStrokeWidth(4);
        Art.P.setColor(0xFFFFC21A);
        c.drawLine(-1000, 195, 150, 195, Art.P);
        c.drawLine(150, 195, 125, 225, Art.P);
        c.drawLine(125, 225, 0, 225, Art.P);
        c.drawLine(150, 195, 1900, 195, Art.P);
        Icons.avatar(c, 66, 155, 74);
        Art.circle(c, 101, 122, 7, 0xFF1ED81E);
        Art.rrect(c, 22, 206, 118, 226, 10, 0xFF0A1F5C);
        Art.rrectStroke(c, 22, 206, 118, 226, 10, 0xFFFFC21A, 3);
        Art.star(c, 28, 215, 18, 0xFFFFC21A, 0xFF8A5200, 2);
        Art.textC(c, "1", 28, 223, 20, 0xFF3A2400, 0, 0, Art.BLACK);
        Icons.gear(c, 150, 148, 48);
        Icons.mail(c, 236, 148, 58);
        Art.circle(c, 258, 128, 9, 0xFFE8202A);
        // gems
        Art.rrect(c, 300, 120, 495, 176, 8, 0xFF061340);
        Art.gem(c, 302, 138, 20);
        Art.text(c, String.valueOf(Prefs.gems()), 450, 164, 40, 0xFFFFFFFF, 0, 0, Art.COND, Paint.Align.RIGHT);
        Art.plus(c, 475, 147, 32);
        // coins
        Art.rrect(c, 535, 120, 825, 176, 8, 0xFF061340);
        Art.coin(c, 532, 148, 20);
        Art.text(c, String.format(Locale.US, "%,d", Prefs.coins()), 770, 164, 40, 0xFFFFFFFF, 0, 0, Art.COND,
                Paint.Align.RIGHT);
        Art.plus(c, 802, 147, 32);
        Icons.cart(c, 862, 148, 52);
    }

    private void drawBottomNav(Canvas c) {
        Art.rrectGrad(c, -1000, 1895, 1900, 2100, 0, 0xFF1A4FB8, 0xFF0B2A78);
        Art.rrectGrad(c, 0, 1865, 214, 2060, 22, 0xFF4A90F0, 0xFF1E5CC8);
        String[] labels = {"HOME", "EVENT", "ADDA", "INVENTORY", "SOCIAL"};
        int[] icons = {Icons.HOME, Icons.EVENT, Icons.MIC, Icons.INVENTORY, Icons.SOCIAL};
        float[] xs = {107, 300, 467, 642, 812};
        for (int i = 0; i < 5; i++) {
            if (i > 1) Art.rrect(c, xs[i] - 88, 1905, xs[i] - 86, 2000, 0, 0x40FFFFFF);
            Icons.draw(c, icons[i], xs[i], i == 0 ? 1895 : 1915, i == 0 ? 95 : 80);
            Art.textC(c, labels[i], xs[i], i == 0 ? 1968 : 1978, i == 0 ? 32 : 28, 0xFFFFFFFF, 0xFF0A1F5C, 4, Art.COND);
        }
        Art.circle(c, 345, 1888, 16, 0xFFE8202A);
        Art.textC(c, "!", 345, 1898, 26, 0xFFFFFFFF, 0, 0, Art.BLACK);
        Art.rrect(c, 388, 1873, 437, 1900, 4, 0xFFE8202A);
        Art.textC(c, "NEW", 412, 1895, 20, 0xFFFFFFFF, 0, 0, Art.BLACK);
    }

    private void shield(Canvas c, float cx, float cy, float s) {
        Art.reset();
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(cx - s * 0.45f, cy - s * 0.4f);
        p.lineTo(cx + s * 0.45f, cy - s * 0.4f);
        p.lineTo(cx + s * 0.4f, cy + s * 0.15f);
        p.lineTo(cx, cy + s * 0.5f);
        p.lineTo(cx - s * 0.4f, cy + s * 0.15f);
        p.close();
        Art.P.setShader(new LinearGradient(0, cy - s / 2, 0, cy + s / 2, 0xFFFFE070, 0xFFB86E00, Shader.TileMode.CLAMP));
        c.drawPath(p, Art.P);
        Art.P.setShader(null);
        c.save();
        c.scale(0.84f, 0.84f, cx, cy);
        Art.P.setShader(new LinearGradient(0, cy - s / 2, 0, cy + s / 2, 0xFF8A3AE0, 0xFF3A0A8A, Shader.TileMode.CLAMP));
        c.drawPath(p, Art.P);
        Art.P.setShader(null);
        c.restore();
        Art.rrect(c, cx - 58, cy - 38, cx + 58, cy - 12, 8, 0xFF7A2AD0);
        Art.textC(c, "SEASON", cx, cy - 17, 22, 0xFFFFE070, 0xFF2A0A5A, 3, Art.BLACK);
        Art.textC(c, "27", cx, cy + 32, 46, 0xFFFFE070, 0xFF2A0A5A, 5, Art.BLACK);
    }

    /** Dice mascot face. */
    static void mascot(Canvas c, float cx, float cy, float s) {
        Art.rrectGrad(c, cx - s * 0.5f, cy - s * 0.45f, cx + s * 0.5f, cy + s * 0.5f, s * 0.2f, 0xFFFFF08A, 0xFFE8A800);
        Art.circle(c, cx - s * 0.18f, cy, s * 0.12f, 0xFFFFFFFF);
        Art.circle(c, cx + s * 0.18f, cy, s * 0.12f, 0xFFFFFFFF);
        Art.circle(c, cx - s * 0.16f, cy + s * 0.02f, s * 0.06f, 0xFF222222);
        Art.circle(c, cx + s * 0.2f, cy + s * 0.02f, s * 0.06f, 0xFF222222);
        Art.reset();
        Art.P.setStyle(Paint.Style.STROKE);
        Art.P.setStrokeWidth(s * 0.05f);
        Art.P.setColor(0xFF6A3A00);
        Art.R.set(cx - s * 0.12f, cy + s * 0.12f, cx + s * 0.12f, cy + s * 0.3f);
        c.drawArc(Art.R, 20, 140, false, Art.P);
    }

    // ------------------------------------------------------------------ popups

    private static final String[] DAY_LABEL = {"300", "2", "CUPCAKE", "500", "8", "600", "BRONZE"};

    private void drawDaily(Canvas c) {
        Art.panel(c, 78, 315, 822, 1688);
        Art.ribbon(c, 450, 322, 440, 80, "DAILY BONUS");
        int claimed = Prefs.bonusDays();
        int cur = claimed % 7;
        // progress box
        Art.rrect(c, 122, 397, 770, 570, 16, 0xFF0A2A7A);
        Art.rrectStroke(c, 122, 397, 770, 570, 16, 0xFF3A8AF0, 3);
        Art.rrect(c, 155, 528, 740, 552, 12, 0xFF08206A);
        float prog = Math.min(1f, (claimed % 28) / 28f);
        Art.rrect(c, 158, 531, 158 + 579 * prog + 50, 549, 10, 0xFFFFC21A);
        int[] lids = {0xFF3FAF3F, 0xFF3F7FE0, 0xFFE0303F, 0xFFD050D0};
        int[] marks = {7, 14, 21, 28};
        for (int i = 0; i < 4; i++) {
            float x = 290 + i * 135;
            Icons.chest(c, x, 475, 80, 0xFFE0B060, lids[i]);
            Art.circle(c, x, 540, 22, 0xFFFFC21A);
            Art.circle(c, x, 540, 18, 0xFF1C4FB0);
            Art.textC(c, String.valueOf(marks[i]), x, 550, 26, 0xFFFFFFFF, 0, 0, Art.BLACK);
        }
        Art.circle(c, 725, 438, 20, 0xFFE8202A);
        Art.textC(c, "i", 725, 448, 28, 0xFFFFFFFF, 0, 0, Art.BLACK);
        // day cards
        for (int d = 0; d < 7; d++) {
            float l, t, r, b;
            if (d < 6) {
                l = 118 + (d % 3) * 225;
                t = d < 3 ? 612 : 912;
                r = l + 202;
                b = t + 255;
            } else {
                l = 125;
                t = 1212;
                r = 762;
                b = 1440;
            }
            boolean done = d < cur;
            boolean today = d == cur;
            boolean pink = d == 2 || d == 4 || d == 6;
            if (today) Art.glow(c, l, t, r, b, 18, 0xFF3CFF3C, 12);
            Art.rrect(c, l, t, r, b, 18, today ? 0xFF3CFF3C : 0xFFE5A200);
            Art.reset();
            Art.P.setShader(new RadialGradient((l + r) / 2, (t + b) / 2, (r - l),
                    pink ? 0xFFE0207A : 0xFF2A6AE0, pink ? 0xFF7A0A4A : 0xFF0A1F6A, Shader.TileMode.CLAMP));
            Art.R.set(l + 6, t + 6, r - 6, b - 6);
            c.drawRoundRect(Art.R, 14, 14, Art.P);
            Art.P.setShader(null);
            // header tag
            int tag = (done || today) ? 0xFFC030E0 : 0xFFFFE030;
            float cx = (l + r) / 2;
            android.graphics.Path p = new android.graphics.Path();
            p.moveTo(cx - 80, t + 6);
            p.lineTo(cx + 80, t + 6);
            p.lineTo(cx + 80, t + 44);
            p.lineTo(cx, t + 58);
            p.lineTo(cx - 80, t + 44);
            p.close();
            Art.reset();
            Art.P.setColor(tag);
            c.drawPath(p, Art.P);
            Art.textC(c, "Day " + (d + 1), cx, t + 40, 32, (done || today) ? 0xFFFFFFFF : 0xFF6A2A00, 0, 0, Art.BLACK);
            float iy = d < 6 ? t + 130 : (t + b) / 2 + 10;
            float ix = d < 6 ? cx : l + 130;
            dayIcon(c, d, ix, iy);
            if (d < 6) {
                Art.rrect(c, l + 22, b - 68, r - 22, b - 18, 8, 0x99000020);
                Art.textFit(c, DAY_LABEL[d], cx, b - 28, 40, r - l - 50, 0xFFFFFFFF, 0, 0,
                        d == 2 ? Art.BLACK : Art.COND);
            } else {
                Art.rrect(c, 505, 1288, 715, 1360, 10, 0xFF6A0A3A);
                Art.textC(c, "BRONZE", 610, 1340, 42, 0xFFFFFFFF, 0, 0, Art.BLACK);
            }
            if (done) {
                Art.check(c, cx - (d < 6 ? 0 : 200), iy, 95, 0xFF1A7A1A, 26);
                Art.check(c, cx - (d < 6 ? 0 : 200), iy, 95, 0xFF3CFF3C, 16);
            }
            if (!done && !today) {
                Art.rrect(c, r - 32, t - 12, r + 2, t + 26, 6, 0xFFFFC21A);
                Art.ring(c, r - 15, t - 16, 10, 0xFFFFC21A, 5);
                Art.circle(c, r - 15, t + 8, 5, 0xFF6A3A00);
            }
        }
        Art.textFit(c, "Come back every day for new reward", 450, 1520, 44, 680, 0xFFFFFFFF, 0xFF0A1F5C, 3, Art.COND);
        Art.pillButton(c, bClaim, "CLAIM", null, down(bClaim), 0xFF3F8BF0, 0xFF12408F);
        Art.greenButton(c, bClaim2x, "", down(bClaim2x));
        Art.coin(c, 570, 1608, 22);
        Art.coin(c, 595, 1614, 22);
        Art.text(c, rewardAmount(cur) > 0 ? String.valueOf(rewardAmount(cur) * 2) : "x2", 730, 1625, 44,
                0xFFFFFFFF, 0xFF1E5A08, 5, Art.BLACK, Paint.Align.RIGHT);
        Art.rrect(c, 470, 1585, 530, 1630, 6, 0xFFE8202A);
        Art.textC(c, "AD", 500, 1620, 28, 0xFFFFFFFF, 0, 0, Art.BLACK);
        Art.rrect(c, 722, 1530, 785, 1572, 6, 0xFFE8202A);
        Art.textC(c, "2x", 753, 1562, 32, 0xFFFFFFFF, 0, 0, Art.BLACK);
    }

    private static int rewardAmount(int day) {
        switch (day) {
            case 0: return 300;
            case 3: return 500;
            case 5: return 600;
            case 6: return 1000;
            default: return 0;
        }
    }

    private void dayIcon(Canvas c, int d, float x, float y) {
        switch (d) {
            case 1: Icons.wheel(c, x, y, 110); break;
            case 2:
                Art.rrect(c, x - 55, y - 55, x + 55, y + 55, 6, 0xFFB03060);
                Art.rrect(c, x - 45, y - 45, x + 45, y + 45, 4, 0xFFE0508A);
                Art.circle(c, x, y + 5, 30, 0xFFFFB0D0);
                Art.circle(c, x, y - 18, 12, 0xFFE8202A);
                break;
            case 4:
                Art.gem(c, x - 22, y + 5, 34);
                Art.gem(c, x + 22, y, 30);
                break;
            case 6: Icons.chest(c, x, y, 150, 0xFFE0B060, 0xFF3FAF3F); break;
            default:
                Art.coin(c, x - 20, y + 10, 38);
                Art.coin(c, x + 22, y + 20, 38);
                Art.coin(c, x, y - 12, 38);
                break;
        }
    }

    private void claimDaily(boolean doubled) {
        int cur = Prefs.bonusDays() % 7;
        int mult = doubled ? 2 : 1;
        String msg;
        if (cur == 1) msg = "You got 2 free spins!";
        else if (cur == 2) msg = "You got a Cupcake!";
        else if (cur == 4) {
            Prefs.addGems(8 * mult);
            msg = "+" + (8 * mult) + " gems";
        } else {
            Prefs.addCoins(rewardAmount(cur) * mult);
            msg = "+" + (rewardAmount(cur) * mult) + " coins";
        }
        Prefs.claimBonus();
        view.toast(msg);
        nextPopup(POP_DAILY);
    }

    private void drawNotify(Canvas c) {
        // mascot with crown
        Art.rrectGrad(c, 385, 440, 525, 590, 26, 0xFFFFFFFF, 0xFFD8DDE8);
        Art.circle(c, 425, 500, 12, 0xFF222222);
        Art.circle(c, 485, 500, 12, 0xFF222222);
        Art.circle(c, 429, 496, 4, 0xFFFFFFFF);
        Art.circle(c, 489, 496, 4, 0xFFFFFFFF);
        Art.reset();
        Art.P.setStyle(Paint.Style.STROKE);
        Art.P.setStrokeWidth(5);
        Art.P.setColor(0xFF222222);
        Art.R.set(435, 520, 475, 545);
        c.drawArc(Art.R, 20, 140, false, Art.P);
        Art.crown(c, 455, 420, 110);
        Art.rrectGrad(c, 395, 590, 515, 690, 18, 0xFFE8202A, 0xFF9A0A10);
        Art.rrect(c, 420, 610, 490, 660, 8, 0xFFFFFFFF);
        Art.textC(c, "LUDO", 455, 645, 22, 0xFF1C4FB0, 0, 0, Art.BLACK);
        Icons.phone(c, 560, 540, 50, 85, 15);
        Art.check(c, 562, 540, 30, 0xFF1ED81E, 6);
        // panel
        Art.rrectGrad(c, 35, 700, 865, 1320, 40, 0xFF5AA8FF, 0xFF1E5CD0);
        Art.rrectGrad(c, 125, 700, 775, 800, 30, 0xFF3F8BF0, 0xFF1E5CD0);
        Art.textC(c, "Notification", 450, 785, 70, 0xFFFFFFFF, 0xFF0A1F5C, 4, Art.COND);
        Art.rrect(c, 60, 820, 840, 1290, 22, 0xFFF7E8D8);
        Art.textFit(c, "Get reminders for daily", 450, 980, 66, 740, 0xFF000000, 0, 0, Art.COND);
        Art.textFit(c, "bonus and rewards?", 450, 1072, 66, 740, 0xFF000000, 0, 0, Art.COND);
        Art.star(c, 110, 1215, 22, 0xFFFFB080, 0, 0);
        Art.star(c, 790, 1205, 24, 0xFFFFB080, 0, 0);
        Art.greenButton(c, bYes, "YES", down(bYes));
        Art.closeButton(c, 825, 772, 42, down(825, 772, 50));
    }

    private void drawOffer(Canvas c) {
        float l = 77, t = 732, r = 822, b = 1268;
        Art.rrect(c, l - 4, t - 4, r + 4, b + 4, 4, 0xFFFFA800);
        Art.reset();
        Art.P.setShader(new RadialGradient(450, 1000, 500, 0xFF8A6A3A, 0xFF1A1030, Shader.TileMode.CLAMP));
        c.drawRect(l, t, r, b, Art.P);
        Art.P.setShader(null);
        Random sp = new Random(7);
        long now = System.currentTimeMillis();
        for (int i = 0; i < 70; i++) {
            float x = l + sp.nextFloat() * (r - l), y = t + sp.nextFloat() * (b - t);
            float tw = (float) (0.5 + 0.5 * Math.sin(now / 200.0 + i));
            Art.circle(c, x, y, 2 + 3 * tw, ((int) (200 * tw) << 24) | 0xFFE070);
        }
        // chests of gold
        for (int side = -1; side <= 1; side += 2) {
            float cx = 450 + side * 250;
            Icons.chest(c, cx, 1110, 190, 0xFFA0602A, 0xFF7A4A1A);
            for (int i = 0; i < 6; i++) Art.coin(c, cx - 60 + i * 24, 1062 - (i % 2) * 10, 16);
        }
        for (int i = 0; i < 14; i++) Art.coin(c, 200 + i * 38, 1200 + (i % 3) * 8, 15);
        Art.rrectGrad(c, 290, 915, 610, 1120, 16, 0xFFB07A2A, 0xFF6A3A10);
        Art.titleText(c, "100K", 450, 1025, 120, Art.BLACK);
        Art.titleText(c, "COINS", 450, 1100, 78, Art.BLACK);
        // characters
        Art.circle(c, 450, 820, 62, 0xFF4FA8FF);
        Art.ring(c, 425, 812, 18, 0xFF222222, 5);
        Art.ring(c, 475, 812, 18, 0xFF222222, 5);
        Art.circle(c, 425, 812, 7, 0xFF222222);
        Art.circle(c, 475, 812, 7, 0xFF222222);
        Art.rrect(c, 520, 780, 600, 830, 12, 0xFFE8202A);
        Art.circle(c, 230, 930, 60, 0xFFE8581A);
        Art.circle(c, 212, 925, 9, 0xFF222222);
        Art.circle(c, 248, 925, 9, 0xFF222222);
        c.save();
        c.rotate(-12, 230, 830);
        Art.rrect(c, 135, 790, 320, 875, 10, 0xFFFFFFFF);
        Art.rrect(c, 143, 798, 312, 867, 8, 0xFFE8202A);
        Art.textC(c, "50% OFF", 228, 852, 50, 0xFFFFE070, 0xFF6A0A0A, 5, Art.BLACK);
        c.restore();
        Art.greenButton(c, bPrice, "AED 9.99", down(bPrice));
        Art.closeButton(c, 797, 750, 42, down(797, 750, 50));
    }

    // ------------------------------------------------------------------ input

    @Override
    void onTap(float x, float y) {
        if (popup == POP_DAILY) {
            if (hit(bClaim, x, y)) claimDaily(false);
            else if (hit(bClaim2x, x, y)) claimDaily(true);
            return;
        }
        if (popup == POP_NOTIFY) {
            if (hit(bYes, x, y)) {
                Prefs.setNotifyAsked();
                view.toast("Reminders turned on");
                nextPopup(POP_NOTIFY);
            } else if (hit(825, 772, 55, x, y)) {
                Prefs.setNotifyAsked();
                nextPopup(POP_NOTIFY);
            }
            return;
        }
        if (popup == POP_OFFER) {
            if (hit(bPrice, x, y)) view.toast("In-app purchases are not available in this version");
            else if (hit(797, 750, 55, x, y) || y < 700 || y > 1300) popup = POP_NONE;
            return;
        }
        if (hit(bComputer, x, y)) view.setScene(new SetupScene(view, this, MODE_COMPUTER));
        else if (hit(bPass, x, y)) view.setScene(new SetupScene(view, this, MODE_PASS));
        else if (hit(bOnline, x, y)) view.setScene(new SetupScene(view, this, MODE_ONLINE));
        else if (hit(bFriends, x, y)) view.setScene(new SetupScene(view, this, MODE_FRIENDS));
        else if (hit(bTeam, x, y)) view.setScene(new SetupScene(view, this, MODE_TEAM));
        else if (hit(bGear, x, y) || hit(66, 155, 45, x, y)) view.setScene(new SettingsScene(view, this));
        else if (hit(bTournament, x, y)) view.toast("Tournaments start soon - stay tuned!");
        else if (hit(bSeason, x, y)) view.toast("Season 27 reward already claimed");
        else if (hit(178, 1453, 70, x, y) || hit(730, 1453, 70, x, y)) view.toast("Coming soon");
        else if (hit(805, 1760, 70, x, y)) {
            Prefs.addCoins(100);
            view.toast("Spin reward: +100 coins");
        } else if (hit(835, 290, 50, x, y)) {
            Prefs.addCoins(50);
            view.toast("Free chest: +50 coins");
        } else if (hit(bFree, x, y)) view.toast("No ads in this version");
        else if (y > 1860 && x > 214) view.toast("Coming soon");
    }

    @Override
    boolean onBack() {
        if (popup != POP_NONE) {
            if (popup == POP_DAILY) claimDaily(false);
            else nextPopup(popup);
            return true;
        }
        return false;
    }
}
