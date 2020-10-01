package com.example.john.heat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HeatActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    /**
     * Saved instance of heat map
     */
    static final String INSTANCE_HEAT_MAP = "com.example.heat.instance_heat_map";

    /**
     * Saved brush size
     */
    static final String SAVED_BRUSH_SIZE = "com.example.heat.brush_size";

    /**
     * Saved brush strength
     */
    static final String SAVED_BRUSH_STRENGTH = "com.example.heat.brush_strength";

    /**
     * Saved cold color
     */
    static final String SAVED_COLD_COLOR = "com.example.heat.cold_color";

    /**
     * Saved hot color
     */
    static final String SAVED_HOT_COLOR = "com.example.heat.hot_color";

    /**
     * Saved heat k value
     */
    static final String SAVED_HEAT_K = "com.example.heat.heat_k";

    /**
     * Saved preferences key
     */
    static final String SAVED_PREFERENCES_KEY = "com.example.heat.saved_preferences_key";

    /**
     * Initial delay before running simulation after boot
     */
    static final int INITIAL_DELAY_MS = 500; // ms

    /**
     * Ideal FPS of simulations
     */
    static final int IDEAL_FPS = 1;

    Bitmap bitmap;
    ScheduledExecutorService drawExecutorService;
    ScheduledFuture<?> drawScheduledFuture;
    int[] heatMap;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If saved instance
        if (savedInstanceState != null) {
            this.heatMap = (int[]) savedInstanceState.getSerializable(INSTANCE_HEAT_MAP);
        }

        initTimers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_heat, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        final SurfaceView hSV = findViewById(R.id.heatSurface);  // surface

        // On FAB Clear
        FloatingActionButton fab = findViewById(R.id.fab);  // get fab
        fab.setOnClickListener(new View.OnClickListener() {  // hook click listenter
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Cleared", Snackbar.LENGTH_LONG)  // set text to appear
                        .setAction("Action", null).show();  // no action
                heatMap = new int[hSV.getWidth() * hSV.getHeight()];
            }
        });

        this.surfaceHolder = hSV.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkAndResumeTimers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {  // if settings clicked
            openSettings();  // open settings
            return true;  // consume event
        } else if (id == R.id.action_about) {  // if about clicked
            openAbout();  // open about dialog
            return true;  // consume event
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);  // call super

        // Save instance
        // Store heat map
        savedInstanceState.putSerializable(INSTANCE_HEAT_MAP,
                heatMap);  // save heat map
    }

    /**
     * Open about dialog
     */
    public void openAbout() {
        // Build about dialog
        AlertDialog aboutDialog = new AlertDialog.Builder(HeatActivity.this).create();  // build
        aboutDialog.setTitle("About");  // set title
        aboutDialog.setMessage("Simulates conductive heat transfer.\nCreated by John Webb.");
        aboutDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();  // hide dialog
                    }
                });  // create ok button

        // Show
        aboutDialog.show();  // show
    }

    /**
     * Open settings
     */
    public void openSettings() {
        // Create Intent to open settings
        Intent intent = new Intent(this, SettingsActivity.class);  // set target and parent

        startActivity(intent);  // open settings
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Rect frame = holder.getSurfaceFrame();

        int width = frame.width();
        int height = frame.height();


        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            if (heatMap == null) {
                heatMap = new int[width * height];
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    void initTimers() {
        final SurfaceView hSV = findViewById(R.id.heatSurface);

        hSV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (drawScheduledFuture == null) {
                    scheduleDraw();
                }
            }
        });
    }

    void checkAndResumeTimers() {
        if (drawScheduledFuture != null && drawScheduledFuture.isCancelled()) {
            scheduleDraw();
        }
    }

    void stopTimers() {
        try {
            if (drawScheduledFuture != null) {
                drawScheduledFuture.cancel(false);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    void scheduleDraw() {
        if (drawScheduledFuture != null && !drawScheduledFuture.isCancelled()) {
            return;
        }

        if (drawExecutorService == null || drawExecutorService.isShutdown()) {
            drawExecutorService = Executors.newScheduledThreadPool(2);
        }

        drawScheduledFuture = drawExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (surfaceHolder != null) {
                    draw();
                }
            }
        }, INITIAL_DELAY_MS, 1000 / IDEAL_FPS, TimeUnit.MILLISECONDS);
    }

    void draw() {
        if (bitmap == null || surfaceHolder == null || heatMap == null) {
            return;
        }

        System.out.println("Draw");

        try {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    int index = y * bitmap.getWidth() + x;
                    heatMap[index] -= 10;
                }
            }

            bitmap.setPixels(heatMap, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(bitmap, 0, 0, null);
            surfaceHolder.unlockCanvasAndPost(canvas);


        } catch (IllegalStateException e) {
            System.err.println(e);
        }
    }
}
