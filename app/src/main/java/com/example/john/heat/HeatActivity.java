package com.example.john.heat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HeatActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    static final int INITIAL_DELAY_MS = 500; // ms
    static final int IDEAL_FPS = 30;
    static final float MAX_HEAT_IN_K = 500;

    Bitmap bitmap;
    ScheduledExecutorService drawExecutorService;
    ScheduledFuture<?> drawScheduledFuture;
    float[] heatMap;
    int[] pixels;
    long previousDrawTime;
    Settings settings;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                heatMap = new float[hSV.getWidth() * hSV.getHeight()];
                pixels = new int[hSV.getWidth() * hSV.getHeight()];
            }
        });

        hSV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onSurfaceTouch(v, event);
                return true;
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

        // If saved instance
        this.settings = Settings.loadSettings(this);

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

            heatMap = new float[width * height];
            pixels = new int[width * height];
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
            drawExecutorService = Executors.newScheduledThreadPool(4);
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

        float[] copyHeatMap = Arrays.copyOf(heatMap, heatMap.length);
        long drawTime = System.currentTimeMillis();
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        if (previousDrawTime > 0) {
            try {
                float diffTimeInSeconds = (drawTime - previousDrawTime) / 1000.0f;

                for (int y = 1; y < height - 1; y++) {
                    for (int x = 1; x < width - 1; x++) {
                        int index = y * width + x;
                        float avgDiffTemp = 8 * copyHeatMap[index]
                                - copyHeatMap[index - 1]
                                - copyHeatMap[index - width]
                                - copyHeatMap[index + width]
                                - copyHeatMap[index + 1]
                                - copyHeatMap[index - 1 - width]
                                - copyHeatMap[index + 1 - width]
                                - copyHeatMap[index - 1 + width]
                                - copyHeatMap[index + 1 + width];

                        if (avgDiffTemp > 0 && avgDiffTemp < MAX_HEAT_IN_K) {
                            float diffHeat = Math.max(Math.min(-settings.heatK * avgDiffTemp * diffTimeInSeconds, MAX_HEAT_IN_K), -MAX_HEAT_IN_K);
                            heatMap[index] = Math.max(Math.min(heatMap[index] + diffHeat, MAX_HEAT_IN_K), 0);
                        }
                        float fractionHeatToMax = heatMap[index] / MAX_HEAT_IN_K;

                        pixels[index] = (int) (settings.coldColor + (settings.hotColor - settings.coldColor) * (fractionHeatToMax));
                    }
                }

                System.out.println(String.format("FPS %s", 1 / diffTimeInSeconds));

                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (IllegalStateException e) {
                System.err.println(e);
            }
        }


        previousDrawTime = drawTime;
    }

    void onSurfaceTouch(View v, MotionEvent event) {
        float centerX = (int) event.getX();
        int centerY = (int) event.getY();

        int maxY = (int) Math.min(centerY + settings.brushSize / 2, bitmap.getHeight());
        int minY = (int) Math.max(centerY - settings.brushSize / 2, 0);
        int maxX = (int) Math.min(centerX + settings.brushSize / 2, bitmap.getWidth());
        int minX = (int) Math.max(centerX - settings.brushSize / 2, 0);
        int width = bitmap.getWidth();

        System.out.println(String.format("On Surface touch %s %s, %s %s", minY, maxY, minX, maxX));

        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                int index = y * width + x;
                heatMap[index] = Math.max(Math.min(heatMap[index] + settings.brushStrength, MAX_HEAT_IN_K), 0);
            }
        }
    }


}
