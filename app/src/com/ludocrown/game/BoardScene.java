package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Ludo board, dice, turn flow and animations. */
class BoardScene extends Scene {
    static final float BX = 18, BY = 570, BS = 862, CS = BS / 15f;

    private static final int S_ROLL = 0, S_ROLLING = 1, S_MOVE = 2, S_MOVING = 3, S_PASS = 4, S_OVER = 5;
    private static final long ROLL_MS = 650, STEP_MS = 125, BACK_MS = 900;

    final GameConfig cfg;
    final LudoGame game;
    int turn;
    private int state = S_ROLL;
    private long stateAt = System.currentTimeMillis();
    private int dice = 6;
    private int pendingRoll;
    private int sixes;
    private final int[] lastDice = {0, 0, 0, 0};
    private List<Integer> moves = new ArrayList<>();
    private boolean autoMove;

    // moving animation
    private int mvToken = -1;
    private final List<Integer> steps = new ArrayList<>();
    private int mvFrom;
    private long mvAt;

    // captured tokens flying back
    private final List<float[]> flying = new ArrayList<>(); // {player, token, fromProgress, startTime}
    private long landAt;
    private int stepsPlayed;

    private boolean paused;
    private final RectF bResume = new RectF(250, 820, 650, 920);
    private final RectF bRestart = new RectF(250, 960, 650, 1060);
    private final RectF bExit = new RectF(250, 1100, 650, 1200);

    BoardScene(GameView v, GameConfig cfg) {
        super(v);
        this.cfg = cfg;
        this.game = new LudoGame(cfg.active, cfg.mode, cfg.oneOut, Prefs.winner());
        // player 1 (first human, blue if present) starts
        turn = -1;
        int[] order = {3, 0, 1, 2};
        for (int p : order) {
            if (cfg.active[p] && !cfg.bot[p]) {
                turn = p;
                break;
            }
        }
        if (turn < 0) for (int p : order) if (cfg.active[p]) { turn = p; break; }
    }

    // ------------------------------------------------------------------ geometry

    static float cx(float col) { return BX + col * CS; }
    static float cy(float row) { return BY + row * CS; }

    private static final int[][] QUAD = {{0, 0}, {9, 0}, {9, 9}, {0, 9}};
    private static final float[][] SLOT = {{2, 2}, {4, 2}, {2, 4}, {4, 4}};

    /** Pixel centre for a token of player p at a progress value. */
    static float[] spot(int p, int t, int prog) {
        float c, r;
        if (prog == LudoGame.YARD) {
            c = QUAD[p][0] + SLOT[t][0];
            r = QUAD[p][1] + SLOT[t][1];
        } else if (prog <= LudoGame.LAST_TRACK) {
            int[] cell = LudoGame.TRACK[LudoGame.cellOf(p, prog)];
            c = cell[0] + 0.5f;
            r = cell[1] + 0.5f;
        } else if (prog < LudoGame.HOME) {
            int i = prog - 51;
            switch (p) {
                case 0: c = 1 + i + 0.5f; r = 7.5f; break;
                case 1: c = 7.5f; r = 1 + i + 0.5f; break;
                case 2: c = 13 - i + 0.5f; r = 7.5f; break;
                default: c = 7.5f; r = 13 - i + 0.5f; break;
            }
        } else {
            float off = (t - 1.5f) * 0.32f;
            switch (p) {
                case 0: c = 6.75f; r = 7.5f + off; break;
                case 1: c = 7.5f + off; r = 6.75f; break;
                case 2: c = 8.25f; r = 7.5f + off; break;
                default: c = 7.5f + off; r = 8.25f; break;
            }
        }
        return new float[]{cx(c), cy(r)};
    }

    private static final RectF[] DICE_BOX = {
            new RectF(72, 415, 330, 552), new RectF(570, 415, 828, 552),
            new RectF(570, 1448, 828, 1585), new RectF(72, 1448, 330, 1585)};

    private static boolean pinLeft(int p) { return p == 0 || p == 3; }

    private static RectF diceRect(int p) {
        RectF b = DICE_BOX[p];
        if (pinLeft(p)) return new RectF(b.left + 112, b.top + 8, b.right - 8, b.bottom - 8);
        return new RectF(b.left + 8, b.top + 8, b.right - 112, b.bottom - 8);
    }

    // ------------------------------------------------------------------ update

    private void setState(int s) {
        state = s;
        stateAt = System.currentTimeMillis();
    }

