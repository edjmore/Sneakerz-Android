package com.p3.ed.sneakerz;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

/**
 * Created by Ed on 6/2/15.
 */
public class IconButton extends Button {
    private static final String TAG = "IconButton";

    private int mIconId;
    private int mIconSize;

    public IconButton(Context context) {
        super(context);
    }

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyAttributes(context, attrs);
    }

    public IconButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyAttributes(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mIconId);
        // Scale the icon if necessary
        if (bitmap.getWidth() > mIconSize && bitmap.getHeight() > mIconSize) {
            bitmap = bitmap.createScaledBitmap(bitmap, mIconSize, mIconSize, false);
        }

        canvas.drawBitmap(bitmap, getWidth() / 2 - mIconSize / 2, getWidth() / 2 - mIconSize / 2,
                null);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.IconButton, 0, 0);
        try {
            mIconId = a.getResourceId(R.styleable.IconButton_customIcon, R.drawable.ic_action_new);
            mIconSize = a.getInt(R.styleable.IconButton_customIconSize, 64);
        } finally {
            a.recycle();
        }
    }
}
