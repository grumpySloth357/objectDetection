package main.SpeakEyE.app;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by skhad on 11/30/2017.
 */

public class CaptionModel implements Classifier {

    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        return null;
    }

    @Override
    public void enableStatLogging(boolean debug) {

    }

    @Override
    public String getStatString() {
        return null;
    }

    @Override
    public void close() {

    }
}
