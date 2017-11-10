package angrysloth357.speakeye;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.view.View;
import java.util.List;
import java.util.Iterator;



public class DetectObjectsActivity extends AppCompatActivity {

    TensorflowObjectDetection objMod;
    Bitmap recognizedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_objects);

        /* Add camera buttons */
        //ImageButton imgBtn = (ImageButton) findViewById(R.id.detectBtn);
        //ImageView mImageView = (ImageView) findViewById(R.id.dispImage);

        /*Tensorflow model*/
        objMod = new TensorflowObjectDetection(getBaseContext());
    }

    /******************** IMAGE STUFF **************************************/
    /* Add intent to use the Camera */
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public void dispatchTakePictureIntent(View view) { //View was needed to use the camera :o
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    /*Get image and display it as imageview*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView mImageView = (ImageView) findViewById(R.id.image);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data"); //get bitmap
            /*Call the detector :)*/
            //TensorflowObjectDetection objMod = new TensorflowObjectDetection(getBaseContext());
            //recognizedImg =  Bitmap.createBitmap(imageBitmap);
            System.out.println(imageBitmap.getHeight()+"_"+imageBitmap.getWidth());
            List<RecognizedObjects> objDetected = objMod.recognizeImage(imageBitmap);
            //iterate through and print everything detected
            for (Iterator<RecognizedObjects> i = objDetected.iterator(); i.hasNext();) {
                RecognizedObjects item = i.next();
                System.out.println(item.toString());
            }
            //Display image on frame
            mImageView.setImageBitmap(recognizedImg);
        }
    }
    /*************************************************************************/
}
