package com.example.john.heat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Thread to update Heat Surface View
 */
class HeatThread extends Thread {

    /**
     * Number of rectangles in heatMap height
     */
    static final int nRectHeight = 100;

    /**
     * Number of rectangles in heatMap width
     */
    static final int nRectWidth = 100;

    /**
     * Handler for thread
     */
    Handler handler;

    /**
     * Create heat map
     */
    float [][] heatMap;

    /**
     * Set canvas height
     */
    int height = 100;

    /**
     * Set canvas width
     */
    int width;

    /**
     * Brush size of touch
     */
    private float brushSize;

    /**
     * Strength of brush (W heat added
     */
    private float brushStrength;

    /**
     * Context of thread
     */
    private Context ctx;

    /**
     * Color ff cold
     */
    private int heatCold;

    /**
     * Color if hot
     */
    private int heatHot;

    /**
     * thermal conductivity of material
     */
    private float heatK;

    /**
     * Surface holder
     */
    private SurfaceHolder sh;

    /**
     * Maximum heat of system (K)
     */
    private float heatMax = 100;  // (kelvin)

    /**
     * Whether thread is running
     */
    private boolean isRunning = true;

    /**
     * Holds current Motion Event
     */
    private MotionEvent motionEvent;

    /**
     * Initialize Heat Thread
     * @param sHolder holder
     * @param context context
     * @param height Height of screen
     * @param width Width of screen
     */
    HeatThread(SurfaceHolder sHolder, Context context, int height, int width) {
        init(sHolder, context, height, width);  // initialize
    }

