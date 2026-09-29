import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Renders the launcher icon PNGs (run at build time, headless). */
public class IconGen {
    public static void main(String[] a) throws Exception {
        String res = a[0];
        int[][] sizes = {{48, 0}, {72, 1}, {96, 2}, {144, 3}, {192, 4}};
        String[] dirs = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
        for (int[] s : sizes) {
            File d = new File(res, "mipmap-" + dirs[s[1]]);
            d.mkdirs();
            ImageIO.write(draw(s[0]), "png", new File(d, "ic_launcher.png"));
        }
    }

    static BufferedImage draw(int n) {
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(n / 192.0, n / 192.0);
        g.setPaint(new GradientPaint(0, 0, new Color(0x3F8BF0), 0, 192, new Color(0x12408F)));
        g.fill(new RoundRectangle2D.Double(4, 4, 184, 184, 44, 44));
        g.setColor(new Color(0xFFC21A));
        g.setStroke(new BasicStroke(6));
        g.draw(new RoundRectangle2D.Double(7, 7, 178, 178, 40, 40));
        // mini board
        int b = 30, s = 132, q = s * 2 / 5;
        g.setColor(Color.WHITE);
        g.fillRect(b, b, s, s);
        Color[] c = {new Color(0xE8202A), new Color(0x0CA24B), new Color(0x1C75BC), new Color(0xFEDB1F)};
        int[][] pos = {{b, b}, {b + s - q, b}, {b, b + s - q}, {b + s - q, b + s - q}};
        for (int i = 0; i < 4; i++) {
            g.setColor(c[i]);
            g.fillRect(pos[i][0], pos[i][1], q, q);
            g.setColor(Color.WHITE);
            g.fillRect(pos[i][0] + 9, pos[i][1] + 9, q - 18, q - 18);
            g.setColor(c[i]);
            g.fillOval(pos[i][0] + 15, pos[i][1] + 15, q - 30, q - 30);
        }
        // die
        g.rotate(Math.toRadians(-12), 96, 96);
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new RoundRectangle2D.Double(66, 70, 64, 64, 18, 18));
        g.setPaint(new GradientPaint(62, 62, Color.WHITE, 126, 126, new Color(0xC9CED6)));
        g.fill(new RoundRectangle2D.Double(62, 62, 64, 64, 18, 18));
        g.setColor(new Color(0x111111));
        int[][] pips = {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}};
        for (int[] p : pips) g.fillOval(62 + 12 + p[0] * 17, 62 + 12 + p[1] * 17, 12, 12);
        g.dispose();
        return img;
    }
}
