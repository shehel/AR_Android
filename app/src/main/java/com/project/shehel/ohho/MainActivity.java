package com.project.shehel.ohho;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static java.lang.Integer.valueOf;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private LocationManager locationManager;
    private LocationListener locationListener;

    private double longitude;
    private double latitude;

    private static String TAG = "MainActivity";

    //Camera frame
    Mat mRgba;
    Mat mGrey;
    boolean check = false;
    JavaCameraView javaCameraView;
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }

            super.onManagerConnected(status);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        //the zero parameter corresponds to the distance moved after which location is updated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10);

                return;
            }
        }
        configureButton();

    }

    private void configureButton() {
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            Log.i(TAG, "success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "fail opencv");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();

    public native static int detectFeatures(long matAddrRgba, long matAddrGray);

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrey= new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        if (!check) {
            mGrey = inputFrame.rgba();
        }
        return mRgba;
    }

    public void traverse (File dir) {

        if (dir.exists()) {

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];

                if (file.isDirectory()) {
                    traverse(file);
                } else {
                    if (file.getName().matches("data")) {
                        Log.i(TAG, "trying to read" + file.getAbsolutePath());

                        //Reading text from file
                        double xlongitude = 0;
                        double xlatitude = 0;
                        String lines;
                        try {
                            Log.i(TAG, "reading" + file.getAbsolutePath());

                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;

                            boolean first = false;
                            while ((line = br.readLine()) != null) {
                                if (first) {
                                    xlatitude = Double.valueOf(line);
                                    break;
                                }
                                xlongitude = Double.valueOf(line);
                                first = true;
                            }
                            br.close();
                            float distanceInMeters = loc1.distanceTo(loc2);
                            Log.i(TAG, "Gotcha" + xlongitude + " ,"+xlatitude);

                        } catch (IOException e) {

                        }
                    }
                }
            }
        }
    }



    //Feature match when button is clicked
    public void detectButtonClick(View v) {
        String path = Environment.getExternalStorageDirectory() + "/data/";
        File location = new File(path);
        Log.i(TAG, "this is the dest"+location.getAbsolutePath());

        traverse (location);



        File root = Environment.getExternalStorageDirectory();
        File file = new File(root, "img3.jpg");
        Mat m = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        if (m.empty())
        {        if (m.empty())Log.i(TAG, "failledd"+file.getAbsolutePath());

        }

            // display error message and exit



        Mat first = new Mat();
        Mat second = new Mat();

        TextView tv = (TextView) findViewById(R.id.textView2);
        tv.setText(String.valueOf(latitude)+" "+String.valueOf(longitude));

        if (check) {
            try {

                second = Utils.loadResource(MainActivity.this, R.drawable.img14, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

            } catch (IOException e) {
                e.printStackTrace();
            }
            int top = 0;
            int temp = 0;
            for (int i = 0; i < 6; i++) {
                try {

                    first = Utils.loadResource(MainActivity.this, getResources().getIdentifier("img" + i,"drawable", MainActivity.this.getPackageName()), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mRgba.empty()) Log.i(TAG, "fail");
                else Log.i(TAG, "Mariooo");
                if (first.empty()) Log.i(TAG, "fail");
                else Log.i(TAG, "Mariooo2");
                temp = (detectFeatures(first.getNativeObjAddr(), second.getNativeObjAddr()));
                if (temp > top) top = temp;
            }
            tv.setText(String.valueOf(top));

        }
        check = false;
    }
}
