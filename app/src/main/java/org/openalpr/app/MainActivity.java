//package org.openalpr.app;
//
//import android.app.Activity;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.preference.PreferenceManager;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import static org.openalpr.app.AppConstants.*;
//
//
//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//public class MainActivity extends Activity {
//
//    private static final int REQ_RESULT_CODE = 1;
//
//    private static final String TAG = "Main Activity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//    }
//
//    public void startScan(View view) {
//        Intent cameraIntent = new Intent(this, CameraActivity.class);
//        MainActivity.this.startActivity(cameraIntent);
//
//    }
//
//}