    private void update() {
        if (paused) return;
        long now = System.currentTimeMillis();
        long dt = now - stateAt;
        switch (state) {
            case S_ROLL:
                if (cfg.bot[turn] && dt > 650) startRoll();
                break;
            case S_ROLLING:
                if (dt > ROLL_MS) finishRoll();
                break;
            case S_MOVE:
                if ((cfg.bot[turn] && dt > 420) || (autoMove && dt > 280)) {
                    int t = moves.size() == 1 ? moves.get(0) : pickBotMove();
                    startMove(t);
                }
                break;
            case S_MOVING:
                // a "tok" as the token lands on each square
                int landed = (int) Math.min(steps.size(), (now - mvAt) / STEP_MS);
                if (landed > stepsPlayed) {
                    stepsPlayed = landed;
                    Sfx.play(Sfx.STEP, 0.9f + 0.03f * (landed % 5));
                }
                if (now - mvAt >= steps.size() * STEP_MS) finishMove();
                break;
            case S_PASS:
                if (dt > 750) nextTurn();
                break;
            case S_OVER:
                if (dt > 1100) view.setScene(new ResultScene(view, this));
                break;
            default:
                break;
        }
    }

    private int pickBotMove() {
        int best = -1, bestScore = Integer.MIN_VALUE;
        for (int t : moves) {
            int s = game.score(turn, t, dice) + game.rnd.nextInt(6);
            if (s > bestScore) {
                bestScore = s;
                best = t;
            }
        }
        return best;
    }

    private void startRoll() {
        pendingRoll = game.rollFor(turn, sixes);
        Sfx.play(Sfx.ROLL);
        setState(S_ROLLING);
    }

    private void finishRoll() {
        dice = pendingRoll;
        lastDice[turn] = dice;
        landAt = System.currentTimeMillis();
        if (dice == 6) Sfx.play(Sfx.SIX);
        if (dice == 6) sixes++;
        if (sixes >= 3) {
            view.toast("Three sixes in a row - turn lost");
            setState(S_PASS);
            return;
        }
        moves = game.allowedMoves(turn, dice);
        if (moves.isEmpty()) {
            setState(S_PASS);
            return;
        }
        autoMove = !cfg.bot[turn] && (moves.size() == 1 || sameSpot(moves));
        setState(S_MOVE);
    }

    /** All movable tokens are in the yard (or stacked) - no real choice to make. */
    private boolean sameSpot(List<Integer> mv) {
        int first = game.pos[turn][mv.get(0)];
        for (int t : mv) if (game.pos[turn][t] != first) return false;
        return true;
    }

    private void startMove(int t) {
        mvToken = t;
        mvFrom = game.pos[turn][t];
        steps.clear();
        int to = game.target(turn, t, dice);
        if (mvFrom == LudoGame.YARD) steps.add(0);
        else for (int s = mvFrom + 1; s <= to; s++) steps.add(s);
        mvAt = System.currentTimeMillis();
        stepsPlayed = 0;
        setState(S_MOVING);
    }

    private void finishMove() {
        int to = steps.get(steps.size() - 1);
        long now = System.currentTimeMillis();
        List<int[]> caps = game.captures(turn, to);
        game.pos[turn][mvToken] = to;
        for (int[] cap : caps) {
            flying.add(new float[]{cap[0], cap[1], game.pos[cap[0]][cap[1]], now});
            game.pos[cap[0]][cap[1]] = LudoGame.YARD;
        }
        if (!caps.isEmpty()) {
            Sfx.play(Sfx.CAPTURE);
            Sfx.vibrate(150);
        } else if (to == LudoGame.HOME) {
            Sfx.play(Sfx.HOME);
        }
        mvToken = -1;
        if (!caps.isEmpty()) view.toast(cfg.names[turn] + " captured " + cfg.names[caps.get(0)[0]] + "!");
        if (game.won(turn)) {
            game.finishOrder.add(turn);
            if (cfg.mode == LudoGame.TEAM) game.finishOrder.add((turn + 2) % 4);
            Sfx.play(Sfx.WIN);
            setState(S_OVER);
            return;
        }
        boolean bonus = dice == 6 || !caps.isEmpty() || to == LudoGame.HOME;
        if (dice != 6) sixes = 0;
        if (bonus && !game.finished(turn)) {
            setState(S_ROLL);
        } else {
            nextTurn();
        }
    }

