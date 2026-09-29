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

    /** PIN protecting the winner control, stored as a SHA-256 hash. */
    static boolean hasPin() { return sp.getString("pinHash", null) != null; }
    static boolean checkPin(String pin) { return hash(pin).equals(sp.getString("pinHash", null)); }
    static void setPin(String pin) { sp.edit().putString("pinHash", hash(pin)).apply(); }

    private static String hash(String s) {
        try {
            byte[] d = java.security.MessageDigest.getInstance("SHA-256").digest(("ludo:" + s).getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (byte x : d) b.append(String.format("%02x", x));
            return b.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean sound() { return sp.getBoolean("sound", true); }
    static void setSound(boolean on) { sp.edit().putBoolean("sound", on).apply(); }

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
