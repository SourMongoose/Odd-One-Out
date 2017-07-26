package com.example.chrisx.oddoneout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class Icon {
    private int id;
    private float angle;
    private float rotateSpeed;

    Icon(int id) {
        this.id = id;
        this.angle = this.rotateSpeed = 0;
    }
    Icon(int id, float angle) {
        this.id = id;
        this.angle = angle;
        this.rotateSpeed = 0;
    }
    Icon(int id, float angle, float rotateSpeed) {
        this.id = id;
        this.angle = angle;
        this.rotateSpeed = rotateSpeed;
    }

    int getID() {
        return id;
    }

    float getAngle() {
        return angle;
    }

    void setAngle(float angle) {
        this.angle = angle;
    }

    void rotate(float angle) {
        setAngle((this.angle + angle + 360) % 360);
    }

    //draws the icon within a square of center (x,y) and side length w
    void drawShape(Canvas c, float x, float y, float w, boolean inverted) {
        w /= 2;
        
        float strokeWidth = c.getWidth()/240;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(inverted ? Color.WHITE : Color.BLACK);
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);

        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(inverted ? Color.WHITE : Color.BLACK);

        c.save();
        c.translate(x, y);
        c.rotate(angle);

        //circle
        if (id == 0) {
            c.drawCircle(0, 0, w, p);
        }
        //circle with dot
        if (id == 1) {
            c.drawCircle(0, 0, w, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //square
        if (id == 2) {
            c.drawRect(-w, -w, w, w, p);
        }
        //square with dot
        if (id == 3) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //triangle (equilateral)
        if (id == 4) {
            float a = (float) Math.sqrt(3);
            c.drawLine(-w, w/a, 0, -2*w/a, p);
            c.drawLine(0, -2*w/a, w, w/a, p);
            c.drawLine(w, w/a, -w, w/a, p);
        }
        //triangle (equilateral) with dot
        if (id == 5) {
            float a = (float) Math.sqrt(3);
            c.drawLine(-w, w/a, 0, -2*w/a, p);
            c.drawLine(0, -2*w/a, w, w/a, p);
            c.drawLine(w, w/a, -w, w/a, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //triangle (fit to square)
        if (id == 6) {
            c.drawLine(-w, w, 0, -w, p);
            c.drawLine(0, -w, w, w, p);
            c.drawLine(w, w, -w, w, p);
        }
        //cross (+)
        if (id == 7) {
            c.drawLine(-w, 0, w, 0, p);
            c.drawLine(0, -w, 0, w, p);
        }
        //cross (x)
        if (id == 8) {
            c.drawLine(-w, -w, w, w, p);
            c.drawLine(-w, w, w, -w, p);
        }
        //arrow (upwards)
        if (id == 9) {
            c.drawLine(0, -w, 0, w, p);
            c.drawLine(0, -w, -2*w/6, -2*w/3, p);
            c.drawLine(0, -w, 2*w/6, -2*w/3, p);
        }
        //die (1)
        if (id == 10) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (2)
        if (id == 11) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(-w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, w/2, strokeWidth*3, p2);
        }
        //die (3)
        if (id == 12) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(-w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (4)
        if (id == 13) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(-w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(-w/2, w/2, strokeWidth*3, p2);
        }
        //die (5)
        if (id == 14) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(-w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(-w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (6)
        if (id == 15) {
            c.drawRect(-w, -w, w, w, p);
            c.drawCircle(-w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(w/2, -w/2, strokeWidth*3, p2);
            c.drawCircle(-w/2, w/2, strokeWidth*3, p2);
            c.drawCircle(-w/2, 0, strokeWidth*3, p2);
            c.drawCircle(w/2, 0, strokeWidth*3, p2);
        }
        /*  __
         * | /
         * |
         */
        if (id == 16) {
            c.drawLine(-w, w, -w, -w, p);
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(w, -w, 0, 0, p);
        }
        /* 
         * |
         * |_\
         */
        if (id == 17) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
            c.drawLine(w, w, 0, 0, p);
        }
        /*  ___
         * |_|
         * |
         */
        if (id == 18) {
            c.drawRect(-w, -w, 0, 0, p);
            c.drawLine(-w, 0, -w, w, p);
            c.drawLine(0, -w, w, -w, p);
        }
        //swirl
        if (id == 19) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, w, -w, w, p);
            c.drawLine(-w, w, -w, -w/2, p);
            c.drawLine(-w, -w/2, w/2, -w/2, p);
            c.drawLine(w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, -w/2, w/2, p);
            c.drawLine(-w/2, w/2, -w/2, 0, p);
            c.drawLine(-w/2, 0, 0, 0, p);
        }
        //hourglass (UL-BR)
        if (id == 20) {
            c.drawLine(-w, 0, 0, -w, p);
            c.drawLine(0, -w, 0, w, p);
            c.drawLine(0, w, w, 0, p);
            c.drawLine(w, 0, -w, 0, p);
        }
        /*  ___
         * |
         * |/
         */
        if (id == 21) {
            c.drawLine(-w, w, -w, -w, p);
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(-w, w, 0, 0, p);
        }
        /* 
         * |\
         * |__
         */
        if (id == 22) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
            c.drawLine(-w, -w, 0, 0, p);
        }
        /*  ___
         *   |_|
         *     |
         */
        if (id == 23) {
            c.drawRect(0, -w, w, 0, p);
            c.drawLine(w, 0, w, w, p);
            c.drawLine(0, -w, -w, -w, p);
        }
        //swirl (backwards)
        if (id == 24) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
            c.drawLine(w, w, w, -w/2, p);
            c.drawLine(w, -w/2, -w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, w/2, 0, p);
            c.drawLine(w/2, 0, 0, 0, p);
        }
        //hourglass (BL-UR)
        if (id == 25) {
            c.drawLine(w, 0, 0, -w, p);
            c.drawLine(0, -w, 0, w, p);
            c.drawLine(0, w, -w, 0, p);
            c.drawLine(-w, 0, w, 0, p);
        }

        //A
        if (id == 100) {
            c.drawLine(-w, w, 0, -w, p);
            c.drawLine(0, -w, w, w, p);
            c.drawLine(-w/2, 0, w/2, 0, p);
        }
        //B
        if (id == 101) {
            c.drawLine(-w, -w, -w, w, p);
            for (float i = -w; i < w/.75; i += w) c.drawLine(-w, i, w/2, i, p);
            c.drawLine(w/2, -w, w, -w/2, p);
            c.drawLine(w, -w/2, w/2, 0, p);
            c.drawLine(w/2, 0, w, w/2, p);
            c.drawLine(w, w/2, w/2, w, p);
        }
        //C
        if (id == 102) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
        }
        //D
        if (id == 103) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, 0, w, p);
            c.drawLine(0, w, w, 0, p);
            c.drawLine(w, 0, 0, -w, p);
            c.drawLine(0, -w, -w, -w, p);
        }
        //E
        if (id == 104) {
            c.drawLine(-w, -w, -w, w, p);
            for (float i = -w; i < w/.75; i += w) c.drawLine(-w, i, w, i, p);
        }
        //F
        if (id == 105) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, 0, w, 0, p);
        }
        //F backwards
        if (id == 106) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, 0, -w, 0, p);
        }
        //G
        if (id == 107) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
            c.drawLine(w, w, w, 0, p);
            c.drawLine(w, 0, 0, 0, p);
        }
        //G backwards
        if (id == 108) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, w, -w, w, p);
            c.drawLine(-w, w, -w, 0, p);
            c.drawLine(-w, 0, 0, 0, p);
        }
        //H
        if (id == 109) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, 0, w, 0, p);
            c.drawLine(w, -w, w, w, p);
        }
        //I - use H rotated 90deg
        //J
        if (id == 110) {
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, w, -w, w, p);
            c.drawLine(-w, w, -w, 0, p);
        }
        //J backwards
        if (id == 111) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
            c.drawLine(w, w, w, 0, p);
        }
        //K
        if (id == 112) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, 0, w, -w, p);
            c.drawLine(-w, 0, w, w, p);
        }
        //L
        if (id == 113) {
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, w, w, w, p);
        }
        //M
        if (id == 114) {
            c.drawLine(-w, w, -w, -w, p);
            c.drawLine(-w, -w, 0, 0, p);
            c.drawLine(0, 0, w, -w, p);
            c.drawLine(w, -w, w, w, p);
        }
        //N
        if (id == 115) {
            c.drawLine(-w, w, -w, -w, p);
            c.drawLine(-w, -w, w, w, p);
            c.drawLine(w, w, w, -w, p);
        }
        //N backwards
        if (id == 116) {
            c.drawLine(w, w, w, -w, p);
            c.drawLine(w, -w, -w, w, p);
            c.drawLine(-w, w, -w, -w, p);
        }
        //O
        if (id == 117) {
            c.drawRect(-w, -w, w, w, p);
        }
        //P
        if (id == 118) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, 0, w, 0, p);
            c.drawLine(w, 0, w, -w, p);
        }
        //P backwards
        if (id == 119) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, 0, -w, 0, p);
            c.drawLine(-w, 0, -w, -w, p);
        }
        //Q
        if (id == 120) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(w, -w, w, 0, p);
            c.drawLine(w, 0, 0, w, p);
            c.drawLine(0, w, -w, w, p);
            c.drawLine(-w, w, -w, -w, p);
            c.drawLine(0, 0, w, w, p);
        }
        //R
        if (id == 121) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(-w, -w, -w, w, p);
            c.drawLine(-w, 0, w, 0, p);
            c.drawLine(w, 0, w, -w, p);
            c.drawLine(-w, 0, w, w, p);
        }
        //R backwards
        if (id == 122) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(w, -w, w, w, p);
            c.drawLine(w, 0, -w, 0, p);
            c.drawLine(-w, 0, -w, -w, p);
            c.drawLine(w, 0, -w, w, p);
        }
        //S
        if (id == 123) {
            c.drawLine(w, -w, -w, -w, p);
            c.drawLine(-w, -w, -w, 0, p);
            c.drawLine(-w, 0, w, 0, p);
            c.drawLine(w, 0, w, w, p);
            c.drawLine(w, w, -w, w, p);
        }
        //S backwards
        if (id == 124) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(w, -w, w, 0, p);
            c.drawLine(w, 0, -w, 0, p);
            c.drawLine(-w, 0, -w, w, p);
            c.drawLine(-w, w, w, w, p);
        }
        //T
        if (id == 125) {
            c.drawLine(-w, -w, w, -w, p);
            c.drawLine(0, -w, 0, w, p);
        }
        //U - use C rotated 90deg
        //V
        if (id == 126) {
            c.drawLine(-w, -w, 0, w, p);
            c.drawLine(0, w, w, -w, p);
        }
        //W
        if (id == 127) {
            c.drawLine(-w, -w, -w/2, w, p);
            c.drawLine(-w/2, w, 0, 0, p);
            c.drawLine(0, 0, w/2, w, p);
            c.drawLine(w/2, w, w, -w, p);
        }
        //X
        if (id == 128) {
            c.drawLine(-w, -w, w, w, p);
            c.drawLine(-w, w, w, -w, p);
        }
        //Y
        if (id == 129) {
            c.drawLine(-w, -w, 0, 0, p);
            c.drawLine(w, -w, 0, 0, p);
            c.drawLine(0, w, 0, 0, p);
        }
        //Z - use N rotated 90deg

        c.restore();
        rotate(rotateSpeed);
    }
}
