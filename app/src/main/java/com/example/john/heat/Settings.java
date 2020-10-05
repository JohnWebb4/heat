package com.example.john.heat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class Settings {
    static final String BRUSH_SIZE_KEY = "com.example.heat.brush_size";
    static final String BRUSH_STRENGTH_KEY = "com.example.heat.brush_strength";
    static final String COLD_COLOR_KEY = "com.example.heat.cold_color";
    static final String HOT_COLOR_KEY = "com.example.heat.hot_color";
    static final String HEAT_K_KEY = "com.example.heat.heat_k";
    static final String SAVED_PREFERENCES_KEY = "com.example.heat.saved_preferences_key";

    private static final float defaultBrushSize = 50;
    private static final float defaultBrushStrength = 10;
    private static final int defaultColdColor = Color.BLUE;
    private static final int defaultHotColor = Color.RED;
    private static final float defaultHeatK = 2;

    public float brushSize;
    public float brushStrength;
    public int coldColor;
    public int hotColor;
    public float heatK;

    public static Settings loadSettings(Context context) {
        float brushSize;
        float brushStrength;
        float heatK;
        int coldColor;
        int hotColor;

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Settings.SAVED_PREFERENCES_KEY,
                Context.MODE_PRIVATE);  // open shared preferences to edit

        brushSize = sharedPreferences.getFloat(BRUSH_SIZE_KEY, defaultBrushSize);
        brushStrength = sharedPreferences.getFloat(BRUSH_STRENGTH_KEY, defaultBrushStrength);
        heatK = sharedPreferences.getFloat(HEAT_K_KEY, defaultHeatK);
        coldColor = sharedPreferences.getInt(COLD_COLOR_KEY, defaultColdColor);
        hotColor = sharedPreferences.getInt(HOT_COLOR_KEY, defaultHotColor);


        return new Settings(brushSize, brushStrength, coldColor, hotColor, heatK);
    }


    public Settings(float brushSize, float brushStrength, int coldColor, int hotColor, float heatK) {
        this.brushSize = brushSize;
        this.brushStrength = brushStrength;
        this.coldColor = coldColor;
        this.hotColor = hotColor;
        this.heatK = heatK;
    }


    public void saveSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Settings.SAVED_PREFERENCES_KEY,
                Context.MODE_PRIVATE);  // open shared preferences to edit
        SharedPreferences.Editor editor = sharedPreferences.edit();  // create editor

        editor.putFloat(BRUSH_SIZE_KEY,
                this.brushSize);  // set brush size
        editor.putFloat(BRUSH_STRENGTH_KEY,
                this.brushStrength);  // set brush strength
        editor.putFloat(HEAT_K_KEY,
                this.heatK);  // set heat k value
        editor.putInt(COLD_COLOR_KEY,
                this.coldColor);  // set cold color
        editor.putInt(HOT_COLOR_KEY,
                this.hotColor);  // set hot color

        editor.apply();  // apply values
    }
}
