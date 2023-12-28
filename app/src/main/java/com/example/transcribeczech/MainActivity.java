package com.example.transcribeczech;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText et;
    private final String[] normalVocals = {"a", "e", "i", "o", "u", "y", "ě"};
    private final String[] longVocals = {"á", "é", "í", "ó", "ú", "ý"};
    private final String[] ipaVocals = {"a", "ɛ", "ɪ", "o", "u", "ɪ", "jɛ"};
    private final String[] toBePalatalConsonants = {"d", "t", "n"};
    private final String[] ipaPalatalConsonants = {"ɟ", "c", "ɲ"};
    private final String[] normalConsonantPairs = {"p", "b", "t", "d", "ť", "ď", "k", "g", "c", "č", "ř", "f", "v", "s", "z", "š", "ž", "h", "w"};
    private final String[] ipaConsonantPairs = {"p", "b", "t", "d", "c", "ɟ", "k", "g", "t͜s", "t͜ʃ", "r̝", "f", "v", "s", "z", "ʃ", "ʒ", "ɦ", "v"};

    enum CharType {VOICED, VOICELESS, VOCAL, OTHER}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.transcribedText);
        textView.setMovementMethod(new ScrollingMovementMethod());
        et = findViewById(R.id.userInput);
    }

    public void handleText(View v) {
        String origText = et.getText().toString();
        String transcribedText = transcribe(origText.toLowerCase().replaceAll("[^\\sa-z0-9ěščřžýáíéťďňůú]", ""));
        textView.setText(origText + "\n\n" + transcribedText);
        et.setText("");
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

                // palatals
                else if (text[i].equals("m") && text[i + 1].matches("ě")) {
                    transcribedText.append("mɲɛ");
                    i++; // skip "ě" after palatals, so it won't become "jɛ"
                } else if (text[i].matches("[dtn]") && text[i + 1].matches("[iíě]")) { // ".*nismus" is for now palatal
                    if (i < 5 && word.matches("od.*|před.*|nad.*|pod.*|před.*|post.*|ad.*|red.*|in.*|en.*") && !word.matches("post[ií][hžt].*|postí|odiv.*|přediv.*|nadi[tv].*|podiv.*")) { // i < 5 because in first string array is "", and some prefixes have 4 characters
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                    } else {
                        transcribedText.append(ipaPalatalConsonants[getIndexOfString(toBePalatalConsonants, text[i])]);
                    }

                    if (text[i + 1].equals("ě")) {
                        transcribedText.append("ɛ");
                        i++; // skip "ě" after palatals, so it won't become "jɛ"
                    }
                }

                // articulatory assimilation of mn around kg
                else if (text[i].matches("[mn]") && (text[i + 1].matches("[kg]"))) {
                    if (text[i].equals("n") && text[i + 1].matches("[kg]")) {
                        transcribedText.append("ŋ");
                    } else {
                        transcribedText.append("ɱ");
                    }

                } else if (text[i].matches("[pbtdťďkgcčřfvszšžh]")) {
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

        for (int i = text.length-2; i > -1 ; i--){
            CharType charI = getCharacterType(text[i]);
            CharType charII = getCharacterType(text[i+1]);

            if (charI.equals(CharType.VOICELESS) && charII.equals(CharType.VOICED) && !text[i].matches("[xŋɱɲjlrmn ]")){  // regex for unpaired consonants and space
                text[i] = ipaVoicedConsonant[getIndexOfString(ipaVoicelessConsonant, text[i])];
            } else if (charI.equals(CharType.VOICED) && charII.equals(CharType.VOICELESS) && !text[i].matches("[xŋɱɲjlrmn ]")) { // regex for unpaired consonants and space
                text[i] = ipaVoicelessConsonant[getIndexOfString(ipaVoicedConsonant, text[i])];
            } else {
                continue;
            }
        }
        return arrayToString(text);
    }




    public CharType getCharacterType(String ch){
        if (ch.matches("[bdɟgvzʒɦ]")) {
            return CharType.VOICED;
        } else if (ch.matches("[ptckfsʃx ]")) {
            return CharType.VOICELESS;
        } else if (ch.matches("[aɛɪoui:]")) {   //asi zbytečné
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

