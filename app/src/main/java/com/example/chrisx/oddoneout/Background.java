package com.example.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Paint;

class Background {
    private String name;

    Background(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    int cost() {
        return 100;
    }

    void drawBackgroundIcon(Canvas c, float x, float y, float w, Theme t) {
        float strokeWidth = c.getWidth()/240;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(t.getC2());
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);

        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(t.getC2());
        p2.setAlpha(100);

        c.save();
        c.translate(x, y);

        if (name.equals("circles")) {
            w /= 2;
            c.drawCircle(w*-.5f, w*-.37f, w*.5f, p2);
            c.drawCircle(w*-.32f, w*.21f, w*.36f, p2);
            c.drawCircle(w*.23f, w*-.66f, w*.25f, p2);
            c.drawCircle(w*.71f, w*-.34f, w*.11f, p2);
            c.drawCircle(w*-.56f, w*.64f, w*.21f, p2);
            c.drawCircle(w*.28f, w*.2f, w*.11f, p2);
            c.drawCircle(w*.64f, w*.35f, w*.36f, p2);
            w *= 2;
        } else {
            c.drawCircle(0, 0, w/2, p);
            float tmp = w/2 * (float)Math.sqrt(2) / 2;
            c.drawLine(-tmp, tmp, tmp, -tmp, p);
        }

        c.restore();
    }

    void drawBackground(Canvas c, Theme t) {
        //flat color
        c.drawColor(t.getC1());

        //effects

    }
}
