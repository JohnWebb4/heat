package com.example.john.heat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

public class ColorWheelView extends androidx.appcompat.widget.AppCompatImageView {
    private ArrayList<IColorListener> colorListeners;

    public ColorWheelView(Context context) {
        super(context);

        this.colorListeners = new ArrayList<>();
    }

    public ColorWheelView(Context context, AttributeSet set) {
        super(context, set);

        this.colorListeners = new ArrayList<>();
    }

    /**
     * Get color from color wheel
     *
     * @param event      Motion event
     * @param colorWheel ImageView for color wheel
     * @return integer index for color
     */
    protected static int getPixelFromWheel(MotionEvent event, ImageView colorWheel) {
        // get bitmap convert to pixels in bitmap
        Bitmap bitmap = ((BitmapDrawable) (colorWheel.getDrawable())).getBitmap();
        int bitmapWidth = bitmap.getWidth();  // bitMap height
        int bitmapHeight = bitmap.getHeight();  // bitmap height
        float imageWidth = colorWheel.getWidth();
        float imageHeight = colorWheel.getHeight();
        int localX = (int) (event.getX() / imageWidth * bitmapWidth);  // get x
        int localY = (int) (event.getY() / imageHeight * bitmapHeight);  // get y

        // Get and set color
        return bitmap.getPixel(localX, localY);  // get pixel
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {  // on touch
        int action = motionEvent.getAction();  // get action
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {  // if down or move
            int color = getPixelFromWheel(motionEvent, this);  // get color

            for (IColorListener listener : this.colorListeners) {
                listener.onColorChange(color);
            }
        }

        return true;  // consume event
    }

    public void addColorListener(IColorListener colorListener) {
        this.colorListeners.add(colorListener);
    }

    public void removeColorListener(IColorListener colorListener) {
        this.colorListeners.remove(colorListener);
    }

}
