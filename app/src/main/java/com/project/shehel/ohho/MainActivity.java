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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Integer.valueOf;
import static java.util.Arrays.sort;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

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
    Location loc;

    //Camera frame
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          //      WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
                loc = location;
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
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees



        return mRgba; // This function must return
    }

    public void traverse (File dir) {
        Location targetLoc = new Location("Dummy");
        if (dir.exists()) {

            File[] files = dir.listFiles();
            sort(files);
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];

                if (file.isDirectory()) {
                    traverse(file);
                } else {
                    Log.i(TAG, "reading" + file.getAbsolutePath());
                    if (file.getName().matches("data")) {

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
                            targetLoc.setLatitude(xlatitude);
                            targetLoc.setLongitude(xlongitude);
                            float distanceInMeters = loc.distanceTo(targetLoc);
                            Log.i(TAG, "Gotcha" + xlongitude + " ,"+xlatitude+" Distance"+distanceInMeters);
                            if (distanceInMeters > 500) {
                                break;
                            }

                        } catch (IOException e) {

                        }
                    } else {
                        Mat target = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                        //Mat target2 = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                        if (target.empty()) {
                            if (target.empty()) Log.i(TAG, "failledd" + file.getAbsolutePath());
                        }

                        TextView tv = (TextView) findViewById(R.id.textView2);

                        int top = 0;
                        int temp = 0;
                        if (mRgba.empty()) Log.i(TAG, "fail");
                        else Log.i(TAG, "Calculating match");
                        temp = (detectFeatures(mRgba.getNativeObjAddr(), target.getNativeObjAddr()));

                        tv.setText(String.valueOf(temp));
                        }
                    }
                }
            }

    }
    public void SaveImage (Mat mat) {
        Mat mIntermediateMat = new Mat();

        Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String filename = "barry.jpg";
        File file = new File(path, filename);

        Boolean bool = null;
        filename = file.toString();
        bool = imwrite(filename, mIntermediateMat);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }


    //Feature match when button is clicked
    public void detectButtonClick(View v) {
        String path = Environment.getExternalStorageDirectory() + "/data/";
        File location = new File(path);
        Log.i(TAG, "this is the dest"+location.getAbsolutePath());
        //SaveImage(mRgba);
        traverse (location);





            // display error message and exit






        check = false;
    }
}
