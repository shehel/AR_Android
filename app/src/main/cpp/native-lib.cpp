#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <stdio.h>

using namespace cv;
using namespace std;

int toGray(Mat img, Mat& gray) {
    cvtColor(img, gray, CV_RGBA2GRAY);
    if (gray.rows == img.rows && gray.cols == img.cols)
        return 1;
    return 0;
}

extern "C"
jint
Java_com_project_shehel_ohho_MainActivity_detectFeatures(
        JNIEnv *env,
        jobject instance, jlong addrRgba, jlong addrGray /* this */) {
    Mat& mRgb  = *(Mat*) addrRgba;
    Mat& mGray  = *(Mat*) addrGray;



    int conv;
    jint retVal;
    conv = toGray(mRgb, mGray);






    retVal = (jint) conv;
    return 69;
}

