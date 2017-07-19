package com.example.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.lang.Math;

class Pack {
    private String name;

    Pack(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    String description() {
        switch(name) {
            case "letter": return "The letters of the English alphabet.";
            default: return "Shapes and stuff.";
        }
    }

    int cost() {
        return 100;
    }

    void drawPack(Canvas c, float x, float y, float w, boolean inverted) {
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
     * Icon table:
     * -----------
     * 0  - circle
     * 1  - circle w/ dot
     * 2  - square
     * 3  - square w/ dot
     * 4  - triangle (equilateral)
     * 5  - triangle w/ dot (equilateral)
     * 6  - triangle (fit to square)
     * 7  - cross (+)
     * 8  - cross (x)
     * 9  - arrow (upwards)
     * 10 - die (1)
     * 11 - die (2)
     * 12 - die (3)
     * 13 - die (4)
     * 14 - die (5)
     * 15 - die (6)
     * 16-20 - letter F, G, P, R, N
     * 21-25 - letter F, G, P, R, N (backwards)
     */

    int[][][] getEasyPairs() {
        if (name.equals("letter")) {
            int[][][] easyPairs = {};
            return easyPairs;
        } else {
            int[][][] easyPairs = {{{0},{1}}, {{2},{3}}, {{4},{5}}, {{0},{2}}, {{2},{6}}, {{0},{6}},
                    {{7},{8}}, {{6},{6,180}}, {{9},{9,180}}, {{15},{15,90}}, {{13},{15}}, {{12},{14}}};
            return easyPairs;
        }
    }

    int[][][] getMediumPairs() {
        if (name.equals("letter")) {
            int[][][] mediumPairs = {};
            return mediumPairs;
        } else {
            int[][][] mediumPairs = {{{11},{12}}, {{13},{14}}, {{9,90},{9,270}},
                    {{11},{11,90}}, {{12},{12,90}}, {{16},{21}}, {{17},{22}}, {{18},{23}},
                    {{19},{24}}, {{20},{25}}, {{20,90},{25,90}}};
            return mediumPairs;
        }
    }

    int[][][] getHardPairs() {
        if (name.equals("letter")) {
            int[][][] hardPairs = {};
            return hardPairs;
        } else {
            int[][][] hardPairs = {{{7,0,-1},{7,0,1}}, {{4,0,-1},{4,0,1}}, {{9,0,-1},{9,0,1}},
                    {{5,0,-1},{5,0,1}}, {{10,0,-1},{10,0,1}}, {{11,0,-1},{11,0,1}}, {{12,0,-1},{12,0,1}},
                    {{13,0,-1},{13,0,1}}, {{14,0,-1},{14,0,1}}, {{15,0,-1},{15,0,1}}, {{9,0,1},{9,180,1}}};
            return hardPairs;
        }
    }

    int[][][] getHardMirror() {
        if (name.equals("letter")) {
            int[][][] hardMirror = {};
            return hardMirror;
        } else {
            int[][][] hardMirror = {{{16},{21}}, {{17},{22}}, {{18},{23}}, {{19},{24}}};
            return hardMirror;
        }
    }
}
