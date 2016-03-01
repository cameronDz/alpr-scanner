package org.openalpr.app;

        import android.app.Activity;
        import android.Manifest;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.hardware.Camera;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Environment;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.RelativeLayout;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;

/**
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
 *      - permissionCheck(): checks and requests permissions used in applicaiton (for API 23)
 *      - onCreate(Bundle)
 *      - onPause
 *      - onResume
 *      - onStop
 *      - onRestart
 *
 *
 * TO DO:
 *  - add logic to save image in proper orientation
 *
 */

public class CameraActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private int MY_PERMISSIONS_CAMERA, MY_PERMISSIONS_ACCESS_FINE_LOCATION, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraActivity";
    private Uri fileUri;
    private Camera mCamera;
    private Context context;
    private CameraPreview mPreview;

    private String tempFilePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        startCamera();
        context = this;

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
        super.onStop();
        if (mPreview != null) {
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeView(mPreview);
            mPreview = null;
        }
        if(mCamera != null){
            stopCamera();

        }

    }

    @Override
    public void onRestart() {
        super.onRestart();

        startCamera();
    }



    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // calls async task to save images without interrupting application
            new SaveImageTask().execute(data);
            Log.d(TAG, "onPictureTaken - jpeg");
            // camera.startPreview();
            /** redirects to VerifyPlateActivity */
//            Intent intent = new Intent(context, VerifyPlateActivity.class);
//            intent.putExtra("picture", tempFilePath);
//            startActivity(intent);

//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("picture", tempFilePath);
//            setResult(Activity.RESULT_OK,returnIntent);
//            finish();

        }
    };


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
            FileOutputStream fos = null;

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

                fos = new FileOutputStream(outFile);
                fos.write(data[0]);
                fos.flush();
                fos.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
                tempFilePath = outFile.getAbsolutePath();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("picture", tempFilePath);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();


            } catch (IOException e) {
                e.printStackTrace();
            }
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("picture", tempFilePath);
//            setResult(Activity.RESULT_OK,returnIntent);
//            finish();
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
//
//            TODO set permissions for newer devices some how.
//
//                mCamera = getCameraInstance();
            }
            else{
                mCamera = getCameraInstance();
            }
            // set camera to auto focus
            Camera.Parameters params = mCamera.getParameters();
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            else if (params.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(params);

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

}
