package com.example.john.heat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Surface view for heat game
 * @author John
 * @see android.view.SurfaceView
 * @see android.view.SurfaceHolder
 */
public class HeatSurfaceView extends SurfaceView
                            implements SurfaceHolder.Callback {

    /**
     * Flag for motion event
     */
    public static final int TASK_MOTION_EVENT = 1;

    /**
     * Flag to clear map
     */
    public static final int TASK_CLEAR_MAP = 2;  // clear map

    /**
     * Surface holder
     */
    public SurfaceHolder sh;

    /**
     * Create heat thread
     */
    public HeatThread thread;

    /**
     * Context of surface
     */
    private Context ctx;

    /**
     * Holds previous heat map to assign
     */
    private float[][] prevHeatMap;

    /**
     * Initialize Heat Surface View
     * @param context: Context of surface view
     */
    public HeatSurfaceView(Context context){
        super(context);  // call super
        init(context);  // initialize surface
    }

    /**
     * Initialize Heat Surface View
     * @param context Context of surface
     * @param attrs Attributes of surface
     */
    public HeatSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);  // call super
        init(context);  // initialize surface
    }

    /**
     * Initialize Heat Surface View
     * @param context Context of surface
     * @param attrs Attributes of surface
     * @param defStyle Style of surface
     */
    public HeatSurfaceView(Context context, AttributeSet attrs,
                           int defStyle){
        super(context, attrs, defStyle);  // call super
        init(context);  // initialize surface
    }

    /**
     * Initialize Heat Surface View
     * @param context Context of surface
     */
    protected void init(Context context) {
        // Get handler and create paint
        sh = getHolder();  // get holder
        sh.addCallback(this);  // add callback

        // Create context and focus
        ctx = context;  // Assign context
        setFocusable(true);  // Focus
    }

    /**
     * Clear heat map
     */
    public void clearMap(){
        Message msg = thread.handler.obtainMessage();  // Initialize message
        msg.what = TASK_CLEAR_MAP;  // clear map
        thread.handler.sendMessage(msg);  // send message
    }

    /**
     * Returns current heat map
     * @return  Current heat map
     */
    public float[][] getHeatMap(){
        return thread.heatMap;  // return heat map
    }

    /**
     * Handle touch events
     * @param event Motion Touch Event
     * @return True if handles
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Send message to handler
        Message msg = thread.handler.obtainMessage();  // Initialize message
        msg.what = TASK_MOTION_EVENT;  // set motion event
        msg.obj = event;  // add event
        thread.handler.sendMessage(msg);  // send message

        Log.d(HeatActivity.LOG_DEBUG,
                String.format("Sent Event: Action: %d, X: %f, Y: %f.",
                event.getAction(), event.getX(), event.getY()));

        // Call super and consume event
        return super.onTouchEvent(event);
    }

    /**
     * Set heat map
     * @param heatMap  New heat map
     */
    public void setHeatMap(float[][] heatMap){
        if (thread != null) {  // is thread null, surface is not created
            thread.heatMap = heatMap;  // set heat map
        }
        else{  // is surface not created
            prevHeatMap = heatMap;  // store will created surface
        }
    }

    /**
     * Callback when surface is changed
     * @param holder Holder of surface
     * @param format Format
     * @param width Width of surface
     * @param height Height of surface
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height){
        // Update width and height
        thread.height = height;  // set height
        thread.width = width;  // set width
    }

    /**
     * Callback when surface is created
     * @param holder Holder of surface
     */
    public void surfaceCreated(SurfaceHolder holder){
        // Update dimensions
        Rect size = holder.getSurfaceFrame();  // get size

        // Initialize thread
        thread = new HeatThread(sh, ctx, size.height(), size.width());  // create thread

        if (prevHeatMap != null){  // if previous heat map
            thread.heatMap = prevHeatMap;  // set heat map
            prevHeatMap = null;  // consume heat map
        }

        // Run
        thread.start();  // start thread
        Log.d(HeatActivity.LOG_DEBUG,
                "Started Heat Thread.");  // write create
    }

    /**
     * Callback when surface is destroyed
     * @param holder Holder of surface
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        // Exit thread
        thread.exit(true);  // exit main loop

        // Loop till joined
        while(true) {
            try {
                thread.join();  // join thread
                Log.d(HeatActivity.LOG_DEBUG,
                        "Heat thread destroyed.");  // write destroy
                break;  // break loop
            }
            catch (InterruptedException e) {  // cannot join thread
                // Continue looping
            }
        }
    }
}
