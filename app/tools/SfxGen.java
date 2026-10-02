import java.io.*;
import java.util.Random;

/** Synthesises the game's sound effects as 16-bit mono WAV files (run at build time). */
public class SfxGen {
    static final int RATE = 22050;

    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        write(new File(dir, "roll.wav"), roll());
        write(new File(dir, "step.wav"), step());
        write(new File(dir, "capture.wav"), capture());
        write(new File(dir, "home.wav"), notes(new double[]{659, 784, 988, 1319}, 0.09, 0.35));
        write(new File(dir, "six.wav"), notes(new double[]{880, 1175}, 0.07, 0.3));
        write(new File(dir, "win.wav"), notes(new double[]{523, 659, 784, 1047, 784, 1047, 1319}, 0.14, 0.45));
        write(new File(dir, "click.wav"), click());
        write(new File(dir, "turn.wav"), notes(new double[]{740}, 0.08, 0.2));
    }

    /**
     * Dice roll: a quick, bright rattle - about ten clicks in 0.3 s with most energy around 2.5-3 kHz
     * (the character measured from the reference gameplay video; the sound itself is synthesised).
     */
    static double[] roll() {
        double[] s = new double[(int) (RATE * 0.34)];
        Random r = new Random(11);
        double t = 0.004;
        int n = 0;
        while (t < 0.30) {
            double amp = 0.75 * (1 - 0.45 * t / 0.3) * (0.75 + 0.25 * r.nextDouble());
            addTick(s, t, amp, 2350 + r.nextInt(900), 3300 + r.nextInt(700), r);
            t += 0.024 + r.nextDouble() * 0.012;
            n++;
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
            double env = Math.exp(-t * 320);
            noise = 0.6 * noise + 0.4 * (r.nextDouble() * 2 - 1); // softened noise, keeps the tick from hissing
            s[start + i] += amp * env * (0.6 * Math.sin(2 * Math.PI * f1 * t) + 0.22 * Math.sin(2 * Math.PI * f2 * t)
                    + 0.18 * noise);
        }
    }

    static void addClick(double[] s, double at, double amp, double freq, Random r) {
        addTick(s, at, amp, freq, freq * 1.4, r);
    }

    /**
     * Token step: a short pitched "pop" around 1.47 kHz lasting under 0.1 s. The game plays it at two
     * alternating rates so successive squares go "pip-pop".
     */
    static double[] step() {
        double[] s = new double[(int) (RATE * 0.095)];
        double ph = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double f = 1470 * (1 + 0.18 * Math.exp(-t * 90));
            ph += 2 * Math.PI * f / RATE;
            double env = Math.min(1, t * 900) * Math.exp(-t * 26);
            s[i] = 0.75 * env * (Math.sin(ph) + 0.18 * Math.sin(2 * ph));
        }
        return s;
    }

    /** Falling "whoop" plus a thump when a token is captured. */
    static double[] capture() {
        double[] s = new double[(int) (RATE * 0.55)];
        double ph = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double f = 1100 * Math.exp(-t * 4.5) + 120;
            ph += 2 * Math.PI * f / RATE;
            double env = Math.min(1, t * 60) * Math.exp(-t * 3.5);
            s[i] = 0.5 * env * (Math.sin(ph) + 0.3 * Math.sin(2 * ph));
            if (t > 0.38) s[i] += 0.6 * Math.exp(-(t - 0.38) * 40) * Math.sin(2 * Math.PI * 90 * (t - 0.38));
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
