package com.p3.ed.sneakerz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Ed on 5/31/15.
 */
public class RoundImageView extends ImageView {
    private static final String TAG = "RoundImageView";

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        Drawable drawable = getDrawable();
        if (drawable == null) return;

        Bitmap roundBitmap = getRoundedCroppedBitmap(((BitmapDrawable) drawable).getBitmap(),
                getWidth() / 2);
        canvas.drawBitmap(roundBitmap, getWidth() / 2 - roundBitmap.getWidth() / 2,
                getHeight() / 2 - roundBitmap.getHeight() / 2, null);
    }

    private static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        int diameter = radius * 2;
        float scale = (float) diameter / Math.min(bitmap.getWidth(), bitmap.getHeight());
        // Preserve aspect ratio
        Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale),
                (int) (bitmap.getHeight() * scale), false);
        // Crop
        finalBitmap = Bitmap.createBitmap(finalBitmap, finalBitmap.getWidth() / 2 - radius,
                finalBitmap.getHeight() / 2 - radius, diameter, diameter);

        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawColor(Color.TRANSPARENT);
        paint.setColor(Color.CYAN);
        canvas.drawCircle(radius, radius, radius, paint);
        // Very helpful explanation of Xfermodes:
        // http://stackoverflow.com/questions/8280027/what-does-porterduff-mode-mean-in-android-graphics-what-does-it-do
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(finalBitmap, 0, 0, paint);

        return output;
    }
}
