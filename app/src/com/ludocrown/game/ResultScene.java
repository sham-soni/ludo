package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Winner screen shown over the finished board. */
class ResultScene extends Scene {
    private final BoardScene board;
    private final List<Integer> ranking = new ArrayList<>();
    private final int winner;
    private final RectF bAgain = new RectF(110, 1420, 430, 1515);
    private final RectF bHome = new RectF(470, 1420, 790, 1515);
    private final float[][] confetti = new float[70][5];

    ResultScene(GameView v, BoardScene board) {
        super(v);
        this.board = board;
        final LudoGame g = board.game;
        ranking.addAll(g.finishOrder);
        List<Integer> rest = new ArrayList<>();
        for (int p = 0; p < 4; p++) if (board.cfg.active[p] && !ranking.contains(p)) rest.add(p);
        Collections.sort(rest, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return g.progress(b) - g.progress(a);
            }
        });
        ranking.addAll(rest);
        winner = ranking.get(0);
        if (!board.cfg.bot[winner]) Prefs.addCoins(250);
        Random r = new Random();
        int[] cols = {0xFFE8202A, 0xFF0CA24B, 0xFFFEDB1F, 0xFF1E8FE8, 0xFFFFFFFF, 0xFFFF7AE0};
        for (float[] c : confetti) {
            c[0] = r.nextFloat() * 900;
            c[1] = -r.nextFloat() * 1500;
            c[2] = 150 + r.nextFloat() * 250;
            c[3] = cols[r.nextInt(cols.length)];
            c[4] = r.nextFloat() * 360;
        }
    }

    @Override
    void draw(Canvas c) {
        board.draw(c);
        Art.rrect(c, -2000, -2000, 3000, 4000, 0, 0xC0000000);
        float k = Math.min(1f, age() / 300f);
        c.save();
        c.scale(0.7f + 0.3f * k, 0.7f + 0.3f * k, 450, 1000);
        Art.panel(c, 70, 440, 830, 1560);
        Art.ribbon(c, 450, 445, 420, 90, "WINNER");
        // glow rays
        long now = System.currentTimeMillis();
        c.save();
        c.rotate((now / 40f) % 360, 450, 700);
        for (int i = 0; i < 12; i++) {
            c.rotate(30, 450, 700);
            Art.rrect(c, 440, 520, 460, 700, 10, 0x22FFE070);
        }
        c.restore();
        Art.crown(c, 450, 585, 150);
        boolean team = board.cfg.mode == LudoGame.TEAM;
        if (team) {
            Art.pin(c, 390, 780, 170, winner, false);
            Art.pin(c, 510, 780, 170, (winner + 2) % 4, false);
        } else {
            Art.pin(c, 450, 785, 190, winner, false);
        }
        String title = team ? "Team " + Art.NAME[winner] + " & " + Art.NAME[(winner + 2) % 4] + " wins!"
                : board.cfg.names[winner] + " wins!";
        Art.textFit(c, title, 450, 890, 60, 700, 0xFFFFD31A, 0xFF3A1C00, 7, Art.BLACK);
        String[] place = {"1st", "2nd", "3rd", "4th"};
        for (int i = 0; i < ranking.size(); i++) {
            int p = ranking.get(i);
            float y = 950 + i * 105;
            Art.rrect(c, 110, y, 790, y + 90, 16, i == 0 ? 0xFF2C6A1E : 0xFF0A2A7A);
            Art.rrectStroke(c, 110, y, 790, y + 90, 16, i == 0 ? 0xFF7CFF3A : 0xFF3A7FE0, 3);
            int medal = i == 0 ? 0xFFFFC21A : i == 1 ? 0xFFC8D0DA : i == 2 ? 0xFFD08A4A : 0xFF5A6A8A;
            Art.circle(c, 165, y + 45, 32, medal);
            Art.textC(c, place[i], 165, y + 56, 28, 0xFF1A1A1A, 0, 0, Art.BLACK);
            Art.pin(c, 250, y + 62, 70, p, false);
            Art.text(c, board.cfg.names[p], 300, y + 58, 40, 0xFFFFFFFF, 0, 0, Art.COND,
                    android.graphics.Paint.Align.LEFT);
            int home = 0;
            for (int t = 0; t < 4; t++) if (board.game.pos[p][t] == LudoGame.HOME) home++;
            Art.text(c, home + "/4 home", 770, y + 56, 30, 0xFFB8C8E8, 0, 0, Art.COND,
                    android.graphics.Paint.Align.RIGHT);
        }
        if (!board.cfg.bot[winner]) {
            Art.coin(c, 370, 1378, 20);
            Art.text(c, "+250 coins", 400, 1390, 36, 0xFFFFE070, 0, 0, Art.BLACK, android.graphics.Paint.Align.LEFT);
        }
        Art.pillButton(c, bAgain, "Play Again", null, down(bAgain));
        Art.pillButton(c, bHome, "Home", null, down(bHome), 0xFF3FB83F, 0xFF12601A);
        c.restore();
        // confetti
        float t = age() / 1000f;
        for (float[] f : confetti) {
            float y = f[1] + f[2] * t;
            if (y > 2100) continue;
            c.save();
            c.rotate(f[4] + t * 200, f[0], y);
            Art.rrect(c, f[0] - 8, y - 4, f[0] + 8, y + 4, 1, (int) f[3]);
            c.restore();
        }
    }

    @Override
    void onTap(float x, float y) {
        if (hit(bAgain, x, y)) view.setScene(new BoardScene(view, board.cfg));
        else if (hit(bHome, x, y)) view.setScene(new HomeScene(view));
    }

    @Override
    boolean onBack() {
        view.setScene(new HomeScene(view));
        return true;
    }
}
