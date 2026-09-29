package com.ludocrown.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/** All drawing helpers. Everything in the game is drawn with Canvas, no image assets. */
final class Art {
    private Art() {}

    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    /** Chunky rounded display font for titles and buttons (Lilita One). */
    static Typeface BLACK = Typeface.create("sans-serif-black", Typeface.NORMAL);
    /** Comic-style font for the big mode buttons (Luckiest Guy). */
    static Typeface LUCKY = BLACK;
    /** Heavy condensed text (Roboto Condensed Black). */
    static Typeface BOLD = Typeface.create("sans-serif", Typeface.BOLD);
    /** Condensed body text (Roboto Condensed Bold). */
    static Typeface COND = Typeface.create("sans-serif-condensed", Typeface.BOLD);

    static void init(android.content.Context ctx) {
        BLACK = load(ctx, "lilita.ttf", BLACK);
        LUCKY = load(ctx, "luckiest.ttf", BLACK);
        BOLD = load(ctx, "robotocond_black.ttf", BOLD);
        COND = load(ctx, "robotocond_bold.ttf", COND);
    }

    private static Typeface load(android.content.Context ctx, String name, Typeface fallback) {
        try {
            return Typeface.createFromAsset(ctx.getAssets(), "fonts/" + name);
        } catch (RuntimeException e) {
            return fallback;
        }
    }
    static final Typeface SERIF = Typeface.create(Typeface.SERIF, Typeface.BOLD);
    static final Typeface MONO = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);

    // Player colour indices: 0 red (top-left), 1 green (top-right), 2 yellow (bottom-right), 3 blue (bottom-left)
    static final int[] COLOR = {0xFFE8202A, 0xFF0CA24B, 0xFFFEDB1F, 0xFF1E8FE8};
    static final int[] BOARD = {0xFFE8202A, 0xFF0CA24B, 0xFFFEDB1F, 0xFF1C75BC};
    static final int[] LIGHT = {0xFFFF7A7F, 0xFF5BE38C, 0xFFFFF27A, 0xFF7FD0FF};
    static final int[] DARK = {0xFF8E0B11, 0xFF03592A, 0xFFB08A00, 0xFF0A4A92};
    static final String[] NAME = {"Red", "Green", "Yellow", "Blue"};

    static final int GOLD = 0xFFFFC21A;
    static final int GOLD_DARK = 0xFFB9790A;
    static final int NAVY = 0xFF0B1E4D;
    static final int TEXT_YELLOW = 0xFFFFD31A;

    static final RectF R = new RectF();
    static final Path PATH = new Path();

    static void reset() {
        P.reset();
        P.setAntiAlias(true);
        P.setFilterBitmap(true);
    }

    // ---------------------------------------------------------------- text

    static void text(Canvas c, String s, float x, float y, float size, int fill, int stroke, float sw,
                     Typeface tf, Paint.Align align) {
        reset();
        P.setTypeface(tf);
        P.setTextSize(size);
        P.setTextAlign(align);
        if (sw > 0) {
            P.setStyle(Paint.Style.STROKE);
            P.setStrokeWidth(sw);
            P.setStrokeJoin(Paint.Join.ROUND);
            P.setColor(stroke);
            c.drawText(s, x, y, P);
        }
        P.setStyle(Paint.Style.FILL);
        P.setColor(fill);
        c.drawText(s, x, y, P);
    }

    static void textC(Canvas c, String s, float x, float y, float size, int fill, int stroke, float sw, Typeface tf) {
        text(c, s, x, y, size, fill, stroke, sw, tf, Paint.Align.CENTER);
    }

    /** Centered text shrunk to fit a max width. */
    static void textFit(Canvas c, String s, float x, float y, float size, float maxW, int fill, int stroke,
                        float sw, Typeface tf) {
        reset();
        P.setTypeface(tf);
        P.setTextSize(size);
        float w = P.measureText(s);
        if (w > maxW) size = size * maxW / w;
        textC(c, s, x, y, size, fill, stroke, sw, tf);
    }

    /** Gradient-filled game title text (yellow to orange) with dark outline. */
    static void titleText(Canvas c, String s, float x, float y, float size, Typeface tf) {
        reset();
        P.setTypeface(tf);
        P.setTextSize(size);
        P.setTextAlign(Paint.Align.CENTER);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(size * 0.16f);
        P.setStrokeJoin(Paint.Join.ROUND);
        P.setColor(0xFF3A1C00);
        c.drawText(s, x, y, P);
        P.setStyle(Paint.Style.FILL);
        P.setShader(new LinearGradient(0, y - size * 0.8f, 0, y, 0xFFFFF27A, 0xFFFFA800, Shader.TileMode.CLAMP));
        c.drawText(s, x, y, P);
        P.setShader(null);
    }

    // ---------------------------------------------------------------- shapes

    static void rrect(Canvas c, float l, float t, float r, float b, float rad, int color) {
        reset();
        P.setColor(color);
        R.set(l, t, r, b);
        c.drawRoundRect(R, rad, rad, P);
    }

    static void rrectGrad(Canvas c, float l, float t, float r, float b, float rad, int top, int bottom) {
        reset();
        P.setShader(new LinearGradient(0, t, 0, b, top, bottom, Shader.TileMode.CLAMP));
        R.set(l, t, r, b);
        c.drawRoundRect(R, rad, rad, P);
        P.setShader(null);
    }

    static void rrectStroke(Canvas c, float l, float t, float r, float b, float rad, int color, float w) {
        reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(w);
        P.setColor(color);
        R.set(l, t, r, b);
        c.drawRoundRect(R, rad, rad, P);
    }

    static void circle(Canvas c, float x, float y, float r, int color) {
        reset();
        P.setColor(color);
        c.drawCircle(x, y, r, P);
    }

    static void ring(Canvas c, float x, float y, float r, int color, float w) {
        reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(w);
        P.setColor(color);
        c.drawCircle(x, y, r, P);
    }

    /** Soft glow made of stacked translucent rounded rectangles. */
    static void glow(Canvas c, float l, float t, float r, float b, float rad, int color, float size) {
        int a = (color >>> 24);
        int rgb = color & 0xFFFFFF;
        for (int i = 6; i >= 1; i--) {
            float g = size * i / 6f;
            int alpha = (int) (a * 0.16f * (7 - i) / 6f);
            rrect(c, l - g, t - g, r + g, b + g, rad + g, (alpha << 24) | rgb);
        }
    }

    static void star(Canvas c, float cx, float cy, float r, int fill, int stroke, float sw) {
        PATH.reset();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float rr = (i % 2 == 0) ? r : r * 0.45f;
            float x = cx + (float) Math.cos(a) * rr, y = cy + (float) Math.sin(a) * rr;
            if (i == 0) PATH.moveTo(x, y); else PATH.lineTo(x, y);
        }
        PATH.close();
        reset();
        if (fill != 0) {
            P.setColor(fill);
            c.drawPath(PATH, P);
        }
        if (sw > 0) {
            P.setStyle(Paint.Style.STROKE);
            P.setStrokeJoin(Paint.Join.ROUND);
            P.setStrokeWidth(sw);
            P.setColor(stroke);
            c.drawPath(PATH, P);
        }
    }

    static void arrow(Canvas c, float cx, float cy, float len, float angleDeg, int color, float w) {
        c.save();
        c.rotate(angleDeg, cx, cy);
        reset();
        P.setColor(color);
        P.setStrokeWidth(w);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeJoin(Paint.Join.ROUND);
        c.drawLine(cx - len / 2, cy, cx + len / 2, cy, P);
        c.drawLine(cx + len / 2, cy, cx + len / 2 - len * 0.3f, cy - len * 0.3f, P);
        c.drawLine(cx + len / 2, cy, cx + len / 2 - len * 0.3f, cy + len * 0.3f, P);
        c.restore();
    }

    // ---------------------------------------------------------------- UI widgets

    /** Big golden-framed mode button like ONLINE / COMPUTER on the home screen. */
    static void modeButton(Canvas c, RectF r, String label, int icon, boolean pressed) {
        c.save();
        if (pressed) c.scale(0.95f, 0.95f, r.centerX(), r.centerY());
        float rad = 26;
        rrect(c, r.left, r.top + 8, r.right, r.bottom + 8, rad, 0xAA000000);
        rrectGrad(c, r.left, r.top, r.right, r.bottom, rad, 0xFFFFE27A, 0xFFE89A00);
        rrectGrad(c, r.left + 7, r.top + 7, r.right - 7, r.bottom - 7, rad - 6, 0xFF3A7FE0, 0xFF1C4FB0);
        float bandTop = r.top + (r.height() * 0.62f);
        c.save();
        R.set(r.left + 7, r.top + 7, r.right - 7, r.bottom - 7);
        PATH.reset();
        PATH.addRoundRect(R, rad - 6, rad - 6, Path.Direction.CW);
        c.clipPath(PATH);
        rrectGrad(c, r.left, bandTop, r.right, r.bottom, 0, 0xFFFFD84A, 0xFFF0A000);
        rrect(c, r.left, bandTop, r.right, bandTop + 4, 0, 0xFFFFF3B0);
        c.restore();
        c.restore();
        c.save();
        if (pressed) c.scale(0.95f, 0.95f, r.centerX(), r.centerY());
        // icons are drawn large and pop out over the top edge, like the original buttons
        Icons.draw(c, icon, r.centerX(), r.top + (bandTop - r.top) * 0.38f, r.height() * 0.56f);
        textFit(c, label, r.centerX(), bandTop + (r.bottom - bandTop) * 0.72f, r.height() * 0.2f, r.width() - 40,
                0xFFFFFFFF, 0xFF4A2A00, 9, LUCKY);
        c.restore();
    }

    /** Blue pill button with glowing golden border (Next, Play, Claim). */
    static void pillButton(Canvas c, RectF r, String label, String sub, boolean pressed) {
        pillButton(c, r, label, sub, pressed, 0xFF3F8BF0, 0xFF12408F);
    }

    static void pillButton(Canvas c, RectF r, String label, String sub, boolean pressed, int top, int bottom) {
        c.save();
        if (pressed) c.scale(0.94f, 0.94f, r.centerX(), r.centerY());
        float rad = r.height() / 2.4f;
        glow(c, r.left, r.top, r.right, r.bottom, rad, 0xFFFFB000, 16);
        rrectGrad(c, r.left, r.top, r.right, r.bottom, rad, 0xFFFFE680, 0xFFE59A00);
        rrectGrad(c, r.left + 8, r.top + 8, r.right - 8, r.bottom - 8, rad - 8, top, bottom);
        rrect(c, r.left + 20, r.top + 12, r.right - 20, r.top + r.height() * 0.42f, rad - 14, 0x30FFFFFF);
        if (sub == null) {
            textFit(c, label, r.centerX(), r.centerY() + r.height() * 0.17f, r.height() * 0.46f, r.width() - 50,
                    0xFFFFFFFF, 0xFF0A1F4A, 8, BLACK);
        } else {
            textFit(c, label, r.centerX(), r.centerY() - 2, r.height() * 0.34f, r.width() - 50,
                    0xFFFFFFFF, 0xFF0A1F4A, 7, BLACK);
            textFit(c, sub, r.centerX(), r.centerY() + r.height() * 0.27f, r.height() * 0.19f, r.width() - 50,
                    0xFFFFFFFF, 0xFF0A1F4A, 5, BLACK);
        }
        c.restore();
    }

    static void greenButton(Canvas c, RectF r, String label, boolean pressed) {
        c.save();
        if (pressed) c.scale(0.94f, 0.94f, r.centerX(), r.centerY());
        rrect(c, r.left, r.top + 6, r.right, r.bottom + 6, 14, 0x66000000);
        rrectGrad(c, r.left, r.top, r.right, r.bottom, 14, 0xFF7EE33A, 0xFF2F9A12);
        rrect(c, r.left + 10, r.top + 6, r.right - 10, r.top + r.height() * 0.4f, 10, 0x33FFFFFF);
        textFit(c, label, r.centerX(), r.centerY() + r.height() * 0.18f, r.height() * 0.5f, r.width() - 30,
                0xFFFFFFFF, 0xFF1E5A08, 7, BOLD);
        c.restore();
    }

    static void closeButton(Canvas c, float cx, float cy, float rad, boolean pressed) {
        if (pressed) rad *= 0.9f;
        circle(c, cx, cy + 4, rad, 0x66000000);
        circle(c, cx, cy, rad, 0xFFFFFFFF);
        reset();
        P.setShader(new RadialGradient(cx - rad * 0.3f, cy - rad * 0.3f, rad, 0xFFFF6B6B, 0xFFC80E14,
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, rad * 0.84f, P);
        P.setShader(null);
        reset();
        P.setColor(0xFFFFFFFF);
        P.setStrokeWidth(rad * 0.26f);
        P.setStrokeCap(Paint.Cap.ROUND);
        float d = rad * 0.36f;
        c.drawLine(cx - d, cy - d, cx + d, cy + d, P);
        c.drawLine(cx + d, cy - d, cx - d, cy + d, P);
    }

    /** Gold circular back button (bottom-left). */
    static void backButton(Canvas c, float cx, float cy, boolean pressed) {
        float rad = pressed ? 40 : 44;
        glow(c, cx - rad, cy - rad, cx + rad, cy + rad, rad, 0xFFFFB000, 14);
        circle(c, cx, cy, rad, 0xFFFFC21A);
        circle(c, cx, cy, rad - 7, 0xFF1C4FB0);
        reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(8);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setColor(0xFFFFD84A);
        R.set(cx - 18, cy - 16, cx + 18, cy + 18);
        c.drawArc(R, -90, 270, false, P);
        PATH.reset();
        PATH.moveTo(cx - 20, cy - 16);
        PATH.lineTo(cx - 2, cy - 28);
        PATH.lineTo(cx - 2, cy - 4);
        PATH.close();
        P.setStyle(Paint.Style.FILL);
        c.drawPath(PATH, P);
    }

    /** Dark blue dialog panel with golden glowing frame. */
    static void panel(Canvas c, float l, float t, float r, float b) {
        glow(c, l, t, r, b, 8, 0xFFFFA000, 22);
        rrect(c, l - 4, t - 4, r + 4, b + 4, 8, 0xFFFFC21A);
        reset();
        P.setShader(new RadialGradient((l + r) / 2, (t + b) / 2, Math.max(r - l, b - t) * 0.7f,
                0xFF1B4FC0, 0xFF06143F, Shader.TileMode.CLAMP));
        c.drawRect(l, t, r, b, P);
        P.setShader(null);
    }

    /** Wide horizontal band used by the setup screens. */
    static void band(Canvas c, float t, float b, float w) {
        rrect(c, 0, t - 5, w, t, 0, 0xFFFFC21A);
        rrect(c, 0, b, w, b + 5, 0, 0xFFFFC21A);
        reset();
        P.setShader(new LinearGradient(0, t, 0, b, new int[]{0xFF0A2A8A, 0xFF1446C8, 0xFF0A2A8A}, null,
                Shader.TileMode.CLAMP));
        c.drawRect(0, t, w, b, P);
        P.setShader(null);
    }

    /** Red ribbon banner with a title (DAILY BONUS). */
    static void ribbon(Canvas c, float cx, float cy, float w, float h, String label) {
        float l = cx - w / 2, r = cx + w / 2;
        reset();
        P.setColor(0xFF8A0A0A);
        PATH.reset();
        PATH.moveTo(l - 70, cy - h * 0.15f);
        PATH.lineTo(l + 30, cy - h * 0.15f);
        PATH.lineTo(l + 30, cy + h * 0.65f);
        PATH.lineTo(l - 70, cy + h * 0.65f);
        PATH.lineTo(l - 35, cy + h * 0.25f);
        PATH.close();
        c.drawPath(PATH, P);
        PATH.reset();
        PATH.moveTo(r + 70, cy - h * 0.15f);
        PATH.lineTo(r - 30, cy - h * 0.15f);
        PATH.lineTo(r - 30, cy + h * 0.65f);
        PATH.lineTo(r + 70, cy + h * 0.65f);
        PATH.lineTo(r + 35, cy + h * 0.25f);
        PATH.close();
        c.drawPath(PATH, P);
        reset();
        P.setShader(new LinearGradient(0, cy - h / 2, 0, cy + h / 2, 0xFFFF3B2F, 0xFFB5100E, Shader.TileMode.CLAMP));
        c.drawRect(l, cy - h / 2, r, cy + h / 2, P);
        P.setShader(null);
        rrect(c, l, cy - h / 2, r, cy - h / 2 + 5, 0, 0xFFFFD84A);
        rrect(c, l, cy + h / 2 - 5, r, cy + h / 2, 0, 0xFFFFD84A);
        titleText(c, label, cx, cy + h * 0.2f, h * 0.62f, BLACK);
    }

    // ---------------------------------------------------------------- game pieces

    static final int[][] PIPS = {
            {},
            {4},
            {0, 8},
            {0, 4, 8},
            {0, 2, 6, 8},
            {0, 2, 4, 6, 8},
            {0, 2, 3, 5, 6, 8},
    };

    /** A flat white dice face. value 0 draws an empty face. */
    static void dice(Canvas c, float cx, float cy, float size, int value, float rot, int face) {
        c.save();
        c.rotate(rot, cx, cy);
        float h = size / 2;
        rrect(c, cx - h, cy - h + size * 0.05f, cx + h, cy + h + size * 0.05f, size * 0.2f, 0x55000000);
        reset();
        P.setShader(new LinearGradient(cx - h, cy - h, cx + h, cy + h, 0xFFFFFFFF, face, Shader.TileMode.CLAMP));
        R.set(cx - h, cy - h, cx + h, cy + h);
        c.drawRoundRect(R, size * 0.2f, size * 0.2f, P);
        P.setShader(null);
        rrectStroke(c, cx - h, cy - h, cx + h, cy + h, size * 0.2f, 0xFF8A8A8A, size * 0.03f);
        if (value >= 1 && value <= 6) {
            for (int idx : PIPS[value]) {
                float px = cx + ((idx % 3) - 1) * size * 0.27f;
                float py = cy + ((idx / 3) - 1) * size * 0.27f;
                circle(c, px, py, size * (value == 1 ? 0.12f : 0.09f), 0xFF111111);
            }
        }
        c.restore();
    }

    /**
     * Map-pin token like the one used on the board. (cx, cy) is the centre of the cell the pin stands on.
     */
    static void pin(Canvas c, float cx, float cy, float size, int color, boolean ghost) {
        float headR = size * 0.30f;
        float headY = cy - size * 0.40f;
        float tipY = cy + size * 0.12f;
        // base ring on the cell
        reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(size * 0.07f);
        P.setColor(0xFF7A1418);
        R.set(cx - size * 0.28f, tipY - size * 0.15f, cx + size * 0.28f, tipY + size * 0.13f);
        c.drawOval(R, P);
        P.setColor(COLOR[color]);
        P.setStrokeWidth(size * 0.05f);
        R.set(cx - size * 0.2f, tipY - size * 0.1f, cx + size * 0.2f, tipY + size * 0.08f);
        c.drawOval(R, P);
        // white pin body
        PATH.reset();
        PATH.addCircle(cx, headY, headR * 1.38f, Path.Direction.CW);
        PATH.moveTo(cx - headR * 1.15f, headY + headR * 0.75f);
        PATH.lineTo(cx, tipY);
        PATH.lineTo(cx + headR * 1.15f, headY + headR * 0.75f);
        PATH.close();
        reset();
        P.setShader(new LinearGradient(cx - headR, 0, cx + headR * 1.4f, 0, 0xFFFFFFFF, 0xFF9EA7B3,
                Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(size * 0.025f);
        P.setColor(0xFF55606E);
        c.drawPath(PATH, P);
        // coloured head
        reset();
        P.setShader(new RadialGradient(cx - headR * 0.35f, headY - headR * 0.4f, headR * 1.3f,
                LIGHT[color], DARK[color], Shader.TileMode.CLAMP));
        c.drawCircle(cx, headY, headR, P);
        P.setShader(null);
        circle(c, cx - headR * 0.35f, headY - headR * 0.4f, headR * 0.28f, 0x99FFFFFF);
        if (ghost) circle(c, cx, headY, headR * 1.4f, 0x66000000);
    }

    /** Flat round checker token (alternate token style). */
    static void disc(Canvas c, float cx, float cy, float size, int color) {
        float r = size * 0.36f;
        circle(c, cx, cy + size * 0.06f, r, 0x66000000);
        circle(c, cx, cy, r, 0xFFFFFFFF);
        reset();
        P.setShader(new RadialGradient(cx - r * 0.3f, cy - r * 0.3f, r * 1.2f, LIGHT[color], DARK[color],
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r * 0.82f, P);
        P.setShader(null);
        star(c, cx, cy, r * 0.5f, 0xDDFFFFFF, 0, 0);
    }

    static void token(Canvas c, int style, float cx, float cy, float size, int color) {
        if (style == 1) disc(c, cx, cy, size, color);
        else pin(c, cx, cy, size, color, false);
    }

    /** 3D pawn used by the logo. baseY is the bottom. */
    static void pawn(Canvas c, float cx, float baseY, float h, int color) {
        float w = h * 0.62f;
        reset();
        P.setColor(0x55000000);
        R.set(cx - w * 0.55f, baseY - h * 0.06f, cx + w * 0.55f, baseY + h * 0.05f);
        c.drawOval(R, P);
        PATH.reset();
        PATH.moveTo(cx - w * 0.5f, baseY - h * 0.02f);
        PATH.quadTo(cx - w * 0.22f, baseY - h * 0.35f, cx - w * 0.14f, baseY - h * 0.66f);
        PATH.lineTo(cx + w * 0.14f, baseY - h * 0.66f);
        PATH.quadTo(cx + w * 0.22f, baseY - h * 0.35f, cx + w * 0.5f, baseY - h * 0.02f);
        PATH.quadTo(cx, baseY + h * 0.07f, cx - w * 0.5f, baseY - h * 0.02f);
        PATH.close();
        reset();
        P.setShader(new LinearGradient(cx - w * 0.5f, 0, cx + w * 0.5f, 0,
                new int[]{DARK[color], LIGHT[color], COLOR[color], DARK[color]}, new float[]{0, 0.3f, 0.6f, 1},
                Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        reset();
        P.setShader(new LinearGradient(cx - w * 0.3f, 0, cx + w * 0.3f, 0, DARK[color], COLOR[color],
                Shader.TileMode.CLAMP));
        R.set(cx - w * 0.28f, baseY - h * 0.72f, cx + w * 0.28f, baseY - h * 0.62f);
        c.drawOval(R, P);
        P.setShader(null);
        float hr = h * 0.2f;
        float hy = baseY - h * 0.78f;
        reset();
        P.setShader(new RadialGradient(cx - hr * 0.4f, hy - hr * 0.4f, hr * 1.4f, 0xFFFFFFFF, COLOR[color],
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, hy, hr, P);
        P.setShader(null);
        P.setShader(new RadialGradient(cx - hr * 0.2f, hy - hr * 0.2f, hr * 1.2f, 0x00000000, DARK[color],
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, hy, hr, P);
        P.setShader(null);
        circle(c, cx - hr * 0.35f, hy - hr * 0.4f, hr * 0.25f, 0xCCFFFFFF);
    }

    static void crown(Canvas c, float cx, float cy, float w) {
        float h = w * 0.62f;
        float l = cx - w / 2, t = cy - h / 2;
        PATH.reset();
        PATH.moveTo(l + w * 0.08f, t + h * 0.78f);
        PATH.lineTo(l, t + h * 0.18f);
        PATH.lineTo(l + w * 0.27f, t + h * 0.48f);
        PATH.lineTo(l + w * 0.5f, t);
        PATH.lineTo(l + w * 0.73f, t + h * 0.48f);
        PATH.lineTo(l + w, t + h * 0.18f);
        PATH.lineTo(l + w * 0.92f, t + h * 0.78f);
        PATH.close();
        reset();
        P.setShader(new LinearGradient(0, t, 0, t + h, 0xFFFFF08A, 0xFFE09500, Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(w * 0.025f);
        P.setStrokeJoin(Paint.Join.ROUND);
        P.setColor(0xFF8A5200);
        c.drawPath(PATH, P);
        rrectGrad(c, l + w * 0.06f, t + h * 0.74f, l + w * 0.94f, t + h, w * 0.05f, 0xFFFFE066, 0xFFC07A00);
        rrectStroke(c, l + w * 0.06f, t + h * 0.74f, l + w * 0.94f, t + h, w * 0.05f, 0xFF8A5200, w * 0.02f);
        circle(c, l, t + h * 0.16f, w * 0.05f, 0xFFFFE680);
        circle(c, l + w * 0.5f, t, w * 0.055f, 0xFFFFE680);
        circle(c, l + w, t + h * 0.16f, w * 0.05f, 0xFFFFE680);
        circle(c, cx, t + h * 0.87f, w * 0.06f, 0xFFE0162B);
        circle(c, cx - w * 0.28f, t + h * 0.87f, w * 0.045f, 0xFF2AA6FF);
        circle(c, cx + w * 0.28f, t + h * 0.87f, w * 0.045f, 0xFF2AA6FF);
    }

    static void coin(Canvas c, float cx, float cy, float r) {
        circle(c, cx, cy + r * 0.12f, r, 0xFFB86E00);
        reset();
        P.setShader(new RadialGradient(cx - r * 0.3f, cy - r * 0.3f, r * 1.3f, 0xFFFFF1A0, 0xFFF0A800,
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, P);
        P.setShader(null);
        ring(c, cx, cy, r * 0.7f, 0xFFD48A00, r * 0.1f);
    }

    static void gem(Canvas c, float cx, float cy, float r) {
        PATH.reset();
        PATH.moveTo(cx - r, cy - r * 0.3f);
        PATH.lineTo(cx - r * 0.5f, cy - r * 0.8f);
        PATH.lineTo(cx + r * 0.5f, cy - r * 0.8f);
        PATH.lineTo(cx + r, cy - r * 0.3f);
        PATH.lineTo(cx, cy + r);
        PATH.close();
        reset();
        P.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r, 0xFFBFF4FF, 0xFF1C7BE0, Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(r * 0.08f);
        P.setColor(0xFF0B4A9A);
        c.drawPath(PATH, P);
        P.setStrokeWidth(r * 0.05f);
        c.drawLine(cx - r, cy - r * 0.3f, cx + r, cy - r * 0.3f, P);
        c.drawLine(cx - r * 0.3f, cy - r * 0.3f, cx, cy + r, P);
        c.drawLine(cx + r * 0.3f, cy - r * 0.3f, cx, cy + r, P);
    }

    static void plus(Canvas c, float cx, float cy, float s) {
        rrectGrad(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, s * 0.2f, 0xFF7CE84A, 0xFF1E8A10);
        rrect(c, cx - s * 0.32f, cy - s * 0.08f, cx + s * 0.32f, cy + s * 0.08f, 2, 0xFFFFFFFF);
        rrect(c, cx - s * 0.08f, cy - s * 0.32f, cx + s * 0.08f, cy + s * 0.32f, 2, 0xFFFFFFFF);
    }

    static void check(Canvas c, float cx, float cy, float s, int color, float w) {
        reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeJoin(Paint.Join.ROUND);
        P.setStrokeWidth(w);
        P.setColor(color);
        PATH.reset();
        PATH.moveTo(cx - s * 0.45f, cy);
        PATH.lineTo(cx - s * 0.12f, cy + s * 0.32f);
        PATH.lineTo(cx + s * 0.48f, cy - s * 0.38f);
        c.drawPath(PATH, P);
    }

    // ---------------------------------------------------------------- logo

    /** Game logo: crown + LUDO letters + CROWN tag + mini board with pawns and a die. */
    static void logo(Canvas c, float cx, float top, float s) {
        c.save();
        c.translate(cx, top);
        c.scale(s, s);
        // mini board in perspective
        c.save();
        Matrix m = new Matrix();
        float[] src = {0, 0, 400, 0, 400, 400, 0, 400};
        float[] dst = {-170, 240, 170, 240, 225, 395, -225, 395};
        m.setPolyToPoly(src, 0, dst, 0, 4);
        rrect(c, -232, 392, 232, 410, 6, 0xFF1A3B7A);
        c.concat(m);
        miniBoard(c);
        c.restore();
        // letters
        String[] L = {"L", "U", "D", "O"};
        int[] ringC = {0xFF4FB3FF, 0xFFE8202A, 0xFF4CC34A, 0xFFF5C518};
        int[] letC = {0xFF1976D2, 0xFFD0101A, 0xFF2E9E2E, 0xFFF0B000};
        for (int i = 0; i < 4; i++) {
            float x = -170 + i * 113;
            float y = 145;
            circle(c, x, y + 5, 58, 0x66000000);
            circle(c, x, y, 58, ringC[i]);
            reset();
            P.setShader(new RadialGradient(x - 18, y - 20, 60, 0xFFFFFFFF, 0xFFD8DDE8, Shader.TileMode.CLAMP));
            c.drawCircle(x, y, 49, P);
            P.setShader(null);
            textC(c, L[i], x, y + 27, 78, letC[i], 0xFF222222, 4, BLACK);
        }
        crown(c, -120, 38, 150);
        text(c, "CROWN", 150, 70, 44, 0xFFFFC53A, 0xFF5A3000, 5, SERIF, Paint.Align.CENTER);
        // pawns and die
        pawn(c, -150, 300, 105, 0);
        pawn(c, 140, 295, 95, 1);
        dice3d(c, 0, 275, 105);
        pawn(c, -205, 425, 210, 3);
        pawn(c, 205, 425, 210, 2);
        c.restore();
    }

    static void dice3d(Canvas c, float cx, float cy, float s) {
        c.save();
        c.rotate(-12, cx, cy);
        rrect(c, cx - s / 2 + 8, cy - s / 2 + 12, cx + s / 2 + 8, cy + s / 2 + 12, s * 0.22f, 0x55000000);
        rrectGrad(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, s * 0.22f, 0xFFFFFFFF, 0xFFC9CED6);
        int[] pips = {0, 2, 3, 5, 6, 8};
        for (int idx : pips) {
            circle(c, cx + ((idx % 3) - 1) * s * 0.27f, cy + ((idx / 3) - 1) * s * 0.27f, s * 0.085f, 0xFF111111);
        }
        c.restore();
    }

    private static void miniBoard(Canvas c) {
        reset();
        P.setColor(0xFFFFFFFF);
        c.drawRect(0, 0, 400, 400, P);
        float q = 160;
        int[] order = {0, 1, 3, 2};
        float[][] qp = {{0, 0}, {240, 0}, {0, 240}, {240, 240}};
        for (int i = 0; i < 4; i++) {
            int col = order[i];
            float x = qp[i][0], y = qp[i][1];
            P.setColor(BOARD[col]);
            c.drawRect(x, y, x + q, y + q, P);
            P.setColor(0xFFFFFFFF);
            c.drawRect(x + 22, y + 22, x + q - 22, y + q - 22, P);
            P.setColor(BOARD[col]);
            c.drawCircle(x + 58, y + 58, 17, P);
            c.drawCircle(x + 102, y + 58, 17, P);
            c.drawCircle(x + 58, y + 102, 17, P);
            c.drawCircle(x + 102, y + 102, 17, P);
        }
        P.setColor(0xFF9AA4B8);
        P.setStrokeWidth(2);
        for (int i = 0; i <= 15; i++) {
            float v = i * 400 / 15f;
            if (v > q - 1 && v < 400 - q + 1) {
                c.drawLine(v, 0, v, 400, P);
                c.drawLine(0, v, 400, v, P);
            }
        }
        P.setColor(0xFF1E8FE8);
        c.drawRect(q + 27, 240, q + 53, 400, P);
    }

    // ---------------------------------------------------------------- background

    /** Blue tiled background with big dice-face squares rotated, similar to the board-game wallpaper. */
    static Bitmap pattern(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(Math.max(1, w), Math.max(1, h), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        reset();
        P.setShader(new RadialGradient(w / 2f, h * 0.45f, Math.max(w, h) * 0.75f, 0xFF2D6FD0, 0xFF16398F,
                Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, P);
        P.setShader(null);
        float unit = w / 3.2f;
        c.save();
        c.rotate(-28, w / 2f, h / 2f);
        int n = (int) (Math.max(w, h) * 1.6f / unit) + 2;
        float sx = w / 2f - n * unit / 2f, sy = h / 2f - n * unit / 2f;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float x = sx + i * unit, y = sy + j * unit;
                int kind = (i + j * 2) % 4;
                // tile block
                rrect(c, x + unit * 0.06f, y + unit * 0.06f, x + unit * 0.94f, y + unit * 0.94f, unit * 0.04f,
                        kind == 0 ? 0x2A7FB5FF : 0x1A0A1F70);
                rrect(c, x + unit * 0.18f, y + unit * 0.18f, x + unit * 0.82f, y + unit * 0.82f, unit * 0.02f,
                        0x24FFFFFF);
                float pr = unit * 0.07f;
                int pc = kind == 1 ? 0x552A1A7A : (kind == 2 ? 0x4418A060 : 0x44103A90);
                circle(c, x + unit * 0.35f, y + unit * 0.35f, pr, pc);
                circle(c, x + unit * 0.65f, y + unit * 0.35f, pr, pc);
                circle(c, x + unit * 0.35f, y + unit * 0.65f, pr, pc);
                circle(c, x + unit * 0.65f, y + unit * 0.65f, pr, pc);
                reset();
                P.setColor(0x1CFFFFFF);
                P.setStyle(Paint.Style.STROKE);
                P.setStrokeWidth(2);
                for (int k = 1; k < 6; k++) {
                    c.drawLine(x + unit * 0.94f, y + unit * k / 6f, x + unit, y + unit * k / 6f, P);
                }
                if ((i + j) % 3 == 0) star(c, x + unit * 0.97f, y + unit * 0.5f, unit * 0.04f, 0, 0x33FFFFFF, 2);
            }
        }
        c.restore();
        return bmp;
    }
}
