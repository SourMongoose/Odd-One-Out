package com.example.chrisx.oddoneout;

/**
 * Organized in order of priority:
 * @TODO unlocking system for 2P (30+ score?)
 * @TODO smoother animation for game over screen (includes "New high score" notif)
 * @TODO make 2P game over screen look better
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

    //1P
    private long score;
    private boolean isHighScore;
    private long previousHigh;
    private float speed = 0;
    private int column = 0;

    private Icon[] row;
    private int correctColumn;
    private float rowPosition;
    private int previousPair = -1;

    //2P
    private long p1_score;
    private long p2_score;
    private boolean p1_ready;
    private boolean p2_ready;
    private Icon[] p1_row;
    private Icon[] p2_row;
    private int p1_column;
    private int p2_column;
    private int p1_correctColumn;
    private int p2_correctColumn;
    private int gamesPlayed;

    //frame data
    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private long startAnimation;
    private long tutorialFrames;
    private long transitionFrames;
    private long gameoverFrames;

    //settings
    private static final float TARGET_FPS_HEIGHT = 175;
    private static final float INVERT_COLORS_HEIGHT = 350;
    private static final float SHOW_1V1_HEIGHT = 525;

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
                                canvas.drawText("target FPS:", convert854(20), convert854(TARGET_FPS_HEIGHT), categoryText);
                                canvas.drawText("30", canvas.getWidth()/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                canvas.drawText("45", canvas.getWidth()*3/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                canvas.drawText("60", canvas.getWidth()*5/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                if (getTargetFPS() == 30) canvas.drawRect(convert854(20), convert854(TARGET_FPS_HEIGHT+30), canvas.getWidth()/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);
                                else if (getTargetFPS() == 45) canvas.drawRect(canvas.getWidth()/4+convert854(20), convert854(TARGET_FPS_HEIGHT+30), canvas.getWidth()*2/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);
                                else if (getTargetFPS() == 60) canvas.drawRect(canvas.getWidth()*2/4+convert854(20), convert854(TARGET_FPS_HEIGHT+30), canvas.getWidth()*3/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);

                                //equivalent of day/night mode
                                canvas.drawText("invert colors:", convert854(20), convert854(INVERT_COLORS_HEIGHT), categoryText);
                                canvas.drawText("on", canvas.getWidth()/8, convert854(INVERT_COLORS_HEIGHT+75), choiceText);
                                canvas.drawText("off", canvas.getWidth()*3/8, convert854(INVERT_COLORS_HEIGHT+75), choiceText);
                                if (getInvertColors().equals("on")) canvas.drawRect(convert854(20), convert854(INVERT_COLORS_HEIGHT+30), canvas.getWidth()/4-convert854(20), convert854(INVERT_COLORS_HEIGHT+90), boxPaint);
                                else if (getInvertColors().equals("off")) canvas.drawRect(canvas.getWidth()/4+convert854(20), convert854(INVERT_COLORS_HEIGHT+30), canvas.getWidth()*2/4-convert854(20), convert854(INVERT_COLORS_HEIGHT+90), boxPaint);

                                //show 1v1 mode as an option
                                canvas.drawText("show 1v1 mode:", convert854(20), convert854(SHOW_1V1_HEIGHT), categoryText);
                                canvas.drawText("on", canvas.getWidth()/8, convert854(SHOW_1V1_HEIGHT+75), choiceText);
                                canvas.drawText("off", canvas.getWidth()*3/8, convert854(SHOW_1V1_HEIGHT+75), choiceText);
                                if (getShow1v1().equals("on")) canvas.drawRect(convert854(20), convert854(SHOW_1V1_HEIGHT+30), canvas.getWidth()/4-convert854(20), convert854(SHOW_1V1_HEIGHT+90), boxPaint);
                                else if (getShow1v1().equals("off")) canvas.drawRect(canvas.getWidth()/4+convert854(20), convert854(SHOW_1V1_HEIGHT+30), canvas.getWidth()*2/4-convert854(20), convert854(SHOW_1V1_HEIGHT+90), boxPaint);

                                //back button
                                Icon backButton = new Icon(5, 270);
                                backButton.drawShape(canvas, 60, canvas.getHeight()-40, 60, getInvertColors().equals("on"));
                            } else if (menu.equals("mode")){
                                Paint modeText = newPaint(Color.BLACK);
                                modeText.setTextAlign(Paint.Align.CENTER);
                                modeText.setTextSize(canvas.getHeight()/4);

                                canvas.drawText("1P", canvas.getWidth()/2, canvas.getHeight()/4-(modeText.ascent()+modeText.descent())/2, modeText);
                                canvas.drawText("2P", canvas.getWidth()/2, canvas.getHeight()*3/4-(modeText.ascent()+modeText.descent())/2, modeText);

                                canvas.drawLine(canvas.getWidth()/10, canvas.getHeight()/2, canvas.getWidth()*9/10, canvas.getHeight()/2, modeText);
                            } else if (menu.equals("1P")) {
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
                                    speed = canvas.getHeight() / Math.max(2.5f - score / 30.f, 1) / getTargetFPS();
                                    rowPosition += speed;

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
                            } else if (menu.equals("2P")) {
                                if (!paused) {
                                    if (p1_ready && p2_ready) {
                                        //show current columns
                                        canvas.drawRect(p1_column * canvas.getWidth()/4, canvas.getHeight()/2, (p1_column + 1) * canvas.getWidth()/4, canvas.getHeight(),
                                                newPaint(getInvertColors().equals("off") ? Color.rgb(245,245,245) : Color.rgb(220,220,220)));
                                        canvas.drawRect((3-p2_column) * canvas.getWidth()/4, 0, (3-p2_column + 1) * canvas.getWidth()/4, canvas.getHeight()/2,
                                                newPaint(getInvertColors().equals("off") ? Color.rgb(245,245,245) : Color.rgb(220,220,220)));
                                        //dividing lines
                                        for (int i = 0; i < 3; i++) {
                                            float x = canvas.getWidth()/4 + i * canvas.getWidth()/4;
                                            canvas.drawLine(x, 0, x, canvas.getHeight(),
                                                    newPaint(getInvertColors().equals("off") ? Color.rgb(200,200,200) : Color.rgb(150,150,150)));
                                        }

                                        //display rows
                                        for (int i = 0; i < p1_row.length; i++) {
                                            p1_row[i].drawShape(canvas, canvas.getWidth()/8 + canvas.getWidth()/4 * i, rowPosition + canvas.getWidth()/8, canvas.getWidth()/4 / (float) Math.sqrt(2) - 10, getInvertColors().equals("on"));
                                        }
                                        flipScreen();
                                        for (int i = 0; i < p2_row.length; i++) {
                                            p2_row[i].drawShape(canvas, canvas.getWidth() / 8 + canvas.getWidth() / 4 * i, rowPosition + canvas.getWidth() / 8, canvas.getWidth() / 4 / (float) Math.sqrt(2) - 10, getInvertColors().equals("on"));
                                        }
                                        canvas.restore();

                                        //move rows up/down the canvas and adjust speed
                                        speed = canvas.getHeight() / 2 / Math.max(2.5f - score / 30.f, 1) / getTargetFPS();
                                        rowPosition += speed;

                                        //check if selected columns are correct
                                        if (rowPosition > canvas.getHeight()) {
                                            if (p1_correctColumn == p1_column && p2_correctColumn == p2_column) {
                                                score++;
                                                p1_correctColumn = (int) (Math.random() * 4);
                                                p1_row = generateRow(p1_correctColumn);
                                                p2_correctColumn = (int) (Math.random() * 4);
                                                p2_row = generateRow(p2_correctColumn);
                                            } else {
                                                gamesPlayed++;
                                                menu = "2P_transition";
                                                transitionFrames = 0;
                                            }
                                        }

                                        //middle bar
                                        canvas.drawRect(-5, canvas.getHeight()/2-canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2+canvas.getWidth()/8, newPaint(Color.WHITE));
                                        canvas.drawLine(-5, canvas.getHeight()/2-canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2-canvas.getWidth()/8, newPaint(Color.BLACK));
                                        canvas.drawLine(-5, canvas.getHeight()/2+canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2+canvas.getWidth()/8, newPaint(Color.BLACK));
                                        Paint scoreText = newPaint(Color.BLACK);
                                        scoreText.setTextAlign(Paint.Align.CENTER);
                                        scoreText.setTextSize(canvas.getWidth()/8);
                                        canvas.drawText(score+"", canvas.getWidth()/8, canvas.getHeight()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                        flipScreen();
                                        canvas.drawText(score+"", canvas.getWidth()/8, canvas.getHeight()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                        canvas.restore();
                                    } else {
                                        Paint readyText = newPaint(Color.BLACK);
                                        readyText.setTextAlign(Paint.Align.CENTER);
                                        readyText.setTextSize(convert854(30));

                                        Icon cancel = new Icon(23);

                                        if (p1_ready) canvas.drawText("Ready!", canvas.getWidth()/2, canvas.getHeight()*3/4, readyText);
                                        else {
                                            canvas.drawText("P1, tap here", canvas.getWidth()/2, canvas.getHeight()*3/4, readyText);
                                            canvas.drawText("when ready", canvas.getWidth()/2, canvas.getHeight()*3/4+convert854(30), readyText);
                                        }
                                        cancel.drawShape(canvas, 40, canvas.getHeight()-40, 30, getInvertColors().equals("on"));

                                        flipScreen();
                                        if (p2_ready) canvas.drawText("Ready!", canvas.getWidth()/2, canvas.getHeight()*3/4, readyText);
                                        else {
                                            canvas.drawText("P2, tap here", canvas.getWidth()/2, canvas.getHeight()*3/4, readyText);
                                            canvas.drawText("when ready", canvas.getWidth()/2, canvas.getHeight()*3/4+convert854(30), readyText);
                                        }
                                        cancel.drawShape(canvas, 40, canvas.getHeight()-40, 30, getInvertColors().equals("on"));
                                        canvas.restore();
                                    }
                                }
                            } else if (menu.equals("transition")) {
                                int alpha = 255 - (int)Math.max(255*(transitionFrames-1.5*getTargetFPS())/(0.5*getTargetFPS()), 0);

                                //show current column
                                canvas.drawRect(column * canvas.getWidth()/4, 0, (column + 1) * canvas.getWidth()/4, canvas.getHeight(),
                                        newPaint(getInvertColors().equals("off") ? Color.argb(alpha,245,245,245) : Color.argb(alpha,200,200,200)));//dividing lines
                                //dividing lines
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
                            } else if (menu.equals("2P_transition")) {
                                //columns fade during first half second
                                if (transitionFrames < 0.5*getTargetFPS()) {
                                    int alpha = (int) (255 - 255 * transitionFrames / (0.5*getTargetFPS()));

                                    //show current columns
                                    canvas.drawRect(p1_column * canvas.getWidth()/4, canvas.getHeight()/2, (p1_column + 1) * canvas.getWidth()/4, canvas.getHeight(),
                                            newPaint(getInvertColors().equals("off") ? Color.argb(alpha,245,245,245) : Color.argb(alpha,220,220,220)));
                                    canvas.drawRect((3-p2_column) * canvas.getWidth()/4, 0, (3-p2_column + 1) * canvas.getWidth()/4, canvas.getHeight()/2,
                                            newPaint(getInvertColors().equals("off") ? Color.argb(alpha,245,245,245) : Color.argb(alpha,220,220,220)));
                                    //dividing lines
                                    for (int i = 0; i < 3; i++) {
                                        float x = canvas.getWidth()/4 + i * canvas.getWidth()/4;
                                        canvas.drawLine(x, 0, x, canvas.getHeight(),
                                                newPaint(getInvertColors().equals("off") ? Color.argb(alpha,200,200,200) : Color.argb(alpha,150,150,150)));
                                    }
                                }

                                draw2PScores();
                                flipScreen();
                                draw2PScores();
                                canvas.restore();

                                //middle bar
                                int barAlpha = transitionFrames > 2.5*getTargetFPS() ? (int) (255*6 - 255 * transitionFrames / (0.5*getTargetFPS())) : 255;
                                canvas.drawRect(-5, canvas.getHeight()/2-canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2+canvas.getWidth()/8, newPaint(Color.argb(barAlpha,255,255,255)));
                                canvas.drawLine(-5, canvas.getHeight()/2-canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2-canvas.getWidth()/8, newPaint(Color.argb(barAlpha,0,0,0)));
                                canvas.drawLine(-5, canvas.getHeight()/2+canvas.getWidth()/8, canvas.getWidth()+5, canvas.getHeight()/2+canvas.getWidth()/8, newPaint(Color.argb(barAlpha,0,0,0)));
                                Paint scoreText = newPaint(Color.argb(barAlpha,0,0,0));
                                scoreText.setTextAlign(Paint.Align.CENTER);
                                scoreText.setTextSize(canvas.getWidth()/8);
                                canvas.drawText(score+"", canvas.getWidth()/8, canvas.getHeight()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                flipScreen();
                                canvas.drawText(score+"", canvas.getWidth()/8, canvas.getHeight()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                canvas.restore();

                                if (transitionFrames < 3*getTargetFPS()) transitionFrames++;
                                else {
                                    if (p1_correctColumn == p1_column) p1_score++;
                                    else if (p2_correctColumn == p2_column) p2_score++;
                                    menu = "2P";
                                    p1_ready = p2_ready = false;
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
                            } else if (menu.equals("2P_gameover")) {
                                Paint p = newPaint(Color.BLACK);
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(70);

                                //display winner
                                String winner = p1_score > p2_score ? "P1 wins!" : p2_score > p1_score ? "P2 wins!" : "It's a tie!";
                                canvas.drawText(winner, canvas.getWidth()/2, canvas.getHeight()/4, p);

                                //show final score
                                p.setTextSize(100);
                                canvas.drawText(p1_score+"", canvas.getWidth()/4, canvas.getHeight()/2, p);
                                canvas.drawText("-", canvas.getWidth()/2, canvas.getHeight()/2, p);
                                canvas.drawText(p2_score+"", canvas.getWidth()*3/4, canvas.getHeight()/2, p);

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
                    if (getShow1v1().equals("off")) {
                        menu = "1P";
                        frameCount = 0;
                        score = 0;
                        row = generateRow();
                    } else {
                        menu = "mode";
                    }
                }
                //how to play button
                else if (Y > convert854(667) && Y < convert854(757)) {
                    menu = "howtoplay";
                    tutorialFrames = 0;
                }
                //settings
                else if (X > canvas.getWidth() - 80 && Y < 80) {
                    previousMenu = menu;
                    menu = "settings";
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
                if (Y > convert854(TARGET_FPS_HEIGHT+30) && Y < convert854(TARGET_FPS_HEIGHT+90)) {
                    if (X < canvas.getWidth()/4) editor.putInt("target_fps", 30);
                    else if (X < canvas.getWidth()*2/4) editor.putInt("target_fps", 45);
                    else if (X < canvas.getWidth()*3/4) editor.putInt("target_fps", 60);
                    editor.apply();

                    nanosecondsPerFrame = (long)1e9 / getTargetFPS();
                    millisecondsPerFrame = (long)1e3 / getTargetFPS();
                } else if (Y > convert854(INVERT_COLORS_HEIGHT+30) && Y < convert854(INVERT_COLORS_HEIGHT+90)) {
                    if (X < canvas.getWidth()/4) editor.putString("invert_colors", "on");
                    else if (X < canvas.getWidth()*2/4) editor.putString("invert_colors", "off");
                    editor.apply();
                } else if (Y > convert854(SHOW_1V1_HEIGHT+30) && Y < convert854(SHOW_1V1_HEIGHT+90)) {
                    if (X < canvas.getWidth()/4) editor.putString("show_1v1", "on");
                    else if (X < canvas.getWidth()*2/4) editor.putString("show_1v1", "off");
                    editor.apply();
                }
            }
        } else if (menu.equals("mode")){
            if (action == MotionEvent.ACTION_UP) {
                if (Y < canvas.getHeight()/2) {
                    //singleplayer
                    menu = "1P";
                    frameCount = 0;
                    score = 0;
                    row = generateRow();
                } else {
                    //1v1
                    menu = "2P";
                    p1_score = p2_score = 0;
                    p1_ready = p2_ready = false;
                    gamesPlayed = 0;
                }
            }
        } else if (menu.equals("1P")) {
            column = (int) (X / (canvas.getWidth()/4));
        } else if (menu.equals("2P")) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                X = event.getX(i);
                Y = event.getY(i);
                if (p1_ready && p2_ready) {
                    if (Y > canvas.getHeight() / 2) p1_column = (int) (X / (canvas.getWidth() / 4));
                    else p2_column = 3 - (int) (X / (canvas.getWidth() / 4));
                } else {
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                        //if either player presses X
                        if ((X < 80 && Y > canvas.getHeight()-80) || (X > canvas.getWidth()-80 && Y < 80)) {
                            if (gamesPlayed == 0) menu = "start";
                            else {
                                menu = "2P_gameover";
                                gameoverFrames = 0;
                            }

                            return true;
                        }

                        //otherwise, ready
                        if (Y > canvas.getHeight() / 2) p1_ready = true;
                        else p2_ready = true;
                        if (p1_ready && p2_ready) {
                            frameCount = 0;
                            score = 0;
                            p1_correctColumn = (int) (Math.random() * 4);
                            p1_row = generateRow(p1_correctColumn);
                            p2_correctColumn = (int) (Math.random() * 4);
                            p2_row = generateRow(p2_correctColumn);
                        }
                    }
                }
            }
        } else if (menu.equals("gameover") || menu.equals("2P_gameover")) {
            if (action == MotionEvent.ACTION_UP) {
                if (X > canvas.getWidth() - 80 && Y < 80) {
                    previousMenu = menu;
                    menu = "settings";
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

    private void flipScreen() {
        canvas.save();
        canvas.rotate(180);
        canvas.translate(-canvas.getWidth(), -canvas.getHeight());
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

    private String getShow1v1() {
        return sharedPref.getString("show_1v1", "off");
    }

    private void draw2PScores() {
        Paint scoreText = newPaint(Color.BLACK);
        scoreText.setTextAlign(Paint.Align.CENTER);
        scoreText.setTextSize(canvas.getHeight()/8);
        float scoreHeight = canvas.getHeight()*3/4 - (scoreText.ascent() + scoreText.descent()) / 2;
        Paint playerText = newPaint(Color.BLACK);
        playerText.setTextAlign(Paint.Align.CENTER);
        playerText.setTextSize(canvas.getHeight()/20);
        float playerHeight = canvas.getHeight()*7/8 - (playerText.ascent() + playerText.descent()) / 2;

        int alpha;
        if (transitionFrames < 0.5*getTargetFPS()) alpha = (int) (255 * transitionFrames / (0.5*getTargetFPS()));
        else if (transitionFrames < 2.5*getTargetFPS()) alpha = 255;
        else alpha = (int) (255*6 - 255 * transitionFrames / (0.5*getTargetFPS()));

        //show previous score during first 1.5 seconds
        if (transitionFrames < 1.5*getTargetFPS()) {
            int scoreAlpha;
            if (transitionFrames < getTargetFPS()) scoreAlpha = 255;
            else scoreAlpha = (int) (255*3 - 255 * transitionFrames / (0.5*getTargetFPS()));

            scoreText.setAlpha(p1_correctColumn == p1_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText(p1_score+"", canvas.getWidth()/4, scoreHeight, scoreText);
            scoreText.setAlpha(alpha);
            canvas.drawText("-", canvas.getWidth()/2, scoreHeight, scoreText);
            scoreText.setAlpha(p2_correctColumn == p2_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText(p2_score+"", canvas.getWidth()*3/4, scoreHeight, scoreText);
        }
        //show resulting score during last 1.5 seconds
        else {
            int scoreAlpha;
            if (transitionFrames > 2*getTargetFPS()) scoreAlpha = 255;
            else scoreAlpha = (int) (-255*3 + 255 * transitionFrames / (0.5*getTargetFPS()));

            scoreText.setAlpha(p1_correctColumn == p1_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText((p1_correctColumn == p1_column ? p1_score + 1 : p1_score)+"", canvas.getWidth()/4, scoreHeight, scoreText);
            scoreText.setAlpha(alpha);
            canvas.drawText("-", canvas.getWidth()/2, scoreHeight, scoreText);
            scoreText.setAlpha(p2_correctColumn == p2_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText((p2_correctColumn == p2_column ? p2_score + 1 : p2_score)+"", canvas.getWidth()*3/4, scoreHeight, scoreText);
        }

        //show P1/P2
        playerText.setAlpha(alpha);
        canvas.drawText("P1", canvas.getWidth()/4, playerHeight, playerText);
        canvas.drawText("P2", canvas.getWidth()*3/4, playerHeight, playerText);
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
        if (menu.equals("1P")) {
            rowPosition = -canvas.getWidth() / 4;
            correctColumn = (int) (Math.random() * 4);
        } else if (menu.equals("2P")) {
            rowPosition = canvas.getHeight()/2 - canvas.getWidth()/8;
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

        int[][][] easyPairs = {{{0},{6}}, {{1},{7}}, {{4},{8}}, {{0},{1}}, {{1},{2}}, {{0},{2}},
                {{3},{23}}, {{2},{2,180}}, {{5},{5,180}}, {{18},{18,90}}, {{16},{18}}, {{15},{17}}};
        int[][][] mediumPairs = {{{14},{15}}, {{16},{17}}, {{5,90},{5,270}},
                {{14},{14,90}}, {{15},{15,90}}, {{9},{19}}, {{10},{20}}, {{11},{21}},
                {{12},{22}}, {{24},{25}}, {{24,90},{25,90}}};
        int[][][] hardPairs = {{{3,0,-1},{3,0,1}}, {{4,0,-1},{4,0,1}}, {{5,0,-1},{5,0,1}},
                {{8,0,-1},{8,0,1}}, {{13,0,-1},{13,0,1}}, {{14,0,-1},{14,0,1}}, {{15,0,-1},{15,0,1}},
                {{16,0,-1},{16,0,1}}, {{17,0,-1},{17,0,1}}, {{18,0,-1},{18,0,1}}, {{5,0,1},{5,180,1}}};
        int[][][] hardMirror = {{{9},{19}}, {{10},{20}}, {{11},{21}}, {{12},{22}}};

        //adjust icon rotational speed to selected fps
        int rotateSpeed = 316 / getTargetFPS();
        for (int pair = 0; pair < hardPairs.length; pair++)
            for (int single = 0; single < 2; single++)
                if (hardPairs[pair][single].length > 2) hardPairs[pair][single][2] *= rotateSpeed;

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

    //overloaded function used for 2P mode
    private Icon[] generateRow(int col) {
        correctColumn = col;
        return generateRow();
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
