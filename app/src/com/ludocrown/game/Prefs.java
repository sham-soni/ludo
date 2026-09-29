package com.ludocrown.game;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/** Persistent settings: wallet, daily bonus progress and the winner-control colour. */
final class Prefs {
    private Prefs() {}

    /** Value stored for "no fixed winner" (fair dice). */
    static final int FAIR = -1;

    private static SharedPreferences sp;

    static void init(Context ctx) {
        sp = ctx.getSharedPreferences("ludo", Context.MODE_PRIVATE);
    }

    /** Colour that always wins. Green (1) by default. */
    static int winner() { return sp.getInt("winner", 1); }
    static void setWinner(int c) { sp.edit().putInt("winner", c).apply(); }

    static int coins() { return sp.getInt("coins", 5850); }
    static void addCoins(int n) { sp.edit().putInt("coins", Math.max(0, coins() + n)).apply(); }
    static int gems() { return sp.getInt("gems", 150); }
    static void addGems(int n) { sp.edit().putInt("gems", Math.max(0, gems() + n)).apply(); }

    static int tokenStyle() { return sp.getInt("token", 0); }
    static void setTokenStyle(int s) { sp.edit().putInt("token", s).apply(); }

    static int bonusDays() { return sp.getInt("bonusDays", 3); }
    static boolean bonusAvailable() { return sp.getInt("bonusDate", 0) != today(); }
    static void claimBonus() {
        sp.edit().putInt("bonusDate", today()).putInt("bonusDays", bonusDays() + 1).apply();
    }

    static boolean notifyAsked() { return sp.getBoolean("notifyAsked", false); }
    static void setNotifyAsked() { sp.edit().putBoolean("notifyAsked", true).apply(); }

    static String name(int color) { return sp.getString("name" + color, null); }
    static void setName(int color, String n) { sp.edit().putString("name" + color, n).apply(); }

    static int today() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
    }
}
