package main.SpeakEyE.app;

/**
 * Created by ntiwa on 11/19/2017.
 */

public class Output {

    private static String outputAudio = "Initial Stuff";

    public static String getAudio(){
        return outputAudio;
    }

    public static void SetAudio(String str) {
        outputAudio = new String(str);
    }
}
