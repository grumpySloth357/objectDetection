# Shreeya Khadka
# Program to detect objects via images

'''Import libraries..'''
import tensorflow as tf
import numpy as np
from matplotlib import pyplot as plt
from PIL import Image
from utils import create_label_map
import os
import timeit
from utils import visualization_utils as vis_util

'''Define PATHs'''
############################ MODELS ###########################
#fast, MAP:21
MODEL_MOBILENET = '../models/ssd_mobilenet_v1_coco.pb'
#fast, MAP:24
MODEL_INCEPTION = '../models/ssd_inception_v2_coco.pb'
#medium, MAP:30
MODEL_MEDIUM = '../models/faster_rcnn_resnet101_coco.pb'
#slow, MAP:37
MODEL_MOST_ACCURATE = '../models/faster_rcnn_inception_resnet_v2_atrous_coco.pb'

############################ LABELS ###########################
LABELS_PATH = './data/mscoco_label_map.pbtxt.txt'
NUM_CLASSES = 90
############################ TEST IMAGES ###########################
PATH_TO_TEST_IMAGES_DIR = './testImages'
TEST_IMAGE_PATHS = [ os.path.join(PATH_TO_TEST_IMAGES_DIR, 'image11.jpg')]#'image{}.jpg'.format(i)) for i in range(10, 10)]
# Size, in inches, of the output images.
IMAGE_SIZE = (8, 4)


'''Create a label map from id<int> --> {'id':<int>, 'name':<str>}'''
label_map = create_label_map.load_labelmap(LABELS_PATH)

'''Return a frozen model graph'''
def load_frozen_model(model_path):
    detection_graph = tf.Graph()
    with detection_graph.as_default():
        od_graph_def = tf.GraphDef()
        with tf.gfile.GFile(model_path, 'rb') as fid:
            serialized_graph = fid.read()
            od_graph_def.ParseFromString(serialized_graph)
            tf.import_graph_def(od_graph_def, name='')
    return detection_graph

'''Load image into numpy array'''
def load_image_into_numpy_array(image):
  (im_width, im_height) = image.size
  return np.array(image.getdata()).reshape(
      (im_height, im_width, 3)).astype(np.uint8)

'''Load model and run test images through it'''
def start_testing_images(model_path):
    #Load model
    detection_graph = load_frozen_model(model_path)
    with detection_graph.as_default():
        with tf.Session(graph=detection_graph) as sess:
            # Definite input and output Tensors for detection_graph
            image_tensor = detection_graph.get_tensor_by_name('image_tensor:0')
            # Each box represents a part of the image where a particular object was detected.
            detection_boxes = detection_graph.get_tensor_by_name('detection_boxes:0')
            # Each score represent how level of confidence for each of the objects.
            # Score is shown on the result image, together with the class label.
            detection_scores = detection_graph.get_tensor_by_name('detection_scores:0')
            detection_classes = detection_graph.get_tensor_by_name('detection_classes:0')
            num_detections = detection_graph.get_tensor_by_name('num_detections:0')

            for image_path in TEST_IMAGE_PATHS:
                start = timeit.default_timer()
                image = Image.open(image_path)
                stop = timeit.default_timer()
                print ("Time to open image: ", stop-start)

                # the array based representation of the image will be used later in order to prepare the
                # result image with boxes and labels on it.
                start = timeit.default_timer()
                image_np = load_image_into_numpy_array(image)
                stop = timeit.default_timer()
                print ("Time to load image: ", stop-start)

                # Expand dimensions since the model expects images to have shape: [1, None, None, 3]
                start = timeit.default_timer()
                image_np_expanded = np.expand_dims(image_np, axis=0)
                stop = timeit.default_timer()
                print ("Time to expand dims: ", stop-start)

                # Actual detection
                start = timeit.default_timer()
                (boxes, scores, classes, num) = sess.run(
                    [detection_boxes, detection_scores, detection_classes, num_detections],
                    feed_dict={image_tensor: image_np_expanded})
                ##### OBJECT DETECTION COMPLETE
                stop = timeit.default_timer()
                print ("Time to pass through model: ", stop-start)


                #### Note Output FORMATS
                #print (boxes.shape) = (1, 100, 4)
                #print (scores.shape) = (1, 100)
                #print (classes.shape) = (1, 100)
                #print (num)

                start = timeit.default_timer()
                # Visualization of the results of a detection.
                vis_util.visualize_boxes_and_labels_on_image_array(
                    image_np,
                    np.squeeze(boxes),
                    np.squeeze(classes).astype(np.int32),
                    np.squeeze(scores),
                    label_map,
                    use_normalized_coordinates=True,
                    line_thickness=8)
                start = timeit.default_timer()

                #plt.figure(figsize=IMAGE_SIZE)
                plt.figure()
                plt.imshow(image_np)
                plt.imsave(arr=image_np, fname='img')

def main():
    default_model = MODEL_INCEPTION
    start_testing_images(default_model)

if __name__ == "__main__":
    main()
