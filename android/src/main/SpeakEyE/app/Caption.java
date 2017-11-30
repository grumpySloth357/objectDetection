package main.SpeakEyE.app;

/**
 * Created by skhad on 11/30/2017.
 */

public class Caption {
    private static int[] sentence; //array of caption word ids
    private static Object state; //Model state after generating the previous word.
    private static float logprob;
    private static Object metadata;

    public static float score;

    /*Constructor*/
    Caption(
            int[] sentence,
            Object state,
            float logprob,
            float score,
            Object metadata
    ) {
        this.sentence = sentence;
        this.state = state;
        this.logprob = logprob;
        this.score = score;
        this.metadata = metadata;
    }

    /*Return -1,0,1 if less than, equal to or greater than other Caption*/
    public int CompareTo(Caption other) {
        if (this.score==other.score) {
            return 0;
        } else if (this.score < other.score) {
            return -1;
        } else {
            return 1;
        }
    }
}

