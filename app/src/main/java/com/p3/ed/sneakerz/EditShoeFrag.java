package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created by Ed on 6/4/15.
 */
public class EditShoeFrag extends Fragment {

    private int mShoeId;
    private Context mContext;
    private Activity mActivity;
    private String mUnits;
    private Shoe mShoe;

    private Uri mTempUri;
    private Bitmap mImgBmp;

    private EditText mEditName;
    private EditText mEditDist;
    private ImageView mImageView;

    private final View.OnClickListener mIvClkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");

            File temp = null;
            try {
                temp = File.createTempFile(mShoe.name + "_SNEAKERZ", ".png", Environment
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

    private Handler mGuiHandler;
    private final Runnable mRefreshImgView = new Runnable() {
        @Override
        public void run() {
            mImageView.setImageBitmap(mImgBmp);
        }
    };

    private final Runnable mHandleNewImage = new Runnable() {
        @Override
        public void run() {
            File pubDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File temp = null;
            try {
                InputStream in = mContext.getContentResolver().openInputStream(mTempUri);
                mImgBmp = BitmapFactory.decodeStream(in);
                // Crop image if larger than screen
                Point screenSize = new Point();
                mActivity.getWindowManager().getDefaultDisplay().getSize(screenSize);
                if (mImgBmp.getWidth() > screenSize.x) {
                    Float scale = (float) screenSize.x / mImgBmp.getWidth();
                    mImgBmp = Bitmap.createScaledBitmap(mImgBmp, (int) (mImgBmp.getWidth() * scale),
                            (int) (mImgBmp.getHeight() * scale), false);
                }

                // Post image to GUI before proceeding
                mGuiHandler.post(mRefreshImgView);

                temp = File.createTempFile(mShoe.name + "_SNEAKERZ", ".png", pubDir);
                FileOutputStream out = new FileOutputStream(temp);
                mImgBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            Uri finalUri = Uri.fromFile(temp);

            DataSrc dataSrc = new DataSrc(mContext);
            try {
                dataSrc.open();

                dataSrc.setImageUri(finalUri, mShoe.getId());
                // Notify everyone that the database has new content
                Intent update = new Intent(ViewShoeActivity.ACTION_DB_UPDATED);
                mContext.sendBroadcast(update);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                dataSrc.close();
            }
        }
    };

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
    public void setArguments(Bundle args) {
        mShoeId = args.getInt(ViewShoeActivity.KEY_SHOE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_shoe_frag, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mActivity = getActivity();
        mGuiHandler = new Handler();

        // Get user prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUnits = prefs.getString(getString(R.string.pref_units),
                getString(R.string.miles)); // default to miles

        // Get shoe data
        DataSrc dataSrc = new DataSrc(mContext);
        try {
            dataSrc.open();

            mShoe = dataSrc.getShoe(mShoeId);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            dataSrc.close();
        }

        // Populate views with current shoe data
        View root = getView();
        // Name
        mEditName = (EditText) root.findViewById(R.id.edit_shoe_name);
        mEditName.setHint(mShoe.name);
        // Distance
        mEditDist = (EditText) root.findViewById(R.id.edit_shoe_dist);
        mEditDist.setHint(mShoe.getDist(mUnits));
        // Units
        TextView units = (TextView) root.findViewById(R.id.edit_shoe_dist_desc);
        units.setText(mUnits);
        // Image
        mImageView = (ImageView) root.findViewById(R.id.edit_shoe_image);
        mImageView.setOnClickListener(mIvClkListener);

        Button finish = (Button) root.findViewById(R.id.edit_shoe_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need to make sure user input is correct
                String newName = mShoe.name;
                double newMiles = mShoe.miles;

                // Name
                String name = mEditName.getText().toString();
                if (name != null && !name.isEmpty()) {
                    newName = name;
                }
                // Distance
                try {
                    String distString = mEditDist.getText().toString();
                    if (distString != null && !distString.isEmpty()) {
                        double dist = Double.parseDouble(distString);
                        // Convert to user's preferred units
                        newMiles = dist;
                        if (mUnits.equals("kilometers")) {
                            newMiles = dist / 1.609344; // kilometers per mile
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Toast.makeText(mContext, "Invalid distance.", Toast.LENGTH_SHORT).show();
                }

                // Writeback to database
                DataSrc dataSrc = new DataSrc(mContext);
                try {
                    dataSrc.open();

                    dataSrc.updateShoe(newName, newMiles, mShoeId);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    dataSrc.close();
                }

                // Switch to run history fragment
                Intent viewHist = new Intent(ViewShoeActivity.ACTION_VIEW_HIST);
                mContext.sendBroadcast(viewHist);
            }
        });

        Button delete = (Button) root.findViewById(R.id.edit_shoe_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSrc dataSrc = new DataSrc(mContext);
                try {
                    dataSrc.open();

                    // Delete shoe and return to main activity
                    dataSrc.deleteShoe(mShoeId);
                    Intent main = new Intent(mContext, MainActivity.class);
                    startActivity(main);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    dataSrc.close();
                }
            }
        });
    }
}
