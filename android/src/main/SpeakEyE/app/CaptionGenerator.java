package main.SpeakEyE.app;

/**
 * Created by skhad on 11/30/2017.
 */


/*
    Generate captions from Show&Tell model
 */
public class CaptionGenerator {
    private static final short BEAM_SIZE=3;
    private static final short MAX_CAPTION_LENGTH = 20;
    /*If LENGTH_NORMALIZATION_FACTOR>0 longer captions are preferred*/
    private static final float LENGTH_NORMALIZATION_FACTOR = 0.0f;

    private CaptionModel model;
    private Vocabulary vocab;

    /*Constructor*/
    CaptionGenerator(
            CaptionModel model,
            Vocabulary vocab
    ) {
        this.model = model;
        this.vocab = vocab;
    }

    /*Pass image to CaptionModel and perform beamsearch to generate caption*/
    public String beam_search() {

        return "";
    }


}
