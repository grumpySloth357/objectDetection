'''
Test frozen tf graph
Shreeya
'''

import tensorflow as tf
import vocabulary
import caption_generator
import timeit
import math
from PIL import Image
import numpy as np

MODEL = './show_and_tell_2m.pb'
MODEL2 = './frozen_graph_new.pb'
MODEL3 = './optimized_graph.pb'
TEST_IMAGE_PATHS = ['./image7.jpg']
IMAGE_SIZE = (8, 4)

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
  img = np.array(image.getdata()).reshape(
      (im_height, im_width, 3)).astype(np.uint8)
  #img_exp = np.expand_dims(img, axis=0)
  imnew = img[0:299,0:299,:]
  print (imnew.shape)
  return imnew

'''Load model and run test images through it'''
def start_testing_images(model_path):
    #Load model
    detection_graph = load_frozen_model(model_path)
    vocab = vocabulary.Vocabulary()
    generator = caption_generator.CaptionGenerator(None, vocab)

    with detection_graph.as_default():
        with tf.Session(graph=detection_graph) as sess:
            #print ([n.name for n in tf.get_default_graph().as_graph_def().node])
            #image_feed = tf.placeholder(dtype=tf.string, shape=[], name="image_feed")
            #input_feed = tf.placeholder(dtype=tf.int64,
            #    shape=[None],  # batch_size
            #    name="input_feed")            
            #for op in detection_graph.get_operations():
            #    print(op.name)
            for image_path in TEST_IMAGE_PATHS:
                start = timeit.default_timer()
                image = tf.gfile.GFile(image_path, 'rb').read()
                img = Image.open(image_path)
                npimg = load_image_into_numpy_array(img)
                print (type(image))
                stop = timeit.default_timer()
                print ("Time to encode image: ", stop-start)
        
                # Actual detection
                start = timeit.default_timer()
                captions = generator.beam_search(sess, image, img)           
                stop = timeit.default_timer()
                print ("Time to Generate captions: ", stop-start)

                #Caption priting...
                start = timeit.default_timer()
                for i, caption in enumerate(captions):
                    sentence = [vocab.id_to_word(w) for w in caption.sentence[1:-1]]
                    sentence = " ".join(sentence)
                    print("  %d) %s (p=%f)" % (i, sentence, math.exp(caption.logprob)))
                stop = timeit.default_timer()
                print ("Time for Caption -> Sentence", stop-start)

                             
def main():
  default_model = MODEL2
  start_testing_images(default_model)

if __name__ == "__main__":
  main()
