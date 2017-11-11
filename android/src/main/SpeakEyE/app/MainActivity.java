package main.SpeakEyE.app;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.content.Intent;

import main.SpeakEyE.app.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchExample(View view) {
        Intent intent = new Intent(this, ClassifierActivity.class);
        startActivity(intent);
    }

    public void launchDetector(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
    }
}
