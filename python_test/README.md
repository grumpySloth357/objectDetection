# objectDetection
Object Detection tests on various Google pretained models

### Output from SSD_MOBILENET

![slow](./fast_1.png "FastModel")

### Output from SSD_INCEPTION

### Output from INCEPTION_RESNET_

![fast](./slow_1.png "SlowModel")


### Per Image Stats

| Stats\Model    | MOBILENET | INCEPTIONNET  | RCNN_INCEPTION_RESNET |
|----------------|-----------|---------------|-----------------------|
| Detection time | ~180ms    | ~250ms        | 24s		     |
| Model Size     | ~30MB     | ~100MB        | 250MB		     |