    private void nextTurn() {
        sixes = 0;
        for (int i = 1; i <= 4; i++) {
            int p = (turn + i) % 4;
            if (cfg.active[p] && !game.finished(p)) {
                turn = p;
                break;
            }
        }
        if (!cfg.bot[turn]) Sfx.play(Sfx.TURN);
        setState(S_ROLL);
    }

    // ------------------------------------------------------------------ drawing

    @Override
    void draw(Canvas c) {
        if (view.scene() == this) update();
        drawBoard(c);
        drawTokens(c);
        for (int p = 0; p < 4; p++) if (cfg.active[p]) drawDiceBox(c, p);
        Icons.menu(c, 97, 1910, 62);
        // "no ads" badge like the original layout
        Art.circle(c, 825, 1900, 52, 0xFF6A0A6A);
        Art.circle(c, 825, 1900, 46, 0xFFB03AD0);
        Art.textC(c, "NO", 825, 1893, 30, 0xFFFFFFFF, 0xFF6A0A2A, 5, Art.BLACK);
        Art.textC(c, "ADS", 825, 1925, 30, 0xFFFFFFFF, 0xFF6A0A2A, 5, Art.BLACK);
        if (paused) drawPause(c);
    }

    private void drawBoard(Canvas c) {
        Art.rrect(c, BX - 6, BY - 6, BX + BS + 6, BY + BS + 6, 4, 0x55000000);
        Art.rrect(c, BX, BY, BX + BS, BY + BS, 0, 0xFFFFFFFF);
        // quadrants
        for (int p = 0; p < 4; p++) {
            float l = cx(QUAD[p][0]), t = cy(QUAD[p][1]);
            Art.rrect(c, l, t, l + 6 * CS, t + 6 * CS, 0, Art.BOARD[p]);
            Art.rrect(c, l + CS, t + CS, l + 5 * CS, t + 5 * CS, 0, 0xFFFFFFFF);
            for (int s = 0; s < 4; s++) {
                Art.circle(c, cx(QUAD[p][0] + SLOT[s][0]), cy(QUAD[p][1] + SLOT[s][1]), CS * 0.55f, Art.BOARD[p]);
            }
        }
        if (state != S_OVER && cfg.active[turn]) {
            float l = cx(QUAD[turn][0]), t = cy(QUAD[turn][1]);
            int a = (int) (120 + 120 * Math.sin(System.currentTimeMillis() / 180.0));
            Art.rrectStroke(c, l + CS * 0.5f, t + CS * 0.5f, l + 5.5f * CS, t + 5.5f * CS, 4, (a << 24) | 0xFFFFFF, 8);
        }
        // coloured track cells
        for (int i = 0; i < 5; i++) {
            fillCell(c, 1 + i, 7, Art.BOARD[0]);
            fillCell(c, 7, 1 + i, Art.BOARD[1]);
            fillCell(c, 13 - i, 7, Art.BOARD[2]);
            fillCell(c, 7, 13 - i, 0xFF29A9E1);
        }
        fillCell(c, 1, 6, Art.BOARD[0]);
        fillCell(c, 8, 1, Art.BOARD[1]);
        fillCell(c, 13, 8, Art.BOARD[2]);
        fillCell(c, 6, 13, 0xFF29A9E1);
        // grid lines on the track arms
        Art.reset();
        Art.P.setColor(0xFF9A9A9A);
        Art.P.setStrokeWidth(1.6f);
        for (int i = 0; i <= 15; i++) {
            float v = i * CS;
            if (i >= 6 && i <= 9) {
                c.drawLine(BX + v, BY, BX + v, BY + 6 * CS, Art.P);
                c.drawLine(BX + v, BY + 9 * CS, BX + v, BY + BS, Art.P);
                c.drawLine(BX, BY + v, BX + 6 * CS, BY + v, Art.P);
                c.drawLine(BX + 9 * CS, BY + v, BX + BS, BY + v, Art.P);
            }
            if (i <= 6 || i >= 9) {
                c.drawLine(BX + 6 * CS, BY + v, BX + 9 * CS, BY + v, Art.P);
                c.drawLine(BX + v, BY + 6 * CS, BX + v, BY + 9 * CS, Art.P);
            }
        }
        // stars on safe squares
        int[][] stars = {{6, 2}, {12, 6}, {8, 12}, {2, 8}};
        for (int[] s : stars) Art.star(c, cx(s[0] + 0.5f), cy(s[1] + 0.52f), CS * 0.36f, 0, 0xFF555555, 2.5f);
        // entry arrows
        Art.arrow(c, cx(0.5f), cy(7.5f), CS * 0.55f, 0, Art.BOARD[0], 3);
        Art.arrow(c, cx(7.5f), cy(0.5f), CS * 0.55f, 90, Art.BOARD[1], 3);
        Art.arrow(c, cx(14.5f), cy(7.5f), CS * 0.55f, 180, 0xFFE0B800, 3);
        Art.arrow(c, cx(7.5f), cy(14.5f), CS * 0.55f, -90, 0xFF29A9E1, 3);
        // centre triangles
        float mx = cx(7.5f), my = cy(7.5f);
        triangle(c, cx(6), cy(6), cx(6), cy(9), mx, my, Art.BOARD[0]);
        triangle(c, cx(6), cy(6), cx(9), cy(6), mx, my, Art.BOARD[1]);
        triangle(c, cx(9), cy(6), cx(9), cy(9), mx, my, Art.BOARD[2]);
        triangle(c, cx(6), cy(9), cx(9), cy(9), mx, my, 0xFF29A9E1);
        Art.rrectStroke(c, BX, BY, BX + BS, BY + BS, 0, 0xFF888888, 2);
        // names
        for (int p = 0; p < 4; p++) {
            if (!cfg.active[p]) continue;
            float x = cx(QUAD[p][0] + 3);
            if (p >= 2) {
                Art.textFit(c, cfg.names[p], x, cy(QUAD[p][1] + 5.72f), 40, 5.6f * CS, 0xFFFFFFFF, 0xFF000000, 5, Art.COND);
            } else {
                c.save();
                c.rotate(180, x, cy(QUAD[p][1] + 0.5f));
                Art.textFit(c, cfg.names[p], x, cy(QUAD[p][1] + 0.5f) + 14, 40, 5.6f * CS, 0xFFFFFFFF, 0xFF000000, 5,
                        Art.COND);
                c.restore();
            }
        }
    }

