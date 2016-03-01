package org.openalpr.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.StringBuilder;
import java.util.TooManyListenersException;

import static org.openalpr.app.AppConstants.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.Alpr;

public class ScanPlate extends Activity implements AsyncListener<AlprResult>, AdapterView.OnItemSelectedListener {

    private static final String TAG = "ScanPlate: ";

    private static final int PLATE_RETURN_COUNT = 10;

    private static final int REQUEST_CODE = 1;

    private String mCurrentPhotoPath;

    private ImageView mImageView;

    private String state;

    private Spinner spinner;

    private String plate;

    private Spinner plateSpinner;

    private AlprResult alprResult;

    private TextView errorText;

    private EditText plateText;

    private ProgressDialog progressDialog;

    private List<String> tempList;

    private ArrayAdapter<String> plateAdaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_plate);

        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean(PREF_INSTALLED_KEY, false)) {

            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean(PREF_INSTALLED_KEY, true).commit();

            copyAssetFolder(getAssets(), RUNTIME_DATA_DIR_ASSET,
                    ANDROID_DATA_DIR + File.separatorChar + RUNTIME_DATA_DIR_ASSET);
        }

        mImageView = (ImageView) findViewById(R.id.imageView);

        /**
         * spinner for state code
         */
        spinner = (Spinner) findViewById(R.id.state_spinner);
        spinner.setSelected(false);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /**
         * spinner for plate
         */
        tempList = new ArrayList<>(10);
        plateSpinner = (Spinner) findViewById(R.id.plate_spinner);
        plateAdaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tempList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plateSpinner.setOnItemSelectedListener(this);
        plateSpinner.setAdapter(plateAdaptor);

        errorText = (TextView) findViewById(R.id.errorTextView);

        plateText = (EditText) findViewById(R.id.plateTextView);
        plateText.setVisibility(View.INVISIBLE);


    }

    private void selectState() {
        Spinner spin = (Spinner) findViewById(R.id.state_spinner);
        state = spin.getSelectedItem().toString();
        Log.d(TAG, "Selected State: " + state);
    }

    private void selectPlate(AlprResult alprResult) {
        final List<AlprCandidate> candList = alprResult.getCandidates();
        plateSpinner = (Spinner) findViewById(R.id.plate_spinner);

        for (int i = 0; i <candList.size(); i++) {
            tempList.add(i,String.valueOf(candList.get(i).getPlate() + "\t" + candList.get(i).getConfidence()));
            Log.d(TAG, String.valueOf(tempList.get(i)));

        }

        runOnUiThread(new Runnable() {
            public void run() {
                plateAdaptor.clear();
                plateAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                plateAdaptor.notifyDataSetChanged();
                plateSpinner.setAdapter(plateAdaptor);
            }
        });

//        int index = plateSpinner.getSelectedItemPosition();
//
//        plateText.setText(candList.get(index).getPlate());

//        Log.d(TAG, "Selected plate: " + plate);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                mCurrentPhotoPath = data.getExtras().getString("picture");

                Log.d(TAG, "Picture Past" + mCurrentPhotoPath);


                final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
                        RUNTIME_DATA_DIR_ASSET + File.separatorChar + OPENALPR_CONF_FILE;
                handleBigCameraPhoto();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = Alpr.Factory.create().recognizeWithCountryRegionNConfig("us", "", mCurrentPhotoPath, openAlprConfFile, 10);

                        Log.d("OPEN ALPR", result);

                        alprResult = processJsonResult(result);
                    }
                });

            }
        }
    }

    private AlprResult processJsonResult(String result) {
        AlprResult alprResult = new AlprResult();
        try {
            JSONObject jsonObject = new JSONObject(result);
            addResult(jsonObject, alprResult);
        } catch (JSONException e) {
            Log.e(TAG, "Exception parsing JSON result", e);
            alprResult.setRecognized(false);
        }
        onPostExecute(alprResult);
        return alprResult;
    }

    private void addResult(JSONObject jsonObject, AlprResult alprResult) throws JSONException {
        JSONArray resultArray = jsonObject.getJSONArray(JSON_RESULT_ARRAY_NAME);
        alprResult.setProcessingTime(jsonObject.getLong("processing_time_ms"));
        AlprResultItem alprResultItem = null;

        for (int i = 0; i < resultArray.length(); i++) {
            JSONObject resultObject = resultArray.getJSONObject(i);
            alprResultItem = new AlprResultItem();

            Log.d(TAG, resultObject.getString("candidates"));
            String candidatesString = resultObject.getString("candidates");
            JSONArray candidatesArray = new JSONArray(candidatesString);

            Log.d(TAG, candidatesArray.toString());
            Log.d(TAG, String.valueOf(candidatesArray.length()));



            for(int j = 0 ; j < candidatesArray.length(); j++) {
                AlprCandidate alprCandidate = new AlprCandidate();
                JSONObject candidateObject = candidatesArray.getJSONObject(j);
                alprCandidate.setConfidence(candidateObject.getDouble("confidence"));
                alprCandidate.setPlate(candidateObject.getString("plate"));
                alprResult.addCandidate(alprCandidate);
            }

            alprResultItem.setPlate(resultObject.getString("plate"));
            alprResultItem.setProcessingTime(resultObject.getLong("processing_time_ms"));
            alprResultItem.setConfidence(resultObject.getDouble("confidence"));
            alprResult.addResultItem(alprResultItem);
            Log.d(TAG, String.valueOf(alprResult.getCandidates().size()));
        }
    }

    @Override
    public void onPostExecute(AlprResult alprResult) {
        if (alprResult.isRecognized()) {

            selectPlate(alprResult);
   //         cleanUp();
        } else {
            setErrorText(getString(R.string.recognition_error));
            cleanUp();
        }
    }

    private void setErrorText(String text) {
        errorText.setText(text);
    }

    @Override
    public void onPreExecute() {
        onProgressUpdate();
    }

    @Override
    public void onProgressUpdate() {
        if (progressDialog == null) {
            prepareProgressDialog();
        }
    }

    private void cleanUp() {
//        progressDialog.dismiss();
//        progressDialog = null;
//        FragmentManager fm = getFragmentManager();
//        AlprFragment alprFragment = (AlprFragment) fm.findFragmentByTag(ALPR_FRAGMENT_TAG);
//        fm.beginTransaction().remove(alprFragment).commitAllowingStateLoss();
    }

    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Processing Image data");
        progressDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectState();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // method needed for adaptor view
        // Do nothing

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            //mCurrentPhotoPath = null;
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }


    private void displayImage() {
        Picasso.with(this)
                .load(mCurrentPhotoPath)
                .resize(mImageView.getWidth(), mImageView.getHeight())
                .into(mImageView);
    }

    public void takePicture(View view) {
        setErrorText("");
        Intent takePictureIntent = new Intent(this, CameraActivity.class);

        startActivityForResult(takePictureIntent, REQUEST_CODE);
    }

    public void enterText(View view) {
        plateText = (EditText) findViewById(R.id.plateTextView);
        plateText.setVisibility(View.VISIBLE);

    }

}
