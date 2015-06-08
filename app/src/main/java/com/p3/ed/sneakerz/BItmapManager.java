package com.p3.ed.sneakerz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ed on 6/4/15.
 */
public class BitmapManager {
    private static final String TAG = "BitmapManager";

    private Context mContext;
    private Map<Uri, Bitmap> mBmpMap;

    public BitmapManager(Context context) {
        mContext = context;
        mBmpMap = new HashMap();
    }

    public void fetchBitmapAsync(final Uri imgUri, final ImageView dstView) {
        if (mBmpMap.containsKey(imgUri)) {
            // Already have the bitmap
            dstView.setImageBitmap(mBmpMap.get(imgUri));
        }

        // Temporarily set the image view to be blank so the default image from XML isn't loaded
        dstView.setImageDrawable(null);

        final Handler guiHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                // Post image to GUI
                dstView.setImageBitmap((Bitmap) m.obj);
            }
        };

        final Thread t = new Thread() {
            @Override
            public void run() {
                // Load image from URI on background thread
                try {
                    InputStream in = mContext.getContentResolver().openInputStream(imgUri);
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    // Don't care about the 'what' field
                    Message m = guiHandler.obtainMessage(0, bmp);
                    guiHandler.sendMessage(m);
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                }
            }
        };
        t.start();
    }
}
