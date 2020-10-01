package com.example.john.heat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    /**
     * Default brush size
     */
    public static final float defBrushSize = 50f;

    /**
     * Default brush strength
     */
    public static final float defBrushStrength = 10f;

    /**
     * Default cold color
     */
    public static final int defHeatCold = Color.BLUE;

    /**
     * Default heat thermal conductivity value
     */
    public static final float defHeatK = 2f;

    /**
     * Default hot color
     */
    public static final int defHeatHot = Color.RED;

    /**
     * Maximum brush size
     */
    public static final float maxBrushSize = 100;

    /**
     * Maximum brush strength in watts
     */
    public static final float maxBrushStrength = 50;

    /**
     * Maximum thermal conductivity value
     */
    public static final float maxHeatK = 10;

    /**
     * Minimum brush size
     */
    public static final float minBrushSize = 10;

    /**
     * Minimum brush strength in watts
     */
    public static final float minBrushStrength = 1;

    /**
     * Minimum thermal conductivity value
     */
    public static final float minHeatK = 1.5f;

    /**
     * Convert seek bar value to actual value
     *
     * @param max       Max value from seekBar
     * @param seekValue Value from seekBar
     * @param valueMax  Max possible actual value
     * @param valueMin  Min possible actual value
     * @return Actual value
     */
    public static float convSeekBar2Value(int max, int seekValue, float valueMax, float valueMin) {
        // Convert
        return Math.max(Math.min((seekValue / (float) (max) * (valueMax - valueMin) + valueMin),
                valueMax),
                valueMin);  // convert
    }

    /**
     * Convert value to seekvar value
     *
     * @param max      Max value from seekbar
     * @param value    Current value
     * @param valueMax Max value
     * @param valueMin Min value
     * @return Nearest value on seekbar
     */
    public static int convValue2SeekBar(int max, float value, float valueMax, float valueMin) {
        // Convert
        return (int) Math.max(Math.min((value - valueMin) / (valueMax - valueMin) * max,
                max),
                0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedPreferences = getSharedPreferences(
                HeatActivity.SAVED_PREFERENCES_KEY,
                Context.MODE_PRIVATE);  // get shared preferences

        float brushSize = sharedPreferences.getFloat(HeatActivity.SAVED_BRUSH_SIZE,
                defBrushSize);  // set brush size
        float brushStrength = sharedPreferences.getFloat(HeatActivity.SAVED_BRUSH_STRENGTH,
                defBrushStrength);  // set brush strength
        int heatCold = sharedPreferences.getInt(HeatActivity.SAVED_COLD_COLOR,
                defHeatCold);  // set cold color
        int heatHot = sharedPreferences.getInt(HeatActivity.SAVED_HOT_COLOR,
                defHeatHot);  // set hot color
        float heatK = sharedPreferences.getFloat(HeatActivity.SAVED_HEAT_K,
                defHeatK);


        // Set seek bars and color wheels
        SeekBar brSizeBar = (SeekBar) findViewById(R.id.seekBarBrSize);  // get seekbar
        brSizeBar.setProgress(convValue2SeekBar(brSizeBar.getMax(),
                brushSize, maxBrushSize, minBrushSize));  // set progress

        SeekBar brStrBar = (SeekBar) findViewById(R.id.seekBarBrStrength);  // get seekbar
        brStrBar.setProgress(convValue2SeekBar(brStrBar.getMax(),
                brushStrength, maxBrushStrength, minBrushStrength));  // set progress

        SeekBar heatKBar = (SeekBar) findViewById(R.id.seekBarHeatK);  // get seekbar
        heatKBar.setProgress(convValue2SeekBar(heatKBar.getMax(),
                heatK, maxHeatK, minHeatK));  // set progress

        final TextView coldView = (TextView) findViewById(R.id.textViewCold);  // get textview
        coldView.setTextColor(heatCold);  // set color

        final TextView hotView = (TextView) findViewById(R.id.textViewHot);  // get textview
        hotView.setTextColor(heatHot);  // set color

        // Hook button save
        Button buttonSave = (Button) findViewById(R.id.buttonSave);  // get button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save();  // Ssve
            }
        });

        // Hook Cold Color Wheel click
        final ColorWheelView colorWheelCold = findViewById(R.id.imageViewCold);  // image view
        colorWheelCold.addColorListener(new IColorListener() {
            @Override
            public void onColorChange(int color) {
                coldView.setTextColor(color);
            }
        });

        // Hook Hot Color Wheel click
        final ColorWheelView colorWheelHot = findViewById(R.id.imageViewHot);  // image view
        colorWheelHot.addColorListener(new IColorListener() {
            @Override
            public void onColorChange(int color) {
                hotView.setTextColor(color);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {  // if back arrow pressed
            finish();  // close settings
            return true;  // consume event
        }
        return super.onOptionsItemSelected(item);  // return super
    }

    /**
     * Save settings and close
     */
    protected void Save() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                HeatActivity.SAVED_PREFERENCES_KEY,
                Context.MODE_PRIVATE);  // open shared preferences to edit
        SharedPreferences.Editor editor = sharedPreferences.edit();  // create editor

        // Add extra data
        SeekBar brSizeBar = (SeekBar) findViewById(R.id.seekBarBrSize);  // get seekbar
        float brushSize = convSeekBar2Value(brSizeBar.getMax(), brSizeBar.getProgress(),
                maxBrushSize, minBrushSize);  // get brush size
        editor.putFloat(HeatActivity.SAVED_BRUSH_SIZE,
                brushSize);  // set brush size

        SeekBar brStrBar = (SeekBar) findViewById((R.id.seekBarBrStrength));  // get seekbar
        float brushStrength = convSeekBar2Value(brStrBar.getMax(), brStrBar.getProgress(),
                maxBrushStrength, minBrushStrength);  // get brush strength
        editor.putFloat(HeatActivity.SAVED_BRUSH_STRENGTH,
                brushStrength);  // set brush strength

        SeekBar heatKBar = (SeekBar) findViewById(R.id.seekBarHeatK);  // get seekbar
        float heatK = convSeekBar2Value(heatKBar.getMax(), heatKBar.getProgress(),
                maxHeatK, minHeatK);  // get heat K
        editor.putFloat(HeatActivity.SAVED_HEAT_K,
                heatK);  // set heat k value

        TextView coldView = (TextView) findViewById(R.id.textViewCold);  // get textview
        int coldColor = coldView.getCurrentTextColor();  // get text color
        editor.putInt(HeatActivity.SAVED_COLD_COLOR,
                coldColor);  // set cold color

        TextView hotView = (TextView) findViewById(R.id.textViewHot);  // get textview
        int hotColor = hotView.getCurrentTextColor();  // get text color
        editor.putInt(HeatActivity.SAVED_HOT_COLOR,
                hotColor);  // set hot color

        editor.apply();  // apply values
        finish();  // close settings
    }
}
