package com.example.chrisx.oddoneout;

/**
 * Organized in order of priority:
 * @TODO more icons/pairs (perhaps split into "packs")
 * @TODO add purchasable themes (background/icon color)
 * @TODO make 2P game over screen look better
 * @TODO update tutorial to make it look better
 * ...
 * @TODO extreme?
 * @TODO global high scores?
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

    private String previousPack;

    private Background background;

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

    private int[] stars = new int[3];
    private int[] upcomingStars = new int[3];

    private int starCollectColumn;
    private long starCollectAnimation;

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
    private static final float SHOW_1V1_HEIGHT = 350;
    //shop
    private static final float BOX_WIDTH = 95;
    private static final float ICON_PACKS_HEIGHT = 175;
    private Pack[] packs = {new Pack("default"), new Pack("letter"), new Pack("fourths")};
    private static final float THEMES_HEIGHT = 375;
    private Theme[] themes = {new Theme(Color.WHITE, Color.BLACK),
            new Theme(Color.BLACK, Color.WHITE),
            new Theme(Color.rgb(25,149,193), Color.WHITE),
            new Theme(Color.rgb(38,92,0), Color.WHITE),
            new Theme(Color.rgb(82,54,52), Color.WHITE),
            new Theme(Color.rgb(216,65,47), Color.WHITE)
    };
    private static final float BACKGROUND_HEIGHT = 595;
    private Background[] backgrounds = {new Background("default"), new Background("circles")};

    //screen touches
    private float downX, downY;
    private float moveX, moveY;
    private boolean scrolled;

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

        background = new Background(getBackground());

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
                            //background
                            background.drawBackground(canvas, themes[getThemeID()], getTargetFPS());

                            if (menu.equals("start")) {
                                Paint title = newPaint(themes[getThemeID()].getC2());
                                title.setTextAlign(Paint.Align.CENTER);
                                title.setTextSize(convert854(150));
                                canvas.drawText("ODD", w()/2+Math.max(startAnimation-getTargetFPS()*6/3,0)/(getTargetFPS()*2/3f)*w(), convert854(225), title);
                                canvas.drawText("ONE", w()/2+Math.max(startAnimation-getTargetFPS()*4/3,0)/(getTargetFPS()*2/3f)*w(), convert854(355), title);
                                canvas.drawText("OUT", w()/2+Math.max(startAnimation-getTargetFPS()*2/3,0)/(getTargetFPS()*2/3f)*w(), convert854(485), title);

                                title.setTextSize(convert854(55));
                                canvas.drawText("start", w()/2-startAnimation/(getTargetFPS()*2/3f)*w(), convert854(632), title);
                                canvas.drawText("how to play", w()/2-startAnimation/(getTargetFPS()*2/3f)*w(), convert854(725), title);

                                //settings icon
                                drawGear(w()-40, 40, 20);
                                //shop
                                drawCart(40, 40, 20);

                                Paint cover = newPaint(themes[getThemeID()].getC1());
                                cover.setAlpha((int)(255*Math.min(1, startAnimation/(getTargetFPS()*2/3f))));
                                canvas.drawRect(18, 18, 62, 62, cover);
                                canvas.drawRect(w()-62, 18, w()-18, 62, cover);

                                if (startAnimation > 0) startAnimation--;
                            } else if (menu.equals("howtoplay")) {
                                float textSize = convert854(40);

                                Paint p = newPaint(themes[getThemeID()].getC2());
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
                                        canvas.drawText(txt[i], w()/2, textSize*2-10 + i*(textSize+5), p);
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
                                        canvas.drawText(txt[i], w()/2, textSize*2-10 + i*(textSize+5), p);
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
                                        canvas.drawText(txt[i], w()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                } else if (tutorialFrames == 3) {
                                    String[] txt = {
                                            "As for how to play",
                                            "the game, that's",
                                            "about it! Good luck :)"};
                                    for (int i = 0; i < txt.length; i++) {
                                        canvas.drawText(txt[i], w()/2, textSize*2-10 + i*(textSize+5), p);
                                    }
                                }

                                if (tutorialFrames < 3) canvas.drawText("Next", w()/2, h()-20, p);
                                else canvas.drawText("Menu", w()/2, h()-20, p);
                            } else if (menu.equals("settings")) {
                                Paint titleText = newPaint(themes[getThemeID()].getC2());
                                titleText.setTextAlign(Paint.Align.CENTER);
                                titleText.setTextSize(convert854(50));
                                canvas.drawText("settings", w()/2, convert854(75), titleText);

                                Paint categoryText = newPaint(themes[getThemeID()].getC2());
                                categoryText.setTextSize(convert854(40));
                                Paint choiceText = newPaint(themes[getThemeID()].getC2());
                                choiceText.setTextAlign(Paint.Align.CENTER);
                                choiceText.setTextSize(convert854(35));
                                Paint boxPaint = newPaint(themes[getThemeID()].getC2());
                                boxPaint.setStyle(Paint.Style.STROKE);
                                boxPaint.setStrokeWidth(convert854(2));

                                //change fps
                                canvas.drawText("target FPS:", convert854(20), convert854(TARGET_FPS_HEIGHT), categoryText);
                                canvas.drawText("30", w()/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                canvas.drawText("45", w()*3/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                canvas.drawText("60", w()*5/8, convert854(TARGET_FPS_HEIGHT+75), choiceText);
                                if (getTargetFPS() == 30) canvas.drawRect(convert854(20), convert854(TARGET_FPS_HEIGHT+30), w()/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);
                                else if (getTargetFPS() == 45) canvas.drawRect(w()/4+convert854(20), convert854(TARGET_FPS_HEIGHT+30), w()*2/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);
                                else if (getTargetFPS() == 60) canvas.drawRect(w()*2/4+convert854(20), convert854(TARGET_FPS_HEIGHT+30), w()*3/4-convert854(20), convert854(TARGET_FPS_HEIGHT+90), boxPaint);

                                //show 1v1 mode as an option
                                canvas.drawText("enable 2P mode:", convert854(20), convert854(SHOW_1V1_HEIGHT), categoryText);
                                canvas.drawText("on", w()/8, convert854(SHOW_1V1_HEIGHT+75), choiceText);
                                canvas.drawText("off", w()*3/8, convert854(SHOW_1V1_HEIGHT+75), choiceText);
                                if (getShow1v1().equals("on")) canvas.drawRect(convert854(20), convert854(SHOW_1V1_HEIGHT+30), w()/4-convert854(20), convert854(SHOW_1V1_HEIGHT+90), boxPaint);
                                else if (getShow1v1().equals("off")) canvas.drawRect(w()/4+convert854(20), convert854(SHOW_1V1_HEIGHT+30), w()*2/4-convert854(20), convert854(SHOW_1V1_HEIGHT+90), boxPaint);

                                //back button
                                Icon backButton = new Icon(9, 270);
                                backButton.drawShape(canvas, 60, h()-40, 60, themes[getThemeID()]);
                            } else if (menu.equals("shop")) {
                                Paint titleText = newPaint(themes[getThemeID()].getC2());
                                titleText.setTextAlign(Paint.Align.CENTER);
                                titleText.setTextSize(convert854(50));
                                canvas.drawText("shop", w()/2, convert854(75), titleText);

                                Paint categoryText = newPaint(themes[getThemeID()].getC2());
                                categoryText.setTextSize(convert854(40));
                                Paint border = newPaint(themes[getThemeID()].getC2());
                                border.setStyle(Paint.Style.STROKE);
                                border.setStrokeWidth(convert854(2));
                                Paint locked = newPaint(themes[getThemeID()].convertColor(Color.argb(175,255,255,255)));
                                Paint costText = newPaint(themes[getThemeID()].getC2());
                                costText.setTextAlign(Paint.Align.CENTER);

                                //packs
                                canvas.drawText("icon packs:", convert854(20), convert854(ICON_PACKS_HEIGHT), categoryText);
                                float boxWidth = convert854(BOX_WIDTH);
                                costText.setTextSize(boxWidth/3.5f);
                                for (int i = 0; i < packs.length; i++) {
                                    float lx = convert854(20) + i * (boxWidth + convert854(20));
                                    float ly = convert854(ICON_PACKS_HEIGHT + 30);
                                    if (getPack().equals(packs[i].getName())) canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, border);
                                    packs[i].drawPack(canvas, lx+boxWidth/2, ly+boxWidth/2, boxWidth-convert854(40), themes[getThemeID()]);

                                    //check if unlocked
                                    if (!ownsPack(packs[i].getName())) {
                                        canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, locked);
                                        canvas.drawText(packs[i].cost()+"", lx+boxWidth/2, ly+boxWidth*9/16, costText);
                                        drawStar(lx+boxWidth/2, ly+boxWidth*3/4, boxWidth/8);
                                    }
                                }

                                //themes
                                canvas.drawText("color themes:", convert854(20), convert854(THEMES_HEIGHT), categoryText);
                                for (int i = 0; i < themes.length; i++) {
                                    float lx = convert854(20) + i * (boxWidth + convert854(20)) - getThemeScroll();
                                    float ly = convert854(THEMES_HEIGHT + 30);
                                    if (getThemeID() == i) canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, border);
                                    themes[i].drawTheme(canvas, lx+boxWidth/2, ly+boxWidth/2, boxWidth-convert854(40));

                                    //check if unlocked
                                    if (!ownsTheme(i)) {
                                        canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, locked);
                                        canvas.drawText(themes[i].cost()+"", lx+boxWidth/2, ly+boxWidth*9/16, costText);
                                        drawStar(lx+boxWidth/2, ly+boxWidth*3/4, boxWidth/8);
                                    }
                                }
                                //scroll bar
                                float shownWidth = w() - convert854(40),
                                        totalWidth = themes.length * (boxWidth + convert854(20)) - convert854(20);
                                canvas.drawRect(convert854(20) + getThemeScroll() / totalWidth * shownWidth,
                                        convert854(THEMES_HEIGHT) + boxWidth + convert854(40),
                                        convert854(20) + (getThemeScroll()+shownWidth) / totalWidth * shownWidth,
                                        convert854(THEMES_HEIGHT) + boxWidth + convert854(50),
                                        newPaint(themes[getThemeID()].convertColor(Color.rgb(128,128,128))));

                                //background effects
                                canvas.drawText("background FX:", convert854(20), convert854(BACKGROUND_HEIGHT), categoryText);
                                for (int i = 0; i < backgrounds.length; i++) {
                                    float lx = convert854(20) + i * (boxWidth + convert854(20));
                                    float ly = convert854(BACKGROUND_HEIGHT + 30);
                                    if (getBackground().equals(backgrounds[i].getName())) canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, border);
                                    backgrounds[i].drawBackgroundIcon(canvas, lx+boxWidth/2, ly+boxWidth/2, boxWidth-convert854(40), themes[getThemeID()]);

                                    //check if unlocked
                                    if (!ownsBackground(backgrounds[i].getName())) {
                                        canvas.drawRect(lx, ly, lx+boxWidth, ly+boxWidth, locked);
                                        canvas.drawText(backgrounds[i].cost()+"", lx+boxWidth/2, ly+boxWidth*9/16, costText);
                                        drawStar(lx+boxWidth/2, ly+boxWidth*3/4, boxWidth/8);
                                    }
                                }

                                //back button
                                Icon backButton = new Icon(9, 270);
                                backButton.drawShape(canvas, 60, h()-40, 60, themes[getThemeID()]);

                                //show number of stars
                                drawStar(w()-40, h()-40, 20);
                                Paint starCount = newPaint(themes[getThemeID()].getC2());
                                starCount.setTextAlign(Paint.Align.RIGHT);
                                starCount.setTextSize(convert854(40));
                                canvas.drawText(getStars()+"", w()-70, h()-40-(starCount.ascent()+starCount.descent())/2, starCount);
                            } else if (menu.equals("mode")){
                                Paint modeText = newPaint(themes[getThemeID()].getC2());
                                modeText.setTextAlign(Paint.Align.CENTER);
                                modeText.setTextSize(h()/4);

                                canvas.drawText("1P", w()/2, h()/4-(modeText.ascent()+modeText.descent())/2, modeText);
                                canvas.drawText("2P", w()/2, h()*3/4-(modeText.ascent()+modeText.descent())/2, modeText);

                                modeText.setTextSize(h()/30);

                                canvas.drawText("choose mode", w()/2, h()/2-(modeText.ascent()+modeText.descent())/2, modeText);
                                float textWidth = modeText.measureText("choose mode");

                                canvas.drawLine(-5, h()/2, w()/2-textWidth/2-convert854(20), h()/2, modeText);
                                canvas.drawLine(w()/2+textWidth/2+convert854(20), h()/2, w()+5, h()/2, modeText);
                            } else if (menu.equals("1P")) {
                                if (!paused) {
                                    //show current column
                                    canvas.drawRect(column * w()/4, 0, (column + 1) * w()/4, h(),
                                            newPaint(themes[getThemeID()].convertColor(Color.argb(30,0,0,0))));
                                    //dividing lines
                                    for (int i = 0; i < 3; i++) {
                                        float x = w()/4 + i * w()/4;
                                        canvas.drawLine(x, 0, x, h(),
                                                newPaint(themes[getThemeID()].convertColor(Color.rgb(175,175,175))));
                                    }

                                    //show current score and high score
                                    Paint scoreTitle = newPaint(themes[getThemeID()].getC2());
                                    scoreTitle.setTextSize(convert854(20));
                                    scoreTitle.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText("score", 10, convert854(25), scoreTitle);
                                    scoreTitle.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText("high", w() - 10, convert854(25), scoreTitle);
                                    Paint scoreText = newPaint(themes[getThemeID()].getC2());
                                    scoreText.setTextSize(convert854(30));
                                    scoreText.setTextAlign(Paint.Align.LEFT);
                                    canvas.drawText(score+"", 10, convert854(60), scoreText);
                                    scoreText.setTextAlign(Paint.Align.RIGHT);
                                    canvas.drawText(getHighScore()+"", w() - 10, convert854(60), scoreText);

                                    //display row
                                    for (int i = 0; i < row.length; i++) {
                                        row[i].drawShape(canvas, w()/8 + w()/4 * i, rowPosition + w()/8, w()/4 / (float) Math.sqrt(2) - 10, themes[getThemeID()]);
                                    }

                                    //draw stars
                                    for (int i = 0; i < stars.length; i++) {
                                        drawStar(w()/8 + stars[i] * w()/4, rowPosition + w()/4 + (stars.length - i) * h()/(stars.length + 1), w()/16);
                                        drawStar(w()/8 + upcomingStars[i] * w()/4, rowPosition - (i + 1) * h()/(stars.length + 1), w()/16);
                                    }
                                    //if a star has recently been collected, show a small animation
                                    if (starCollectAnimation > 0) {
                                        if (starCollectAnimation > getTargetFPS() / 8) {
                                            //star rises back up, and then...
                                            float starHeight = h() - w()*5/16 + starCollectAnimation * w()*3/16 / (getTargetFPS()/8);
                                            float x = (float) starCollectAnimation / (getTargetFPS()/8);
                                            float starWidth = (-x*x + 4*x - 3) * (w()/16);
                                            drawStar(w()/8 + starCollectColumn * w()/4, starHeight, starWidth);
                                        } else {
                                            //explodes
                                            float x = (float) starCollectAnimation / (getTargetFPS()/8);
                                            float explosionWidth = (1 - x*x) * (w()/16);
                                            for (float angle = 0; angle < 1.9*Math.PI; angle += (2*Math.PI)/12) {
                                                canvas.drawCircle(w()/8 + starCollectColumn * w()/4 + explosionWidth*(float)Math.cos(angle), h() - w()/8 - explosionWidth*(float)Math.sin(angle), convert854(2), newPaint(themes[getThemeID()].getC2()));
                                            }
                                        }

                                        starCollectAnimation--;
                                    }

                                    //move row down the canvas and adjust speed
                                    speed = h() / Math.max(2.5f - score / 30.f, 1) / getTargetFPS();
                                    rowPosition += speed;

                                    //check if stars are collected
                                    for (int i = 0; i < stars.length; i++) {
                                        if (stars[i] >= 0) {
                                            float starHeight = rowPosition + w()/4 + (stars.length - i) * h()/(stars.length + 1) - w()/16;
                                            if (starHeight > h()) {
                                                if (stars[i] == column) {
                                                    editor.putInt("stars", getStars() + 1);
                                                    editor.apply();
                                                    starCollectColumn = stars[i];
                                                    starCollectAnimation = getTargetFPS() / 4;
                                                    stars[i] = -1;
                                                }
                                            }
                                        }
                                    }

                                    //check if selected column is correct
                                    if (rowPosition > h()) {
                                        if (correctColumn == column) {
                                            score++;
                                            row = generateRow();
                                        } else {
                                            menu = "transition";
                                            transitionFrames = 0;
                                            previousPack = getPack();
                                            if (score > getHighScore()) {
                                                isHighScore = true;
                                                previousHigh = getHighScore();
                                                editor.putInt(getPack()+"_high_score", (int) score);
                                                editor.apply();
                                            } else isHighScore = false;
                                        }
                                    }
                                }
                            } else if (menu.equals("2P")) {
                                if (!paused) {
                                    if (p1_ready && p2_ready) {
                                        //show current columns
                                        canvas.drawRect(p1_column * w()/4, h()/2, (p1_column + 1) * w()/4, h(),
                                                newPaint(themes[getThemeID()].convertColor(Color.argb(30,0,0,0))));
                                        canvas.drawRect((3-p2_column) * w()/4, 0, (3-p2_column + 1) * w()/4, h()/2,
                                                newPaint(themes[getThemeID()].convertColor(Color.argb(30,0,0,0))));
                                        //dividing lines
                                        for (int i = 0; i < 3; i++) {
                                            float x = w()/4 + i * w()/4;
                                            canvas.drawLine(x, 0, x, h(),
                                                    newPaint(themes[getThemeID()].convertColor(Color.rgb(175,175,175))));
                                        }

                                        //display rows
                                        for (int i = 0; i < p1_row.length; i++) {
                                            p1_row[i].drawShape(canvas, w()/8 + w()/4 * i, rowPosition + w()/8, w()/4 / (float) Math.sqrt(2) - 10, themes[getThemeID()]);
                                        }
                                        flipScreen();
                                        for (int i = 0; i < p2_row.length; i++) {
                                            p2_row[i].drawShape(canvas, w() / 8 + w() / 4 * i, rowPosition + w() / 8, w() / 4 / (float) Math.sqrt(2) - 10, themes[getThemeID()]);
                                        }
                                        canvas.restore();

                                        //move rows up/down the canvas and adjust speed
                                        speed = h() / 2 / Math.max(2.5f - score / 30.f, 1) / getTargetFPS();
                                        rowPosition += speed;

                                        //check if selected columns are correct
                                        if (rowPosition > h()) {
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
                                        canvas.drawRect(-5, h()/2-w()/8, w()+5, h()/2+w()/8, newPaint(themes[getThemeID()].getC1()));
                                        canvas.drawLine(-5, h()/2-w()/8, w()+5, h()/2-w()/8, newPaint(themes[getThemeID()].getC2()));
                                        canvas.drawLine(-5, h()/2+w()/8, w()+5, h()/2+w()/8, newPaint(themes[getThemeID()].getC2()));
                                        Paint scoreText = newPaint(themes[getThemeID()].getC2());
                                        scoreText.setTextAlign(Paint.Align.CENTER);
                                        scoreText.setTextSize(w()/8);
                                        canvas.drawText(score+"", w()/8, h()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                        flipScreen();
                                        canvas.drawText(score+"", w()/8, h()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                        canvas.restore();
                                    } else {
                                        Paint readyText = newPaint(themes[getThemeID()].getC2());
                                        readyText.setTextAlign(Paint.Align.CENTER);
                                        readyText.setTextSize(convert854(30));

                                        Icon cancel = new Icon(8);

                                        if (p1_ready) canvas.drawText("Ready!", w()/2, h()*3/4, readyText);
                                        else {
                                            canvas.drawText("P1, tap here", w()/2, h()*3/4, readyText);
                                            canvas.drawText("when ready", w()/2, h()*3/4+convert854(30), readyText);
                                        }
                                        cancel.drawShape(canvas, 40, h()-40, 30, themes[getThemeID()]);

                                        flipScreen();
                                        if (p2_ready) canvas.drawText("Ready!", w()/2, h()*3/4, readyText);
                                        else {
                                            canvas.drawText("P2, tap here", w()/2, h()*3/4, readyText);
                                            canvas.drawText("when ready", w()/2, h()*3/4+convert854(30), readyText);
                                        }
                                        cancel.drawShape(canvas, 40, h()-40, 30, themes[getThemeID()]);
                                        canvas.restore();
                                    }
                                }
                            } else if (menu.equals("transition")) {
                                int alpha = 255 - (int)Math.max(255*(transitionFrames-1.5*getTargetFPS())/(0.5*getTargetFPS()), 0);

                                //show current column
                                canvas.drawRect(column * w()/4, 0, (column + 1) * w()/4, h(),
                                        newPaint(themes[getThemeID()].convertColor(Color.argb(alpha*30/255,0,0,0))));
                                //dividing lines
                                for (int i = 0; i < 3; i++) {
                                    float x = w()/4 + i * w()/4;
                                    canvas.drawLine(x, 0, x, h(),
                                            newPaint(themes[getThemeID()].convertColor(Color.argb(alpha,175,175,175))));
                                }

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, w()/8+w()/4*i, rowPosition+w()/8, w()/4/(float)Math.sqrt(2)-10, themes[getThemeID()]);
                                }
                                //box the correct column
                                Paint box = newPaint(themes[getThemeID()].getC2());
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(w()/150);
                                box.setAlpha(255-alpha);
                                canvas.drawRect(correctColumn*w()/4, h()-w()/4, (correctColumn+1)*w()/4, h(), box);

                                //move row back up to visible screen
                                if (rowPosition > h()-w()/4) {
                                    rowPosition -= h()/3/getTargetFPS();
                                    rowPosition = Math.max(rowPosition, h()-w()/4);
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
                                    canvas.drawRect(p1_column * w()/4, h()/2, (p1_column + 1) * w()/4, h(),
                                            newPaint(themes[getThemeID()].convertColor(Color.argb(alpha*30/255,0,0,0))));
                                    canvas.drawRect((3-p2_column) * w()/4, 0, (3-p2_column + 1) * w()/4, h()/2,
                                            newPaint(themes[getThemeID()].convertColor(Color.argb(alpha*30/255,0,0,0))));
                                    //dividing lines
                                    for (int i = 0; i < 3; i++) {
                                        float x = w()/4 + i * w()/4;
                                        canvas.drawLine(x, 0, x, h(),
                                                newPaint(themes[getThemeID()].convertColor(Color.argb(alpha,150,150,150))));
                                    }
                                }

                                draw2PScores();
                                flipScreen();
                                draw2PScores();
                                canvas.restore();

                                //middle bar
                                int barAlpha = transitionFrames > 2.5*getTargetFPS() ? (int) (255*6 - 255 * transitionFrames / (0.5*getTargetFPS())) : 255;
                                canvas.drawRect(-5, h()/2-w()/8, w()+5, h()/2+w()/8, newPaint(themes[getThemeID()].convertColor(Color.argb(barAlpha,255,255,255))));
                                canvas.drawLine(-5, h()/2-w()/8, w()+5, h()/2-w()/8, newPaint(themes[getThemeID()].convertColor(Color.argb(barAlpha,0,0,0))));
                                canvas.drawLine(-5, h()/2+w()/8, w()+5, h()/2+w()/8, newPaint(themes[getThemeID()].convertColor(Color.argb(barAlpha,0,0,0))));
                                Paint scoreText = newPaint(themes[getThemeID()].convertColor(Color.argb(barAlpha,0,0,0)));
                                scoreText.setTextAlign(Paint.Align.CENTER);
                                scoreText.setTextSize(w()/8);
                                canvas.drawText(score+"", w()/8, h()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                flipScreen();
                                canvas.drawText(score+"", w()/8, h()/2-(scoreText.ascent()+scoreText.descent())/2, scoreText);
                                canvas.restore();

                                if (transitionFrames < 3*getTargetFPS()) transitionFrames++;
                                else {
                                    if (p1_correctColumn == p1_column) p1_score++;
                                    else if (p2_correctColumn == p2_column) p2_score++;
                                    menu = "2P";
                                    p1_ready = p2_ready = false;
                                }
                            } else if (menu.equals("gameover")) {
                                Paint p = newPaint(themes[getThemeID()].getC2());
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(30);

                                //new high score?
                                if (isHighScore) {
                                    Paint bannerText = newPaint(themes[getThemeID()].getC2());
                                    bannerText.setTextAlign(Paint.Align.CENTER);
                                    bannerText.setTextSize(50);
                                    canvas.drawText("NEW HIGH", w()/2, h()/4-25, bannerText);
                                    canvas.drawText("SCORE!", w()/2, h()/4+25, bannerText);

                                    Paint border = newPaint(themes[getThemeID()].getC2());
                                    for (int i = -100; i < w()+100; i += 50) {
                                        canvas.drawCircle(i-((float)gameoverFrames/getTargetFPS()*100%50), h()/4-80, 5, border);
                                        canvas.drawCircle(i+((float)gameoverFrames/getTargetFPS()*100%50), h()/4+45, 5, border);
                                    }
                                }

                                //final score/high score
                                canvas.drawText("You scored", w()/2, h()/2-75, p);
                                if (isHighScore) canvas.drawText("Previous high", w()/2, h()/2+40, p);
                                else canvas.drawText("High score", w()/2, h()/2+40, p);
                                p.setTextSize(70);
                                canvas.drawText(score+"", w()/2, h()/2-5, p);
                                if (isHighScore) canvas.drawText(previousHigh+"", w()/2, h()/2+110, p);
                                else canvas.drawText(getHighScore(previousPack)+"", w()/2, h()/2+110, p);

                                p.setTextSize(30);
                                p.setAlpha((int)(255*Math.abs(Math.sin((float)gameoverFrames/getTargetFPS()*60*2/180*Math.PI))));
                                canvas.drawText("tap anywhere", w()/2, h()*3/4, p);
                                canvas.drawText("to continue", w()/2, h()*3/4+30, p);

                                //settings
                                drawGear(w()-40, 40, 20);
                                //shop
                                drawCart(40, 40, 20);

                                //fade-in effect (for 1 sec)
                                int alpha = (int) (255 - 255f * Math.min(gameoverFrames, getTargetFPS()) / getTargetFPS());
                                canvas.drawRect(-5, -5, w()+5, h()+5, newPaint(themes[getThemeID()].convertColor(Color.argb(alpha,255,255,255))));

                                //display row
                                for (int i = 0; i < row.length; i++) {
                                    row[i].drawShape(canvas, w()/8+w()/4*i, rowPosition+w()/8, w()/4/(float)Math.sqrt(2)-10, themes[getThemeID()]);
                                }
                                //box the correct column
                                Paint box = newPaint(themes[getThemeID()].getC2());
                                box.setStyle(Paint.Style.STROKE);
                                box.setStrokeWidth(3);
                                canvas.drawRect(correctColumn*w()/4, h()-w()/4, (correctColumn+1)*w()/4, h(), box);

                                gameoverFrames++;
                            } else if (menu.equals("2P_gameover")) {
                                Paint p = newPaint(themes[getThemeID()].getC2());
                                p.setTextAlign(Paint.Align.CENTER);
                                p.setTextSize(70);

                                //display winner
                                String winner = p1_score > p2_score ? "P1 wins!" : p2_score > p1_score ? "P2 wins!" : "It's a tie!";
                                canvas.drawText(winner, w()/2, h()/4, p);

                                //show final score
                                p.setTextSize(100);
                                canvas.drawText(p1_score+"", w()/4, h()/2, p);
                                canvas.drawText("-", w()/2, h()/2, p);
                                canvas.drawText(p2_score+"", w()*3/4, h()/2, p);

                                p.setTextSize(30);
                                p.setAlpha((int)(255*Math.abs(Math.sin((float)gameoverFrames/getTargetFPS()*60*2/180*Math.PI))));
                                canvas.drawText("tap anywhere", w()/2, h()*3/4, p);
                                canvas.drawText("to continue", w()/2, h()*3/4+30, p);

                                //settings
                                drawGear(w()-40, 40, 20);
                                //shop
                                drawCart(40, 40, 20);

                                //fade-in effect (for 1 sec)
                                int alpha = (int) (255 - 255f * Math.min(gameoverFrames, getTargetFPS()) / getTargetFPS());
                                canvas.drawRect(-5, -5, w()+5, h()+5, newPaint(themes[getThemeID()].convertColor(Color.argb(alpha,255,255,255))));

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
    public void onBackPressed() {
        if (menu.equals("settings") || menu.equals("shop")) {
            menu = previousMenu;
        } else if (menu.equals("howtoplay")) {
            if (tutorialFrames > 0) tutorialFrames--;
        }
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
                        upcomingStars[0] = upcomingStars[1] = upcomingStars[2] = -1;
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
                else if (X > w() - 80 && Y < 80) {
                    previousMenu = menu;
                    menu = "settings";
                }
                //shop
                else if (X < 80 && Y < 80) {
                    previousMenu = menu;
                    menu = "shop";
                }
            }
        } else if (menu.equals("howtoplay")) {
            if (action == MotionEvent.ACTION_UP) {
                if (Y > h() - 80) {
                    if (tutorialFrames < 3) tutorialFrames++;
                    else menu = "start";
                }
            }
        } else if (menu.equals("settings")) {
            if (action == MotionEvent.ACTION_UP) {
                if (X < 120 && Y > h() - 80) {
                    menu = previousMenu;
                }
            }
            if (action == MotionEvent.ACTION_DOWN) {
                if (Y > convert854(TARGET_FPS_HEIGHT+30) && Y < convert854(TARGET_FPS_HEIGHT+90)) {
                    if (X < w()/4) editor.putInt("target_fps", 30);
                    else if (X < w()*2/4) editor.putInt("target_fps", 45);
                    else if (X < w()*3/4) editor.putInt("target_fps", 60);
                    editor.apply();

                    nanosecondsPerFrame = (long)1e9 / getTargetFPS();
                    millisecondsPerFrame = (long)1e3 / getTargetFPS();
                } else if (Y > convert854(SHOW_1V1_HEIGHT+30) && Y < convert854(SHOW_1V1_HEIGHT+90)) {
                    if (X < w()/4) editor.putString("show_1v1", "on");
                    else if (X < w()*2/4) editor.putString("show_1v1", "off");
                    editor.apply();
                }
            }
        } else if (menu.equals("shop")) {
            if (action == MotionEvent.ACTION_DOWN) {
                downX = moveX = X;
                downY = moveY = Y;
                scrolled = false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                float boxWidth = convert854(BOX_WIDTH);
                float ly = convert854(THEMES_HEIGHT + 30);

                if (downY > ly && downY < ly+boxWidth) {
                    float shownWidth = w() - convert854(40),
                            totalWidth = themes.length * (boxWidth + convert854(20)) - convert854(20);
                    float dx = moveX - X;

                    if (getThemeScroll() + dx < 0) {
                        editor.putFloat("theme_scroll", 0);
                    } else if (getThemeScroll() + dx > totalWidth - shownWidth) {
                        editor.putFloat("theme_scroll", totalWidth - shownWidth);
                    } else {
                        editor.putFloat("theme_scroll", getThemeScroll() + dx);
                    }
                    editor.apply();
                }

                moveX = X;
                moveY = Y;
                if (Math.abs(X - downX) + Math.abs(Y - downY) > convert854(40)) scrolled = true;
            } else if (action == MotionEvent.ACTION_UP) {
                if (X < 120 && Y > h() - 80) {
                    menu = previousMenu;
                } else {
                    if (!scrolled) {
                        float boxWidth = convert854(BOX_WIDTH);
                        for (int i = 0; i < packs.length; i++) {
                            float lx = convert854(20) + i * (boxWidth + convert854(20));
                            float ly = convert854(ICON_PACKS_HEIGHT + 30);

                            if (X > lx && X < lx + boxWidth && Y > ly && Y < ly + boxWidth) {
                                if (!ownsPack(packs[i].getName())) {
                                    if (getStars() >= packs[i].cost()) {
                                        editor.putBoolean("owns_pack_" + packs[i].getName(), true);
                                        editor.putInt("stars", getStars() - packs[i].cost());
                                        editor.apply();
                                    }
                                } else {
                                    editor.putString("pack", packs[i].getName());
                                    editor.apply();
                                }
                            }
                        }
                        for (int i = 0; i < themes.length; i++) {
                            float lx = convert854(20) + i * (boxWidth + convert854(20)) - getThemeScroll();
                            float ly = convert854(THEMES_HEIGHT + 30);

                            if (X > lx && X < lx + boxWidth && Y > ly && Y < ly + boxWidth) {
                                if (!ownsTheme(i)) {
                                    if (getStars() >= themes[i].cost()) {
                                        editor.putBoolean("owns_theme_" + i, true);
                                        editor.putInt("stars", getStars() - themes[i].cost());
                                        editor.apply();
                                    }
                                } else {
                                    editor.putInt("theme", i);
                                    editor.apply();
                                }
                            }
                        }
                        for (int i = 0; i < backgrounds.length; i++) {
                            float lx = convert854(20) + i * (boxWidth + convert854(20));
                            float ly = convert854(BACKGROUND_HEIGHT + 30);

                            if (X > lx && X < lx + boxWidth && Y > ly && Y < ly + boxWidth) {
                                if (!ownsBackground(backgrounds[i].getName())) {
                                    if (getStars() >= backgrounds[i].cost()) {
                                        editor.putBoolean("owns_background_" + backgrounds[i].getName(), true);
                                        editor.putInt("stars", getStars() - backgrounds[i].cost());
                                        editor.apply();
                                    }
                                } else {
                                    if (!backgrounds[i].getName().equals(getBackground())) {
                                        editor.putString("background", backgrounds[i].getName());
                                        editor.apply();
                                        background = new Background(backgrounds[i].getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (menu.equals("mode")){
            if (action == MotionEvent.ACTION_UP) {
                if (Y < h()/2) {
                    //singleplayer
                    menu = "1P";
                    frameCount = 0;
                    score = 0;
                    upcomingStars[0] = upcomingStars[1] = upcomingStars[2] = -1;
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
            column = (int) (X / (w()/4));
        } else if (menu.equals("2P")) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                X = event.getX(i);
                Y = event.getY(i);
                if (p1_ready && p2_ready) {
                    if (Y > h() / 2) p1_column = (int) (X / (w() / 4));
                    else p2_column = 3 - (int) (X / (w() / 4));
                } else {
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                        //if either player presses X
                        if ((X < 80 && Y > h()-80) || (X > w()-80 && Y < 80)) {
                            if (gamesPlayed == 0) menu = "start";
                            else {
                                menu = "2P_gameover";
                                gameoverFrames = 0;
                            }

                            return true;
                        }

                        //otherwise, ready
                        if (Y > h() / 2) p1_ready = true;
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
            if (action == MotionEvent.ACTION_UP && gameoverFrames > getTargetFPS()) {
                if (X > w() - 80 && Y < 80) {
                    //settings
                    previousMenu = menu;
                    menu = "settings";
                } else if (X < 80 && Y < 80) {
                    //shop
                    previousMenu = menu;
                    menu = "shop";
                } else menu = "start";
            }
        }

        return true;
    }

    //shorthand for w() and h()
    private float w() {
        return canvas.getWidth();
    }
    private float h() {
        return canvas.getHeight();
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(spinnaker);

        return p;
    }

    private float convert854(float f) {
        return h() / (854 / f);
    }

    private void flipScreen() {
        canvas.save();
        canvas.rotate(180);
        canvas.translate(-w(), -h());
    }

    private int getHighScore() {
        return sharedPref.getInt(getPack()+"_high_score", 0);
    }

    private int getHighScore(String p) {
        return sharedPref.getInt(p+"_high_score", 0);
    }

    private int getStars() {
        return sharedPref.getInt("stars", 0);
    }
    
    private int getTargetFPS() {
        return sharedPref.getInt("target_fps", 60);
    }

    private String getShow1v1() {
        return sharedPref.getString("show_1v1", "off");
    }

    private String getPack() {
        return sharedPref.getString("pack", "default");
    }

    private int getThemeID() {
        return sharedPref.getInt("theme", 0);
    }

    private String getBackground() {
        return sharedPref.getString("background", "default");
    }

    private float getThemeScroll() {
        return sharedPref.getFloat("theme_scroll", 0);
    }

    private boolean ownsPack(String p) {
        return p.equals("default") || sharedPref.getBoolean("owns_pack_"+p, false);
    }

    private boolean ownsTheme(int id) {
        return id == 0 || sharedPref.getBoolean("owns_theme_"+id, false);
    }

    private boolean ownsBackground(String b) {
        return b.equals("default") || sharedPref.getBoolean("owns_background_"+b, false);
    }

    private void draw2PScores() {
        Paint scoreText = newPaint(themes[getThemeID()].getC2());
        scoreText.setTextAlign(Paint.Align.CENTER);
        scoreText.setTextSize(h()/8);
        float scoreHeight = h()*3/4 - (scoreText.ascent() + scoreText.descent()) / 2;
        Paint playerText = newPaint(themes[getThemeID()].getC2());
        playerText.setTextAlign(Paint.Align.CENTER);
        playerText.setTextSize(h()/20);
        float playerHeight = h()*7/8 - (playerText.ascent() + playerText.descent()) / 2;

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
            canvas.drawText(p1_score+"", w()/4, scoreHeight, scoreText);
            scoreText.setAlpha(alpha);
            canvas.drawText("-", w()/2, scoreHeight, scoreText);
            scoreText.setAlpha(p2_correctColumn == p2_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText(p2_score+"", w()*3/4, scoreHeight, scoreText);
        }
        //show resulting score during last 1.5 seconds
        else {
            int scoreAlpha;
            if (transitionFrames > 2*getTargetFPS()) scoreAlpha = 255;
            else scoreAlpha = (int) (-255*3 + 255 * transitionFrames / (0.5*getTargetFPS()));

            scoreText.setAlpha(p1_correctColumn == p1_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText((p1_correctColumn == p1_column ? p1_score + 1 : p1_score)+"", w()/4, scoreHeight, scoreText);
            scoreText.setAlpha(alpha);
            canvas.drawText("-", w()/2, scoreHeight, scoreText);
            scoreText.setAlpha(p2_correctColumn == p2_column ? Math.min(scoreAlpha, alpha) : alpha);
            canvas.drawText((p2_correctColumn == p2_column ? p2_score + 1 : p2_score)+"", w()*3/4, scoreHeight, scoreText);
        }

        //show P1/P2
        playerText.setAlpha(alpha);
        canvas.drawText("P1", w()/4, playerHeight, playerText);
        canvas.drawText("P2", w()*3/4, playerHeight, playerText);
    }

    private void drawGear(float x, float y, float w) {
        Paint p = newPaint(themes[getThemeID()].getC2());
        p.setStrokeWidth(convert854(2));
        p.setStyle(Paint.Style.STROKE);

        for (float angle = 0; angle < 5.5/3*Math.PI; angle += Math.PI/3) {
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

    private void drawStar(float x, float y, float w) {
        Paint p = newPaint(themes[getThemeID()].getC2());
        p.setStrokeWidth(convert854(2));

        for (float angle = (float)Math.PI/2; angle < 9*Math.PI/4; angle += 2*Math.PI/5) {
            canvas.drawLine(x+w*(float)Math.cos(angle), y-w*(float)Math.sin(angle),
                    x+w*3/5*(float)Math.cos(angle+Math.PI/5), y-w*3/5*(float)Math.sin(angle+Math.PI/5), p);
            canvas.drawLine(x+w*3/5*(float)Math.cos(angle+Math.PI/5), y-w*3/5*(float)Math.sin(angle+Math.PI/5),
                    x+w*(float)Math.cos(angle+2*Math.PI/5), y-w*(float)Math.sin(angle+2*Math.PI/5), p);
        }
    }

    private void drawCart(float x, float y, float w) {
        Paint p = newPaint(themes[getThemeID()].getC2());
        p.setStrokeWidth(convert854(2));
        p.setStyle(Paint.Style.STROKE);

        canvas.drawLine(x-w, y-w/2, x-w*.6f, y-w/2, p);
        canvas.drawLine(x-w*.6f, y-w/2, x-w*.2f, y+w/3, p);
        canvas.drawLine(x-w*.2f, y+w/3, x+w*.6f, y+w/3, p);
        canvas.drawLine(x+w*.6f, y+w/3, x+w, y-w/3, p);
        canvas.drawLine(x+w, y-w/3, x-w*.5f, y-w/3, p);
        canvas.drawCircle(x-w*.2f, y+w*2/3, w/6, p);
        canvas.drawCircle(x+w*.6f, y+w*2/3, w/6, p);
    }

    private Icon[] generateRow() {
        if (menu.equals("1P")) {
            rowPosition = -w() / 4;
            correctColumn = (int) (Math.random() * 4);

            for (int i = 0; i < stars.length; i++) {
                stars[i] = upcomingStars[i];
                if (Math.random() < (score + 6)/75.) upcomingStars[i] = (int) (Math.random() * 4);
                else upcomingStars[i] = -1;
            }
        } else if (menu.equals("2P")) {
            rowPosition = h()/2 - w()/8;
        }

        Pack activePack = new Pack(getPack());
        int[][][] easyPairs = activePack.getEasyPairs(),
                mediumPairs = activePack.getMediumPairs(),
                hardPairs = activePack.getHardPairs(),
                hardMirror = activePack.getHardMirror();

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

        if (getPack().equals("fourths")) {
            int angle = ((int)(Math.random()*4)) * 90;
            for (Icon i : output) i.rotate(angle);
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