    private void fillCell(Canvas c, float col, float row, int color) {
        Art.rrect(c, cx(col), cy(row), cx(col + 1), cy(row + 1), 0, color);
    }

    private void triangle(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        Path p = new Path();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.close();
        Art.reset();
        Art.P.setColor(color);
        c.drawPath(p, Art.P);
    }

    private static class Drawn {
        int p, t;
        float x, y, scale = 1;
        boolean highlight;
    }

    private void drawTokens(Canvas c) {
        long now = System.currentTimeMillis();
        List<Drawn> list = new ArrayList<>();
        Map<Long, List<Drawn>> groups = new HashMap<>();
        boolean showMoves = state == S_MOVE && !cfg.bot[turn] && !autoMove;
        for (int p = 0; p < 4; p++) {
            if (!cfg.active[p]) continue;
            for (int t = 0; t < 4; t++) {
                if (p == turn && t == mvToken) continue;
                if (isFlying(p, t)) continue;
                Drawn d = new Drawn();
                d.p = p;
                d.t = t;
                int prog = game.pos[p][t];
                float[] s = spot(p, t, prog);
                d.x = s[0];
                d.y = s[1];
                d.highlight = showMoves && p == turn && moves.contains(t);
                list.add(d);
                if (prog != LudoGame.YARD && prog != LudoGame.HOME) {
                    long key;
                    if (prog <= LudoGame.LAST_TRACK) key = LudoGame.cellOf(p, prog);
                    else key = 100 + p * 10 + prog;
                    List<Drawn> g = groups.get(key);
                    if (g == null) {
                        g = new ArrayList<>();
                        groups.put(key, g);
                    }
                    g.add(d);
                }
            }
        }
        for (List<Drawn> g : groups.values()) {
            if (g.size() < 2) continue;
            int n = g.size();
            for (int i = 0; i < n; i++) {
                Drawn d = g.get(i);
                d.scale = 0.72f;
                float ang = (float) (2 * Math.PI * i / n);
                d.x += (float) Math.cos(ang) * CS * 0.2f;
                d.y += (float) Math.sin(ang) * CS * 0.14f;
            }
        }
        Collections.sort(list, new Comparator<Drawn>() {
            @Override
            public int compare(Drawn a, Drawn b) {
                if (a.highlight != b.highlight) return a.highlight ? 1 : -1;
                return Float.compare(a.y, b.y);
            }
        });
        for (Drawn d : list) {
            float size = CS * 1.08f * d.scale;
            if (game.pos[d.p][d.t] == LudoGame.HOME) size = CS * 0.62f;
            float bob = 0;
            if (d.highlight) {
                float pulse = (float) (0.5 + 0.5 * Math.sin(now / 130.0));
                Art.ring(c, d.x, d.y + CS * 0.1f, CS * (0.38f + 0.1f * pulse), 0xEEFFFFFF, 5);
                bob = -(float) Math.abs(Math.sin(now / 200.0)) * 10;
            }
            Art.token(c, cfg.tokenStyle, d.x, d.y + CS * 0.12f + bob, size, d.p);
        }
        // flying (captured) tokens
        // captured tokens run backwards along the track to their yard, then hop in
        for (int i = flying.size() - 1; i >= 0; i--) {
            float[] f = flying.get(i);
            int p = (int) f[0], t = (int) f[1], from = (int) f[2];
            int n = from + 2; // squares back to the start, plus the hop into the yard
            float stepMs = Math.min(40f, BACK_MS / (float) n);
            float k = (now - (long) f[3]) / stepMs;
            if (k >= n - 1) {
                flying.remove(i);
                continue;
            }
            int idx = (int) k;
            float fr = k - idx;
            int a = from - idx, b = from - idx - 1; // b == -1 is the yard
            float[] pa = spot(p, t, a), pb = spot(p, t, b < 0 ? LudoGame.YARD : b);
            float hop = b < 0 ? 140 : 8;
            float x = pa[0] + (pb[0] - pa[0]) * fr, y = pa[1] + (pb[1] - pa[1]) * fr - (float) Math.sin(fr * Math.PI) * hop;
            Art.token(c, cfg.tokenStyle, x, y + CS * 0.12f, CS * 1.08f, p);
        }
        // moving token
        if (state == S_MOVING && mvToken >= 0) {
            float k = (now - mvAt) / (float) STEP_MS;
            int i = Math.min(steps.size() - 1, (int) k);
            float f = Math.min(1f, k - i);
            int prevProg = i == 0 ? mvFrom : steps.get(i - 1);
            float[] a = spot(turn, mvToken, prevProg), b = spot(turn, mvToken, steps.get(i));
            float x = a[0] + (b[0] - a[0]) * f, y = a[1] + (b[1] - a[1]) * f - (float) Math.sin(f * Math.PI) * CS * 0.45f;
            Art.token(c, cfg.tokenStyle, x, y + CS * 0.12f, CS * 1.15f, turn);
        }
    }

