package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created by Ed on 5/29/15.
 */
public class ImageFrag extends Fragment {

    private int mShoe_id;

    @Override
    public void setArguments(Bundle args) {
        mShoe_id = args.getInt(EditShoeActivity.SHOE_ID);
        String uriString = args.getString(EditShoeActivity.IMG_URI);
        if (uriString != null) {
            mLargeImgUri = Uri.parse(uriString);
        }
    }

    private Handler mGuiHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGuiHandler = new Handler();
    }

    private Context mContext;
    private Point screenSize;

    private Bitmap mLrgBmp;
    private ImageView mImgView;

    private Shoe mShoe;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Activity is an extension of context
        mContext = getActivity();
        // Get screen size for later
        getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize = new Point());

        // Load the image on background thread and display
        if (mLargeImgUri != null) {
            Thread t = new Thread(loadAndDisplay);
            t.start();
        }

        // Load shoe data
        DataSrc dataSrc = new DataSrc(mContext);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(mShoe_id);
        } catch (SQLException sqle) {
            // TODO: Actually handle the case when there was an error getting shoe data
            sqle.printStackTrace();
        } finally {
            // Avoid memory leak
            if (dataSrc != null && dataSrc.isOpen()) dataSrc.close();
        }
    }

    private final Runnable loadAndDisplay = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream in = mContext.getContentResolver().openInputStream(mLargeImgUri);
                mLrgBmp = BitmapFactory.decodeStream(in);

                mGuiHandler.post(refreshView);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
    };

    private final Runnable refreshView = new Runnable() {
        @Override
        public void run() {
            // Just put the loaded bitmap in the image view
            mImgView.setImageBitmap(mLrgBmp);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_frag, container, false);

        // Only child view in this fragment
        mImgView = (ImageView) view.findViewById(R.id.image_frag_view);
        setEditable(mEditable);

        return view;
    }

    // Listener only needs to be set once
    private boolean mHasListener = false;
    private boolean mEditable = false;

    public void setEditable(boolean editable) {
        mEditable = editable;
        // Wait if we don't have the view yet
        if (mImgView == null) {
            return;
        }
        // Image view is clickable in edit mode
        if (editable) {
            if (!mHasListener) {
                mImgView.setOnClickListener(imgViewClickListener);
            }
            mImgView.setClickable(true);
        } else {
            // Don't let user edit image
            mImgView.setClickable(false);
        }
    }

    public static final int IMG_REQUEST = 0;
    // Let user set their own shoe image
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

    // This URI will hold the path to the current image (not the location of a new one)
    private Uri mLargeImgUri;

    // These URI's will be null unless the user sets a new image
    private Uri mNewLrgImgUri, mNewSmlImgUri = null;

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
                InputStream in = mContext.getContentResolver().openInputStream(mLargeImgUri);
                mLrgBmp = BitmapFactory.decodeStream(in);
                // Crop the bitmap to fit screen
                int bmpWidth = mLrgBmp.getWidth();
                int bmpHeight = mLrgBmp.getHeight();
                // Only crop if the image is larger than the screen
                if (bmpWidth > screenSize.x) {
                    // Preserve aspect ratio
                    float scale = (float) screenSize.x / bmpWidth;
                    mLrgBmp = Bitmap.createScaledBitmap(mLrgBmp,
                            (int) (bmpWidth * scale), (int) (bmpHeight * scale), false);
                }

                // Display the bitmap
                mGuiHandler.post(refreshView);

                // Store large bitmap to private directory
                File temp = createPublicTempFile(mShoe.name + "_LARGE");
                // Save the URI for later
                mNewLrgImgUri = Uri.fromFile(temp);
                FileOutputStream out = new FileOutputStream(temp);
                // Write to file
                mLrgBmp.compress(Bitmap.CompressFormat.PNG, 100, out);

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

    private File createPrivateTempFile(String fileName) {
        // Get private directory
        File dir = mContext.getFilesDir();
        String type = ".png";
        try {
            return File.createTempFile(fileName, type, dir);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Error
        return null;
    }

    private Bitmap createSmallImage() {
        Bitmap bmp = null;
        try {
            // Have to load from storage to get a mutable bitmap
            InputStream in = mContext.getContentResolver().openInputStream(mNewLrgImgUri);
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

    // The database has been updated
    public static final String DB_UPDATED = "com.P3.ed.sneakerz.db_updated";

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            // Create a new data source just in case the activity was destroyed
            DataSrc dataSrc = new DataSrc(mContext);
            try {
                dataSrc.open();
                // Only write back if both URI's are not null
                if (mNewSmlImgUri != null && mNewLrgImgUri != null) {
                    dataSrc.setImageUris(mNewSmlImgUri, mNewLrgImgUri, mShoe.get_id());
                }

                // Let other activities know there is new data
                Intent dbUpdated = new Intent();
                dbUpdated.setAction(DB_UPDATED);
                mContext.sendBroadcast(dbUpdated);

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                // Avoid database leak
                if (dataSrc != null && dataSrc.isOpen()) dataSrc.close();
            }
        }
    };
}
