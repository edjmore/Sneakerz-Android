package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by Ed on 5/29/15.
 */
public class ImageFrag extends Fragment {

    private Bitmap mBmp;
    private ImageView mImgView;

    @Override
    public void setArguments(Bundle args) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImgView = (ImageView) getView().findViewById(R.id.image_frag_view);
    }
}
