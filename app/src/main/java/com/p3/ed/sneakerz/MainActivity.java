package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private DataSrc mDataSrc;

    private ListView mShoeList;
    private Cursor mShoeCursor;
    private ShoeCursorAdapter mShoeAdapter;

    private EditText mEditText;

    private Uri mLrgImgUri, mSmlImgUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize database
        mDataSrc = new DataSrc(this);

        // Initialize the default shoe image if necessary
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String uriString = prefs.getString(DEF_IMG_SML, null);
        if (uriString == null) {
            Thread t = new Thread(initDefImage);
            t.start();
        } else {
            mSmlImgUri = Uri.parse(uriString);
            uriString = prefs.getString(DEF_IMG_LRG, uriString);
            mLrgImgUri = Uri.parse(uriString);
        }


        // Get handles for views
        mShoeList = (ListView) findViewById(R.id.main_shoe_list);
        mEditText = (EditText) findViewById(R.id.main_shoe_name_input);

        Button button = (Button) findViewById(R.id.main_add_shoe_button);
        // Add a new row to the database when button is clicked
        button.setOnClickListener(buttonClickListener);

        // Register reciever
        IntentFilter filter = new IntentFilter(ImageFrag.DB_UPDATED);
        registerReceiver(broadcastReceiver, filter);

        // Open edit shoe activity when list item is clicked
        mShoeList.setOnItemClickListener(lvClickListener);

        // Load data into list view
        refreshViews();
    }

    public static final String DEF_IMG_LRG = "def_img_lrg";
    public static final String DEF_IMG_SML = "def_img_sml";

    private final Runnable initDefImage = new Runnable() {
        @Override
        public void run() {
            Uri lrgImgUri = Uri.EMPTY;
            Uri smlImgUri = Uri.EMPTY;

            // Get path to default image
            Resources r = getResources();
            String scheme = ContentResolver.SCHEME_ANDROID_RESOURCE;
            String path = r.getResourcePackageName(R.mipmap.def_sneakers) + '/' +
                    r.getResourceTypeName(R.mipmap.def_sneakers) + '/' +
                    r.getResourceEntryName(R.mipmap.def_sneakers);
            Uri imgUri = Uri.parse(scheme + "://" + path);

            // Load the image into a bitmap
            Bitmap bmp = null;
            try {
                InputStream in = getContentResolver().openInputStream(imgUri);
                // Want mutable bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                bmp = BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }

            if (bmp != null) {
                // Crop the bitmap to large image specs
                Point screenSize = new Point();
                getWindowManager().getDefaultDisplay().getSize(screenSize);
                int bmpWidth = bmp.getWidth();
                int bmpHeight = bmp.getHeight();
                // Only crop if the image is larger than the screen
                if (bmpWidth > screenSize.x) {
                    // Preserve aspect ratio
                    float scale = (float) screenSize.x / bmpWidth;
                    bmp = Bitmap.createScaledBitmap(bmp,
                            (int) (bmpWidth * scale), (int) (bmpHeight * scale), false);
                }

                // Save to public directory
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File temp = null;
                try {
                    // Create a temp file
                    temp = File.createTempFile("def_img_LARGE", ".png", dir);
                    // Write file
                    FileOutputStream out = new FileOutputStream(temp);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

                    lrgImgUri = Uri.fromFile(temp);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
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

                // Store to private directory
                dir = getFilesDir();
                try {
                    temp = File.createTempFile("def_image_SMALL", ".png", dir);
                    FileOutputStream out = new FileOutputStream(temp);
                    bmp.compress(Bitmap.CompressFormat.PNG, 50, out);

                    smlImgUri = Uri.fromFile(temp);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                // Write to shared preferences
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(DEF_IMG_LRG, lrgImgUri == Uri.EMPTY ? null : lrgImgUri.toString());
                prefEditor.putString(DEF_IMG_SML, smlImgUri == Uri.EMPTY ? null : smlImgUri.toString());
                prefEditor.commit();

                // For this activity
                mLrgImgUri = lrgImgUri;
                mSmlImgUri = smlImgUri;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        refreshViews();
    }

    private final View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String input = mEditText.getText().toString();
            // Only add a new shoe if input is not empty
            if (!input.isEmpty()) {
                try {
                    mDataSrc.open();
                    Shoe shoe = mDataSrc.addShoe(input);
                    if (mSmlImgUri != null) {
                        mDataSrc.setImageUris(mSmlImgUri, mLrgImgUri, shoe.get_id());
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            // Refresh list view
            refreshViews();
        }
    };

    private final AdapterView.OnItemClickListener lvClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get the shoe's ID from view
            int _id = (int) view.getTag();

            // Start edit shoe activity and pass the ID along
            Intent intent = new Intent(MainActivity.this, EditShoeActivity.class);
            intent.putExtra(DbHelper.SHOES_ID, _id);
            startActivity(intent);
        }
    };

    private void refreshViews() {
        // Get all shoe data
        try {
            mDataSrc.open();
            mShoeCursor = mDataSrc.getAllShoes();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        // Bind to the list view (if not null or empty)
        if (mShoeCursor != null && mShoeCursor.getCount() > 0) {
            mShoeCursor.moveToNext();
            mShoeAdapter = new ShoeCursorAdapter(this, mShoeCursor, 0);
            mShoeList.setAdapter(mShoeAdapter);
        }

        // Clear text
        mEditText.setText("");
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Refresh views if data has been updated in another activity
            if (intent.getAction() == ImageFrag.DB_UPDATED) {
                refreshViews();
            }
        }
    };

    @Override
    public void onDestroy() {
        // Need to make sure nothing is leaked
        if (mDataSrc != null && mDataSrc.isOpen()) mDataSrc.close();
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }
}
