package com.ludocrown.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/** Simple vector icons drawn with Canvas. */
final class Icons {
    private Icons() {}

    static final int ONLINE = 1, TEAM = 2, FRIENDS = 3, COMPUTER = 4, PASS = 5, GEAR = 6, MAIL = 7, CART = 8,
            CUP = 9, SNAKE = 10, CHEST = 11, TARGET = 12, HELP = 13, WHEEL = 14, HOME = 15, EVENT = 16, MIC = 17,
            INVENTORY = 18, SOCIAL = 19, ROBOT = 20, AVATAR = 21, MENU = 22, TROPHY = 23;

    private static final Paint P = Art.P;
    private static final Path PATH = new Path();

    static void draw(Canvas c, int icon, float cx, float cy, float s) {
        switch (icon) {
            case ONLINE: online(c, cx, cy, s); break;
            case TEAM: team(c, cx, cy, s); break;
            case FRIENDS: friends(c, cx, cy, s); break;
            case COMPUTER: computer(c, cx, cy, s); break;
            case PASS: pass(c, cx, cy, s); break;
            case GEAR: gear(c, cx, cy, s); break;
            case MAIL: mail(c, cx, cy, s); break;
            case CART: cart(c, cx, cy, s); break;
            case CUP: cup(c, cx, cy, s); break;
            case SNAKE: snake(c, cx, cy, s); break;
            case CHEST: chest(c, cx, cy, s, 0xFFB5651D, 0xFF3FAF3F); break;
            case TARGET: target(c, cx, cy, s); break;
            case HELP: help(c, cx, cy, s); break;
            case WHEEL: wheel(c, cx, cy, s); break;
            case HOME: home(c, cx, cy, s); break;
            case EVENT: event(c, cx, cy, s); break;
            case MIC: mic(c, cx, cy, s); break;
            case INVENTORY: inventory(c, cx, cy, s); break;
            case SOCIAL: social(c, cx, cy, s); break;
            case ROBOT: robot(c, cx, cy, s, 0xFFFFFFFF); break;
            case AVATAR: avatar(c, cx, cy, s); break;
            case MENU: menu(c, cx, cy, s); break;
            case TROPHY: trophy(c, cx, cy, s); break;
            default: break;
        }
    }

    static void phone(Canvas c, float cx, float cy, float w, float h, float rot) {
        c.save();
        c.rotate(rot, cx, cy);
        Art.rrect(c, cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2, w * 0.18f, 0xFF3A2400);
        Art.rrectGrad(c, cx - w / 2 + 4, cy - h / 2 + 4, cx + w / 2 - 4, cy + h / 2 - 4, w * 0.14f, 0xFFFFE070, 0xFFE8A200);
        c.restore();
    }

