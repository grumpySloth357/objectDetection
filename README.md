# objectDetection

Here, we are building a real-time object detection application on Android: **Speak EyE**.

Speak EyE is an android application geared towards visually impaired individuals that can describe their surrounds and also provide some alerts.

## [**Trello**](https://trello.com/b/G7R60axl/group-workflow)


# Navigation Folders

**android**:      Our main android app.(Object detection, Audio description, Alert system)

**python_test**:  Folder for python based object detection tests

**cpp_test**:     Folder for C++ based object detection tests

**models**:       Where you should save your .pb models 

The models can be downloaded from: [Tensorflow](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md)

**Test Result**:  Android Profiler Result, including CPU and Memory test result images and a battery test report :)

# How to Run the App

1. Download our repository in Githup.

2. In Android Studio, click open and choose the corresponding app in the location where you download it.(like SpeakEyE or Android)

3. Run it in Android Studio.(You may need to get everything updated)

4. Choose the emulator and then you can see what we have done.

(To run the app "Android", you need to add the model **ssd_mobilenet_v1_coco.pb** in "ObjectDetection-- Android-- assets". But the models are too large to upload to Git. We have uploaded them to a Google drive: https://drive.google.com/open?id=1zB53T-PWOrXztuH9_s5xrc_7Ar_kcRke
You can download and add them to the path mentioned above. After adding you need to re-gradle the app in Android Studio.)


# Contributors

Jiali Ge        :[@ivyoo00](https://github.com/ivyoo00)

Nidhi Tiwari    :[@nidhi-bu](https://github.com/nidhi-bu)

Shreeya Khadka  :[@angrySloth357](https://github.com/angrySloth357)

Yuchen Wang     :[@lowycve](https://github.com/lowycve)
