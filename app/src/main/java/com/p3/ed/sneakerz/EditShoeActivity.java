package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class EditShoeActivity extends Activity {
    public static final String TAG = "EditShoeActivity";

    private Shoe mShoe;
    private DataSrc mDataSrc;

    // Image view bitmap will be loaded from this URI
    private Uri mLargeImgUri;

    private Point screenSize;

    public static final String SHOE_ID = "shoe_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shoe_activity);

        // Load data from intent and get shoe
        Intent intent = getIntent();
        int _id = intent.getIntExtra(DbHelper.SHOES_ID, 0);

        mDataSrc = new DataSrc(this);
        try {
            mDataSrc.open();
            mShoe = mDataSrc.getShoe(_id);

            // Check for images
            Pair<Uri, Uri> uris = mShoe.getImageUris();
            if (uris.second != null) mLargeImgUri = uris.second;

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        // Get handles on child views
        mNameView = (TextView) findViewById(R.id.edit_shoe_name);
        mMilesView = (TextView) findViewById(R.id.edit_shoe_dist);

        // Get screen size for later
        screenSize = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screenSize);

        // For GUI updates
        mGuiHandler = new Handler();

        // Inititialize image view reference
        mImgView = (ImageView) findViewById(R.id.edit_shoe_large_image);
        mImgView.setOnClickListener(imgViewClickListener);

        // Reciever for fragment manipulations
        IntentFilter filter = new IntentFilter();
        filter.addAction(ADD_RUN);
        filter.addAction(VIEW_HIST);
        registerReceiver(br, filter);

        // Populate views from shoe data
        if (mShoe != null) {
            refreshViews();

            // Load run history fragment for this shoe
            RunHistFrag runHistFrag = new RunHistFrag();
            // The most confusing way to initialize a variable
            (mArgs = new Bundle()).putInt(SHOE_ID, mShoe.get_id());
            runHistFrag.setArguments(mArgs);

            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().add(R.id.edit_shoe_frame, runHistFrag).commit();
        }
    }

    // Extra values passed to fragments
    Bundle mArgs;

    public static final String ADD_RUN = "com.p3.ed.sneakerz.add_run";
    public static final String VIEW_HIST = "com.p3.ed.sneakerz.view_hist";

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentManager fm = getFragmentManager();

            switch (intent.getAction()) {
                case ADD_RUN:
                    NewRunFrag newRunFrag = new NewRunFrag();
                    newRunFrag.setArguments(mArgs);

                    fm.beginTransaction().replace(R.id.edit_shoe_frame, newRunFrag).commit();
                    break;

                case VIEW_HIST:
                    // Want to update content
                    Thread t = new Thread(refreshViews);
                    t.start();

                    RunHistFrag runHistFrag = new RunHistFrag();
                    runHistFrag.setArguments(mArgs);

                    fm.beginTransaction().replace(R.id.edit_shoe_frame, runHistFrag).commit();
                    break;
            }
        }
    };

    // Give user options to set image when they click the image view
    private final View.OnClickListener imgViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Use the camera to take a picture...
            Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // This path is for the large image
            File temp = createPublicTempFile(mShoe.name + "_LARGE");
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

    public static final int IMG_REQUEST = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Only proceed if everything went alright in the sub-activity
        if (resultCode == Activity.RESULT_OK) {

            // User either took a picture or chose an existing image
            if (requestCode == IMG_REQUEST) {

                // Data is null if the user took a photo with the camera
                if (data == null) {
                    // Image was stored at the URI we provided in the intent
                }
                // Data is not null if the user chose an image from the gallery
                else {
                    // Get the URI the selected image is stored at
                    mLargeImgUri = data.getData();
                }

                // Handle the new image
                Thread t = new Thread(handleNewImage);
                t.start();
            }
        }
    }

    /*
    When the user sets a new image we do the following:
    1.) Load the image from URI to a bitmap
    2.) Post the bitmap to the image view
    3.) Store the bitmap to a public directory
    4.) Create a smaller, round bitmap
    5.) Store the smaller bitmap to a private directory
    6.) Write the URI's of the small and large bitmaps to the shoe database
     */
    private final Runnable handleNewImage = new Runnable() {
        @Override
        public void run() {
            try {
                // Load bitmap from URI
                InputStream in = getContentResolver().openInputStream(mLargeImgUri);
                mLargeImgBitmap = BitmapFactory.decodeStream(in);
                // Crop the bitmap to fit screen
                int bmpWidth = mLargeImgBitmap.getWidth();
                int bmpHeight = mLargeImgBitmap.getHeight();
                // Only crop if the image is larger than the screen
                if (bmpWidth > screenSize.x) {
                    // Preserve aspect ratio
                    float scale = (float) screenSize.x / bmpWidth;
                    mLargeImgBitmap = Bitmap.createScaledBitmap(mLargeImgBitmap,
                            (int) (bmpWidth * scale), (int) (bmpHeight * scale), false);
                }

                // Display the bitmap
                mGuiHandler.post(refreshViews);

                // Store large bitmap to private directory
                File temp = createPublicTempFile(mShoe.name + "_LARGE");
                // Save the URI for later
                mNewLrgImgUri = Uri.fromFile(temp);
                FileOutputStream out = new FileOutputStream(temp);
                // Write to file
                mLargeImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                // Create the smaller image and store it to a private directory
                Thread t = new Thread(createSmallImgAndStore);
                t.start();

            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
    };

    private File createPublicTempFile(String fileName) {
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

    private File createPrivateTempFile(String fileName) {
        // TODO: Is this private storage?
        File dir = getFilesDir();
        String type = ".png";
        try {
            return File.createTempFile(fileName, type, dir);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Error
        return null;
    }

    private final Runnable createSmallImgAndStore = new Runnable() {
        @Override
        public void run() {
            // Do the image manipulation to get a round image
            Bitmap smlImg = createSmallImage();

            // Store the image to our private directory
            File temp = createPrivateTempFile(mShoe.name + "_SMALL");
            // Save the file URI for later
            mNewSmlImgUri = Uri.fromFile(temp);
            try {
                FileOutputStream out = new FileOutputStream(temp);
                // TODO: Test different amounts of compression
                smlImg.compress(Bitmap.CompressFormat.PNG, 50, out);

            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            // Write the new URI's to the database
            Thread t = new Thread(writeBack);
            t.start();
        }
    };

    private Bitmap createSmallImage() {
        Bitmap bmp = null;
        try {
            // Have to load from storage to get a mutable bitmap
            InputStream in = getContentResolver().openInputStream(mNewLrgImgUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            bmp = BitmapFactory.decodeStream(in, null, options);

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        // Make the image square...
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int newSide = Math.min(width, height);
        bmp = Bitmap.createBitmap(bmp, width / 2 - newSide / 2, height / 2 - newSide / 2, newSide,
                newSide);
        // And make it a bit smaller if necessary (semi-arbitrarily)
        int maxDim = (int) (screenSize.x / 5.1);
        if (newSide > maxDim) {
            bmp = Bitmap.createScaledBitmap(bmp, maxDim, maxDim, false);
        }

        // Proceed if there was no error
        if (bmp != null) {
            // Want largest circle within image
            int side = bmp.getWidth();
            int radius = side / 2;
            Pair<Integer, Integer> center = new Pair(side / 2, side / 2);

            // Hack to clear pixels beside those inside circle
            int white = getResources().getColor(R.color.white);
            // TODO: Use loop tiling for cache optimization
            for (int y = 0; y < side; y++) {
                for (int x = 0; x < side; x++) {

                    // See if we are within circle radius
                    double x2 = Math.pow(x - center.first, 2);
                    double y2 = Math.pow(y - center.second, 2);
                    int dist = (int) Math.pow(x2 + y2, 0.5);
                    // Clear pixels outside of circle
                    if (dist > radius) {
                        bmp.setPixel(x, y, white);
                    }
                }
            }
        }

        return bmp;
    }

    // To refresh views from background threads
    private Handler mGuiHandler;
    private final Runnable refreshViews = new Runnable() {
        @Override
        public void run() {
            refreshViews();
        }
    };

    // Child views
    private TextView mNameView, mMilesView;

    // Loaded bitmap is displayed in the image view
    private Bitmap mLargeImgBitmap;
    private ImageView mImgView;

    private void refreshViews() {
        mNameView.setText(mShoe.name);
        mMilesView.setText(String.valueOf(mShoe.miles));

        // Only refresh the image view if there is a user image to display
        if (mLargeImgBitmap != null) {
            mImgView.setImageBitmap(mLargeImgBitmap);
        } else if (mLargeImgUri != null) {
            // There is an image, but we need to load it
            try {
                InputStream in = getContentResolver().openInputStream(mLargeImgUri);
                mLargeImgBitmap = BitmapFactory.decodeStream(in);

                mImgView.setImageBitmap(mLargeImgBitmap);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
    }

    // Values to write back to the shoe database
    private Uri mNewLrgImgUri, mNewSmlImgUri = null;

    @Override
    public void onDestroy() {
        // Don't want to leak database or receiver
        if (mDataSrc != null && !mDataSrc.isOpen()) mDataSrc.close();
        if (br != null) unregisterReceiver(br);

        super.onDestroy();
    }

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            // Create a new data source just in case the activity was destroyed
            mDataSrc = new DataSrc(getApplicationContext());
            try {
                mDataSrc.open();
                // Only write back if both URI's are not null
                if (mNewSmlImgUri != null && mNewLrgImgUri != null) {
                    mDataSrc.setImageUris(mNewSmlImgUri, mNewLrgImgUri, mShoe.get_id());
                }

                // Close the database
                if (mDataSrc != null && mDataSrc.isOpen()) mDataSrc.close();

                // Let other activities know there is new data
                Intent dbUpdated = new Intent();
                dbUpdated.setAction(DB_UPDATED);
                sendBroadcast(dbUpdated);

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    };

    // The database has been updated
    public static final String DB_UPDATED = "com.P3.ed.sneakerz.db_updated";
}
