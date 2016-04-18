package org.openalpr.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.openalpr.app.AppConstants.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.Alpr;

/**
 *  Create by Travis
 *
 *  Takes the image path from the just taken picture form cameraActivity and sends it to
 *  the ALPR function to retrieve the plate number if successful.
 *
 *  returns error if unsuccessful
 *
 *  Upon completion directs to VerifyPlateActivity
 */

public class ScanPlate extends Activity implements AsyncListener<AlprResult> {

    private static final String TAG = "ScanPlate: ";

    private static final int PLATE_RETURN_COUNT = 10;

    private static final int REQUEST_CODE = 1;

    private String mCurrentPhotoPath;

    private ImageView mImageView;

    private String result;

    private ProgressDialog progressDialog;

    private AlprResult alprResult;

    private ArrayList<String> candidateList;

    private String[] plateArray;

    private List<String> tempList;

    private ArrayAdapter<String> plateAdaptor;

    private List<AlprCandidate> candList;

    private LatLng mLatLng;

    private String mTimeStamp;


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

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        mImageView = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra("picture");
        mTimeStamp = intent.getStringExtra("timestamp");
        Bundle bundle = intent.getParcelableExtra("latlng");
        mLatLng = bundle.getParcelable("mlatlng");
        Log.d(TAG, "latlng: " + mLatLng.latitude + ", "+ mLatLng.longitude);


        handleBigCameraPhoto();
        startScanPlate();
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


    /**
     * Methon that makes the call to the ALRP, sets up the file paths
     * alpr returns json string of results
     *
     *
     */
    public void startScanPlate() {
        final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
                RUNTIME_DATA_DIR_ASSET + File.separatorChar + OPENALPR_CONF_FILE;
        handleBigCameraPhoto();
        onProgressUpdate();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                result = Alpr.Factory.create().recognizeWithCountryRegionNConfig("us", "", mCurrentPhotoPath, openAlprConfFile, 10);

                Log.d("OPEN ALPR", result);

                alprResult = processJsonResult(result);
            }
        });

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

    /**
     * check for error
     * If no error make the json string a json object to be parsed
     *
     * @param result
     * @return
     */

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

    /**
     * Parse the json object of results to get plate numbers and confidences
     *
     * @param jsonObject
     * @param alprResult
     * @throws JSONException
     */

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

            candidateList = new ArrayList<>();
            plateArray = new String[candidatesArray.length()];

            for(int j = 0 ; j < candidatesArray.length(); j++) {
                AlprCandidate alprCandidate = new AlprCandidate();
                JSONObject candidateObject = candidatesArray.getJSONObject(j);
                alprCandidate.setConfidence(candidateObject.getDouble("confidence"));
                alprCandidate.setPlate(candidateObject.getString("plate"));
                candidateList.add(j, candidateObject.getString("plate") + "\t\t" + candidateObject.getDouble("confidence"));
                plateArray[j] = candidateObject.getString("plate");
                alprResult.addCandidate(alprCandidate);
            }

            alprResultItem.setPlate(resultObject.getString("plate"));
            alprResultItem.setProcessingTime(resultObject.getLong("processing_time_ms"));
            alprResultItem.setConfidence(resultObject.getDouble("confidence"));
            alprResult.addResultItem(alprResultItem);
            Log.d(TAG, String.valueOf(alprResult.getCandidates().size()));
        }
    }

    /**
     * After all needed data, plate and confidences have been retrieved call verifyplate activity
     * passing the needed array of plates, list of confidence, and image path
     *
     * @param alprResult
     */
    @Override
    public void onPostExecute(AlprResult alprResult) {

        Intent intent = new Intent(this, VerifyPlateActivity.class);
        intent.putExtra("picture", mCurrentPhotoPath);
        intent.putExtra("recognized", alprResult.isRecognized());
        intent.putExtra("plateList", plateArray);
        intent.putStringArrayListExtra("candidateList", candidateList);
        intent.putExtra("timestamp", mTimeStamp);

        Bundle args = new Bundle();
        args.putParcelable("mlatlng", mLatLng);
        intent.putExtra("latlng", args);
        progressDialog.dismiss();
        startActivity(intent);

        Log.d(TAG, "AFTER PROCESSING IMAGE");
        Log.d(TAG, String.valueOf(alprResult.getCandidates().size()));
    }

    /**
     * simple progress spinner that displays for user to indicate the image is being processed
     */

    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Processing Image");
        progressDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    /**
     * These three methods are used for setting up the proper file path to the needed settings file
     * for the alpr function call to native C code
     *
     * @param assetManager
     * @param fromAssetPath
     * @param toPath
     * @return
     */
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


    /**
     * Display the image used for loading the image to the screen
     *
     */
    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {

            mImageView = (ImageView) findViewById(R.id.imageView);
            Picasso.with(this)
                    .load(new File(mCurrentPhotoPath))
                    .resize(600, 600)
                    .into(mImageView);

        }
    }


}
