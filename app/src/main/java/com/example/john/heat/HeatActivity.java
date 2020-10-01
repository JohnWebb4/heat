package com.example.john.heat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HeatActivity extends AppCompatActivity {
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

    Bitmap bitmap;
    float[][] heatMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SurfaceView hSV = (SurfaceView) findViewById(R.id.heatSurface);  // surface

        // On FAB Clear
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);  // get fab
        fab.setOnClickListener(new View.OnClickListener() {  // hook click listenter
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Cleared", Snackbar.LENGTH_LONG)  // set text to appear
                        .setAction("Action", null).show();  // no action
                heatMap = new float[hSV.getWidth()][hSV.getHeight()];
            }
        });

        // If saved instance
        if (savedInstanceState != null) {
            float[][] heatMap = (float[][]) savedInstanceState.getSerializable(INSTANCE_HEAT_MAP);
            if (heatMap == null) {  // if no key to heat map
                heatMap = new float[hSV.getWidth()][hSV.getHeight()];  // initialize
            }
            this.heatMap = heatMap;  // set previous heat map, till start
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_heat, menu);
        return true;
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
}
