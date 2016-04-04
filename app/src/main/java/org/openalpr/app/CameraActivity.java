package org.openalpr.app;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * Created by Anthony Brignano on 2/1/16.
 *
 * CameraActivity: For the camera scanning
 *      (associated view activity_camera)
 *      (see also CameraPreview.java class)
 *
 *      - getCameraInstance(): opens camera on users Android device
 *      - getOutputMediaFile(int): creates PlateScanner directory on users device for associated files
 *      - getOutputMediaFileUri(int)
 *      - refreshGallery(File): reloads users image gallery (for after an image is saved)
 *      - startCamera(): Opens the camera and prepares the view
 *      - stopCamera(): Closes the camera
 *      - checkCameraHardware(Context): returns true if the users device has a camera
 *      - SaveImageTask: async task saves images in background while application remains running
 *      - permissionCheck(): checks and requests permissions used in the applicaiton (for API 23)
 *      - setCameraParameters(): sets parameters in the camera for both its orientation and auto-focus
 *      - onCreate(Bundle)
 *      - onPause
 *      - onResume
 *      - onStop
 *      - onRestart
 *
 *
 * TO DO:
 *  - test if logic to save image in proper orientation is still working
 *
 */

public class CameraActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int MY_PERMISSIONS_CAMERA, MY_PERMISSIONS_ACCESS_FINE_LOCATION, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private Context context;
    private CameraPreview mPreview;
    private LatLng mLatLng_Exif;
    private Location mLastLocation = null;
    private LatLng mLatLng;
    private float mDist = 0;
    private GoogleApiClient mGoogleApiClient;
    private String mDateTime = null;
    private String tempFilePath;
    private long startTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        startCamera();
        context = this;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 // get an image from the camera
                                                 mCamera.takePicture(null, null, mPicture);
                                             }
                                         }
        );

    }

    @Override
    protected void onPause() {
        if (mCamera != null) {
            stopCamera();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onStop() {
        if (mPreview != null) {
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeView(mPreview);
            mPreview = null;
        }
        if(mCamera != null){
            stopCamera();

        }
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();

        startCamera();
    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // calls async task to save images without interrupting application
            new SaveImageTask().execute(data);

            /**
             * will have to move the getting of latitude and longitude to where the intent is for
             * directing to the next activity so that they can be passes as intent extras to be
             * available
             *
             * the mLatLng variable has a class scope so it can be accessed from anywhere?
             */


        }
    };

    @Override
    public void onConnected(Bundle connectionHint) {
        // check if API version is 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // check if permission is granted
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }
        else{
            // if API version is not 23 just set mLastLocation
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d(TAG, mLastLocation.toString());
        }
        // set LatLng variable -- not sure if this is necessary, mLastLocation already exists as a Location variable
        if (mLastLocation != null) {
            mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.d(TAG, mLatLng.latitude + ", " + mLatLng.longitude);
        }
    }

    @Override
    public void onConnectionSuspended(int i){
        // code to be run when connection is stopped
    }
    @Override
    public void onConnectionFailed(ConnectionResult cr){
        // code to be run when connection fails
    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();    // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PlateScanner");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }


        return mediaFile;
    }

    /** async task to save images to /PlateScanner/camera_test directory */
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            startTime = System.currentTimeMillis();
            FileOutputStream fos;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                // save_dir corresponds to the file directory which images will be saved to
                String save_dir = "/camera_test";
                File dir = new File (sdCard.getAbsolutePath() + save_dir);
                dir.mkdirs();


                /** format the filename below, current options:
                 *    fileName -> "timeInMilliseconds"
                 *    timeStamp -> "MM/DD/YYYY"
                 *    format -> jpg
                 *    */
                String fileName = String.format("%d", System.currentTimeMillis());
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                String format = ".jpg";
                File outFile = new File(dir, timeStamp + "_" + fileName + format);

                tempFilePath = outFile.getAbsolutePath();

                Bitmap bmp = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);

                Matrix matrix = new Matrix();
                Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
                int rotation = context.getResources().getConfiguration().orientation;

                //  fix screen orientation
                if (display.getRotation() == Surface.ROTATION_0) {

                    if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
                        matrix.postRotate(0);
                    } else {
                        matrix.postRotate(90);
                    }
                }
                else if (display.getRotation() == Surface.ROTATION_90) {
                    if (rotation == Configuration.ORIENTATION_PORTRAIT) {
                        matrix.postRotate(270);
                    }
                    else {
                        matrix.postRotate(0);
                    }
                }
                else if (display.getRotation() == Surface.ROTATION_180) {
                    if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
                        matrix.postRotate(180);
                    }else {
                        matrix.postRotate(270);
                    }
                }
                else if (display.getRotation() == Surface.ROTATION_270) {
                    if (rotation == Configuration.ORIENTATION_PORTRAIT) {
                        matrix.postRotate(90);
                    } else {
                        matrix.postRotate(180);
                    }
                }

                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                try {
                    fos = new FileOutputStream(tempFilePath);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);

                /** here is where you need to get the information from exif */
                getPhotoInfo(tempFilePath);

                if(mLatLng != null && mLatLng_Exif == null) {
                    Double lng = mLatLng.longitude;
                    Double lat = mLatLng.latitude;
                    String latlng = "(" + lat.toString() + ", " + lng.toString() + ")";
                    Log.d(TAG, "LatLng FROM GPS: " + latlng);
                } else if(mLatLng_Exif != null) {
                    Double lng = mLatLng_Exif.longitude;
                    Double lat = mLatLng_Exif.latitude;
                    String latlng = "(" + lat.toString() + ", " + lng.toString() + ")";
                    Log.d(TAG, "LatLng FROM PHOTO: " + latlng);
                }
                Bundle args = new Bundle();
                args.putParcelable("mlatlng", mLatLng);


                Intent intent = new Intent(context, ScanPlate.class);
                intent.putExtra("picture", tempFilePath);
                intent.putExtra("latlng", args);
                Log.d(TAG, "latlng after putExtra " + mLatLng.latitude + ", " + mLatLng.longitude);
                final long elapsedTimeMillis = System.currentTimeMillis() - startTime;
                Log.d(TAG, "TOTAL SAVE IMAGE TIME: " + (elapsedTimeMillis/1000) + " seconds");

                CameraActivity.this.startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    /** refresh photo galley on upload */
    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);

    }

    /** initialize necessary camera functions */
    public void startCamera(){
        if(mCamera == null){
            // check if device has a camera
            // Create an instance of Camera
            // check if API version is 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // verify permissions for camera
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCamera = getCameraInstance();
                }

                /**
                 *
                 * TODO set permissions for newer devices some how.
                 * mCamera = getCameraInstance();
                 *
                 */

            }
            else{
                mCamera = getCameraInstance();
            }

            setCameraParameters();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            // bring button to front of frame layout
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
            relativeLayout.bringToFront();
        }
    }

    /** properly exit camera functions */
    private void stopCamera() {
        if (this.mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;

            } catch (final Exception e) {
                //handle some exceptions
            }
        }
    }

    /** set parameters for auto-focus & image stabilization */
    public void setCameraParameters() {
        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();

        // enable autofocus
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        else if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }


        Display display = this.getWindowManager().getDefaultDisplay();

        int rotation = this.getResources().getConfiguration().orientation;
        Log.d(TAG, "Screen rotation is " + rotation);
        Log.d(TAG, "Display rotation is " + display.getRotation());

        //  fix screen orientation
        if (display.getRotation() == Surface.ROTATION_0) {

            if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(0);
            } else {
                mCamera.setDisplayOrientation(90);
            }
        }

        else if (display.getRotation() == Surface.ROTATION_90) {
            if (rotation == Configuration.ORIENTATION_PORTRAIT) {
                mCamera.setDisplayOrientation(270);
            }
            else {
                mCamera.setDisplayOrientation(0);
            }
        }

        else if (display.getRotation() == Surface.ROTATION_180) {
            if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(180);
            }else {
                mCamera.setDisplayOrientation(270);
            }
        }

        else if (display.getRotation() == Surface.ROTATION_270) {
            if (rotation == Configuration.ORIENTATION_PORTRAIT) {
                mCamera.setDisplayOrientation(90);
            } else {
                mCamera.setDisplayOrientation(180);
            }
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    /** zoom on finger pinch
     * need to adjust the speed based on size of screen?
     * is too slow for travis on his device */
    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        // change newDist to equal the position of the SeekBar
        // mDist * SeekBar_location (0-1.0)
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    /** refocus preview after zoom */
    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** request permissions for API 23 */
    private void permissionCheck(){
        int permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) &
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) &
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_CAMERA);
        }
    }

    /** update class variables for LatLng & date/timestamp here */
    private void getPhotoInfo(String filename){
        try{
            ExifInterface ex = new ExifInterface(filename);
            Double lat, lon;
            String attr_longitude = ex.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String attr_latitude = ex.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String attr_latitude_ref = ex.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String attr_longitude_ref = ex.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            mDateTime = ex.getAttribute(ExifInterface.TAG_DATETIME);



            if((attr_latitude != null)
                    && (attr_latitude_ref != null)
                    && (attr_longitude != null)
                    && (attr_longitude_ref != null))
            {
                if(attr_latitude_ref.equals("N")){
                    lat = convertToDegree(attr_latitude);
                }
                else{
                    lat = 0 - convertToDegree(attr_latitude);
                }

                if(attr_longitude_ref.equals("E")){
                    lon = convertToDegree(attr_longitude);
                }
                else{
                    lon = 0 - convertToDegree(attr_longitude);
                }

                mLatLng_Exif = new LatLng(lat, lon);
                Log.d(TAG, "GPS FROM PHOTO: " + mLatLng_Exif.toString());
                Log.d(TAG, "DATETIME FROM PHOTO: " + mDateTime);
            }
            else{
                Log.d(TAG, "Photo Exif data appears to be null.");
                Log.d(TAG, "lat = " + attr_latitude);
                Log.d(TAG, "lat_ref = " + attr_latitude_ref);
                Log.d(TAG, "lon = " + attr_longitude);
                Log.d(TAG, "long_ref = " + attr_longitude_ref);
            }


        } catch(IOException e){
            e.printStackTrace();
        }

    }

    /** convert the String latitude/longitude to a double value for the corresponding degree */
    private double convertToDegree(String stringDMS){
        double result;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0/S1;

        result = FloatD + (FloatM/60) + (FloatS/3600);

        return result;

    }

}
