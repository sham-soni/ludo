import java.io.*;
import java.util.Random;

/** Synthesises the game's sound effects as 16-bit mono WAV files (run at build time). */
public class SfxGen {
    static final int RATE = 22050;

    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        write(new File(dir, "roll.wav"), roll(11));
        write(new File(dir, "roll2.wav"), roll(23));
        write(new File(dir, "roll3.wav"), roll(37));
        write(new File(dir, "pick.wav"), pick());
        write(new File(dir, "step.wav"), step());
        write(new File(dir, "capture.wav"), capture());
        write(new File(dir, "home.wav"), notes(new double[]{659, 784, 988, 1319}, 0.09, 0.35));
        write(new File(dir, "six.wav"), notes(new double[]{880, 1175}, 0.07, 0.3));
        write(new File(dir, "win.wav"), notes(new double[]{523, 659, 784, 1047, 784, 1047, 1319}, 0.14, 0.45));
        write(new File(dir, "click.wav"), click());
        write(new File(dir, "turn.wav"), notes(new double[]{740}, 0.08, 0.2));
    }

    /**
     * Dice roll, modelled on the reference recording's profile: two quick bursts of ~5 clicks (~20 ms
     * apart) separated by a ~50 ms gap, 0.28 s in all. Clicks ring at 2.1-3.2 kHz; the first is lower
     * (~1.4-2 kHz). Each seed gives a slightly different shake.
     */
    static double[] roll(int seed) {
        double[] s = new double[(int) (RATE * 0.32)];
        Random r = new Random(seed);
        double t = 0.006 + r.nextDouble() * 0.01;
        for (int burst = 0; burst < 2; burst++) {
            int clicks = 5 + r.nextInt(2);
            for (int k = 0; k < clicks; k++) {
                boolean first = burst == 0 && k == 0;
                double f1 = first ? 1400 + r.nextInt(600) : 1500 + r.nextInt(1700);
                double f2 = 2000 + r.nextInt(1000);
                double amp = (0.55 + 0.45 * r.nextDouble()) * (burst == 1 && k > 0 && k < 3 ? 1.0 : 0.75);
                addTick(s, t, amp, f1, f2, r);
                t += 0.016 + r.nextDouble() * 0.008 + (burst == 1 ? 0.006 : 0);
            }
            t += 0.03 + r.nextDouble() * 0.02;
        }
        return s;
    }

    /** A hard plastic tick: two damped resonances plus a little noise. */
    static void addTick(double[] s, double at, double amp, double f1, double f2, Random r) {
        int start = (int) (at * RATE);
        int len = (int) (RATE * 0.02);
        double noise = 0;
        for (int i = 0; i < len && start + i < s.length; i++) {
            double t = i / (double) RATE;
            double env = Math.exp(-t * 230);
            noise = 0.6 * noise + 0.4 * (r.nextDouble() * 2 - 1); // softened noise, keeps the tick from hissing
            s[start + i] += amp * env * (0.6 * Math.sin(2 * Math.PI * f1 * t) + 0.22 * Math.sin(2 * Math.PI * f2 * t)
                    + 0.18 * noise);
        }
    }

    static void addClick(double[] s, double at, double amp, double freq, Random r) {
        addTick(s, at, amp, freq, freq * 1.4, r);
    }

    /**
     * Token step, modelled on the reference profile: a short high tap gliding ~2.25 -> 1.5 kHz (15 ms),
     * then a "bloop" that sweeps up from ~520 Hz to ~1.65 kHz over 30 ms and falls back to ~400 Hz,
     * loudest around 50-60 ms and gone by ~95 ms.
     */
    static double[] step() {
        double[] s = new double[(int) (RATE * 0.11)];
        // pitch knots of the bloop (seconds after it starts, Hz), interpolated in log-frequency
        double[][] knots = {{0, 520}, {0.012, 670}, {0.022, 1190}, {0.032, 1660}, {0.042, 1190}, {0.052, 740}, {0.075, 420}};
        double[][] amps = {{0, 0.57}, {0.012, 0.64}, {0.022, 0.77}, {0.032, 0.9}, {0.042, 0.9}, {0.052, 0.34}, {0.062, 0.11}, {0.08, 0}};
        double ph = 0, ph2 = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            if (t < 0.03) { // tap: ~2.5 kHz gliding to 1.5 kHz, as loud as the bloop's peak
                double f = 1480 + 1250 * Math.exp(-t * 110);
                ph2 += 2 * Math.PI * f / RATE;
                double e = Math.min(1, t * 3000) * (t < 0.018 ? 1 : Math.exp(-(t - 0.018) * 260));
                s[i] += 0.95 * e * Math.sin(ph2);
            }
            double u = t - 0.026;
            if (u >= 0) {
                double f = Math.exp(interp(knots, u, true));
                ph += 2 * Math.PI * f / RATE;
                double env = interp(amps, u, false) * Math.min(1, u * 1500);
                double low = Math.max(0, 1 - f / 1200);
                s[i] += env * (Math.sin(ph) + 0.4 * low * Math.sin(3 * ph));
            }
        }
        return s;
    }

    static double interp(double[][] k, double x, boolean logY) {
        if (x <= k[0][0]) return logY ? Math.log(k[0][1]) : k[0][1];
        for (int i = 1; i < k.length; i++) {
            if (x <= k[i][0]) {
                double a = (x - k[i - 1][0]) / (k[i][0] - k[i - 1][0]);
                double y0 = logY ? Math.log(k[i - 1][1]) : k[i - 1][1], y1 = logY ? Math.log(k[i][1]) : k[i][1];
                return y0 + (y1 - y0) * a;
            }
        }
        double last = k[k.length - 1][1];
        return logY ? Math.log(last) : last;
    }

    /** "Choose a token": seven buzzy beeps (50 ms on, 30 ms off) on ~877 Hz with strong 2nd/3rd harmonics. */
    static double[] pick() {
        double[] s = new double[(int) (RATE * 0.6)];
        for (int b = 0; b < 7; b++) {
            int start = (int) (b * 0.086 * RATE);
            int len = (int) (0.055 * RATE);
            for (int i = 0; i < len && start + i < s.length; i++) {
                double t = i / (double) RATE;
                double env = Math.min(1, t * 400) * Math.min(1, (0.055 - t) * 400);
                double w = 2 * Math.PI * 877 * t;
                s[start + i] = 0.5 * env * (0.45 * Math.sin(w) + 0.9 * Math.sin(2 * w) + 0.35 * Math.sin(3 * w));
            }
        }
        return s;
    }

    /**
     * Capture, modelled on the reference profile: ~1 s of rapid bright ticks (about 26 per second, one per
     * square as the token runs home), noisy with energy at 2.7-3.5 kHz, swelling over 0.15 s and fading
     * at the end.
     */
    static double[] capture() {
        double[] s = new double[(int) (RATE * 1.15)];
        Random r = new Random(5);
        double t = 0;
        while (t < 1.08) {
            double env = Math.min(1, t / 0.15) * (t > 0.95 ? Math.max(0, (1.08 - t) / 0.13) : 1);
            int start = (int) (t * RATE);
            int len = (int) (0.045 * RATE);
            double f1 = 2650 + r.nextInt(600), f2 = 3000 + r.nextInt(600);
            double noise = 0, amp = 0.35 + 0.25 * r.nextDouble();
            for (int i = 0; i < len && start + i < s.length; i++) {
                double u = i / (double) RATE;
                noise = 0.45 * noise + 0.55 * (r.nextDouble() * 2 - 1);
                double e = Math.exp(-u * 70);
                s[start + i] += amp * env * e * (0.42 * noise + 0.4 * Math.sin(2 * Math.PI * f1 * u) + 0.32 * Math.sin(2 * Math.PI * f2 * u));
            }
            t += 0.016 + r.nextDouble() * 0.03; // dense, irregular rattle
        }
        return s;
    }

    /** A little arpeggio of bell-like notes. */
    static double[] notes(double[] freqs, double gap, double tail) {
        double total = gap * (freqs.length - 1) + tail;
        double[] s = new double[(int) (RATE * total)];
        for (int n = 0; n < freqs.length; n++) {
            int start = (int) (n * gap * RATE);
            for (int i = 0; start + i < s.length; i++) {
                double t = i / (double) RATE;
                double env = Math.min(1, t * 200) * Math.exp(-t * 7);
                s[start + i] += 0.3 * env * (Math.sin(2 * Math.PI * freqs[n] * t) + 0.35 * Math.sin(4 * Math.PI * freqs[n] * t)
                        + 0.12 * Math.sin(6 * Math.PI * freqs[n] * t));
            }
        }
        return s;
    }

    /** Soft pop for menu buttons. */
    static double[] click() {
        double[] s = new double[(int) (RATE * 0.06)];
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double f = 600 + 1400 * Math.exp(-t * 80);
            s[i] = 0.55 * Math.exp(-t * 55) * Math.sin(2 * Math.PI * f * t);
        }
        return s;
    }

    static void write(File f, double[] s) throws IOException {
        double peak = 0;
        for (double v : s) peak = Math.max(peak, Math.abs(v));
        double gain = peak > 0.95 ? 0.95 / peak : 1;
        // short fade-out avoids a click at the end
        int fade = Math.min(s.length, RATE / 100);
        for (int i = 0; i < fade; i++) s[s.length - 1 - i] *= i / (double) fade;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (double v : s) {
            int x = (int) Math.round(Math.max(-1, Math.min(1, v * gain)) * 32767);
            data.write(x & 0xFF);
            data.write((x >> 8) & 0xFF);
        }
        byte[] pcm = data.toByteArray();
        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(f))) {
            o.writeBytes("RIFF");
            o.writeInt(Integer.reverseBytes(36 + pcm.length));
            o.writeBytes("WAVEfmt ");
            o.writeInt(Integer.reverseBytes(16));
            o.writeShort(Short.reverseBytes((short) 1));
            o.writeShort(Short.reverseBytes((short) 1));
            o.writeInt(Integer.reverseBytes(RATE));
            o.writeInt(Integer.reverseBytes(RATE * 2));
            o.writeShort(Short.reverseBytes((short) 2));
            o.writeShort(Short.reverseBytes((short) 16));
            o.writeBytes("data");
            o.writeInt(Integer.reverseBytes(pcm.length));
            o.write(pcm);
        }
    }
}
