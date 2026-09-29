package com.ludocrown.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ludo rules. Token progress: -1 = in yard, 0..50 = main track (relative to the colour's start),
 * 51..55 = home column, 56 = reached home.
 *
 * Dice rolls go through {@link #rollFor(int)}, which applies the "winner control" setting: the chosen
 * colour (and its partner in Team Up) gets favourable rolls, and every other player only receives rolls
 * that can neither finish the game for them nor capture a token of the chosen colour.
 */
final class LudoGame {
    static final int CLASSIC = 0, TEAM = 1, QUICK = 2;
    static final int YARD = -1, HOME = 56, LAST_TRACK = 50;
    static final int[] OFFSET = {0, 13, 26, 39};

    /** 52 main-track cells as {col, row} starting at red's start square, going clockwise. */
    static final int[][] TRACK = {
            {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
            {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1}, {6, 0},
            {7, 0},
            {8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5},
            {9, 6}, {10, 6}, {11, 6}, {12, 6}, {13, 6}, {14, 6},
            {14, 7},
            {14, 8}, {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
            {8, 9}, {8, 10}, {8, 11}, {8, 12}, {8, 13}, {8, 14},
            {7, 14},
            {6, 14}, {6, 13}, {6, 12}, {6, 11}, {6, 10}, {6, 9},
            {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8}, {0, 8},
            {0, 7},
            {0, 6},
    };
    static final boolean[] SAFE = new boolean[52];

    static {
        int[] safe = {0, 8, 13, 21, 26, 34, 39, 47};
        for (int s : safe) SAFE[s] = true;
    }

    final int mode;
    final boolean[] active;
    final int favored;
    final int[][] pos = new int[4][4];
    final Random rnd = new Random();
    final List<Integer> finishOrder = new ArrayList<>();

    LudoGame(boolean[] active, int mode, boolean oneOut, int favored) {
        this.active = active.clone();
        this.mode = mode;
        this.favored = (favored >= 0 && favored < 4 && active[favored]) ? favored : -1;
        for (int p = 0; p < 4; p++) {
            for (int t = 0; t < 4; t++) {
                pos[p][t] = mode == QUICK ? 0 : YARD;
            }
            if (oneOut && mode != QUICK) pos[p][0] = 0;
        }
    }

    static int cellOf(int p, int prog) {
        if (prog < 0 || prog > LAST_TRACK) return -1;
        return (OFFSET[p] + prog) % 52;
    }

    boolean sameSide(int a, int b) {
        return a == b || (mode == TEAM && (a + 2) % 4 == b);
    }

    boolean canMove(int p, int t, int roll) {
        int s = pos[p][t];
        if (s == HOME) return false;
        if (s == YARD) return roll == 6;
        return s + roll <= HOME;
    }

    int target(int p, int t, int roll) {
        int s = pos[p][t];
        return s == YARD ? 0 : s + roll;
    }

    List<Integer> movable(int p, int roll) {
        List<Integer> r = new ArrayList<>();
        for (int t = 0; t < 4; t++) if (canMove(p, t, roll)) r.add(t);
        return r;
    }

    /** Opponent tokens that would be captured by moving p's token to progress prog. {player, token} pairs. */
    List<int[]> captures(int p, int prog) {
        List<int[]> r = new ArrayList<>();
        int cell = cellOf(p, prog);
        if (cell < 0 || SAFE[cell]) return r;
        for (int q = 0; q < 4; q++) {
            if (!active[q] || sameSide(p, q)) continue;
            for (int t = 0; t < 4; t++) {
                if (cellOf(q, pos[q][t]) == cell) r.add(new int[]{q, t});
            }
        }
        return r;
    }

    boolean finished(int p) {
        int home = 0;
        for (int t = 0; t < 4; t++) if (pos[p][t] == HOME) home++;
        return mode == QUICK ? home >= 1 : home == 4;
    }

    /** True when p's side has won the game. */
    boolean won(int p) {
        if (mode == TEAM) return finished(p) && (!active[(p + 2) % 4] || finished((p + 2) % 4));
        return finished(p);
    }

    boolean favoredSide(int p) {
        return favored >= 0 && sameSide(p, favored);
    }

    // ------------------------------------------------------------------ simulated outcomes

    /** Would moving token t by roll win the game for p? */
    private boolean moveWins(int p, int t, int roll) {
        int old = pos[p][t];
        pos[p][t] = target(p, t, roll);
        boolean w = won(p);
        pos[p][t] = old;
        return w;
    }

    private boolean moveHitsFavored(int p, int t, int roll) {
        for (int[] cap : captures(p, target(p, t, roll))) {
            if (favoredSide(cap[0])) return true;
        }
        return false;
    }

    /** Heuristic value of a move (used by bots and by the favoured roll picker). */
    int score(int p, int t, int roll) {
        int from = pos[p][t];
        int to = target(p, t, roll);
        int s = 0;
        if (moveWins(p, t, roll)) s += 1000;
        if (to == HOME) s += 120;
        s += captures(p, to).size() * 90;
        if (from == YARD) s += 70;
        int cell = cellOf(p, to);
        if (to > LAST_TRACK && from <= LAST_TRACK) s += 45; // enters home column
        if (cell >= 0) {
            if (SAFE[cell]) s += 25;
            s -= 30 * threats(p, cell);
        }
        int fromCell = cellOf(p, from);
        if (fromCell >= 0 && !SAFE[fromCell]) s += 20 * threats(p, fromCell);
        s += to / 4;
        return s;
    }

    /** Number of opponent tokens within 1..6 steps behind a track cell. */
    int threats(int p, int cell) {
        if (SAFE[cell]) return 0;
        int n = 0;
        for (int q = 0; q < 4; q++) {
            if (!active[q] || sameSide(p, q)) continue;
            for (int t = 0; t < 4; t++) {
                int c = cellOf(q, pos[q][t]);
                if (c < 0) continue;
                int d = (cell - c + 52) % 52;
                // the opponent must still be on the track for that distance
                if (d >= 1 && d <= 6 && pos[q][t] + d <= LAST_TRACK) n++;
            }
        }
        return n;
    }

    int bestMove(int p, int roll) {
        int best = -1, bestScore = Integer.MIN_VALUE;
        for (int t : movable(p, roll)) {
            int s = score(p, t, roll) + rnd.nextInt(6);
            if (s > bestScore) {
                bestScore = s;
                best = t;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------ dice

    int rollFor(int p, int sixes) {
        if (favored < 0) return 1 + rnd.nextInt(6);
        if (favoredSide(p)) return favoredRoll(p, sixes);
        return restrictedRoll(p);
    }

    private int favoredRoll(int p, int sixes) {
        int[] val = new int[7];
        int best = -1;
        for (int r = 1; r <= 6; r++) {
            int v = -50;
            for (int t : movable(p, r)) v = Math.max(v, score(p, t, r));
            if (r == 6 && sixes >= 2) v = -500; // never lose a turn to three sixes
            v += rnd.nextInt(30);
            val[r] = v;
            if (best < 0 || v > val[best]) best = r;
        }
        // mostly the best roll, sometimes a random still-useful one so it looks natural
        if (rnd.nextInt(100) < 25) {
            int r = 1 + rnd.nextInt(6);
            if (val[r] > 0 && !(r == 6 && sixes >= 2)) return r;
        }
        return best;
    }

    private int restrictedRoll(int p) {
        List<Integer> ok = new ArrayList<>();
        List<Integer> noMove = new ArrayList<>();
        for (int r = 1; r <= 6; r++) {
            boolean bad = false;
            List<Integer> mv = movable(p, r);
            if (mv.isEmpty()) noMove.add(r);
            for (int t : mv) {
                if (moveWins(p, t, r) || moveHitsFavored(p, t, r)) {
                    bad = true;
                    break;
                }
            }
            if (!bad) {
                ok.add(r);
                if (r != 6) ok.add(r); // sixes are half as likely for the other players
            }
        }
        if (!ok.isEmpty()) return ok.get(rnd.nextInt(ok.size()));
        if (!noMove.isEmpty()) return noMove.get(rnd.nextInt(noMove.size()));
        // every roll would either win or capture: allow a capture but never a win
        for (int r = 1; r <= 6; r++) {
            boolean wins = false;
            for (int t : movable(p, r)) if (moveWins(p, t, r)) wins = true;
            if (!wins) return r;
        }
        return 1 + rnd.nextInt(6);
    }

    /** Whether the player can legally be forced to move the given token given the rolled value. */
    boolean moveAllowed(int p, int t, int roll) {
        if (!canMove(p, t, roll)) return false;
        if (favored < 0 || favoredSide(p)) return true;
        // non-favoured players may never win or capture the favoured colour
        return !moveWins(p, t, roll) && !moveHitsFavored(p, t, roll);
    }

    List<Integer> allowedMoves(int p, int roll) {
        List<Integer> r = new ArrayList<>();
        for (int t = 0; t < 4; t++) if (moveAllowed(p, t, roll)) r.add(t);
        return r;
    }

    /** Sum of progress, used for ranking players who did not finish. */
    int progress(int p) {
        int s = 0;
        for (int t = 0; t < 4; t++) s += pos[p][t] + 1;
        return s;
    }
}
