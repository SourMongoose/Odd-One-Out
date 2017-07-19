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
            c.drawCircle(0, 0, w/2, p);
        }
        //circle with dot
        if (id == 1) {
            c.drawCircle(0, 0, w/2, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //square
        if (id == 2) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
        }
        //square with dot
        if (id == 3) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //triangle (equilateral)
        if (id == 4) {
            float a = (float) Math.sqrt(3);
            c.drawLine(-w/2, w/2/a, 0, -w/a, p);
            c.drawLine(0, -w/a, w/2, w/2/a, p);
            c.drawLine(w/2, w/2/a, -w/2, w/2/a, p);
        }
        //triangle (equilateral) with dot
        if (id == 5) {
            float a = (float) Math.sqrt(3);
            c.drawLine(-w/2, w/2/a, 0, -w/a, p);
            c.drawLine(0, -w/a, w/2, w/2/a, p);
            c.drawLine(w/2, w/2/a, -w/2, w/2/a, p);
            c.drawCircle(0, 0, strokeWidth*2, p2);
        }
        //triangle (fit to square)
        if (id == 6) {
            c.drawLine(-w/2, w/2, 0, -w/2, p);
            c.drawLine(0, -w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, -w/2, w/2, p);
        }
        //cross (+)
        if (id == 7) {
            c.drawLine(-w/2, 0, w/2, 0, p);
            c.drawLine(0, -w/2, 0, w/2, p);
        }
        //cross (x)
        if (id == 8) {
            c.drawLine(-w/2, -w/2, w/2, w/2, p);
            c.drawLine(-w/2, w/2, w/2, -w/2, p);
        }
        //arrow (upwards)
        if (id == 9) {
            c.drawLine(0, -w/2, 0, w/2, p);
            c.drawLine(0, -w/2, -w/6, -w/3, p);
            c.drawLine(0, -w/2, w/6, -w/3, p);
        }
        //die (1)
        if (id == 10) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (2)
        if (id == 11) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(-w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, w/4, strokeWidth*3, p2);
        }
        //die (3)
        if (id == 12) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(-w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (4)
        if (id == 13) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(-w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(-w/4, w/4, strokeWidth*3, p2);
        }
        //die (5)
        if (id == 14) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(-w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(-w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(0, 0, strokeWidth*3, p2);
        }
        //die (6)
        if (id == 15) {
            c.drawRect(-w/2, -w/2, w/2, w/2, p);
            c.drawCircle(-w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(w/4, -w/4, strokeWidth*3, p2);
            c.drawCircle(-w/4, w/4, strokeWidth*3, p2);
            c.drawCircle(-w/4, 0, strokeWidth*3, p2);
            c.drawCircle(w/4, 0, strokeWidth*3, p2);
        }
        //letter F
        if (id == 16) {
            c.drawLine(-w/2, -w/2, w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, 0, w/2, 0, p);
        }
        //letter G
        if (id == 17) {
            c.drawLine(-w/2, -w/2, w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, w/2, 0, p);
            c.drawLine(w/2, 0, 0, 0, p);
        }
        //letter P
        if (id == 18) {
            c.drawLine(-w/2, -w/2, w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, 0, w/2, 0, p);
            c.drawLine(w/2, 0, w/2, -w/2, p);
        }
        //letter R
        if (id == 19) {
            c.drawLine(-w/2, -w/2, w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, 0, w/2, 0, p);
            c.drawLine(w/2, 0, w/2, -w/2, p);
            c.drawLine(-w/2, 0, w/2, w/2, p);
        }
        //letter N
        if (id == 20) {
            c.drawLine(-w/2, w/2, -w/2, -w/2, p);
            c.drawLine(-w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, w/2, -w/2, p);
        }
        //letter F (backwards)
        if (id == 21) {
            c.drawLine(w/2, -w/2, -w/2, -w/2, p);
            c.drawLine(w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, 0, -w/2, 0, p);
        }
        //letter G (backwards)
        if (id == 22) {
            c.drawLine(w/2, -w/2, -w/2, -w/2, p);
            c.drawLine(w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, w/2, -w/2, w/2, p);
            c.drawLine(-w/2, w/2, -w/2, 0, p);
            c.drawLine(-w/2, 0, 0, 0, p);
        }
        //letter P (backwards)
        if (id == 23) {
            c.drawLine(w/2, -w/2, -w/2, -w/2, p);
            c.drawLine(w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, 0, -w/2, 0, p);
            c.drawLine(-w/2, 0, -w/2, -w/2, p);
        }
        //letter R (backwards)
        if (id == 24) {
            c.drawLine(w/2, -w/2, -w/2, -w/2, p);
            c.drawLine(w/2, -w/2, w/2, w/2, p);
            c.drawLine(w/2, 0, -w/2, 0, p);
            c.drawLine(-w/2, 0, -w/2, -w/2, p);
            c.drawLine(w/2, 0, -w/2, w/2, p);
        }
        //letter N (backwards)
        if (id == 25) {
            c.drawLine(w/2, w/2, w/2, -w/2, p);
            c.drawLine(w/2, -w/2, -w/2, w/2, p);
            c.drawLine(-w/2, w/2, -w/2, -w/2, p);
        }

        c.restore();
        rotate(rotateSpeed);
    }
}
