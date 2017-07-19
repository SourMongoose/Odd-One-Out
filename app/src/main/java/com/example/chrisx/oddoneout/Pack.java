package com.example.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.lang.Math;

public class Pack {
    private String name;

    public Pack(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String description() {
        switch(name) {
            case "letter": return "The letters of the English alphabet.";
            default: return "Shapes and stuff.";
        }
    }

    public int cost() {
        return 100;
    }

    public void drawPack(Canvas c, float x, float y, float w, boolean inverted) {
        float strokeWidth = c.getWidth()/240;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(inverted ? Color.WHITE : Color.BLACK);
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);

        c.save();
        c.translate(x, y);

        if (name.equals("letter")) {
            c.drawLine(-w/2, w/2, 0, -w/2, p);
            c.drawLine(0, -w/2, w/2, w/2, p);
            c.drawLine(-w/4, 0, w/4, 0, p);
        } else {
            //default pack
            c.drawCircle(0, 0, w/2, p);
            float tmp = (float)(w/2 / Math.sqrt(2));
            c.drawRect(-tmp, -tmp, tmp, tmp, p);
        }

        c.restore();
    }

    /*
     * Icon table (for reference):
     * -----------
     * 0  - circle
     * 1  - square
     * 2  - triangle (fit to square)
     * 3  - cross (+)
     * 4  - triangle (equilateral)
     * 5  - arrow (upwards)
     * 6  - circle w/ dot
     * 7  - square w/ dot
     * 8  - triangle w/ dot (equilateral)
     * 9  - letter F
     * 10 - letter G
     * 11 - letter P
     * 12 - letter R
     * 13 - die (1)
     * 14 - die (2)
     * 15 - die (3)
     * 16 - die (4)
     * 17 - die (5)
     * 18 - die (6)
     * 19 - letter F (backwards)
     * 20 - letter G (backwards)
     * 21 - letter P (backwards)
     * 22 - letter R (backwards)
     * 23 - cross (x)
     * 24 - letter N
     * 25 - letter N (backwards)
     */

    public int[][][] getEasyPairs() {
        if (name.equals("letter")) {
            int[][][] easyPairs = {};
            return easyPairs;
        } else {
            int[][][] easyPairs = {{{0},{6}}, {{1},{7}}, {{4},{8}}, {{0},{1}}, {{1},{2}}, {{0},{2}},
                    {{3},{23}}, {{2},{2,180}}, {{5},{5,180}}, {{18},{18,90}}, {{16},{18}}, {{15},{17}}};
            return easyPairs;
        }
    }

    public int[][][] getMediumPairs() {
        if (name.equals("letter")) {
            int[][][] mediumPairs = {};
            return mediumPairs;
        } else {
            int[][][] mediumPairs = {{{14},{15}}, {{16},{17}}, {{5,90},{5,270}},
                    {{14},{14,90}}, {{15},{15,90}}, {{9},{19}}, {{10},{20}}, {{11},{21}},
                    {{12},{22}}, {{24},{25}}, {{24,90},{25,90}}};
            return mediumPairs;
        }
    }

    public int[][][] getHardPairs() {
        if (name.equals("letter")) {
            int[][][] hardPairs = {};
            return hardPairs;
        } else {
            int[][][] hardPairs = {{{3,0,-1},{3,0,1}}, {{4,0,-1},{4,0,1}}, {{5,0,-1},{5,0,1}},
                    {{8,0,-1},{8,0,1}}, {{13,0,-1},{13,0,1}}, {{14,0,-1},{14,0,1}}, {{15,0,-1},{15,0,1}},
                    {{16,0,-1},{16,0,1}}, {{17,0,-1},{17,0,1}}, {{18,0,-1},{18,0,1}}, {{5,0,1},{5,180,1}}};
            return hardPairs;
        }
    }

    public int[][][] getHardMirror() {
        if (name.equals("letter")) {
            int[][][] hardMirror = {};
            return hardMirror;
        } else {
            int[][][] hardMirror = {{{9},{19}}, {{10},{20}}, {{11},{21}}, {{12},{22}}};
            return hardMirror;
        }
    }
}
