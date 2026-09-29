package com.ludocrown.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/** Single view hosting the current scene; scales the 900x2000 virtual canvas to the screen. */
public class GameView extends View {
    static final float VW = 900, VH = 2000;

    private Scene scene;
    private Bitmap pattern;
    private float scale = 1, ox, oy;

    boolean pressing;
    float downX, downY;

    private String toast;
    private long toastAt;

    public GameView(Context ctx) {
        super(ctx);
        setFocusable(true);
        scene = new SplashScene(this);
    }

    void setScene(Scene s) {
        scene = s;
        pressing = false;
        invalidate();
    }

    Scene scene() { return scene; }

    Bitmap pattern() {
        if (pattern == null || pattern.getWidth() != getWidth() || pattern.getHeight() != getHeight()) {
            pattern = Art.pattern(getWidth(), getHeight());
        }
        return pattern;
    }

    void toast(String msg) {
        toast = msg;
        toastAt = System.currentTimeMillis();
    }

    interface NameCallback { void onName(String name); }

    void askName(String current, final NameCallback cb) {
        final EditText et = new EditText(getContext());
        et.setText(current);
        et.setSingleLine(true);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        et.setSelectAllOnFocus(true);
        new AlertDialog.Builder(getContext())
                .setTitle("Enter player name")
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String n = et.getText().toString().trim();
                        if (n.length() > 0) cb.onName(n);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    interface PinCallback { void onPin(String pin); }

    void askPin(String title, final PinCallback cb) {
        final EditText et = new EditText(getContext());
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        cb.onPin(et.getText().toString().trim());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        scale = Math.min(w / VW, h / VH);
        ox = (w - VW * scale) / 2f;
        oy = (h - VH * scale) / 2f;
    }

    @Override
    protected void onDraw(Canvas c) {
        Scene s = scene;
        s.drawScreen(c, getWidth(), getHeight());
        c.save();
        c.translate(ox, oy);
        c.scale(scale, scale);
        s.draw(c);
        drawToast(c);
        c.restore();
        postInvalidateOnAnimation();
    }

    private void drawToast(Canvas c) {
        if (toast == null) return;
        long t = System.currentTimeMillis() - toastAt;
        if (t > 2400) {
            toast = null;
            return;
        }
        int a = t > 2000 ? (int) (255 * (2400 - t) / 400f) : 255;
        Art.reset();
        Art.P.setTextSize(34);
        Art.P.setTypeface(Art.BOLD);
        float w = Math.min(820, Art.P.measureText(toast) + 60);
        Art.rrect(c, 450 - w / 2, 1720, 450 + w / 2, 1790, 35, (a * 3 / 4) << 24);
        Art.textFit(c, toast, 450, 1767, 34, 780, (a << 24) | 0xFFFFFF, 0, 0, Art.BOLD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = (e.getX() - ox) / scale, y = (e.getY() - oy) / scale;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressing = true;
                downX = x;
                downY = y;
                return true;
            case MotionEvent.ACTION_UP:
                boolean was = pressing;
                pressing = false;
                if (was) scene.onTap(downX, downY);
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressing = false;
                return true;
            default:
                return true;
        }
    }

    boolean onBack() {
        return scene.onBack();
    }
}
