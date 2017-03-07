#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <stdio.h>
#include <android/log.h>

using namespace cv;
using namespace std;

#define PI 3.14159265
#define  LOG_TAG    "testjni"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

//Variable Declarations
Ptr<ORB> orb;
BFMatcher matcher(NORM_HAMMING);


float ratio = 0.95f;
float deltaX;
float deltaY;
float angle;
bool refineF = false;
double distance = 10.0;
double confidence = 0.99;


int ratioTest(std::vector<std::vector<cv::DMatch> >
              &matches) {
    int removed=0;
    // for all matches
    for (std::vector<std::vector<cv::DMatch> >::iterator
                 matchIterator= matches.begin();
         matchIterator!= matches.end(); ++matchIterator) {
        // if 2 NN has been identified
        if (matchIterator->size() > 1) {
            // check distance ratio
            if ((*matchIterator)[0].distance/
                (*matchIterator)[1].distance > ratio) {
                matchIterator->clear(); // remove match
                removed++;
            }
        } else { // does not have 2 neighbours
            matchIterator->clear(); // remove match
            removed++;
        }
    }
    return removed;
}
void symmetryTest(
        const std::vector<std::vector<cv::DMatch> >& matches1,
        const std::vector<std::vector<cv::DMatch> >& matches2,
        std::vector<cv::DMatch>& symMatches) {
    //__android_log_print(ANDROID_LOG_INFO, "sometag", "Got to push");
    int count = 0;
    // for all matches image 1 -> image 2
    for (std::vector<std::vector<cv::DMatch> >::
         const_iterator matchIterator1= matches1.begin();
         matchIterator1!= matches1.end(); ++matchIterator1) {
        // ignore deleted matches
        if (matchIterator1->size() < 2)
            continue;
        // for all matches image 2 -> image 1
        for (std::vector<std::vector<cv::DMatch> >::
             const_iterator matchIterator2= matches2.begin();
             matchIterator2!= matches2.end();
             ++matchIterator2) {

            // ignore deleted matches
            if (matchIterator2->size() < 2)
                continue;
            // Match symmetry test
            if ((*matchIterator1)[0].queryIdx ==
                (*matchIterator2)[0].trainIdx &&
                (*matchIterator2)[0].queryIdx ==
                (*matchIterator1)[0].trainIdx) {

                count++;
                // add symmetrical match
                symMatches.push_back(
                        cv::DMatch((*matchIterator1)[0].queryIdx,
                                   (*matchIterator1)[0].trainIdx,
                                   (*matchIterator1)[0].distance));
                break; // next match in image 1 -> image 2
            }
        }
    }

    __android_log_print(ANDROID_LOG_INFO, "sometag", "Count of sym push = %d", count);

}
cv::Mat ransacTest(
        const std::vector<cv::DMatch>& matches,
        const std::vector<cv::KeyPoint>& keypoints1,
        const std::vector<cv::KeyPoint>& keypoints2,
        std::vector<cv::DMatch>& outMatches) {
    // Convert keypoints into Point2f
    std::vector<cv::Point2f> points1, points2;
    cv::Mat fundemental;
    for (std::vector<cv::DMatch>::
         const_iterator it= matches.begin();
         it!= matches.end(); ++it) {
        // Get the position of left keypoints
        float x= keypoints1[it->queryIdx].pt.x;
        float y= keypoints1[it->queryIdx].pt.y;
        points1.push_back(cv::Point2f(x,y));
        // Get the position of right keypoints
        x= keypoints2[it->trainIdx].pt.x;
        y= keypoints2[it->trainIdx].pt.y;
        points2.push_back(cv::Point2f(x,y));
    }
    // Compute F matrix using RANSAC
    std::vector<uchar> inliers(points1.size(),0);
    if (points1.size()>0&&points2.size()>0){
        cv::Mat fundemental= cv::findFundamentalMat(
                cv::Mat(points1),cv::Mat(points2), // matching points
                inliers,       // match status (inlier or outlier)
                CV_FM_RANSAC, // RANSAC method
                10,      // distance to epipolar line
                confidence); // confidence probability
        // extract the surviving (inliers) matches
        std::vector<uchar>::const_iterator
                itIn= inliers.begin();
        std::vector<cv::DMatch>::const_iterator
                itM= matches.begin();
        // for all matches
        for ( ;itIn!= inliers.end(); ++itIn, ++itM) {
            if (*itIn) { // it is a valid match
                outMatches.push_back(*itM);
            }
        }
        if (refineF) {
            // The F matrix will be recomputed with
            // all accepted matches
            // Convert keypoints into Point2f
            // for final F computation
            points1.clear();
            points2.clear();
            for (std::vector<cv::DMatch>::
                 const_iterator it= outMatches.begin();
                 it!= outMatches.end(); ++it) {
                // Get the position of left keypoints
                float x= keypoints1[it->queryIdx].pt.x;
                float y= keypoints1[it->queryIdx].pt.y;
                points1.push_back(cv::Point2f(x,y));
                // Get the position of right keypoints
                x= keypoints2[it->trainIdx].pt.x;
                y= keypoints2[it->trainIdx].pt.y;
                points2.push_back(cv::Point2f(x,y));
            }
            // Compute 8-point F from all accepted matches
            if (points1.size()>0&&points2.size()>0){
                fundemental= cv::findFundamentalMat(
                        cv::Mat(points1),cv::Mat(points2), // matches
                        CV_FM_8POINT); // 8-point method
            }
        }
    }
    return fundemental;
}
int toGray(Mat& captured, Mat& target) {
    std::vector<cv::KeyPoint> keypointsCaptured;
    std::vector<cv::KeyPoint> keypointsTarget;

    cv::Mat descriptorsCaptured;
    cv::Mat descriptorsTarget;
    //cv::Mat captured;
    std::vector<cv::DMatch> matches;
    std::vector<cv::DMatch> symMatches;

    //std::vector<std::vector<cv::DMatch> > matches1;
    //std::vector<std::vector<cv::DMatch> > matches2;
    //Mat captured, target;
    /*captured = imread("/storage/emulated/0/data/img1.jpg", IMREAD_GRAYSCALE);
    target = imread("/storage/emulated/0/data/img3.jpg", IMREAD_GRAYSCALE);
    if (!captured.data) {
        // Print error message and quit
        __android_log_print(ANDROID_LOG_INFO, "sometag", "I cant do this.");

    }
    if (!target.data) {
        // Print error message and quit
        __android_log_print(ANDROID_LOG_INFO, "sometag", "cant do nuthin.");


    }*/
    //cvtColor(capturedR, captured, CV_RGBA2GRAY);
    //cvtColor(targetR, target, CV_RGBA2GRAY);


    orb = ORB::create();

    //Pre-process
    resize(captured, captured, Size(480,360));
    medianBlur(captured, captured, 5);

    resize(target, target, Size(480,360));
    medianBlur(target, target, 5);

    orb->detectAndCompute(captured, noArray(), keypointsCaptured, descriptorsCaptured);
    orb->detectAndCompute(target, noArray(), keypointsTarget, descriptorsTarget);
    //__android_log_print(ANDROID_LOG_INFO, "sometag", "keypoints2 size = %d", keypointsTarget.size());
    //__android_log_print(ANDROID_LOG_INFO, "sometag", "keypoints size = %d", keypointsCaptured.size());

    //Match images based on k nearest neighbour
    std::vector<std::vector<cv::DMatch> > matches1;
    matcher.knnMatch(descriptorsCaptured , descriptorsTarget,
                     matches1, 2);
    //__android_log_print(ANDROID_LOG_INFO, "sometag", "Matches1 = %d",     matches1.size());
    std::vector<std::vector<cv::DMatch> > matches2;
    matcher.knnMatch(descriptorsTarget , descriptorsCaptured,
                     matches2, 2);
   //Ratio filter
    ratioTest(matches1);
    ratioTest(matches2);
    symmetryTest(matches1,matches2,symMatches);
    ransacTest(symMatches,
               keypointsCaptured, keypointsTarget, matches);
    __android_log_print(ANDROID_LOG_INFO, "sometag", "Sym Matches = %d", symMatches.size());
    __android_log_print(ANDROID_LOG_INFO, "sometag", "Matches before muh shiz= %d", matches.size());

    const int symMatchCount = matches.size();

    //float meanboy;
    Point2f point1;
    Point2f point2;
    float median;
    float meanBoy=0;
    float greatest = 0;
    float lowest = 0;
    int count = 0;
    vector<float> angleList;
    vector<Point2f> point1List;
    vector<Point2f> point2List;

    for(int i = 0; i < matches.size(); i++)
    {
        point1 = keypointsCaptured[matches[i].queryIdx].pt;
        point2 = keypointsTarget[matches[i].trainIdx].pt;
        point1List.push_back (point1);
        point2List.push_back (point2);

        deltaY = ((360-point2.y) - (360-point1.y));
        deltaX = (point2.x+480 - point1.x);

        angle = atan2 (deltaY, deltaX) * 180 / PI;
        cout << "ORB Matching Results" << angle <<endl;
        //if (angle > greatest) greatest = angle;
        //if (angle < lowest) lowest = angle;
        meanBoy += angle;

        angleList.push_back (angle);
        //std::cout << "points " << "(" << point1.x << "," <<360-point1.y<<") (" << point2.x << ","<<360-point2.y<<") angle:" <<angle << std::endl;
        //std::cout << angle << std::endl;

    }
    // do something with the best points...

    //std::cout << "Mean" << meanBoy/symMatchCount << std::endl;
    vector<float> angleLCopy(angleList);
    std::sort(angleLCopy.begin(), angleLCopy.end());
    /*		         if(angleList.size() % 2 == 0)
                             median = (angleList[angleList.size()/2 - 1] + angleList[angleList.size()/2]) / 2;
                     else
                             median = angleList[angleList.size()/2];
                    */
    size_t medianIndex = angleLCopy.size() / 2;
    nth_element(angleLCopy.begin(), angleLCopy.begin()+medianIndex, angleLCopy.end());
    median = angleLCopy[medianIndex];
    std::cout << "new Median method " << angleLCopy[medianIndex] << std::endl;
    //std::cout << "greatest " << greatest << "|| lowest "<< lowest << std::endl;

    //std::cout << "No of matches by shehel: " << angleList[35] << " size " << symMatchCount << std::endl;
    //std::cout << "Median" << median << std::endl;
    //std::cout << matches.size()<< std::endl;
    count=0;
    for(auto i = matches.begin(); i != matches.end();) {

        //std::cout << angleList.at(count)<< std::endl;

        //if (angle > greatest) greatest = angle;
        //if (angle < lowest) lowest = angle;
        point1 = point1List.at(count);
        point2 = point2List.at(count);

        deltaY = ((360 - point2.y) - (360 - point1.y));
        deltaX = ((point2.x + 480) - point1.x);

        angle = atan2(deltaY, deltaX) * 180 / PI;
        //angleList.push_back (angle);
        cout << "Is it sorted? " << angleList.at(count) << endl;

        if (angleList.at(count) > (median + 5) | angleList.at(count) < (median - 5)) {
            //cout << "bitch is gone" << angleList.at(count) << endl;
            matches.erase(i);
            count++;

        }
            //{i++; count++;}
        else {
            cout << "Points A (" << point1.x << ", " << point1.y << ") B (" <<
            point2.x + 480 << ", " << point2.y << ") Deltas of X" << deltaX << " Y " <<
            deltaY << "  Angle " << angle << endl;
            cout << "bitch aint going no where" << angleList.at(count) << endl;

            ++i;
            count++;
            //if (angle>0.5 | angle < -0.7)
            //matches.erase(matches.begin()+i);
            // do something with the best points...
        }
    }
    __android_log_print(ANDROID_LOG_INFO, "sometag", "Matches after muh shiz= %d", matches.size());

    return (matches.size());
}



extern "C"
jint
Java_com_project_shehel_ohho_MainActivity_detectFeatures(
        JNIEnv *env,
        jobject instance, jlong addrRgba, jlong addrGray /* this */) {
    Mat &mRgb = *(Mat *) addrRgba;
    Mat &mGray = *(Mat *) addrGray;


    //int conv;
    jint retVal;
    //conv =

    int conv = toGray(mRgb, mGray);


    retVal = (jint) conv;
    return retVal;
}
