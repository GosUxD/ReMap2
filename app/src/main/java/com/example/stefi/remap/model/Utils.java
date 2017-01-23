package com.example.stefi.remap.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.example.stefi.remap.R;

/**
 * Created by Daniel on 1/22/2017.
 */

public class Utils {

    public static Bitmap getBitmap(Context context) {
        int px = context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = context.getResources().getDrawable(R.drawable.ic_pin);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);
        return mDotMarkerBitmap;
    }
}
