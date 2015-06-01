package com.p3.ed.sneakerz;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.NumberPicker;

import java.util.ArrayList;

/**
 * Created by Ed on 6/1/15.
 */
public class MyNumPicker extends NumberPicker {
    private static final String TAG = "MyNumPicker";

    public MyNumPicker(Context context) {
        super(context);
    }

    public MyNumPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyAttributes(context, attrs);
    }

    public MyNumPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyAttributes(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MyNumPicker, 0, 0);
        try {
            int maxVal = a.getInt(R.styleable.MyNumPicker_maxNum, 9);
            setMaxValue(maxVal);
            int minVal = a.getInt(R.styleable.MyNumPicker_minNum, 0);
            setMinValue(minVal);
        } finally {
            a.recycle();
        }
    }
}
