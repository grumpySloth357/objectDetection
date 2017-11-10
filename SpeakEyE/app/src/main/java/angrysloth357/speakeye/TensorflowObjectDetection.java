package angrysloth357.speakeye;

/**
 * Created by skhadka on 11/7/17.
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


/* Class to detect objects using Tensorflow */
public class TensorflowObjectDetection {
    private static final String TAG = "TensorFlowOBJDetect";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 50;
    private static final float THRESHOLD = 0.1f;


    // Config values.
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "image_tensor";
    private static final String[] OUTPUT_NAMES = {"detection_boxes", "detection_scores", "detection_classes", "num_detections"};
    private static final String MODEL_FILE = "file:///android_asset/ssd_inception_v2_coco.pb";
    private static final String LABEL_FILE = "file:///android_asset/mscoco_labels.txt";

    // Options
    private boolean logStats = false;
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final boolean MAINTAIN_ASPECT = true;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    private byte[] byteValues;
    private float[] outputLocations;
    private float[] outputScores;
    private float[] outputClasses;
    private float[] outputNumDetections;

    private TensorFlowInferenceInterface inferenceInterface;

    /* Constructor */
    public TensorflowObjectDetection(Context myContext) {

        AssetManager assetManager = myContext.getAssets();

        String actualFilename = LABEL_FILE.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                this.labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }

        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        final Graph g = this.inferenceInterface.graph();

        // The inputName node has a shape of [N, H, W, C], where
        // N is the batch size
        // H = W are the height and width
        // C is the number of channels (3 for our purposes - RGB)
        final Operation inputOp = g.operation(this.INPUT_NAME);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + this.INPUT_NAME + "'");
        }

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

        this.intValues = new int[INPUT_SIZE * INPUT_SIZE];
        this.byteValues = new byte[INPUT_SIZE * INPUT_SIZE * 3];
        this.outputScores = new float[MAX_RESULTS];
        this.outputLocations = new float[MAX_RESULTS * 4];
        this.outputClasses = new float[MAX_RESULTS];
        this.outputNumDetections = new float[1];
    }

    public List<RecognizedObjects> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            byteValues[i * 3 + 2] = (byte) (intValues[i] & 0xFF);
            byteValues[i * 3 + 1] = (byte) ((intValues[i] >> 8) & 0xFF);
            byteValues[i * 3 + 0] = (byte) ((intValues[i] >> 16) & 0xFF);
        }
        Trace.endSection(); // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(INPUT_NAME, byteValues, 1, INPUT_SIZE, INPUT_SIZE, 3);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(OUTPUT_NAMES, logStats);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        outputLocations = new float[MAX_RESULTS * 4];
        outputScores = new float[MAX_RESULTS];
        outputClasses = new float[MAX_RESULTS];
        outputNumDetections = new float[1];
        inferenceInterface.fetch(OUTPUT_NAMES[0], outputLocations);
        inferenceInterface.fetch(OUTPUT_NAMES[1], outputScores);
        inferenceInterface.fetch(OUTPUT_NAMES[2], outputClasses);
        inferenceInterface.fetch(OUTPUT_NAMES[3], outputNumDetections);
        Trace.endSection();

        // Find the best detections.
        final PriorityQueue<RecognizedObjects> pq =
                new PriorityQueue<RecognizedObjects>(
                        1,
                        new Comparator<RecognizedObjects>() {
                            @Override
                            public int compare(final RecognizedObjects lhs, final RecognizedObjects rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        // Scale them back to the input size.
        System.out.println("OutputLoc len: "+outputLocations.length);
        System.out.println("OutputScores len: "+outputScores.length);
        System.out.println("OutputClasses len: "+outputClasses.length);
        System.out.println("OutputNumDet len: "+outputNumDetections.length);
        for (int i = 0; i < outputScores.length; ++i) {
            System.out.println("Recognized: "+labels.get((int) outputClasses[i])+": "+outputScores[i]);
            final RectF detection =
                    new RectF(
                            outputLocations[4 * i + 1] * INPUT_SIZE,
                            outputLocations[4 * i] * INPUT_SIZE,
                            outputLocations[4 * i + 3] * INPUT_SIZE,
                            outputLocations[4 * i + 2] * INPUT_SIZE);
            pq.add(
                    new RecognizedObjects("" + i, labels.get((int) outputClasses[i]), outputScores[i], detection));
        }

        final ArrayList<RecognizedObjects> recognitions = new ArrayList<RecognizedObjects>();
        for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }

    public void enableStatLogging(boolean logStats) {
        this.logStats = logStats;
    }

    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    public void close() {
        inferenceInterface.close();
    }

}