    private boolean isFlying(int p, int t) {
        for (float[] f : flying) if ((int) f[0] == p && (int) f[1] == t) return true;
        return false;
    }

    private void drawDiceBox(Canvas c, int p) {
        RectF b = DICE_BOX[p];
        boolean mine = p == turn && state != S_OVER;
        Art.glow(c, b.left, b.top, b.right, b.bottom, 14, mine ? 0xFFFFD000 : 0x66FFB000, mine ? 16 : 8);
        Art.rrect(c, b.left - 4, b.top - 4, b.right + 4, b.bottom + 4, 16, 0xFFFFC21A);
        Art.rrectGrad(c, b.left, b.top, b.right, b.bottom, 14, 0xFF1C4FB0, 0xFF0A2A7A);
        float pinX = pinLeft(p) ? b.left + 56 : b.right - 56;
        Art.rrectGrad(c, pinLeft(p) ? b.left : b.right - 112, b.top, pinLeft(p) ? b.left + 112 : b.right, b.bottom, 12,
                Art.COLOR[p], 0xFF0A2A7A);
        Art.token(c, cfg.tokenStyle, pinX, b.centerY() + 32, 100, p);
        RectF d = diceRect(p);
        Art.rrect(c, d.left - 3, d.top - 3, d.right + 3, d.bottom + 3, 14, 0xFFFFC21A);
        Art.rrectGrad(c, d.left, d.top, d.right, d.bottom, 12, 0xFFF8E0E0, 0xFFE0B0B8);
        if (cfg.bot[p]) Icons.robot(c, pinLeft(p) ? b.left + 20 : b.right - 20, b.top + 22, 26, 0xFFFFFFFF);
        if (mine) {
            long now = System.currentTimeMillis();
            if (state == S_ROLLING) {
                // tumbling dice: spins and bounces, faces change slower as it settles
                float k = Math.min(1f, (now - stateAt) / (float) ROLL_MS);
                int tick = (int) (Math.pow(k, 0.6) * 14);
                int face = 1 + (int) (((tick * 7919L) ^ (turn * 31L)) % 6);
                float rot = (1 - k) * (1 - k) * 540;
                float hop = (float) Math.abs(Math.sin(k * Math.PI * 2.5)) * 38 * (1 - k);
                float sc = 1 + 0.12f * (float) Math.sin(k * Math.PI);
                Art.reset();
                Art.P.setColor(0x44000000);
                Art.R.set(d.centerX() - 40, d.centerY() + 40, d.centerX() + 40, d.centerY() + 54);
                c.drawOval(Art.R, Art.P);
                Art.dice(c, d.centerX(), d.centerY() - hop, 92 * sc, face, rot, 0xFFDADADA);
            } else if (state == S_ROLL) {
                float s = 92 + (float) Math.sin(now / 150.0) * 5;
                Art.dice(c, d.centerX(), d.centerY(), s, dice, 0, 0xFFDADADA);
                // arrow pointing at the dice
                float bob = (float) Math.sin(now / 150.0) * 10;
                boolean leftSide = pinLeft(p);
                float ax = leftSide ? b.right + 45 + bob : b.left - 45 - bob;
                arrowShape(c, ax, b.centerY(), leftSide);
            } else {
                // small squash as the dice lands
                float t = Math.min(1f, (now - landAt) / 180f);
                float sc = 1 + 0.14f * (1 - t) * (float) Math.cos(t * Math.PI * 1.5);
                Art.dice(c, d.centerX(), d.centerY(), 92 * sc, dice, 0, 0xFFDADADA);
            }
        } else if (lastDice[p] > 0 && state != S_OVER) {
            // faded last roll for other players
            Art.dice(c, d.centerX(), d.centerY(), 70, 0, 0, 0xFFF0D8D8);
        }
    }

