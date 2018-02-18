# Android Application for Real Time Image Matching in Smartphones.
Several description algorithms were evaluated for real time performance and matches corrected for accuracy with several tests. 
Image matching algorithm was run as native code with Android NDK. 


### Prerequisites
- OpenCV 3.2
- Android SDK 23.0 - 25.0
- Android NDK 

### Results
Several tests were applied to fix the numerous greedy matches that occur in vanilla patch description algorithms. The system is also reaonably invariant to viewpoint change. <br>
Tests include
- RANSAC
- Ratio check
- Symmetry check
- Angle consistency
Accuracy measured for image pairs with increasing viewpoint changes. 20 point matches is considered the threshold to be labelled as matched.
![Accuracy](images/performance.png)
 <figure>
  <img src="images/time.png" alt="Time taken" width="200" height="250">
  <figcaption>Fig1. - Time taken for matching 16 pairs.</figcaption>
</figure> 
<figure>
  <img src="images/filter.png" alt="Before and after adding tests" width="350" height="500" align="center">
  <figcaption>Fig2. - Before and after tests we added in a false pair.</figcaption>
</figure> 
<figure>
  <img src="images/true.png" alt="A true pair" align="center">
  <figcaption>Fig3. - A true positive pair with scale and viewpoint changes after tests.</figcaption>
</figure> 