    /**
     * Initialize heat thread
     * @param sHolder surface holder
     * @param context context
     * @param height height of surface
     * @param width width of surface
     */
    private void init(SurfaceHolder sHolder, Context context, int height, int width) {
        // Assign variables
        sh = sHolder;  // set holder
        handler = new Handler(Looper.getMainLooper()) {
            /**
             * Handle messages
             * @param message Message to handle
             */
            @Override
            public void handleMessage(Message message) {
                if (message.what == HeatSurfaceView.TASK_MOTION_EVENT) {
                    motionEvent = (MotionEvent) message.obj;  // get motion event
                    Log.d(HeatActivity.LOG_DEBUG,
                            String.format("Received Event: Action: %d, X: %f, Y: %f.",
                                    motionEvent.getAction(), motionEvent.getX(),
                                    motionEvent.getY()));
                }
                if (message.what == HeatSurfaceView.TASK_CLEAR_MAP) {
                    clearMap();  // clear map
                }
            }
        };  // Create handler
        ctx = context;  // set context
        this.height = height;  // set height
        this.width = width;  // set width)

        // Create heat map
        heatMap = new float[nRectWidth][nRectHeight];

        // Load preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                HeatActivity.SAVED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        brushSize = sharedPreferences.getFloat(HeatActivity.SAVED_BRUSH_SIZE,
                SettingsActivity.defBrushSize);  // get brush size
        brushStrength = sharedPreferences.getFloat(HeatActivity.SAVED_BRUSH_STRENGTH,
                SettingsActivity.defBrushStrength);  // set brush strength
        heatCold = sharedPreferences.getInt(HeatActivity.SAVED_COLD_COLOR,
                SettingsActivity.defHeatCold);  // set cold color
        heatHot = sharedPreferences.getInt(HeatActivity.SAVED_HOT_COLOR,
                SettingsActivity.defHeatHot);  // set hot color
        heatK = sharedPreferences.getFloat(HeatActivity.SAVED_HEAT_K,
                SettingsActivity.defHeatK);  // set heat k value
    }

    /**
     * Clears heat map
     */
    private void clearMap() {
        Log.d(HeatActivity.LOG_DEBUG, "Cleared Heat Map");  // write clear
        heatMap = new float[nRectWidth][nRectHeight];  // clear map
    }

    /**
     * Draw objects to canvas
     * @param canvas Canvas to draw to
     */
    private void doDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);  // create paint
        paint.setColor(Color.BLUE);  // set color
        paint.setStyle(Paint.Style.FILL);

        // Draw background
        canvas.drawColor(Color.BLACK);  // Draw background

        // Calculate rectangle size
        float rectH = getRectH();  // get rectangle height
        float rectW = getRectW();  // get rectangle width

        // Get cold values
        int coldA = Color.alpha(heatCold);
        int coldR = Color.red(heatCold);
        int coldG = Color.green(heatCold);
        int coldB = Color.blue(heatCold);

        // Get hot values
        int hotA = Color.alpha(heatHot);
        int hotR = Color.red(heatHot);
        int hotG = Color.green(heatHot);
        int hotB = Color.blue(heatHot);

        // Calculate difference in colors
        int diffA = hotA - coldA;
        int diffR = hotR - coldR;
        int diffG = hotG - coldG;
        int diffB = hotB - coldB;

        // Draw heat
        for (int i = 0; i < heatMap.length; i++) {  // cycle through columns
            for (int j = 0; j < heatMap[0].length; j++) {  // cycle through rows
                float value = heatMap[i][j] / heatMax;
                // Calculate color
                int heatColor = Color.argb(
                        (int) (diffA * value + coldA),
                        (int) (diffR * value + coldR),
                        (int) (diffG * value + coldG),
                        (int) (diffB * value + coldB));

                // Set paint
                paint.setColor(heatColor);  // set color

                // Draw
                canvas.drawRect(i * rectW, j * rectH,
                        (i + 1) * rectW, (j + 1) * rectH,
                        paint);
            }

        }
    }

    /**
     * Call on screen touch
     */
    private void doTouch() {
        float dispX = -12f;  // displacement for x
        float dispY = -134f;  // displacement for y

        if (motionEvent != null) {  // if event
            int action = motionEvent.getAction();  // get action
            if (action == MotionEvent.ACTION_DOWN ||
                    action == MotionEvent.ACTION_MOVE) {  // if down
                int x = (int)((motionEvent.getX() + dispX) / getRectW());  // get x value
                int y = (int)((motionEvent.getY() + dispY)/ getRectH());  // get y value

                float rectH = getRectH();  // get rectangle height
                float rectW = getRectW();  // get rectangle width

                int brushRadH = (int)(brushSize / rectH);  // get brush height
                int brushRadW = (int)(brushSize / rectW);  // get brush width

                // Update heat map
                for (int i = -brushRadW; i <= brushRadW; i++) {
                    for (int j = -brushRadH; j <= brushRadH; j++) {
                        if (x+i > 0 && x+i < heatMap.length &&
                                y+j > 0 && y+j < heatMap[0].length) {  // if in bounds
                            float radius = (float) Math.sqrt(Math.pow(i * rectW, 2) +
                                    Math.pow(j * rectH, 2));  // get radius
                            if (radius <= brushSize) {  // if inside circle
                                heatMap[x + i][y + j] = Math.min(heatMap[x + i][y + j] +
                                                brushStrength / heatK,
                                        heatMax);  // add brush strength and clamp
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update thread values
     */
    private void doUpdate() {
        // Handle heat decay
        float heatNoise = 0.001f;  // set noise value

        // Store previous state
        float[][] prevHeatMap = heatMap;  // store previous heat map

        // Update state
        for (int i = 0; i < heatMap.length; i++) {  // cycle through rows
            for (int j = 0; j < heatMap[0].length; j++) {  // cycle through columns
                // Get heat transferred
                float q = -4 * prevHeatMap[i][j];  // preliminary heat transferred
                if (i > 0) // If tile to left
                    q += prevHeatMap[i - 1][j];  // add temp
                if (i < prevHeatMap.length - 1)  // if tile to right
                    q += prevHeatMap[i + 1][j];  // add temp
                if (j > 0)  // if tile above
                    q += prevHeatMap[i][j - 1];  // add temp
                if (j < prevHeatMap[0].length - 1)  // if tile below
                    q += prevHeatMap[i][j + 1];  // add temp
                float dT = q / heatK / 2;  // get delta temp

                // Update temperature and clamp
                heatMap[i][j] = Math.min(Math.max(prevHeatMap[i][j] + dT, 0f),
                        heatMax);  // clamp at zero

                // Remove noise
                if (heatMap[i][j] < heatNoise) {  // if noise
                    heatMap[i][j] = 0;  // set to zero
                }
            }
        }
    }

    /**
     * Close thread
     * @param isExit Should exit thread
     */
    void exit(boolean isExit){
        isRunning = !isExit;  // set is running
    }

    /**
     * Get height of a rectangle
     * @return Height of a rectangle
     */
    private float getRectH(){return (float)(height) / heatMap[0].length;}  // get height

    /**
     * Get width of a rectangle
     * @return Width of a rectangle
     */
    private float getRectW(){return (float)(width) / heatMap.length;}  // get width

    /**
     * Run thread
     */
    @Override
    public void run() {
        isRunning = true;  // set is running
        int sleepFor = 10;  // sleep for x milliseconds

        while (isRunning) {
            // Declare canvas and cufrent time
            Canvas c = null;  // Create canvas

            // Update surface view
            try {  // attempt to update canvas
                c = sh.lockCanvas(null);  // lock canvas
                if (c != null) {  // if canvas to draw to
                    doUpdate();  // update
                    doTouch();  // handle touch commands
                    doDraw(c);  // draw
                }
                else{  // if no canvas
                    Log.d(HeatActivity.LOG_DEBUG,
                            "Canvas destroyed. Cannot draw.");  // log
                }
            } finally {  // apply updates to canvas
                if (c != null) // if updated canvas
                    sh.unlockCanvasAndPost(c);  // update and unlock
            }

            // Sleep for 10 millisecond
            try {
                sleep(sleepFor);  // sleep
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());  // write
            }
        }
    }
}