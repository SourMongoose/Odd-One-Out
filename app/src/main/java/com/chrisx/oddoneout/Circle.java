package com.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class Circle {
    private static final float MIN_R = 1f/24;
    private static final float MAX_R = 1f/6;
    private static final float MIN_SEC = 6;
    private static final float MAX_SEC = 10;

    private float x, y, sx, sy, ex, ey, r, sec;

    Circle(Canvas c) {
        r = (MIN_R + (float)(Math.random()*(MAX_R-MIN_R))) * c.getWidth();
        sec = MIN_SEC + (float)(Math.random()*(MAX_SEC-MIN_SEC));

        x = sx = (float)Math.random() * c.getWidth();
        y = sy = -r;
        ex = (float)Math.random() * c.getWidth();
        ey = c.getHeight() + r;
    }

    void update(int fps) {
        float sec_per_frame = 1f / fps;
        float frac_moved = sec_per_frame / sec;

        x += (ex - sx) * frac_moved;
        y += (ey - sy) * frac_moved;
    }

    void draw(Canvas c, Theme t) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(t.getC2());
        if (t.getC1() == Color.BLACK) p.setAlpha(40);
        else if (t.getC1() == Color.WHITE) p.setAlpha(10);
        else p.setAlpha(20);

        c.drawCircle(x, y, r, p);
    }

    boolean remove() {
        return y > ey;
    }
}
