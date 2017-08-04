package com.chrisx.oddoneout;

import android.graphics.Canvas;
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
            case "fourths": return "Circles split into shaded fourths.";
            default: return "Shapes and stuff.";
        }
    }

    int cost() {
        return 200;
    }

    void drawPack(Canvas c, float x, float y, float w, Theme t) {
        float strokeWidth = c.getWidth()/240;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(t.getC2());
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);

        c.save();
        c.translate(x, y);

        if (name.equals("letter")) {
            c.drawLine(-w/2, w/2, 0, -w/2, p);
            c.drawLine(0, -w/2, w/2, w/2, p);
            c.drawLine(-w/4, 0, w/4, 0, p);
        } else if (name.equals("fourths")) {
            c.drawCircle(0, 0, w/2, p);
            c.drawLine(-w/2, 0, w/2, 0, p);
            c.drawLine(0, -w/2, 0, w/2, p);
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
     * 16 - UL triangle w/o BL
     * 17 - BL triangle w/o UL
     * 18 - unnamed shape
     * 19 - swirl
     * 20 - hourglass (UL-BR)
     * 21 - UR triangle w/o BR
     * 22 - BR triangle w/o UR
     * 23 - unnamed shape (backwards)
     * 24 - swirl (backwards)
     * 25 - hourglass (BL-UR)
     *
     * 100 - A
     * 101 - B
     * 102 - C
     * 103 - D
     * 104 - E
     * 105, 106 - F
     * 107, 108 - G
     * 109 - H
     * 110, 111 - J
     * 112 - K
     * 113 - L
     * 114 - M
     * 115, 116 - N
     * 117 - O
     * 118, 119 - P
     * 120 - Q
     * 121, 122 - R
     * 123, 124 - S
     * 125 - T
     * 126 - V
     * 127 - W
     * 128 - X
     * 129 - Y
     *
     * (0 - white; 1 - gray; 2 - black) starting from upper-left going clockwise
     * 200 - 0000
     * 201 - 0001
     * 202 - 0002
     * 203 - 0011
     * 204 - 0012
     * 205 - 0021
     * 206 - 0022
     * 207 - 0101
     * 208 - 0102
     * 209 - 0111
     * 210 - 0112
     * 211 - 0121
     * 212 - 0122
     * 213 - 0202
     * 214 - 0211
     * 215 - 0212
     * 216 - 0221
     * 217 - 0222
     * 218 - 1111
     * 219 - 1112
     * 220 - 1122
     * 221 - 1212
     * 222 - 1222
     * 223 - 2222
     */

    int[][][] getEasyPairs() {
        if (name.equals("letter")) {
            int[][][] easyPairs = {{{102},{117}}, {{102,270},{117}}, {{104},{105}}, {{109,90},{125}},
                    {{117},{120}}, {{104},{114,270}}, {{123},{116,90}}, {{112,270},{126}}, {{100},{100,180}},
                    {{109},{109,90}}, {{114,180},{127}}, {{127,180},{114}}, {{101},{101,180}}, {{103},{103,180}},
                    {{112},{112,180}}};
            return easyPairs;
        } else if (name.equals("fourths")) {
            int[][][] easyPairs = {{{200},{201}}, {{200},{202}}, {{200},{207}}, {{200},{213}}, {{200},{218}},
                    {{200},{223}}, {{201},{202}}, {{201},{203}}, {{201},{207}}, {{201,90},{209}}, {{201},{218}},
                    {{202},{213}}, {{202},{206}}, {{202},{223}}, {{203},{204}}, {{203},{205}}, {{203},{209}},
                    {{203},{218}}, {{203},{206}}, {{204},{210}}, {{204},{206}}, {{204},{211,90}}, {{204},{215}},
                    {{204},{212,90}}, {{205},{206}}, {{205},{211}}, {{205},{214,90}}, {{205},{216}},
                    {{205},{215,90}}, {{206},{212}}, {{206},{216,90}}, {{206},{220}}, {{206},{217}},
                    {{206},{217,90}}, {{207},{208}}, {{207},{209}}, {{207},{211}}, {{207},{221,90}},
                    {{208},{210}}, {{208},{214,180}}, {{208},{212}}, {{208},{214,90}}, {{209},{210}},
                    {{209},{211}}, {{209},{214}}, {{209},{218}}, {{209},{219,90}}, {{210},{211}},
                    {{210},{212}}, {{210},{215}}, {{211},{212}}, {{211},{216}}, {{212},{216}}, {{213},{215}},
                    {{213},{223}}, {{214},{216}}, {{214},{215}}, {{215},{217}}, {{216},{217}}, {{218},{219}},
                    {{218},{220}}, {{218},{221}}, {{219},{221}}, {{219},{220}}, {{220},{222}}, {{221},{222}},
                    {{222},{223}}};
            return easyPairs;
        } else {
            int[][][] easyPairs = {{{0},{1}}, {{2},{3}}, {{4},{5}}, {{0},{2}}, {{2},{6}}, {{0},{6}},
                    {{7},{8}}, {{6},{6,180}}, {{9},{9,180}}, {{15},{15,90}}, {{13},{15}}, {{12},{14}}};
            return easyPairs;
        }
    }

    int[][][] getMediumPairs() {
        if (name.equals("letter")) {
            int[][][] mediumPairs = {{{100},{126,180}}, {{100,180},{126}}, {{102},{102,180}},
                    {{104},{104,180}}, {{105},{106}}, {{107},{108}}, {{110},{111}},
                    {{113},{113,270}}, {{115},{116}}, {{118},{119}}, {{120},{120,90}}, {{121},{122}},
                    {{123},{124}}, {{102,270},{110}}};
            return mediumPairs;
        } else if (name.equals("fourths")) {
            int[][][] mediumPairs = {{{201},{201,90}}, {{202},{202,90}}, {{204},{205}}, {{207},{207,90}},
                    {{208},{208,90}}, {{208},{208,180}}, {{209},{209,90}}, {{210},{214}}, {{210,90},{214}},
                    {{211},{211,180}}, {{212},{216,90}}, {{213},{213,90}}, {{215},{215,90}}, {{212,90},{216}},
                    {{217},{217,90}}, {{219},{219,90}}, {{221},{221,90}}, {{222},{222,90}}};
            return mediumPairs;
        } else {
            int[][][] mediumPairs = {{{11},{12}}, {{13},{14}}, {{9,90},{9,270}},
                    {{11},{11,90}}, {{12},{12,90}}, {{16},{21}}, {{17},{22}}, {{18},{23}},
                    {{19},{24}}, {{20},{25}}, {{16,180},{21,180}}, {{17,180},{22,180}}};
            return mediumPairs;
        }
    }

    int[][][] getHardPairs() {
        if (name.equals("letter")) {
            int[][][] hardPairs = new int[30][2][3];
            for (int i = 0; i < hardPairs.length; i++) {
                hardPairs[i][0][0] = hardPairs[i][1][0] = 100 + i;
                hardPairs[i][0][1] = hardPairs[i][1][1] = 0;
                hardPairs[i][0][2] = -1;
                hardPairs[i][1][2] = 1;
            }
            return hardPairs;
        } else if (name.equals("fourths")) {
            int[][][] hardPairs = new int[23][2][3];
            for (int i = 0; i < hardPairs.length; i++) {
                hardPairs[i][0][0] = hardPairs[i][1][0] = 200 + i;
                hardPairs[i][0][1] = hardPairs[i][1][1] = 0;
                hardPairs[i][0][2] = -1;
                hardPairs[i][1][2] = 1;
            }
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
            int[][][] hardMirror = {{{105},{106}}, {{107},{108}}, {{110},{111}}, {{118},{119}}, {{121},{122}}};
            return hardMirror;
        } else if (name.equals("fourths")) {
            int[][][] hardMirror = {{{204},{205}}, {{210,90},{214}}, {{212},{216,90}}};
            return hardMirror;
        } else {
            int[][][] hardMirror = {{{16},{21}}, {{17},{22}}, {{19},{24}}};
            return hardMirror;
        }
    }
}
