package main.SpeakEyE.app;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import main.SpeakEyE.app.env.Logger;

/**
 * Created by skhad on 11/29/2017.
 */

public class Vocabulary {
    private static final Logger LOGGER = new Logger();

    private int start_id;
    private int end_id;
    private int unk_id;

    private HashMap<String, Integer> vocab = new HashMap<String, Integer>(12000);
    private HashMap<Integer, String> reverse_vocab = new HashMap<Integer, String>(12000);

    /*Constructor*/
    Vocabulary(
            final AssetManager assetManager
    ) throws IOException {
        //String vocab_file="file:///android_asset/word_counts.txt";
        String vocab_file = "word_counts.txt";
        String start_word="<S>";
        String end_word="</S>";
        String unk_word="<UNK>";

        /*Read buffered vocab file*/
        InputStream vocabInput = null;
        vocabInput = assetManager.open(vocab_file);
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(vocabInput));
        String line;
        Integer i = 0;
        while ((line = br.readLine()) != null) {
            //LOGGER.w(line);
            String[] arr = line.split(" "); //Input format number: Label
            reverse_vocab.put(i, arr[0]);
            vocab.put(arr[0], i);
            i++;
        }
        br.close();

        /*Add unk_word in vocab*/
        if (vocab.get(unk_word)==null) {
            vocab.put(unk_word, i);
            reverse_vocab.put(i, unk_word);
        }

        /*Save special word IDs*/
        start_id = vocab.get(start_word);
        end_id = vocab.get(end_word);
        unk_id = vocab.get(unk_word);
    }

    /*Return integer corresponding to word, unknown word id otherwise*/
    public Integer word_to_id(String word) {
        Integer index = vocab.get(word);
        if (index !=null) {
            return index;
        } else {
            return unk_id;
        }
    }

    /*Return string corresponding to id or unknown otherwise*/
    public String id_to_word(Integer id) {
        String str = reverse_vocab.get(id);
        if (str!=null) {
            return str;
        } else {
            return reverse_vocab.get(unk_id);
        }
    }

}
