package com.project.shehel.ohho;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.IOException;
import java.lang.reflect.Field;

import static java.lang.Integer.valueOf;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private static String TAG = "MainActivity";

    //Camera frame
    Mat mRgba;
    Mat mGrey;
    boolean check = false;
    JavaCameraView javaCameraView;
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch(status) {
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

        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
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

    //Feature match when button is clicked

    public void detectButtonClick(View v) {
        Mat first = new Mat();
        Mat second = new Mat();

        TextView tv = (TextView) findViewById(R.id.textView2);
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
        check = true;
    }
}
