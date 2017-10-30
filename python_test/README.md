# objectDetection
Object Detection tests on various Google pretained models

### Output from SSD_MOBILENET

![fast1](./fast_1.png "Mobilenet")

### Output from SSD_INCEPTIONNET

![fast2](./fast_2.png "Inceptionnet")


### Output from RCNN_INCEPTION_RESNET

![slow](./slow_1.png "RCNN")


### Per Image Stats

| Stats\Model    | MOBILENET | INCEPTIONNET  | RCNN_INCEPTION_RESNET |
|----------------|-----------|---------------|-----------------------|
| Detection time | ~180ms    | ~250ms        | ~24s                  |
| Model Size     | ~30MB     | ~100MB        | ~250MB                |
