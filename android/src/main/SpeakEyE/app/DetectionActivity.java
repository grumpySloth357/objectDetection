package main.SpeakEyE.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Trace;
import android.util.Size;
import android.util.TypedValue;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import java.util.Iterator;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import main.SpeakEyE.app.OverlayView.DrawCallback;
import main.SpeakEyE.app.env.BorderedText;
import main.SpeakEyE.app.env.ImageUtils;
import main.SpeakEyE.app.env.Logger;
//import main.SpeakEyE.app.tracking.MultiBoxTracker;
import android.view.Display;

import main.SpeakEyE.app.R;


public class DetectionActivity extends CameraActivity implements ImageReader.OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final int INPUT_SIZE = 300; //224

    private static final String INPUT_NAME = "image_tensor";
    private static final String[] OUTPUT_NAMES = {"detection_boxes", "detection_scores", "detection_classes", "num_detections"};

    //private static final String MODEL_FILE = "file:///android_asset/ssd_inception_v2_coco.pb"; // takes 6s/image
    private static final String MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_coco.pb"; //~1-2s/image
    private static final String LABEL_FILE = "file:///android_asset/mscoco_labels.txt";

    private static final int TF_OD_API_INPUT_SIZE = 300; //300

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private static final boolean SAVE_PREVIEW_BITMAP = false;

    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private ResultsView resultsView;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private static final boolean MAINTAIN_ASPECT = true;


    private BorderedText borderedText;

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    private static final float TEXT_SIZE_DIP = 10;

    private boolean computing = false;


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector = TensorflowObjectDetection.create(
                    getAssets(), MODEL_FILE, LABEL_FILE, TF_OD_API_INPUT_SIZE);
        } catch (final IOException e) {
            LOGGER.e("Exception initializing classifier!", e);
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        resultsView = (ResultsView) findViewById(R.id.results);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

        sensorOrientation = rotation + screenOrientation;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbBytes = new int[previewWidth * previewHeight];
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        INPUT_SIZE, INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        yuvBytes = new byte[3][];

        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (computing) {
                image.close();
                return;
            }
            computing = true;

            Trace.beginSection("imageAvailable");

            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }

        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        System.out.println("Detection time: "+lastProcessingTimeMs);
                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

                        /*Alert on certain objects*/
                        String title;
                        for (final Classifier.Recognition result : results){
                            title = result.getTitle();
                            if (detector.getFlag(title) && result.getArea()>=0.2f) {
                                detector.updateFlagCount(title); //Update new flag counter
                                if (detector.getFlagCount(title) ==0 ) { //1st flag => notify
                                    Intent i = new Intent(getBaseContext(), AudioDescription.class);
                                    String str = "Warning " + title + " detected";
                                    i.putExtra("word", str);
                                    startService(i);
                                }
                            }
                        }

                        /*Set results on a box*/
                        resultsView.setResults(results);

                        /*Box around results?*/
                        /*
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        System.out.println("Canvas: "+canvas.getHeight()+", "+canvas.getWidth());
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            System.out.println("BEFORE: "+result.getTitle() + ":" + location + ", area: " + result.getArea());
                            if (location != null){// && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);

                                cropToFrameTransform.mapRect(location);
                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }
                        //Print cropped recognition
                        for (final Classifier.Recognition result : mappedRecognitions) {
                            final RectF location = result.getLocation();
                            System.out.println("MAPPED: "+result.getTitle() + ":" + location + ", area: " + result.getArea());
                        }

                        //Lets try drawing things..
                        final Paint textPaint = new Paint();
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextSize(60.0f);

                        final Paint boxPaint = new Paint();
                        boxPaint.setColor(Color.RED);
                        boxPaint.setAlpha(200);
                        boxPaint.setStyle(Style.STROKE);

                        for (final Classifier.Recognition result : mappedRecognitions) {
                            final RectF rect = result.getLocation();
                            canvas.drawRect(rect, boxPaint);
                            canvas.drawText("" + result.getTitle(), rect.left, rect.top, textPaint);
                            borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + result.getTitle());
                        }
                        */

                        /*********************************************************/


                        requestRender();
                        computing = false;
                    }
                });

        Trace.endSection();
    }

    @Override
    public void onSetDebug(boolean debug) {
        detector.enableStatLogging(debug);
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (detector != null) {
                String statString = detector.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }
}
