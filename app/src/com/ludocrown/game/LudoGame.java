package com.ludocrown.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ludo rules. Token progress: -1 = in yard, 0..50 = main track (relative to the colour's start),
 * 51..55 = home column, 56 = reached home.
 *
 * Dice rolls go through {@link #rollFor(int, int)}, which applies the "winner control" setting: rolls stay
 * close to fair and the game goes back and forth, but the chosen colour (and its partner in Team Up) is
 * steered to finish first and no other player is ever given the winning roll.
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

    /*
     * Winner control. Most rolls are close to fair; the dice are only nudged, and the nudge grows as the
     * game goes on:
     *  - a player who gets too far ahead of the chosen colour sees slightly weaker rolls,
     *  - the chosen colour gets slightly better rolls when it falls behind, and more so near the end,
     *  - a roll that would let another player win is never given.
     * The strength, how far others may lead and when captures of the chosen colour become rare are all
     * randomised per game, so no two games play out the same way.
     */
    private final double steer = 0.7 + rnd.nextDouble() * 0.7;
    private final double slack = 0.05 + rnd.nextDouble() * 0.12;
    private final int captureCut = 16 + rnd.nextInt(20);

    /** Fraction (0..1) of the way to finishing, for a colour. */
    double tokenProgress(int p) {
        int s = 0;
        for (int t = 0; t < 4; t++) {
            int v = pos[p][t];
            s += v == YARD ? 0 : v + 1;
        }
        if (mode == QUICK) {
            int best = 0;
            for (int t = 0; t < 4; t++) best = Math.max(best, pos[p][t] + 1);
            return best / 57.0;
        }
        return s / 228.0;
    }

    /** Progress of a side (a colour, or a team in Team Up). */
    double sideProgress(int p) {
        int m = (p + 2) % 4;
        if (mode == TEAM && active[m]) return (tokenProgress(p) + tokenProgress(m)) / 2;
        return tokenProgress(p);
    }

    int homeCount(int p) {
        int n = 0;
        for (int t = 0; t < 4; t++) if (pos[p][t] == HOME) n++;
        return n;
    }

    private double bestOpponentProgress() {
        double best = 0;
        for (int q = 0; q < 4; q++) if (active[q] && !favoredSide(q)) best = Math.max(best, sideProgress(q));
        return best;
    }

    /** Some non-favoured player is one exact roll away from winning. */
    boolean opponentNearWin() {
        for (int q = 0; q < 4; q++) if (active[q] && !favoredSide(q) && nearWin(q)) return true;
        return false;
    }

    private boolean nearWin(int p) {
        for (int r = 1; r <= 6; r++) for (int t : movable(p, r)) if (moveWins(p, t, r)) return true;
        return false;
    }

    /** After moving t by roll, could p win with a single further roll? */
    private boolean wouldBeNearWin(int p, int t, int roll) {
        int old = pos[p][t];
        pos[p][t] = target(p, t, roll);
        boolean near = !won(p) && nearWin(p);
        pos[p][t] = old;
        return near;
    }

    /** The chosen side is itself close to finishing (last stretch). */
    private boolean favoredNearWin() {
        if (nearWin(favored)) return true;
        int m = (favored + 2) % 4;
        return mode == TEAM && active[m] && nearWin(m);
    }

    int rollFor(int p, int sixes) {
        if (favored < 0) return 1 + rnd.nextInt(6);
        // how good each roll would be for this player (best move it allows)
        double[] q = new double[7];
        for (int r = 1; r <= 6; r++) {
            q[r] = -200;
            for (int t : movable(p, r)) q[r] = Math.max(q[r], score(p, t, r));
        }
        double[] rank = new double[7];
        for (int r = 1; r <= 6; r++) {
            double below = 0;
            for (int o = 1; o <= 6; o++) {
                if (q[o] < q[r]) below += 1;
                else if (q[o] == q[r] && o != r) below += 0.5;
            }
            rank[r] = below / 5.0;
        }
        double pf = sideProgress(favored);
        double opp = bestOpponentProgress();
        double late = Math.max(pf, opp);
        double[] w = new double[7];
        if (favoredSide(p)) {
            double behind = opp - pf;
            double want = steer * (Math.max(0, behind - slack * 0.5) * 7 + late * late * 0.7);
            if (opponentNearWin()) want += 1.5;
            want = Math.min(want, 3.5);
            for (int r = 1; r <= 6; r++) w[r] = Math.exp(want * (rank[r] - 0.5) * 2);
            if (sixes >= 2) w[6] *= 0.08;
        } else {
            double ahead = sideProgress(p) - pf;
            double allowed = slack * (1 - late) + 0.05;
            double damp = Math.min(3.5, steer * Math.max(0, ahead - allowed) * 9);
            boolean favNear = favoredNearWin();
            int favHome = homeCount(favored);
            if (mode == TEAM && active[(favored + 2) % 4]) favHome = Math.max(favHome, homeCount((favored + 2) % 4));
            for (int r = 1; r <= 6; r++) {
                w[r] = Math.exp(-damp * (rank[r] - 0.5) * 2);
                for (int t : movable(p, r)) {
                    if (moveWins(p, t, r)) {
                        w[r] = 0;
                        break;
                    }
                    int to = target(p, t, r);
                    if (to == HOME && homeCount(p) + 1 > favHome + 1) w[r] *= 0.1;
                    // avoid parking another player one roll from victory while the chosen colour is far off
                    if (!favNear && wouldBeNearWin(p, t, r)) w[r] *= 0.15;
                    for (int[] cap : captures(p, to)) {
                        if (favoredSide(cap[0]) && (pos[cap[0]][cap[1]] >= captureCut || late > 0.6)) w[r] *= 0.1;
                    }
                }
            }
        }
        double sum = 0;
        for (int r = 1; r <= 6; r++) sum += w[r];
        if (sum <= 0) {
            // every roll would win for this player: give one with no legal move
            for (int r = 1; r <= 6; r++) if (movable(p, r).isEmpty()) return r;
            return 1 + rnd.nextInt(6);
        }
        double x = rnd.nextDouble() * sum;
        for (int r = 1; r <= 6; r++) {
            x -= w[r];
            if (x <= 0 && w[r] > 0) return r;
        }
        for (int r = 6; r >= 1; r--) if (w[r] > 0) return r;
        return 1;
    }

    /** Whether the player can legally be forced to move the given token given the rolled value. */
    boolean moveAllowed(int p, int t, int roll) {
        if (!canMove(p, t, roll)) return false;
        if (favored < 0 || favoredSide(p)) return true;
        // non-favoured players may never make the winning move
        return !moveWins(p, t, roll);
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
