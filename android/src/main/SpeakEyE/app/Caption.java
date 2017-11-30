package main.SpeakEyE.app;

import android.graphics.Paint;

import java.util.Comparator;

/**
 * Created by skhad on 11/30/2017.
 */

public class Caption implements Comparable<Caption>{
    private static int[] sentence; //array of caption word ids
    private static Object state; //Model state after generating the previous word.
    private static float logprob;
    private static Object metadata;

    public static float score;

    /*Constructor*/
    public Caption(
            int[] sentence,
            Object state,
            float logprob,
            float score,
            Object metadata
    ) {
        super();
        this.sentence = sentence;
        this.state = state;
        this.logprob = logprob;
        this.score = score;
        this.metadata = metadata;
    }

    /*Return -1,0,1 if less than, equal to or greater than other Caption*/
    public int compareTo(Caption other) {
        if (this.score==other.score) {
            return 0;
        } else if (this.score < other.score) {
            return -1;
        } else {
            return 1;
        }
    }

    /*Build comparator for sorting captions based on scores: To be used with Array.sort(arr, Caption.CaptionComparator)*/
    public static Comparator<Caption> CaptionComparator
            = new Comparator<Caption>() {

        public int compare(Caption c1, Caption c2) {
            return c1.compareTo(c2); //Ascending order
            //return c2.compareTo(c1); //Descending order
        }

    };
}

