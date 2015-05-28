package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class EditShoeActivity extends Activity {

    private Shoe mShoe;
    private DataSrc mData;

    // Child views
    private TextView mNameView, mMilesView;

    // Path to images (null until the user chooses an image)
    private Uri mLargeImgUri;
    private Uri mSmallImgUri;
    public static final int IMG_REQUEST = 0;

    // Loaded bitmap is displayed in the image view
    private Bitmap mImgBmp;
    private ImageView mImgView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shoe_activity);

        // Load data from intent and get shoe
        Intent intent = getIntent();
        int _id = intent.getIntExtra(DbHelper.SHOES_ID, 0);

        mData = new DataSrc(this);
        try {
            mData.open();
            mShoe = mData.getShoe(_id);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        // Get handles on child views
        mNameView = (TextView) findViewById(R.id.edit_shoe_name);
        mMilesView = (TextView) findViewById(R.id.edit_shoe_dist);

        // Populate views from shoe data
        if (mShoe != null) {
            refreshViews();
        }

        // For GUI updates
        mGuiHandler = new Handler();

        // Inititialize image view reference
        mImgView = (ImageView) findViewById(R.id.edit_shoe_large_image);
        mImgView.setOnClickListener(imgViewClickListener);
    }

    private File getTempFile(String fileName) {
        // Public image storage
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String type = ".png";
        try {
            // Create a temp file
            return File.createTempFile(fileName, type, dir);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Something went wrong
        return null;
    }

    // Give user options to set image when they click the image view
    private final View.OnClickListener imgViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Use the camera to take a picture...
            Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // This path is for the large image
            File temp = getTempFile(mShoe.name + "_LARGE");
            takePic.putExtra(MediaStore.EXTRA_OUTPUT, mLargeImgUri = Uri.fromFile(temp));

            // ...or pick an image from the gallery
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");

            // Let user choose between options
            Intent chooser = Intent.createChooser(pickImage, "Take a picture or choose an image.");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePic});
            startActivityForResult(chooser, IMG_REQUEST);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Only proceed if everything went alright in the sub-activity
        if (resultCode == Activity.RESULT_OK) {

            // User either took a picture or chose an existing image
            if (requestCode == IMG_REQUEST) {

                // Data is null if the user took a photo with the camera
                if (data == null) {
                    // TODO: Store large image to our public directory
                }
                // Data is not null if the user chose an image from the gallery
                else {
                    // Get the URI the selected image is stored at
                    mLargeImgUri = data.getData();
                }

                // Load bitmap in a new thread and post results
                Thread t = new Thread(loadBitmapFromUri);
                t.start();

                // TODO: Create small image and store in our private directory
                // TODO: Save URI's in shoe database
            }
        }
    }

    // Load bitmap and then update views
    private final Runnable loadBitmapFromUri = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream in = getContentResolver().openInputStream(mLargeImgUri);
                mImgBmp = BitmapFactory.decodeStream(in);

                // Display the bitmap
                mGuiHandler.post(refreshViews);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
    };

    // To refresh views from background threads
    private Handler mGuiHandler;
    private final Runnable refreshViews = new Runnable() {
        @Override
        public void run() {
            refreshViews();
        }
    };

    private void refreshViews() {
        mNameView.setText(mShoe.name);
        mMilesView.setText(String.valueOf(mShoe.miles));

        // Only refresh the image view if there is a user image to display
        if (mImgBmp != null) {
            mImgView.setImageBitmap(mImgBmp);
        }
    }

    @Override
    public void onDestroy() {
        if (mData != null) mData.close();
        super.onDestroy();
    }
}
