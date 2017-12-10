package main.SpeakEyE.app;

/**
 * Created by skhadka on 11/10/17.
 */

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.Graph;

import main.SpeakEyE.app.env.Logger;

import android.graphics.RectF;
import android.text.TextUtils;

import java.io.InputStream;
import java.util.HashMap;

public class TensorflowObjectDetection implements Classifier {
    private static final Logger LOGGER = new Logger();

    // Only return this many results.
    private static final int MAX_RESULTS = 100;

    // Config values.
    private String inputName;
    private int inputSize;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private HashMap<Integer, String> labels_map = new HashMap<Integer, String>(100);
    private HashMap<String, Boolean> flag_map = new HashMap<String, Boolean>(100);
    private final int COUNT_THRESHOLD = 5;
    protected HashMap<String, Integer> flag_counter = new HashMap<String, Integer>(100);
    private int[] intValues;
    private byte[] byteValues;
    private float[] outputLocations;
    private float[] outputScores;
    private float[] outputClasses;
    private float[] outputNumDetections;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    public static Classifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize) throws IOException {
        final TensorflowObjectDetection d = new TensorflowObjectDetection();

        InputStream labelsInput = null;
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        labelsInput = assetManager.open(actualFilename);
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        Integer id;
        Integer flag;
        while ((line = br.readLine()) != null) {
            LOGGER.w(line);
            d.labels.add(line);
            String[] arr = line.split(":"); //Input format number: Label
            id = Integer.parseInt(arr[0]);
            d.labels_map.put(id, arr[1]);
            flag = Integer.parseInt(arr[2]);
            if (flag==1)
                d.flag_map.put(arr[1], true);
            else if (flag==0)
                d.flag_map.put(arr[1], false);
            else
                throw new RuntimeException("Unrecognized flag");
            d.flag_counter.put(arr[1], 0);
        }
        br.close();


        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        final Graph g = d.inferenceInterface.graph();

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
        return d;
    }

    private TensorflowObjectDetection() {}

    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            byteValues[i * 3 + 2] = (byte) (intValues[i] & 0xFF); //Green
            //System.out.println("2"+byteValues[i * 3 + 2]);
            byteValues[i * 3 + 1] = (byte) ((intValues[i] >> 8) & 0xFF); //Blue
            //System.out.println("1"+byteValues[i * 3 + 1]);
            byteValues[i * 3 + 0] = (byte) ((intValues[i] >> 16) & 0xFF); //Red
            //System.out.println(" 0"+byteValues[i * 3 + 0]);
        }
        Trace.endSection(); // preprocessBitmap
        /*Test: make image from byteArray*/
//        int height = (int) bitmap.getHeight();
//        int width = (int) bitmap.getWidth();
//        Bitmap test_image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        int column = 0;
//        int row = 0;
//        int count = 0;
//        while(row < height)
//        {
//            column = 0;
//            while(column < width)
//            {
//
//                int red_pos = 3*(row*width + column);
//                int blue_pos = red_pos+1;
//                int green_pos = blue_pos+1;
//                test_image.setPixel(column, row, Color.rgb(byteValues[red_pos], byteValues[green_pos], byteValues[blue_pos]));
//                if (byteValues[red_pos]==0 && byteValues[blue_pos]==0 && byteValues[green_pos]==0) {
//                    count ++;
//                }
//                column++;
//            }
//            row++;
//        }
//        System.out.println("#0 pixels: " + count);
//        //ImageView testIm = (ImageView) findViewById(R.id.testImage);
//        ImageUtils.saveBitmap(test_image);
        /*********************************/

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, byteValues, 1, inputSize, inputSize, 3);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, logStats);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        outputLocations = new float[MAX_RESULTS * 4];
        outputScores = new float[MAX_RESULTS];
        outputClasses = new float[MAX_RESULTS];
        outputNumDetections = new float[1];
        inferenceInterface.fetch(outputNames[0], outputLocations);
        inferenceInterface.fetch(outputNames[1], outputScores);
        inferenceInterface.fetch(outputNames[2], outputClasses);
        inferenceInterface.fetch(outputNames[3], outputNumDetections);
        Trace.endSection();

        // Find the best detections.
        final PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        1,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(final Recognition lhs, final Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        // Scale them back to the input size.
        //System.out.println("OutputScores.length: "+outputScores.length);
        HashSet<String> hash_Set = new HashSet<String>(20);
        for (int i = 0; i < outputScores.length; ++i) {
            //System.out.println(i+": OutputClass: "+outputClasses[i]);
            //System.out.println(i+": Detected: "+labels.get((int) outputClasses[i])+"\t Confidence: "+outputScores[i]);
            //System.out.println(i+": Detected: "+labels_map.get((int) outputClasses[i])+"\t Confidence: "+outputScores[i]);
            //System.out.println("Detected <FLAG> "+ flag_map.get((int) outputClasses[i]));
            if (outputScores[i]>=0.3f) { /*Only add things if they are over 0.3 threshold*/
                hash_Set.add(labels_map.get((int) outputClasses[i]));
                final RectF detection =
                                        new RectF(
                            outputLocations[4 * i + 1] * inputSize,
                            outputLocations[4 * i] * inputSize,
                            outputLocations[4 * i + 3] * inputSize,
                            outputLocations[4 * i + 2] * inputSize);
                final float area = (detection.width()*detection.height())/(inputSize*inputSize);
                pq.add(
                        //new Recognition("" + i, labels.get((int) outputClasses[i]), outputScores[i], detection));
                        new Recognition("" + i, labels_map.get((int) outputClasses[i]), outputScores[i], detection, area));
            }
        }
        //Hashset into string...
        String storageStr = TextUtils.join(" ", hash_Set);
        System.out.println(storageStr);
        //Output.SetAudio(storageStr);

        //System.out.println("Got out of recognition loop!!");
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }

    /*Return flag for an object id*/
    @Override
    public Boolean getFlag(String title) {
        return flag_map.get(title);
    }

    /*Get Flag count*/
    public Integer getFlagCount(String title) {return flag_counter.get(title);}

    /*Set Flag Count*/
    public void updateFlagCount(String title) {
        int count = this.flag_counter.get(title);
        if (count>COUNT_THRESHOLD) { //reset counter
            this.flag_counter.put(title, 0);
        } else { //increase counter
            this.flag_counter.put(title, count+1);
        }
    }

    @Override
    public void enableStatLogging(final boolean logStats) {
        this.logStats = logStats;
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }
}
