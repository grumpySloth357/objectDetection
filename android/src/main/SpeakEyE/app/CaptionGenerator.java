package main.SpeakEyE.app;

import android.content.res.AssetManager;

import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;

/**
 * Created by skhad on 11/30/2017.
 */


/*
    Generate captions from Show&Tell model
 */
public class CaptionGenerator {
    private static final short BEAM_SIZE=3;
    private static final short MAX_CAPTION_LENGTH = 20;
    /*If LENGTH_NORMALIZATION_FACTOR>0 longer captions are preferred*/
    private static final float LENGTH_NORMALIZATION_FACTOR = 0.0f;

    //private CaptionModel model;
    private TensorFlowInferenceInterface inferenceInterface; /*Caption model*/
    private Vocabulary vocab;

    /*Constructor*/
    CaptionGenerator(
            final AssetManager assetManager,
            final String modelFilename
            ) throws IOException {

        //Initialize vocab
        //this.vocab = new Vocabulary(assetManager);

        /*Initialize */
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        final Graph g = this.inferenceInterface.graph();
        /*
        d.inputName = "image_tensor";
        // The inputName node has a shape of [N, H, W, C], where
        // N is the batch size
        // H = W are the height and width
        // C is the number of channels (3 for our purposes - RGB)
        final Operation inputOp = g.operation(d.inputName);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + d.inputName + "'");
        }
        d.inputSize = inputSize;
        // The outputScoresName node has a shape of [N, NumLocations], where N
        // is the batch size.
        final Operation outputOp1 = g.operation("detection_scores");
        if (outputOp1 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_scores'");
        }
        final Operation outputOp2 = g.operation("detection_boxes");
        if (outputOp2 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_boxes'");
        }
        final Operation outputOp3 = g.operation("detection_classes");
        if (outputOp3 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_classes'");
        }

        // Pre-allocate buffers.
        d.outputNames = new String[] {"detection_boxes", "detection_scores",
                "detection_classes", "num_detections"};
        d.intValues = new int[d.inputSize * d.inputSize];
        d.byteValues = new byte[d.inputSize * d.inputSize * 3];
        d.outputScores = new float[MAX_RESULTS];
        d.outputLocations = new float[MAX_RESULTS * 4];
        d.outputClasses = new float[MAX_RESULTS];
        d.outputNumDetections = new float[1];
        return d; */

    }

    /*Pass image to CaptionModel and perform beamsearch to generate caption
     * Return one caption the one with best score :)
    * */
    public String beam_search() {
        //Feed in the image to get the initial state.
        //initial_state = feed_image(sess, encoded_image)
        return "";
    }


}
