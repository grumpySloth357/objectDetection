package angrysloth357.speakeye;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Example of a call to a native method
    TextView tv = (TextView) findViewById(R.id.sample_text);
    tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /* Open the object dection activity which takes picture and detects objects in it :)*/
    public void startObjDetector(View view) {
        // Do something in response to button
        //1. Make an intent to communicate with new activity, needs => this:current context; DisplayMessageActivity.class: class of app to deliver intent to
        Intent intent = new Intent(this, DetectObjectsActivity.class);
        //Intent intent = new Intent(this, CameraActivity.class);
        //Start the ObjDetector :)
        startActivity(intent);
    }

    public void startCameraActivity(View view) {
        // Do something in response to button
        //1. Make an intent to communicate with new activity, needs => this:current context; DisplayMessageActivity.class: class of app to deliver intent to
        Intent intent = new Intent(this, CameraActivity.class);
        //Start the ObjDetector :)
        startActivity(intent);
    }
}