    private void arrowShape(Canvas c, float x, float y, boolean pointLeft) {
        Path p = new Path();
        float d = pointLeft ? -1 : 1;
        p.moveTo(x + d * 30, y);
        p.lineTo(x, y - 34);
        p.lineTo(x, y - 16);
        p.lineTo(x - d * 26, y - 16);
        p.lineTo(x - d * 26, y + 16);
        p.lineTo(x, y + 16);
        p.lineTo(x, y + 34);
        p.close();
        Art.reset();
        Art.P.setShader(new android.graphics.LinearGradient(0, y - 34, 0, y + 34, 0xFFFFE040, 0xFFFF5A00,
                android.graphics.Shader.TileMode.CLAMP));
        c.drawPath(p, Art.P);
        Art.P.setShader(null);
        Art.P.setStyle(Paint.Style.STROKE);
        Art.P.setStrokeWidth(3);
        Art.P.setColor(0xFF8A2A00);
        c.drawPath(p, Art.P);
    }

    private void drawPause(Canvas c) {
        Art.rrect(c, -2000, -2000, 3000, 4000, 0, 0xB0000000);
        Art.panel(c, 170, 680, 730, 1260);
        Art.titleText(c, "PAUSED", 450, 770, 60, Art.BLACK);
        Art.pillButton(c, bResume, "Resume", null, down(bResume));
        Art.pillButton(c, bRestart, "Restart", null, down(bRestart));
        Art.pillButton(c, bExit, "Home", null, down(bExit));
    }

    // ------------------------------------------------------------------ input

    @Override
    void onTap(float x, float y) {
        if (paused) {
            if (hit(bResume, x, y)) paused = false;
            else if (hit(bRestart, x, y)) view.setScene(new BoardScene(view, cfg));
            else if (hit(bExit, x, y)) view.setScene(new HomeScene(view));
            return;
        }
        if (hit(97, 1910, 50, x, y)) {
            paused = true;
            return;
        }
        if (hit(825, 1900, 55, x, y)) {
            view.toast("This version has no ads");
            return;
        }
        if (cfg.bot[turn]) return;
        if (state == S_ROLL) {
            RectF b = DICE_BOX[turn];
            RectF big = new RectF(b.left - 30, b.top - 30, b.right + 90, b.bottom + 30);
            if (!pinLeft(turn)) big.left -= 60;
            if (big.contains(x, y)) startRoll();
            return;
        }
        if (state == S_MOVE && !autoMove) {
            int best = -1;
            double bestD = CS * 0.9;
            for (int t : moves) {
                float[] s = spot(turn, t, game.pos[turn][t]);
                double d = Math.hypot(x - s[0], y - (s[1] - CS * 0.2f));
                if (d < bestD) {
                    bestD = d;
                    best = t;
                }
            }
            if (best >= 0) startMove(best);
        }
    }

    @Override
    boolean onBack() {
        paused = !paused;
        return true;
    }
}
