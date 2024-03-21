package com.example.transcribeczech;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private ImageView iv;
    int SELECT_PICTURE = 200;   //
    private EditText et;
    private final String[] normalVocals = {"a", "e", "i", "o", "u", "y", "ě"};
    private final String[] longVocals = {"á", "é", "í", "ó", "ú", "ý"};
    private final String[] ipaVocals = {"a", "ɛ", "ɪ", "o", "u", "ɪ", "jɛ"};
    private final String[] toBePalatalConsonants = {"d", "t", "n"};
    private final String[] ipaPalatalConsonants = {"ɟ", "c", "ɲ"};
    private final String[] normalConsonantPairs = {"p", "b", "t", "d", "ť", "ď", "k", "g", "c", "č", "ř", "f", "v", "s", "z", "š", "ž", "h", "w","q","x","n"};
    private final String[] ipaConsonantPairs = {"p", "b", "t", "d", "c", "ɟ", "k", "g", "t͜s", "t͜ʃ", "r̝", "f", "v", "s", "z", "ʃ", "ʒ", "ɦ", "v","kv","ks","n"};

    enum CharType {VOICED, VOICELESS, VOCAL, OTHER}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.transcribedText);
        textView.setMovementMethod(new ScrollingMovementMethod());
        et = findViewById(R.id.userInput);
        iv = findViewById(R.id.imageView);
    }

    public void handleText(View v) {
        String origText = et.getText().toString();
        handleText(origText);
    }

    public void handleText(String origText) {
        //String origText = getTextFromImage();
        String transcribedText = transcribe(origText.toLowerCase().replaceAll("[^\\sa-z0-9ěščřžýáíéťďňůú]", ""));
        textView.setText(transcribedText);
        et.setText("");
    }


    public String getTextFromImage(Bitmap bitmap){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()){
            Toast.makeText(getApplicationContext(), "No text", Toast.LENGTH_SHORT).show();
        }
        else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                stringBuilder.append(myItem.getValue());
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }
        return null;
    }



    public void imageChooser(View v) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    iv.setImageURI(selectedImageUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        String text = getTextFromImage(bitmap);
                        handleText(text);
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
        }
    }




    public String transcribe(String origText) {

        StringBuilder transcribedText = new StringBuilder();

        for (String word : origText.split(" ")) {

            word += " ";
            String[] text = word.split("");

            for (int i = 0; i < text.length; i++) {

                // vocals
                if (text[i].matches("[aeiouyě]")) {
                    transcribedText.append(ipaVocals[getIndexOfString(normalVocals, text[i])]);
                } else if (text[i].matches("[áéíóúýů]")) {
                    if (text[i].equals("ů")) {
                        transcribedText.append("u:");
                    } else if (text[i].equals("í") || text[i].equals("ý")) {
                        transcribedText.append("i:");
                    } else {
                        transcribedText.append(ipaVocals[getIndexOfString(longVocals, text[i])]).append(":");
                    }
                }

                // ch
                else if (text[i].equals("c") && text[i + 1].equals("h")) {
                    transcribedText.append("x");
                    i++;    // skip "h" in original text
                }

                // dcera, srdce
                else if (text[i].equals("d") && word.matches("dcer.*|srdc.*")){
                    transcribedText.append("t͜s");
                    i++;   // skip "c" in original text
                }
                //x-denní
                else if (text[i].equals("n") && word.matches(".*denn.*")){
                    transcribedText.append("n");
                    i++;    // skip second "n" in original text
                }

                // palatals
                else if (text[i].equals("m") && text[i + 1].matches("ě")) {
                    transcribedText.append("mɲɛ");
                    i++; // skip "ě" after palatals, so it won't become "jɛ"
                }
                else if (text[i].matches("[dtn]") && text[i + 1].matches("[iíě]")) { // "technika" etc is for now palatal
                    if (i < 5 && word.matches("od.*|nad.*|pod.*|před.*|post.*|ad.*|red.*|in.*|en.*") && !word.matches("post[ií][hžt].*|postí|odiv.*|přediv.*|nadi[tv].*|podiv.*")) { // i < 5 because in first string array is "", and some prefixes have 4 characters
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                    } else if (word.matches(".*ing.*|.*iont.*|.*itid.*|.*itud.*|.*iád.*|.*ián.*|.*iatr.* |.*istik.*|.*[^vo][dtn]ikac.*|.*[dtn]iz[ou].*|.*[dtn]i[sz]m.*")&& !word.matches("nizozem.*|proti.*")){
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                    } else {
                        transcribedText.append(ipaPalatalConsonants[getIndexOfString(toBePalatalConsonants, text[i])]);
                    }

                    if (text[i + 1].equals("ě")) {
                        transcribedText.append("ɛ");
                        i++; // skip "ě" after palatals, so it won't become "jɛ"
                    }
                }

                // articulatory assimilation of n around kg
                else if (text[i].matches("n") && (text[i + 1].matches("[kg]"))) {
                    transcribedText.append("ŋ");
                }
                // articulatory assimilation of m before vf
                else if (text[i].matches("m") && (text[i + 1].matches("[kg]"))) {
                            transcribedText.append("ɱ");

                } else if (text[i].matches("[pbtdťďkgcčřfvszšžhwqx]")) {
                    transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                } else {
                    transcribedText.append(text[i]);
                }
            }

        }
        return regressiveAssimilation(transcribedText.toString());
    }

    public int getIndexOfString(String[] list, String match) {
        int index = -1;
        for (int i = 0; i < list.length; i++) {
            if (match.equals(list[i])) {
                index = i;
                break;
            }
        }
        return index;
    }

    public String regressiveAssimilation(String transcribedText) {
        String[] ipaVoicedConsonant = {"b", "d", "ɟ", "g", "r̝", "v", "z", "ʒ", "ɦ", "m", "n", "r", "j", "l", "ŋ", "ɱ", "ɲ"};
        String[] ipaVoicelessConsonant = {"p", "t", "c", "k", "r̝̊", "f", "s", "ʃ", "x"};
        String[] words = transcribedText.split(" ");

        String[] text = transcribedText.split("");
        int wordIndex = words.length+1;
        boolean prep; // prep as preposition
        for (int i = text.length-2; i > -1 ; i--){
            prep  = false;
            CharType charI = getCharacterType(text[i]);

            CharType charII;
            if (wordIndex <= words.length && words[wordIndex-2].matches("v|k|s|z|bez|od|pod|nad|pr̝ɛs")){      // check one-syllable prepositions
                String [] preposition = words[wordIndex-2].split("");
                if (text[i].matches(preposition[preposition.length-1])){  // check next word when on last letter
                    charII = getCharacterType(text[i+2]);
                    prep = true;
                } else {
                    charII = getCharacterType(text[i+1]);
                }
            } else {
                charII = getCharacterType(text[i+1]);
            }

            if (charI.equals(CharType.VOICELESS) && charII.equals(CharType.VOICED) && !text[i].matches("[x ]") && !text[i+1].matches("v")) {  // ch and space can't become voiced, v doesn't make consonants voiced
                text[i] = ipaVoicedConsonant[getIndexOfString(ipaVoicelessConsonant, text[i])];
            } else if (prep && charI.equals(CharType.VOICED) && charII.equals(CharType.VOCAL)){ // one-syllable voiced preposition reaction to word starting with vocal
                text[i] = ipaVoicelessConsonant[getIndexOfString(ipaVoicedConsonant, text[i])];
            } else if (charI.equals(CharType.VOICED) && charII.equals(CharType.VOICELESS)) {
                text[i] = ipaVoicelessConsonant[getIndexOfString(ipaVoicedConsonant, text[i])];
            } else if (text[i].equals(" ")){
                wordIndex--;
            }
        }
        return arrayToString(text);
    }


    public CharType getCharacterType(String ch){
        if (ch.matches("[bdɟgvzʒɦ]")) {
            return CharType.VOICED;
        } else if (ch.matches("[ptckfsʃx ]")) {
            return CharType.VOICELESS;
        } else if (ch.matches("[aɛɪoui:]")) {
            return CharType.VOCAL;
        } else {
            return CharType.OTHER;
        }
    }

    public String arrayToString(String[] list){
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s);
        }
        return result.toString();
    }
}

