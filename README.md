# Android Application for Real Time Image Matching in Smartphones.
Several description algorithms were evaluated for real time performance and matches corrected for accuracy with several tests. 
Image matching algorithm was run as native code with Android NDK. 


### Prerequisites
- OpenCV 3.2
- Android SDK 23.0 - 25.0
- Android NDK 

### Results
The post matching tests fixes the numerous greedy matches that occur in vanilla patch description algorithms. The system is also reaonably invariant to viewpoint change.
Accuracy measured for image pairs with increasing viewpoint changes. 20 point matches is considered the threshold to be labelled as matched.
![Accuracy](images/performance.png)
![Time taken](images/time.png){:height="36px" width="16px"}.
![Before and after adding tests](images/filter.png){:height="36px" width="36px"}.
![A true match](images/true.png)


