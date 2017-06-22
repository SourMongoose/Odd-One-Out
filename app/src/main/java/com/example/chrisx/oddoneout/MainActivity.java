package com.example.chrisx.oddoneout;

/**
 * Organized in order of priority:
 * @TODO add tutorial ("how to play" button)
 * @TODO other game modes (1v1 maybe)
 * @TODO user preferences (night mode, fps, etc.)
 * @TODO smoother animation for game over screen (includes "New high score" notif)
 * @TODO more icons/pairs
 * @TODO organize icons
 * ...
 * @TODO extreme?
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private Bitmap bmp;
    private Canvas canvas;
    private LinearLayout ll;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface spinnaker;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    private long score = 0;
    private boolean isHighScore;
    private long previousHigh;
    private float speed = 0;
    private int column = 0;

    private Icon[] row;
    private int correctColumn;
    private float rowPosition;
    private int previousPair = -1;

    private static final int FRAMES_PER_SECOND = 60;
    private static final long NANOSECONDS_PER_FRAME = (long)1e9 / FRAMES_PER_SECOND;
    private static final long MILLISECONDS_PER_FRAME = (long)1e3 / FRAMES_PER_SECOND;

    private long startAnimation = FRAMES_PER_SECOND*8/3;
    private long tutorialFrames;
    private long transitionFrames;
    private long gameoverFrames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hide app preview in task manager (anti-cheat)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        //creates the bitmap
        //note: Star 4.5 is 480x854
        bmp = Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels,
                Bitmap.Config.ARGB_8888);

        //creates canvas
        canvas = new Canvas(bmp);

        ll = (LinearLayout) findViewById(R.id.draw_area);
        ll.setBackgroundDrawable(new BitmapDrawable(bmp));

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putInt("high_score", 0);
        editor.apply();

        spinnaker = Typeface.createFromAsset(getAssets(), "fonts/Spinnaker-Regular.ttf");

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (true) {
                    long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //white background
                            canvas.drawColor(Color.WHITE);

                            if (menu.equals("start")) {
                                Paint title = newPaint(Color.BLACK);
                                title.setTextAlign(Paint.Align.CENTER);
                                title.setTextSize((int)(canvas.getHeight()/5.69));
                                canvas.drawText("ODD", canvas.getWidth()/2+Math.max(startAnimation-FRAMES_PER_SECOND*6/3,0)/(FRAMES_PER_SECOND*2/3f)*canvas.getWidth(), (int)(canvas.getHeight()/3.79), title);
                                canvas.drawText("ONE", canvas.getWidth()/2+Math.max(startAnimation-FRAMES_PER_SECOND*4/3,0)/(FRAMES_PER_SECOND*2/3f)*canvas.getWidth(), (int)(canvas.getHeight()/2.4), title);
                                canvas.drawText("OUT", canvas.getWidth()/2+Math.max(startAnimation-FRAMES_PER_SECOND*2/3,0)/(FRAMES_PER_SECOND*2/3f)*canvas.getWidth(), (int)(canvas.getHeight()/1.76), title);

                                title.setTextSize((int)(canvas.getHeight()/15.52));
                                canvas.drawText("start", canvas.getWidth()/2-startAnimation/(FRAMES_PER_SECOND*2/3f)*canvas.getWidth(), (int)(canvas.getHeight()/1.35), title);
                                canvas.drawText("how to play", canvas.getWidth()/2-startAnimation/(FRAMES_PER_SECOND*2/3f)*canvas.getWidth(), (int)(canvas.getHeight()/1.177), title);

                                if (startAnimation > 0) startAnimation--;
                            } else if (menu.equals("howtoplay")) {
                                Paint p = newPaint(Color.BLACK);
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(40);

                                //@TODO make compatible w/ diff screen sizes
                                if (tutorialFrames == 0) {
                                    canvas.drawText("The objective of", canvas.getWidth() / 2, 70, p);
                                    canvas.drawText("this game is to find", canvas.getWidth() / 2, 115, p);
                                    canvas.drawText("the \"odd one out,\"", canvas.getWidth() / 2, 160, p);
                                    canvas.drawText("or something that is", canvas.getWidth() / 2, 205, p);
                                    canvas.drawText("different from the", canvas.getWidth() / 2, 250, p);
                                    canvas.drawText("others around it.", canvas.getWidth() / 2, 295, p);
                                } else if (tutorialFrames == 1) {
                                    canvas.drawText("There will be rows", canvas.getWidth()/2, 70, p);
                                    canvas.drawText("of 4 icons each", canvas.getWidth()/2, 115, p);
                                    canvas.drawText("moving down the", canvas.getWidth()/2, 160, p);
                                    canvas.drawText("screen, gradually", canvas.getWidth()/2, 205, p);
                                    canvas.drawText("speeding up as your", canvas.getWidth()/2, 250, p);
                                    canvas.drawText("score increases.", canvas.getWidth()/2, 295, p);
                                }

                                canvas.drawText("Next", canvas.getWidth()/2, canvas.getHeight()-20, p);

                                if (tutorialFrames !=  0 && tutorialFrames != 1) tutorialFrames++;
                            } else if (menu.equals("game")) {
                                if (!paused) {
                                    //show current column
                                    canvas.drawRect(column * canvas.getWidth() / 4, 0, (column + 1) * canvas.getWidth() / 4, canvas.getHeight(), newPaint(Color.rgb(245, 245, 245)));
                                    //dividing lines
                                    for (int i = 0; i < 3; i++) {
                                        float x = canvas.getWidth() / 4 + i * canvas.getWidth() / 4;
                                        canvas.drawLine(x, 0, x, canvas.getHeight(), newPaint(Color.rgb(200, 200, 200)));
                                    }

                                    //show current score and high score
                                    Paint scoreTitle = newPaint(Color.BLACK);
                                    scoreTitle.setTextSize(20);
                                    scoreTitle.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText("score", 10, 25, scoreTitle);
                                    scoreTitle.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText("high", canvas.getWidth() - 10, 25, scoreTitle);
                                    Paint scoreText = newPaint(Color.BLACK);
                                    scoreText.setTextSize(30);
                                    scoreText.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText(score + "", 10, 60, scoreText);
                                    scoreText.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText(sharedPref.getInt("high_score", 0) + "", canvas.getWidth() - 10, 60, scoreText);

                                    //display row
                                    for (int i = 0; i < row.length; i++) {
                                        row[i].drawShape(canvas, canvas.getWidth() / 8 + canvas.getWidth() / 4 * i, rowPosition + canvas.getWidth() / 8, canvas.getWidth() / 4 / (float) Math.sqrt(2) - 10);
                                    }

                                    //move row down the canvas and adjust speed
                                    rowPosition += speed;
                                    speed = canvas.getHeight() / Math.max(2.5f - score / 30.f, 1) / FRAMES_PER_SECOND;

                                    //check if selected column is correct
                                    if (rowPosition > canvas.getHeight()) {
                                        if (correctColumn == column) {
                                            score++;
                                            row = generateRow();
                                        } else {
                                            menu = "transition";
                                            transitionFrames = 0;
                                            if (score > sharedPref.getInt("high_score", 0)) {
                                                isHighScore = true;
                                                previousHigh = sharedPref.getInt("high_score", 0);
                                                editor.putInt("high_score", (int) score);
                                                editor.apply();
                                            } else isHighScore = false;
                                        }
                                    }
                                }
                            } else if (menu.equals("transition")) {
                                int alpha = 255 - (int)Math.max(255*(transitionFrames-1.5*FRAMES_PER_SECOND)/(0.5*FRAMES_PER_SECOND), 0);
                                //show current column
                                canvas.drawRect(column*canvas.getWidth()/4, 0, (column+1)*canvas.getWidth()/4, canvas.getHeight(), newPaint(Color.argb(alpha,245,245,245)));
                                //dividing lines
                                for (int i = 0; i < 3; i++) {
                                    float x = canvas.getWidth()/4 + i * canvas.getWidth()/4;
                                    canvas.drawLine(x, 0, x, canvas.getHeight(), newPaint(Color.argb(alpha,200,200,200)));
                                }

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, canvas.getWidth()/8+canvas.getWidth()/4*i, rowPosition+canvas.getWidth()/8, canvas.getWidth()/4/(float)Math.sqrt(2)-10);
                                }
                                //box the correct column
                                Paint box = newPaint(Color.BLACK);
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(canvas.getWidth()/150);
                                box.setAlpha(255-alpha);
                                canvas.drawRect(correctColumn*canvas.getWidth()/4, canvas.getHeight()-canvas.getWidth()/4, (correctColumn+1)*canvas.getWidth()/4, canvas.getHeight(), box);

                                //move row back up to visible screen
                                if (rowPosition > canvas.getHeight()-canvas.getWidth()/4) {
                                    rowPosition -= canvas.getHeight()/3/FRAMES_PER_SECOND;
                                }

                                if (transitionFrames < 2*FRAMES_PER_SECOND) transitionFrames++;
                                else {
                                    menu = "gameover";
                                    gameoverFrames = 0;
                                }
                            } else if (menu.equals("gameover")) {
                                Paint p = newPaint(Color.BLACK);
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(30);

                                //new high score?
                                if (isHighScore) {
                                    Paint bannerText = newPaint(Color.BLACK);
                                    bannerText.setTextAlign(Paint.Align.CENTER);
                                    bannerText.setTextSize(50);
                                    canvas.drawText("NEW HIGH", canvas.getWidth()/2, canvas.getHeight()/4-25, bannerText);
                                    canvas.drawText("SCORE!", canvas.getWidth()/2, canvas.getHeight()/4+25, bannerText);

                                    Paint border = newPaint(Color.BLACK);
                                    for (int i = -100; i < canvas.getWidth()+100; i += 50) {
                                        canvas.drawCircle(i-((float)gameoverFrames/FRAMES_PER_SECOND*100%50), canvas.getHeight()/4-80, 5, border);
                                        canvas.drawCircle(i+((float)gameoverFrames/FRAMES_PER_SECOND*100%50), canvas.getHeight()/4+45, 5, border);
                                    }
                                }

                                //final score/high score
                                canvas.drawText("You scored", canvas.getWidth()/2, canvas.getHeight()/2-75, p);
                                if (isHighScore) canvas.drawText("Previous high", canvas.getWidth()/2, canvas.getHeight()/2+40, p);
                                else canvas.drawText("High score", canvas.getWidth()/2, canvas.getHeight()/2+40, p);
                                p.setTextSize(70);
                                canvas.drawText(score+"", canvas.getWidth()/2, canvas.getHeight()/2-5, p);
                                if (isHighScore) canvas.drawText(previousHigh+"", canvas.getWidth()/2, canvas.getHeight()/2+110, p);
                                else canvas.drawText(sharedPref.getInt("high_score", 0)+"", canvas.getWidth()/2, canvas.getHeight()/2+110, p);

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, canvas.getWidth()/8+canvas.getWidth()/4*i, rowPosition+canvas.getWidth()/8, canvas.getWidth()/4/(float)Math.sqrt(2)-10);
                                }
                                //box the correct column
                                Paint box = newPaint(Color.BLACK);
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(3);
                                canvas.drawRect(correctColumn*canvas.getWidth()/4, canvas.getHeight()-canvas.getWidth()/4, (correctColumn+1)*canvas.getWidth()/4, canvas.getHeight(), box);

                                gameoverFrames++;
                            }

                            //update canvas
                            ll.invalidate();
                        }
                    });

                    frameCount++;

                    //wait until frame is done
                    while (System.nanoTime() - startTime < NANOSECONDS_PER_FRAME);
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        int action = event.getAction();

        if (menu.equals("start") && startAnimation == 0) {
            if (action == MotionEvent.ACTION_UP) {
                //start button
                if (Y > (int)(canvas.getHeight()/1.48) && Y < (int)(canvas.getHeight()/1.28)) {
                    menu = "game";
                    frameCount = 0;
                    score = 0;
                    row = generateRow();
                }
                //how to play button
                else if (Y > (int)(canvas.getHeight()/1.28) && Y < (int)(canvas.getHeight()/1.128)) {
                    menu = "howtoplay";
                    tutorialFrames = 0;
                }
            }
        } else if (menu.equals("howtoplay")) {
            if (action == MotionEvent.ACTION_UP) {
                if (Y > canvas.getHeight() - 80) {
                    if (tutorialFrames == 0 || tutorialFrames == 1) tutorialFrames++;
                }
            }
        } else if (menu.equals("game")) {
            column = (int) (X / (canvas.getWidth()/4));
        } else if (menu.equals("gameover")) {
            if (action == MotionEvent.ACTION_UP) menu = "start";
        }

        return true;
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(spinnaker);
        return p;
    }

    private Icon[] generateRow() {
        rowPosition = -canvas.getWidth()/4;
        correctColumn = (int)(Math.random()*4);

        /**
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

        int[][][] easyPairs = {{{0},{6}}, {{1},{7}}, {{4},{8}}, {{0},{1}}, {{1},{2}}, {{0},{2}},
                {{3},{23}}, {{2},{2,180}}, {{5},{5,180}}, {{18},{18,90}}, {{15},{17}}};
        int[][][] mediumPairs = {{{14},{15}}, {{16},{17}}, {{16},{18}}, {{5,90},{5,270}},
                {{14},{14,90}}, {{15},{15,90}}, {{9},{19}}, {{10},{20}}, {{11},{21}},
                {{12},{22}}, {{24},{25}}, {{24,90},{25,90}}};
        int[][][] hardPairs = {{{3,0,-5},{3,0,5}}, {{4,0,-5},{4,0,5}}, {{5,0,-5},{5,0,5}},
                {{8,0,-5},{8,0,5}}, {{13,0,-5},{13,0,5}}, {{14,0,-5},{14,0,5}}, {{15,0,-5},{15,0,5}},
                {{16,0,-5},{16,0,5}}, {{17,0,-5},{17,0,5}}, {{18,0,-5},{18,0,5}}, {{5,0,5},{5,180,5}}};
        int[][][] hardMirror = {{{9},{19}}, {{10},{20}}, {{11},{21}}, {{12},{22}}};

        Icon[] output = new Icon[4];
        if (score < 4) {
            //easy
            updatePair(output, easyPairs);
        } else if (score < 8) {
            //easy or medium
            if ((int)(Math.random()*2) == 0) updatePair(output, easyPairs);
            else updatePair(output, mediumPairs);
        } else if (score < 20) {
            //medium
            updatePair(output, mediumPairs);
        } else if (score < 30) {
            //medium or hard pair
            if ((int)(Math.random()*2) == 0) updatePair(output, mediumPairs);
            else updatePair(output, hardPairs);
        } else {
            //hard
            if ((int)(Math.random()*2) == 0) updatePair(output, hardPairs);
            else updateMirror(output, hardMirror);
        }

        return output;
    }

    private Icon toIcon(int[] a) {
        if (a.length == 1) return new Icon(a[0]);
        if (a.length == 2) return new Icon(a[0], a[1]);
        return new Icon(a[0], a[1], a[2]);
    }

    private void updatePair(Icon[] o, int[][][] a) {
        //chooses pair out of array
        int pair = (int)(Math.random()*a.length);
        while (pair == previousPair) pair = (int)(Math.random()*a.length);
        previousPair = pair;

        //picks one icon out of the pair to be the odd one out, the other icon will be in the 3 other columns
        int single = (int)(Math.random()*2);
        for (int i = 0; i < o.length; i++) {
            if (i == correctColumn) o[i] = toIcon(a[pair][single]);
            else o[i] = toIcon(a[pair][1-single]);
        }
    }

    private void updateMirror(Icon[] o, int[][][] a) {
        updatePair(o, a);
        int rotate90 = (int)(Math.random()*o.length);
        int rotate180 = rotate90;
        while (rotate180 == rotate90) rotate180 = (int)(Math.random()*o.length);
        int rotate270 = rotate90;
        while (rotate270 == rotate90 || rotate270 == rotate180) rotate270 = (int)(Math.random()*o.length);
        o[rotate90].rotate(90);
        o[rotate180].rotate(180);
        o[rotate270].rotate(270);
    }
}