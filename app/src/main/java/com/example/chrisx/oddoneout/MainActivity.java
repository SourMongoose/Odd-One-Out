package com.example.chrisx.oddoneout;

/**
 * Organized in order of priority:
 * @TODO be able to change user preferences
 * @TODO other game modes (1v1 maybe)
 * @TODO smoother animation for game over screen (includes "New high score" notif)
 * @TODO more icons/pairs
 * @TODO organize icons
 * @TODO update tutorial to make it look better
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
import android.view.Window;
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
    private String previousMenu;

    private long score = 0;
    private boolean isHighScore;
    private long previousHigh;
    private float speed = 0;
    private int column = 0;

    private Icon[] row;
    private int correctColumn;
    private float rowPosition;
    private int previousPair = -1;

    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private long startAnimation;
    private long tutorialFrames;
    private long transitionFrames;
    private long gameoverFrames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        nanosecondsPerFrame = (long)1e9 / getTargetFPS();
        millisecondsPerFrame = (long)1e3 / getTargetFPS();
        startAnimation = getTargetFPS()*8/3;

        spinnaker = Typeface.createFromAsset(getAssets(), "fonts/Spinnaker-Regular.ttf");

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //white background
                            if (getInvertColors().equals("on")) canvas.drawColor(Color.BLACK);
                            else canvas.drawColor(Color.WHITE);

                            if (menu.equals("start")) {
                                Paint title = newPaint(Color.BLACK);
                                title.setTextAlign(Paint.Align.CENTER);
                                title.setTextSize(convert854(150));
                                canvas.drawText("ODD", canvas.getWidth()/2+Math.max(startAnimation-getTargetFPS()*6/3,0)/(getTargetFPS()*2/3f)*canvas.getWidth(), convert854(225), title);
                                canvas.drawText("ONE", canvas.getWidth()/2+Math.max(startAnimation-getTargetFPS()*4/3,0)/(getTargetFPS()*2/3f)*canvas.getWidth(), convert854(355), title);
                                canvas.drawText("OUT", canvas.getWidth()/2+Math.max(startAnimation-getTargetFPS()*2/3,0)/(getTargetFPS()*2/3f)*canvas.getWidth(), convert854(485), title);

                                title.setTextSize(convert854(55));
                                canvas.drawText("start", canvas.getWidth()/2-startAnimation/(getTargetFPS()*2/3f)*canvas.getWidth(), convert854(632), title);
                                canvas.drawText("how to play", canvas.getWidth()/2-startAnimation/(getTargetFPS()*2/3f)*canvas.getWidth(), convert854(725), title);

                                //settings icon
                                drawGear(canvas.getWidth()-40, 40, 20);
                                Paint cover = newPaint(Color.WHITE);
                                cover.setAlpha((int)(255*Math.min(1, startAnimation/(getTargetFPS()*2/3f))));
                                canvas.drawRect(canvas.getWidth()-80, 0, canvas.getWidth(), 80, cover);

                                if (startAnimation > 0) startAnimation--;
                            } else if (menu.equals("howtoplay")) {
                                float textSize = convert854(40);

                                Paint p = newPaint(Color.BLACK);
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(textSize);

                                if (tutorialFrames == 0) {
                                    String[] txt = {
                                            "The objective of",
                                            "this game is to find",
                                            "the \"odd one out,\"",
                                            "or something that is",
                                            "different from the",
                                            "others around it."};
                                    for (int i = 0; i < txt.length; i++) {
                                        canvas.drawText(txt[i], canvas.getWidth()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                } else if (tutorialFrames == 1) {
                                    String[] txt = {
                                            "There will be rows",
                                            "of 4 icons each",
                                            "moving down the",
                                            "screen, gradually",
                                            "speeding up as your",
                                            "score increases."};
                                    for (int i = 0; i < txt.length; i++) {
                                        canvas.drawText(txt[i], canvas.getWidth()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                } else if (tutorialFrames == 2) {
                                    String[] txt = {
                                            "In each row, there",
                                            "will be one icon",
                                            "that is different",
                                            "from the rest. Tap",
                                            "on that icon's",
                                            "column before the",
                                            "row reaches the",
                                            "bottom of the screen.",
                                            "(Note: a new row",
                                            "will only appear",
                                            "after the previous",
                                            "row has gone off",
                                            "the screen.)"};
                                    for (int i = 0; i < txt.length; i++) {
                                        canvas.drawText(txt[i], canvas.getWidth()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                } else if (tutorialFrames == 3) {
                                    String[] txt = {
                                            "As for how to play",
                                            "the game, that's",
                                            "about it! Good luck :)"};
                                    for (int i = 0; i < txt.length; i++) {
                                        canvas.drawText(txt[i], canvas.getWidth()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                }

                                if (tutorialFrames < 3) canvas.drawText("Next", canvas.getWidth()/2, canvas.getHeight()-20, p);
                                else canvas.drawText("Menu", canvas.getWidth()/2, canvas.getHeight()-20, p);
                            } else if (menu.equals("settings")) {
                                Paint titleText = newPaint(Color.BLACK);
                                titleText.setTextAlign(Paint.Align.CENTER);
                                titleText.setTextSize(convert854(50));
                                canvas.drawText("settings", canvas.getWidth()/2, convert854(75), titleText);

                                Paint categoryText = newPaint(Color.BLACK);
                                categoryText.setTextSize(convert854(40));
                                Paint choiceText = newPaint(Color.BLACK);
                                choiceText.setTextAlign(Paint.Align.CENTER);
                                choiceText.setTextSize(convert854(35));
                                Paint boxPaint = newPaint(Color.BLACK);
                                boxPaint.setStyle(Paint.Style.STROKE);
                                boxPaint.setStrokeWidth(convert854(2));

                                //change fps
                                canvas.drawText("target FPS:", convert854(20), convert854(200), categoryText);
                                canvas.drawText("30", canvas.getWidth()/8, convert854(275), choiceText);
                                canvas.drawText("45", canvas.getWidth()*3/8, convert854(275), choiceText);
                                canvas.drawText("60", canvas.getWidth()*5/8, convert854(275), choiceText);
                                if (getTargetFPS() == 30) canvas.drawRect(convert854(20), convert854(230), canvas.getWidth()/4-convert854(20), convert854(290), boxPaint);
                                else if (getTargetFPS() == 45) canvas.drawRect(canvas.getWidth()/4+convert854(20), convert854(230), canvas.getWidth()*2/4-convert854(20), convert854(290), boxPaint);
                                else if (getTargetFPS() == 60) canvas.drawRect(canvas.getWidth()*2/4+convert854(20), convert854(230), canvas.getWidth()*3/4-convert854(20), convert854(290), boxPaint);

                                //equivalent of day/night mode
                                canvas.drawText("invert colors:", convert854(20), convert854(400), categoryText);
                                canvas.drawText("on", canvas.getWidth()/8, convert854(475), choiceText);
                                canvas.drawText("off", canvas.getWidth()*3/8, convert854(475), choiceText);
                                if (getInvertColors().equals("on")) canvas.drawRect(convert854(20), convert854(430), canvas.getWidth()/4-convert854(20), convert854(490), boxPaint);
                                else if (getInvertColors().equals("off")) canvas.drawRect(canvas.getWidth()/4+convert854(20), convert854(430), canvas.getWidth()*2/4-convert854(20), convert854(490), boxPaint);

                                //back button
                                Icon backButton = new Icon(5, 270);
                                backButton.drawShape(canvas, 60, canvas.getHeight()-40, 60, getInvertColors().equals("on"));
                            } else if (menu.equals("game")) {
                                if (!paused) {
                                    //show current column
                                    canvas.drawRect(column * canvas.getWidth()/4, 0, (column + 1) * canvas.getWidth()/4, canvas.getHeight(),
                                            newPaint(getInvertColors().equals("off") ? Color.rgb(245,245,245) : Color.rgb(220,220,220)));
                                    //dividing lines
                                    for (int i = 0; i < 3; i++) {
                                        float x = canvas.getWidth()/4 + i * canvas.getWidth()/4;
                                        canvas.drawLine(x, 0, x, canvas.getHeight(),
                                                newPaint(getInvertColors().equals("off") ? Color.rgb(200,200,200) : Color.rgb(150,150,150)));
                                    }

                                    //show current score and high score
                                    Paint scoreTitle = newPaint(Color.BLACK);
                                    scoreTitle.setTextSize(convert854(20));
                                    scoreTitle.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText("score", 10, convert854(25), scoreTitle);
                                    scoreTitle.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText("high", canvas.getWidth() - 10, convert854(25), scoreTitle);
                                    Paint scoreText = newPaint(Color.BLACK);
                                    scoreText.setTextSize(convert854(30));
                                    scoreText.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText(score+"", 10, convert854(60), scoreText);
                                    scoreText.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText(getHighScore()+"", canvas.getWidth() - 10, convert854(60), scoreText);

                                    //display row
                                    for (int i = 0; i < row.length; i++) {
                                        row[i].drawShape(canvas, canvas.getWidth()/8 + canvas.getWidth()/4 * i, rowPosition + canvas.getWidth()/8, canvas.getWidth()/4 / (float) Math.sqrt(2) - 10, getInvertColors().equals("on"));
                                    }

                                    //move row down the canvas and adjust speed
                                    rowPosition += speed;
                                    speed = canvas.getHeight() / Math.max(2.5f - score / 30.f, 1) / getTargetFPS();

                                    //check if selected column is correct
                                    if (rowPosition > canvas.getHeight()) {
                                        if (correctColumn == column) {
                                            score++;
                                            row = generateRow();
                                        } else {
                                            menu = "transition";
                                            transitionFrames = 0;
                                            if (score > getHighScore()) {
                                                isHighScore = true;
                                                previousHigh = getHighScore();
                                                editor.putInt("high_score", (int) score);
                                                editor.apply();
                                            } else isHighScore = false;
                                        }
                                    }
                                }
                            } else if (menu.equals("transition")) {
                                int alpha = 255 - (int)Math.max(255*(transitionFrames-1.5*getTargetFPS())/(0.5*getTargetFPS()), 0);
                                //show current column
                                canvas.drawRect(column * canvas.getWidth()/4, 0, (column + 1) * canvas.getWidth()/4, canvas.getHeight(),
                                        newPaint(getInvertColors().equals("off") ? Color.argb(alpha,245,245,245) : Color.argb(alpha,200,200,200)));//dividing lines
                                for (int i = 0; i < 3; i++) {
                                    float x = canvas.getWidth()/4 + i * canvas.getWidth()/4;
                                    canvas.drawLine(x, 0, x, canvas.getHeight(),
                                            newPaint(getInvertColors().equals("off") ? Color.argb(alpha,200,200,200) : Color.argb(alpha,150,150,150)));
                                }

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, canvas.getWidth()/8+canvas.getWidth()/4*i, rowPosition+canvas.getWidth()/8, canvas.getWidth()/4/(float)Math.sqrt(2)-10, getInvertColors().equals("on"));
                                }
                                //box the correct column
                                Paint box = newPaint(Color.BLACK);
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(canvas.getWidth()/150);
                                box.setAlpha(255-alpha);
                                canvas.drawRect(correctColumn*canvas.getWidth()/4, canvas.getHeight()-canvas.getWidth()/4, (correctColumn+1)*canvas.getWidth()/4, canvas.getHeight(), box);

                                //move row back up to visible screen
                                if (rowPosition > canvas.getHeight()-canvas.getWidth()/4) {
                                    rowPosition -= canvas.getHeight()/3/getTargetFPS();
                                    rowPosition = Math.max(rowPosition, canvas.getHeight()-canvas.getWidth()/4);
                                }

                                if (transitionFrames < 2*getTargetFPS()) transitionFrames++;
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
                                        canvas.drawCircle(i-((float)gameoverFrames/getTargetFPS()*100%50), canvas.getHeight()/4-80, 5, border);
                                        canvas.drawCircle(i+((float)gameoverFrames/getTargetFPS()*100%50), canvas.getHeight()/4+45, 5, border);
                                    }
                                }

                                //final score/high score
                                canvas.drawText("You scored", canvas.getWidth()/2, canvas.getHeight()/2-75, p);
                                if (isHighScore) canvas.drawText("Previous high", canvas.getWidth()/2, canvas.getHeight()/2+40, p);
                                else canvas.drawText("High score", canvas.getWidth()/2, canvas.getHeight()/2+40, p);
                                p.setTextSize(70);
                                canvas.drawText(score+"", canvas.getWidth()/2, canvas.getHeight()/2-5, p);
                                if (isHighScore) canvas.drawText(previousHigh+"", canvas.getWidth()/2, canvas.getHeight()/2+110, p);
                                else canvas.drawText(getHighScore()+"", canvas.getWidth()/2, canvas.getHeight()/2+110, p);

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, canvas.getWidth()/8+canvas.getWidth()/4*i, rowPosition+canvas.getWidth()/8, canvas.getWidth()/4/(float)Math.sqrt(2)-10, getInvertColors().equals("on"));
                                }
                                //box the correct column
                                Paint box = newPaint(Color.BLACK);
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(3);
                                canvas.drawRect(correctColumn*canvas.getWidth()/4, canvas.getHeight()-canvas.getWidth()/4, (correctColumn+1)*canvas.getWidth()/4, canvas.getHeight(), box);

                                p.setTextSize(30);
                                p.setAlpha((int)(255*Math.abs(Math.sin((float)gameoverFrames/getTargetFPS()*60*2/180*Math.PI))));
                                canvas.drawText("tap anywhere", canvas.getWidth()/2, canvas.getHeight()*3/4, p);
                                canvas.drawText("to continue", canvas.getWidth()/2, canvas.getHeight()*3/4+30, p);

                                //settings
                                drawGear(canvas.getWidth()-40, 40, 20);

                                gameoverFrames++;
                            }

                            //update canvas
                            ll.invalidate();
                        }
                    });

                    frameCount++;

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
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
                if (Y > convert854(577) && Y < convert854(667)) {
                    menu = "game";
                    frameCount = 0;
                    score = 0;
                    row = generateRow();
                }
                //how to play button
                else if (Y > convert854(667) && Y < convert854(757)) {
                    menu = "howtoplay";
                    tutorialFrames = 0;
                }
                //settings
                else if (X > canvas.getWidth() - 80 && Y < 80) {
                    menu = "settings";
                    previousMenu = "start";
                }
            }
        } else if (menu.equals("howtoplay")) {
            if (action == MotionEvent.ACTION_UP) {
                if (Y > canvas.getHeight() - 80) {
                    if (tutorialFrames < 3) tutorialFrames++;
                    else menu = "start";
                }
            }
        } else if (menu.equals("settings")) {
            if (action == MotionEvent.ACTION_UP) {
                if (X < 120 && Y > canvas.getHeight() - 80) {
                    menu = previousMenu;
                }
            }
            if (action == MotionEvent.ACTION_DOWN) {
                if (Y > convert854(230) && Y < convert854(290)) {
                    if (X < canvas.getWidth()/4) editor.putInt("target_fps", 30);
                    else if (X < canvas.getWidth()*2/4) editor.putInt("target_fps", 45);
                    else if (X < canvas.getWidth()*3/4) editor.putInt("target_fps", 60);
                    editor.apply();

                    nanosecondsPerFrame = (long)1e9 / getTargetFPS();
                    millisecondsPerFrame = (long)1e3 / getTargetFPS();
                } else if (Y > convert854(430) && Y < convert854(490)) {
                    if (X < canvas.getWidth()/4) editor.putString("invert_colors", "on");
                    else if (X < canvas.getWidth()*2/4) editor.putString("invert_colors", "off");
                    editor.apply();
                }
            }
        } else if (menu.equals("game")) {
            column = (int) (X / (canvas.getWidth()/4));
        } else if (menu.equals("gameover")) {
            if (action == MotionEvent.ACTION_UP) {
                if (X > canvas.getWidth() - 80 && Y < 80) {
                    menu = "settings";
                    previousMenu = "gameover";
                } else menu = "start";
            }
        }

        return true;
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(spinnaker);

        if (getInvertColors().equals("on")) {
            p.setARGB(p.getAlpha(),
                    255 - ((p.getColor() >> 16) & 0xff),
                    255 - ((p.getColor() >> 8) & 0xff),
                    255 - (p.getColor() & 0xff));
        }

        return p;
    }

    private float convert854(float f) {
        return canvas.getHeight() / (854 / f);
    }

    private int getHighScore() {
        return sharedPref.getInt("high_score", 0);
    }
    
    private int getTargetFPS() {
        return sharedPref.getInt("target_fps", 60);
    }
    
    private String getInvertColors() {
        return sharedPref.getString("invert_colors", "off");
    }

    private void drawGear(float x, float y, float w) {
        Paint p = newPaint(Color.BLACK);
        p.setStrokeWidth(convert854(2));
        p.setStyle(Paint.Style.STROKE);

        for (float angle = 0; angle < 2*Math.PI; angle += Math.PI/3) {
            canvas.drawLine(x+w*3/4*(float)Math.cos(angle+Math.PI/18), y-w*3/4*(float)Math.sin(angle+Math.PI/18),
                    x+w*(float)Math.cos(angle+Math.PI/10), y-w*(float)Math.sin(angle+Math.PI/10), p);
            canvas.drawLine(x+w*(float)Math.cos(angle+Math.PI/10), y-w*(float)Math.sin(angle+Math.PI/10),
                    x+w*(float)Math.cos(angle+Math.PI*7/30), y-w*(float)Math.sin(angle+Math.PI*7/30), p);
            canvas.drawLine(x+w*(float)Math.cos(angle+Math.PI*7/30), y-w*(float)Math.sin(angle+Math.PI*7/30),
                    x+w*3/4*(float)Math.cos(angle+Math.PI*5/18), y-w*3/4*(float)Math.sin(angle+Math.PI*5/18), p);
            canvas.drawLine(x+w*3/4*(float)Math.cos(angle+Math.PI*5/18), y-w*3/4*(float)Math.sin(angle+Math.PI*5/18),
                    x+w*3/4*(float)Math.cos(angle+Math.PI*7/18), y-w*3/4*(float)Math.sin(angle+Math.PI*7/18), p);
        }
        canvas.drawCircle(x, y, w/2, p);
    }

    private Icon[] generateRow() {
        rowPosition = -canvas.getWidth()/4;
        correctColumn = (int)(Math.random()*4);

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

        int[][][] easyPairs = {{{0},{6}}, {{1},{7}}, {{4},{8}}, {{0},{1}}, {{1},{2}}, {{0},{2}},
                {{3},{23}}, {{2},{2,180}}, {{5},{5,180}}, {{18},{18,90}}, {{16},{18}}, {{15},{17}}};
        int[][][] mediumPairs = {{{14},{15}}, {{16},{17}}, {{5,90},{5,270}},
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