    static void online(Canvas c, float cx, float cy, float s) {
        phone(c, cx - s * 0.62f, cy + s * 0.05f, s * 0.36f, s * 0.6f, -12);
        phone(c, cx + s * 0.62f, cy + s * 0.05f, s * 0.36f, s * 0.6f, 12);
        for (int i = 0; i < 7; i++) {
            Art.circle(c, cx - s * 0.36f + i * s * 0.12f, cy + s * 0.33f, s * 0.025f, 0xFFFFE070);
        }
        float r = s * 0.36f;
        Art.reset();
        P.setShader(new RadialGradient(cx - r * 0.3f, cy - r * 0.3f - s * 0.15f, r * 1.4f, 0xFF3B4FD8, 0xFF0A0F5A,
                Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy - s * 0.15f, r, P);
        P.setShader(null);
        // continents
        Art.circle(c, cx - r * 0.3f, cy - s * 0.15f - r * 0.25f, r * 0.35f, 0xFF28D628);
        Art.circle(c, cx + r * 0.25f, cy - s * 0.15f - r * 0.1f, r * 0.28f, 0xFF28D628);
        Art.circle(c, cx + r * 0.1f, cy - s * 0.15f + r * 0.45f, r * 0.22f, 0xFF28D628);
    }

    static void team(Canvas c, float cx, float cy, float s) {
        mapPin(c, cx - s * 0.55f, cy - s * 0.1f, s * 0.42f, 0xFF1FA84A, 0xFF1E8FE8);
        mapPin(c, cx + s * 0.55f, cy - s * 0.1f, s * 0.42f, 0xFFFFD21A, 0xFFE8202A);
        // sword
        c.save();
        Art.rrectGrad(c, cx - s * 0.05f, cy - s * 0.55f, cx + s * 0.05f, cy + s * 0.2f, s * 0.03f, 0xFFFFFFFF, 0xFF9AA8C0);
        Art.rrectGrad(c, cx - s * 0.2f, cy + s * 0.18f, cx + s * 0.2f, cy + s * 0.26f, s * 0.03f, 0xFFFFD84A, 0xFFC07A00);
        Art.rrect(c, cx - s * 0.04f, cy + s * 0.26f, cx + s * 0.04f, cy + s * 0.45f, s * 0.02f, 0xFF8A4A10);
        Art.circle(c, cx, cy + s * 0.48f, s * 0.05f, 0xFFFFC21A);
        c.restore();
    }

    static void mapPin(Canvas c, float cx, float cy, float s, int a, int b) {
        PATH.reset();
        PATH.addCircle(cx, cy - s * 0.2f, s * 0.42f, Path.Direction.CW);
        PATH.moveTo(cx - s * 0.36f, cy);
        PATH.lineTo(cx, cy + s * 0.55f);
        PATH.lineTo(cx + s * 0.36f, cy);
        PATH.close();
        Art.reset();
        P.setColor(0xFFF2F4F8);
        c.drawPath(PATH, P);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(2);
        P.setColor(0xFF55606E);
        c.drawPath(PATH, P);
        Art.circle(c, cx - s * 0.12f, cy - s * 0.3f, s * 0.2f, a);
        Art.circle(c, cx + s * 0.12f, cy - s * 0.1f, s * 0.2f, b);
    }

    static void heart(Canvas c, float cx, float cy, float s, int color) {
        PATH.reset();
        PATH.moveTo(cx, cy + s * 0.45f);
        PATH.cubicTo(cx - s * 0.9f, cy - s * 0.1f, cx - s * 0.4f, cy - s * 0.75f, cx, cy - s * 0.3f);
        PATH.cubicTo(cx + s * 0.4f, cy - s * 0.75f, cx + s * 0.9f, cy - s * 0.1f, cx, cy + s * 0.45f);
        PATH.close();
        Art.reset();
        P.setShader(new RadialGradient(cx - s * 0.2f, cy - s * 0.3f, s, 0xFFFF6B6B, color, Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
    }

    static void friends(Canvas c, float cx, float cy, float s) {
        phone(c, cx - s * 0.62f, cy + s * 0.05f, s * 0.36f, s * 0.6f, -12);
        phone(c, cx + s * 0.62f, cy + s * 0.05f, s * 0.36f, s * 0.6f, 12);
        for (int i = 0; i < 7; i++) {
            Art.circle(c, cx - s * 0.36f + i * s * 0.12f, cy + s * 0.33f, s * 0.025f, 0xFFFFE070);
        }
        heart(c, cx - s * 0.08f, cy - s * 0.25f, s * 0.55f, 0xFFD0101A);
        heart(c, cx + s * 0.18f, cy - s * 0.05f, s * 0.42f, 0xFFB00010);
    }

    static void computer(Canvas c, float cx, float cy, float s) {
        Art.rrect(c, cx - s * 0.28f, cy - s * 0.45f, cx + s * 0.28f, cy + s * 0.45f, s * 0.08f, 0xFF3A2400);
        Art.rrectGrad(c, cx - s * 0.24f, cy - s * 0.41f, cx + s * 0.24f, cy + s * 0.41f, s * 0.06f, 0xFFFFE070, 0xFFE8A200);
        Art.textC(c, "VS", cx, cy + s * 0.12f, s * 0.34f, 0xFF3A2400, 0, 0, Art.BLACK);
    }

    static void person(Canvas c, float cx, float cy, float s, int color) {
        Art.circle(c, cx, cy - s * 0.3f, s * 0.2f, color);
        Art.reset();
        P.setColor(color);
        Art.R.set(cx - s * 0.35f, cy - s * 0.08f, cx + s * 0.35f, cy + s * 0.5f);
        c.drawRoundRect(Art.R, s * 0.25f, s * 0.25f, P);
    }

    static void pass(Canvas c, float cx, float cy, float s) {
        person(c, cx - s * 0.62f, cy, s * 0.7f, 0xFFFFC21A);
        person(c, cx + s * 0.62f, cy, s * 0.7f, 0xFFFFC21A);
        c.save();
        c.rotate(20, cx, cy + s * 0.15f);
        Art.rrect(c, cx - s * 0.18f, cy + s * 0.05f, cx + s * 0.18f, cy + s * 0.3f, 4, 0xFFFFC21A);
        c.restore();
    }

    static void gear(Canvas c, float cx, float cy, float s) {
        Art.reset();
        P.setColor(0xFFEAF2FF);
        for (int i = 0; i < 8; i++) {
            c.save();
            c.rotate(i * 45, cx, cy);
            c.drawRect(cx - s * 0.1f, cy - s * 0.5f, cx + s * 0.1f, cy - s * 0.3f, P);
            c.restore();
        }
        Art.circle(c, cx, cy, s * 0.36f, 0xFFEAF2FF);
        Art.circle(c, cx, cy, s * 0.15f, 0xFF2A64C8);
    }

    static void mail(Canvas c, float cx, float cy, float s) {
        Art.rrectGrad(c, cx - s * 0.55f, cy - s * 0.35f, cx + s * 0.55f, cy + s * 0.35f, 4, 0xFFFFE070, 0xFFE8A200);
        Art.reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(s * 0.06f);
        P.setColor(0xFF9A5A00);
        c.drawLine(cx - s * 0.55f, cy - s * 0.35f, cx, cy + s * 0.05f, P);
        c.drawLine(cx + s * 0.55f, cy - s * 0.35f, cx, cy + s * 0.05f, P);
    }

    static void cart(Canvas c, float cx, float cy, float s) {
        Art.reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(s * 0.1f);
        P.setStrokeJoin(Paint.Join.ROUND);
        P.setColor(0xFFFFC21A);
        PATH.reset();
        PATH.moveTo(cx - s * 0.55f, cy - s * 0.4f);
        PATH.lineTo(cx - s * 0.38f, cy - s * 0.4f);
        PATH.lineTo(cx - s * 0.25f, cy + s * 0.2f);
        PATH.lineTo(cx + s * 0.4f, cy + s * 0.2f);
        PATH.lineTo(cx + s * 0.5f, cy - s * 0.25f);
        PATH.lineTo(cx - s * 0.33f, cy - s * 0.25f);
        c.drawPath(PATH, P);
        Art.circle(c, cx - s * 0.18f, cy + s * 0.4f, s * 0.09f, 0xFFFFC21A);
        Art.circle(c, cx + s * 0.32f, cy + s * 0.4f, s * 0.09f, 0xFFFFC21A);
    }

    static void roundFrame(Canvas c, float cx, float cy, float s) {
        Art.glow(c, cx - s * 0.55f, cy - s * 0.48f, cx + s * 0.55f, cy + s * 0.48f, s * 0.35f, 0xFFFFB000, 10);
        Art.rrectGrad(c, cx - s * 0.55f, cy - s * 0.48f, cx + s * 0.55f, cy + s * 0.48f, s * 0.35f, 0xFFFFE070, 0xFFD08000);
        Art.rrectGrad(c, cx - s * 0.47f, cy - s * 0.4f, cx + s * 0.47f, cy + s * 0.4f, s * 0.3f, 0xFF3A7FE0, 0xFF1C4FB0);
    }

    static void cup(Canvas c, float cx, float cy, float s) {
        roundFrame(c, cx, cy, s);
        PATH.reset();
        PATH.moveTo(cx - s * 0.22f, cy - s * 0.12f);
        PATH.lineTo(cx + s * 0.22f, cy - s * 0.12f);
        PATH.lineTo(cx + s * 0.16f, cy + s * 0.32f);
        PATH.lineTo(cx - s * 0.16f, cy + s * 0.32f);
        PATH.close();
        Art.reset();
        P.setShader(new LinearGradient(cx - s * 0.2f, 0, cx + s * 0.2f, 0, 0xFFFF4A3A, 0xFFA00A0A, Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        Art.dice(c, cx - s * 0.08f, cy - s * 0.2f, s * 0.2f, 5, -15, 0xFFDDDDDD);
        Art.textC(c, "7", cx + s * 0.1f, cy - s * 0.02f, s * 0.5f, 0xFFFFC21A, 0xFF5A2A00, 5, Art.BLACK);
    }

    static void snake(Canvas c, float cx, float cy, float s) {
        roundFrame(c, cx, cy, s);
        Art.reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(s * 0.05f);
        P.setColor(0xFFE8E8E8);
        c.drawLine(cx + s * 0.05f, cy + s * 0.3f, cx + s * 0.28f, cy - s * 0.3f, P);
        c.drawLine(cx + s * 0.2f, cy + s * 0.3f, cx + s * 0.4f, cy - s * 0.25f, P);
        P.setStrokeWidth(s * 0.1f);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setColor(0xFF2EB82E);
        PATH.reset();
        PATH.moveTo(cx - s * 0.3f, cy + s * 0.25f);
        PATH.cubicTo(cx - s * 0.05f, cy + s * 0.1f, cx - s * 0.45f, cy - s * 0.1f, cx - s * 0.2f, cy - s * 0.22f);
        c.drawPath(PATH, P);
        Art.circle(c, cx - s * 0.18f, cy - s * 0.24f, s * 0.08f, 0xFF2EB82E);
        Art.dice(c, cx + s * 0.1f, cy + s * 0.12f, s * 0.22f, 5, 12, 0xFFFF6060);
    }

    static void chest(Canvas c, float cx, float cy, float s, int wood, int lid) {
        Art.rrectGrad(c, cx - s * 0.45f, cy - s * 0.05f, cx + s * 0.45f, cy + s * 0.4f, 4, wood, 0xFF7A3A10);
        Art.rrectGrad(c, cx - s * 0.48f, cy - s * 0.35f, cx + s * 0.48f, cy, s * 0.12f, lid, 0xFF1E6A1E);
        Art.rrect(c, cx - s * 0.1f, cy - s * 0.1f, cx + s * 0.1f, cy + s * 0.12f, 3, 0xFFFFD84A);
        Art.circle(c, cx, cy, s * 0.03f, 0xFF3A2400);
    }

    static void target(Canvas c, float cx, float cy, float s) {
        Art.circle(c, cx, cy, s * 0.45f, 0xFFFFFFFF);
        Art.circle(c, cx, cy, s * 0.38f, 0xFFE0162B);
        Art.circle(c, cx, cy, s * 0.26f, 0xFFFFFFFF);
        Art.circle(c, cx, cy, s * 0.14f, 0xFFE0162B);
        Art.reset();
        P.setStrokeWidth(s * 0.06f);
        P.setColor(0xFF1E88E5);
        c.drawLine(cx, cy, cx + s * 0.5f, cy - s * 0.35f, P);
    }

    static void help(Canvas c, float cx, float cy, float s) {
        Art.rrect(c, cx - s * 0.42f, cy - s * 0.35f, cx + s * 0.42f, cy + s * 0.3f, s * 0.15f, 0xFFFFC21A);
        Art.rrect(c, cx - s * 0.36f, cy - s * 0.29f, cx + s * 0.36f, cy + s * 0.24f, s * 0.12f, 0xFF1C4FB0);
        Art.textC(c, "?", cx, cy + s * 0.16f, s * 0.5f, 0xFFFFD84A, 0xFF1C1C1C, 3, Art.BLACK);
    }

    static void wheel(Canvas c, float cx, float cy, float s) {
        Art.coin(c, cx - s * 0.3f, cy + s * 0.4f, s * 0.14f);
        Art.coin(c, cx + s * 0.25f, cy + s * 0.42f, s * 0.14f);
        Art.circle(c, cx, cy, s * 0.42f, 0xFFFFC21A);
        int[] cols = {0xFF1E88E5, 0xFFFFE070, 0xFF1E88E5, 0xFFFFE070, 0xFF1E88E5, 0xFFFFE070, 0xFF1E88E5, 0xFFFFE070};
        Art.reset();
        Art.R.set(cx - s * 0.36f, cy - s * 0.36f, cx + s * 0.36f, cy + s * 0.36f);
        for (int i = 0; i < 8; i++) {
            P.setColor(cols[i]);
            c.drawArc(Art.R, i * 45, 45, true, P);
        }
        Art.circle(c, cx, cy, s * 0.15f, 0xFFFFC21A);
        Art.textC(c, "SPIN", cx, cy + s * 0.05f, s * 0.12f, 0xFF5A2A00, 0, 0, Art.BLACK);
    }

    static void home(Canvas c, float cx, float cy, float s) {
        PATH.reset();
        PATH.moveTo(cx - s * 0.55f, cy - s * 0.05f);
        PATH.lineTo(cx, cy - s * 0.5f);
        PATH.lineTo(cx + s * 0.55f, cy - s * 0.05f);
        PATH.close();
        Art.reset();
        P.setColor(0xFFFFC21A);
        c.drawPath(PATH, P);
        Art.rrect(c, cx - s * 0.4f, cy - s * 0.1f, cx + s * 0.4f, cy + s * 0.38f, 4, 0xFFFFF4D0);
        for (int i = 0; i < 4; i++) {
            Art.circle(c, cx + ((i % 2) - 0.5f) * s * 0.22f, cy + s * 0.07f + (i / 2) * s * 0.2f, s * 0.07f, 0xFFE8A200);
        }
    }

    static void event(Canvas c, float cx, float cy, float s) {
        Art.rrect(c, cx - s * 0.4f, cy - s * 0.3f, cx + s * 0.4f, cy + s * 0.38f, 5, 0xFFEAF2FF);
        Art.rrect(c, cx - s * 0.4f, cy - s * 0.3f, cx + s * 0.4f, cy - s * 0.12f, 5, 0xFF4A90E2);
        Art.star(c, cx - s * 0.05f, cy + s * 0.1f, s * 0.2f, 0xFFFFC21A, 0xFF8A5200, 2);
        Art.circle(c, cx + s * 0.3f, cy + s * 0.3f, s * 0.14f, 0xFF3AB03A);
        Art.check(c, cx + s * 0.3f, cy + s * 0.3f, s * 0.14f, 0xFFFFFFFF, 3);
    }

    static void mic(Canvas c, float cx, float cy, float s) {
        Art.rrectGrad(c, cx - s * 0.16f, cy - s * 0.45f, cx + s * 0.16f, cy + s * 0.1f, s * 0.16f, 0xFFFFE070, 0xFFD08000);
        Art.reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(s * 0.05f);
        P.setColor(0xFFEAF2FF);
        Art.R.set(cx - s * 0.26f, cy - s * 0.2f, cx + s * 0.26f, cy + s * 0.22f);
        c.drawArc(Art.R, 0, 180, false, P);
        c.drawLine(cx, cy + s * 0.22f, cx, cy + s * 0.38f, P);
        c.drawLine(cx - s * 0.15f, cy + s * 0.38f, cx + s * 0.15f, cy + s * 0.38f, P);
    }

    static void inventory(Canvas c, float cx, float cy, float s) {
        Art.dice(c, cx - s * 0.12f, cy + s * 0.05f, s * 0.42f, 5, -10, 0xFFCCCCCC);
        Art.pawn(c, cx + s * 0.25f, cy + s * 0.3f, s * 0.55f, 2);
    }

    static void social(Canvas c, float cx, float cy, float s) {
        Art.rrect(c, cx - s * 0.45f, cy - s * 0.4f, cx + s * 0.15f, cy + s * 0.05f, s * 0.2f, 0xFFEAF2FF);
        Art.rrect(c, cx - s * 0.05f, cy - s * 0.2f, cx + s * 0.45f, cy + s * 0.15f, s * 0.17f, 0xFFFFC21A);
        for (int i = 0; i < 3; i++) Art.circle(c, cx - s * 0.3f + i * s * 0.15f, cy - s * 0.18f, s * 0.04f, 0xFF4A6AA0);
        person(c, cx + s * 0.1f, cy + s * 0.38f, s * 0.35f, 0xFF3A7FE0);
    }

    static void robot(Canvas c, float cx, float cy, float s, int color) {
        Art.rrectStroke(c, cx - s * 0.35f, cy - s * 0.25f, cx + s * 0.35f, cy + s * 0.3f, s * 0.1f, color, s * 0.08f);
        Art.circle(c, cx - s * 0.14f, cy, s * 0.07f, color);
        Art.circle(c, cx + s * 0.14f, cy, s * 0.07f, color);
        Art.rrect(c, cx - s * 0.03f, cy - s * 0.42f, cx + s * 0.03f, cy - s * 0.25f, 1, color);
        Art.circle(c, cx, cy - s * 0.44f, s * 0.06f, color);
        Art.rrect(c, cx - s * 0.47f, cy - s * 0.05f, cx - s * 0.37f, cy + s * 0.12f, 1, color);
        Art.rrect(c, cx + s * 0.37f, cy - s * 0.05f, cx + s * 0.47f, cy + s * 0.12f, 1, color);
    }

    static void avatar(Canvas c, float cx, float cy, float s) {
        Art.rrectGrad(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, 6, 0xFFFFFFFF, 0xFFB0B8C8);
        Art.circle(c, cx, cy - s * 0.12f, s * 0.2f, 0xFF8A94A8);
        Art.reset();
        P.setColor(0xFF8A94A8);
        Art.R.set(cx - s * 0.35f, cy + s * 0.08f, cx + s * 0.35f, cy + s * 0.7f);
        c.drawOval(Art.R, P);
        Art.rrectStroke(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, 6, 0xFFFFFFFF, 3);
    }

    static void menu(Canvas c, float cx, float cy, float s) {
        Art.glow(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, s * 0.25f, 0xFFFFB000, 10);
        Art.rrectGrad(c, cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, s * 0.25f, 0xFFFFE070, 0xFFD08000);
        Art.rrectGrad(c, cx - s * 0.42f, cy - s * 0.42f, cx + s * 0.42f, cy + s * 0.42f, s * 0.2f, 0xFF3A7FE0, 0xFF1C4FB0);
        for (int i = -1; i <= 1; i++) {
            Art.circle(c, cx - s * 0.2f, cy + i * s * 0.17f, s * 0.045f, 0xFFFFD84A);
            Art.rrect(c, cx - s * 0.1f, cy + i * s * 0.17f - s * 0.04f, cx + s * 0.26f, cy + i * s * 0.17f + s * 0.04f,
                    3, 0xFFFFD84A);
        }
    }

    static void trophy(Canvas c, float cx, float cy, float s) {
        Art.reset();
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(s * 0.07f);
        P.setColor(0xFFE0A000);
        Art.R.set(cx - s * 0.5f, cy - s * 0.4f, cx - s * 0.1f, cy + s * 0.05f);
        c.drawArc(Art.R, 90, 180, false, P);
        Art.R.set(cx + s * 0.1f, cy - s * 0.4f, cx + s * 0.5f, cy + s * 0.05f);
        c.drawArc(Art.R, -90, 180, false, P);
        PATH.reset();
        PATH.moveTo(cx - s * 0.32f, cy - s * 0.45f);
        PATH.lineTo(cx + s * 0.32f, cy - s * 0.45f);
        PATH.quadTo(cx + s * 0.3f, cy + s * 0.12f, cx, cy + s * 0.15f);
        PATH.quadTo(cx - s * 0.3f, cy + s * 0.12f, cx - s * 0.32f, cy - s * 0.45f);
        Art.reset();
        P.setShader(new LinearGradient(cx - s * 0.3f, 0, cx + s * 0.3f, 0, 0xFFFFF08A, 0xFFE09500, Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        Art.rrect(c, cx - s * 0.06f, cy + s * 0.12f, cx + s * 0.06f, cy + s * 0.3f, 2, 0xFFE09500);
        Art.rrectGrad(c, cx - s * 0.25f, cy + s * 0.28f, cx + s * 0.25f, cy + s * 0.42f, 4, 0xFFFFE070, 0xFFB86E00);
        Art.star(c, cx, cy - s * 0.18f, s * 0.13f, 0xFFFFFFFF, 0, 0);
    }
}
