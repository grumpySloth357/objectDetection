package main.SpeakEyE.app;

/**
 * Created by ntiwa on 11/17/2017.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;

import java.util.Locale;


public class AudioDescription extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private String word;
    private boolean isInit;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(),this);
        handler = new Handler();
        if(isInit){
            speak(word);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacksAndMessages(null);
        word = intent.getStringExtra("word");

        if(isInit){
            speak(word);
        }
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                stopSelf();
            }
        },15*1000);
        return AudioDescription.START_NOT_STICKY;

    }

    private void speak(String speech) {
        speech = ""+word;
        if (tts != null){
            tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                speak(word);
                isInit = true;
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
