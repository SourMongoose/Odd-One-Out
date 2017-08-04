package com.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class Theme {
    //c1 - background color
    //c2 - text/icon color
    private int c1, c2;

    Theme(int c1, int c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    int getC1() {
        return c1;
    }

    int getC2() {
        return c2;
    }

    int cost() {
        return 50;
    }

    int convertColor(int c) {
        int r = Color.red(c2) + (Color.red(c1) - Color.red(c2)) * Color.red(c) / 255;
        int g = Color.green(c2) + (Color.green(c1) - Color.green(c2)) * Color.green(c) / 255;
        int b = Color.blue(c2) + (Color.blue(c1) - Color.blue(c2)) * Color.blue(c) / 255;
        return Color.argb(Color.alpha(c), r, g, b);
    }

    void drawTheme(Canvas c, float x, float y, float w) {
        float strokeWidth = c.getWidth()/240;

        Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setStyle(Paint.Style.FILL);
        background.setColor(c1);
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(strokeWidth);
        border.setColor(c2);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(w/3);
        text.setColor(c2);

        c.drawCircle(x, y, w/2, background);
        c.drawText("Abc", x, y-(text.ascent()+text.descent())/2, text);
        c.drawCircle(x, y, w/2, border);
    }
}
