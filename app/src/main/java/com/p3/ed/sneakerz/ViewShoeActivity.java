package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by Ed on 6/1/15.
 */
public class ViewShoeActivity extends ActionBarActivity {

    public static final String KEY_SHOE_ID = "shoe_id";
    private Shoe mShoe;
    private Uri mTempUri;
    private Bundle mArgs;

    public static final String ACTION_ADD_RUN = "com.p3.ed.action.ADD_RUN",
            ACTION_VIEW_HIST = "com.p3.ed.action.VIEW_HIST";
    public static final String ACTION_DB_UPDATED = "com.p3.ed.action.DP_UPDATED";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentManager fm = getFragmentManager();

            switch (intent.getAction()) {
                case ACTION_ADD_RUN:
                    NewRunFrag newRunFrag = new NewRunFrag();
                    newRunFrag.setArguments(mArgs);
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, newRunFrag)
                            .commit();
                    break;

                case ACTION_VIEW_HIST:
                    RunHistFrag runHistFrag = new RunHistFrag();
                    runHistFrag.setArguments(mArgs);
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, runHistFrag)
                            .commit();
                    break;

                case ACTION_DB_UPDATED:
                    loadShoeData();
                    refreshViews();
                    break;
            }
        }
    };

    private Bitmap mImgBmp;
    private final Handler mGuiHandler = new Handler();
    private final Runnable mRefreshImgView = new Runnable() {
        @Override
        public void run() {
            ImageView imgView = (ImageView) findViewById(R.id.view_shoe_image);
            imgView.setImageBitmap(mImgBmp);
        }
    };

    private final View.OnClickListener mIvClkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");

            File temp = null;
            try {
                temp = File.createTempFile(mShoe.name, ".png", Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, mTempUri = Uri.fromFile(temp));

            Intent chooser = Intent.createChooser(pickImage,
                    "Choose an image or take a picture.");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{captureImage});
            startActivityForResult(chooser, 0);
        }
    };

    private final Runnable mHandleNewImage = new Runnable() {
        @Override
        public void run() {
            File pubDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File temp = null;
            try {
                InputStream in = getContentResolver().openInputStream(mTempUri);
                mImgBmp = BitmapFactory.decodeStream(in);
                // Crop image if larger than screen
                Point screenSize = new Point();
                getWindowManager().getDefaultDisplay().getSize(screenSize);
                if (mImgBmp.getWidth() > screenSize.x) {
                    Float scale = (float) screenSize.x / mImgBmp.getWidth();
                    mImgBmp = Bitmap.createScaledBitmap(mImgBmp, (int) (mImgBmp.getWidth() * scale),
                            (int) (mImgBmp.getHeight() * scale), false);
                }

                // Post image to GUI before proceeding
                mGuiHandler.post(mRefreshImgView);

                temp = File.createTempFile(mShoe.name, ".png", pubDir);
                FileOutputStream out = new FileOutputStream(temp);
                mImgBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            Uri finalUri = Uri.fromFile(temp);

            DataSrc dataSrc = new DataSrc(getApplicationContext());
            try {
                dataSrc.open();
                dataSrc.setImageUri(finalUri, mShoe.getId());
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                dataSrc.close();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_shoe_activity);

        Intent intent = getIntent();
        int _id = intent.getIntExtra(KEY_SHOE_ID, 1);
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(_id);

            refreshViews();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }

        ImageView imageView = (ImageView) findViewById(R.id.view_shoe_image);
        imageView.setOnClickListener(mIvClkListener);

        FragmentManager fm = getFragmentManager();
        RunHistFrag runHistFrag = new RunHistFrag();
        mArgs = new Bundle();
        mArgs.putInt(ViewShoeActivity.KEY_SHOE_ID, mShoe.getId());
        runHistFrag.setArguments(mArgs);
        fm.beginTransaction().add(R.id.view_shoe_frag_container, runHistFrag).commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADD_RUN);
        filter.addAction(ACTION_VIEW_HIST);
        filter.addAction(ACTION_DB_UPDATED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_shoe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set title to name of this shoe
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mShoe.name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // TODO: Switch to edit mode
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // User captured a new image
            } else {
                // User picked an existing image
                mTempUri = data.getData();
            }

            Thread t = new Thread(mHandleNewImage);
            t.start();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void loadShoeData() {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(mShoe.getId());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }
    }

    private void refreshViews() {
        ImageView imageView = (ImageView) findViewById(R.id.view_shoe_image);
        imageView.setImageURI(mShoe.getImageUri());

        TextView distView = (TextView) findViewById(R.id.view_shoe_dist);
        TextView descView = (TextView) findViewById(R.id.view_shoe_dist_desc);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String units = prefs.getString("key_pref_dist", "miles");
        distView.setText(mShoe.getDist(units));
        descView.setText(units);
    }
}
