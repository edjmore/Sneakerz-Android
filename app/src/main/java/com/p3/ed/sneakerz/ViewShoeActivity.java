package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
    private Uri tempUri;

    public static final String ACTION_ADD_RUN = "com.p3.ed.action.ADD_RUN",
            ACTION_VIEW_HIST = "com.p3.ed.action.VIEW_HIST";
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentManager fm = getFragmentManager();
            switch (intent.getAction()) {
                case ACTION_ADD_RUN:
                    NewRunFrag newRunFrag = new NewRunFrag();
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, newRunFrag)
                            .commit();
                    break;
                case ACTION_VIEW_HIST:
                    RunHistFrag runHistFrag = new RunHistFrag();
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, runHistFrag)
                            .commit();
                    break;
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
        }

        // TODO: Replace with more familiar UX for editing image
        ImageView imageView = (ImageView) findViewById(R.id.view_shoe_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickImage = new Intent(Intent.ACTION_PICK);
                pickImage.setType("image/*");

                File temp = null;
                try {
                    temp = File.createTempFile(mShoe.name, ".png");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, tempUri = Uri.fromFile(temp));

                Intent chooser = Intent.createChooser(pickImage,
                        "Choose an image or take a picture.");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{captureImage});
                startActivityForResult(chooser, 0);
            }
        });

        FragmentManager fm = getFragmentManager();
        RunHistFrag runHistFrag = new RunHistFrag();
        fm.beginTransaction().add(R.id.view_shoe_frag_container, runHistFrag).commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADD_RUN);
        filter.addAction(ACTION_VIEW_HIST);
        registerReceiver(br, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_shoe_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // User captured a new image
            } else {
                // User picked an existing image
                tempUri = data.getData();
            }

            File pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File temp = null;
            try {
                InputStream in = getContentResolver().openInputStream(tempUri);

                temp = File.createTempFile(mShoe.name, ".png", pubDir);
                FileOutputStream out = new FileOutputStream(temp);

                byte[] bytes = new byte[in.available()];
                in.read(bytes);
                out.write(bytes);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            Uri finalUri = Uri.fromFile(temp);

            DataSrc dataSrc = new DataSrc(this);
            try {
                dataSrc.open();
                dataSrc.setImageUri(finalUri, mShoe.getId());

                mShoe = dataSrc.getShoe(mShoe.getId());
                refreshViews();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(br);
        super.onDestroy();
    }

    private void refreshViews() {
        // Image
        ImageView imageView = (ImageView) findViewById(R.id.view_shoe_image);
        imageView.setImageURI(mShoe.getImageUri());
        // Name
        TextView nameView = (TextView) findViewById(R.id.view_shoe_name);
        nameView.setText(mShoe.name);
        // Distance
        TextView distView = (TextView) findViewById(R.id.view_shoe_dist);
        DecimalFormat df = new DecimalFormat("0.0");
        distView.setText(df.format(mShoe.miles));
    }
}
