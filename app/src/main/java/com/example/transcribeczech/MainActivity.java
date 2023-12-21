package com.example.transcribeczech;


import static android.text.TextUtils.indexOf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText et;
    private final String[] normalVocals = {"a","e","i","o","u","y","ě"};
    private final String[] longVocals = {"á","é","í","ó","ú","ý"};
    private final String[] ipaVocals = {"a","ɛ","ɪ","o","u","ɪ","jɛ"};
    private final String[] toBePalatalConsonants = {"d","t","n"};
    private final String[] ipaPalatalConsonants = {"ɟ","c","ɲ"};
    private final String[] normalConsonantPairs = {"p","b","t","d","ť","ď","k","g","c","dz","č","dž","ř2","ř","f","v","s","z","š","ž","h2","h"};
    private final String[] ipaConsonantPairs = {"p","b","t","d","c","ɟ","k","g","t͜s","d͜z","t͜ʃ","d͜ʒ","r̝̊","r̝","f","v","s","z","ʃ","ʒ","x","ɦ"};
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

        for (String word : origText.split(" ") ) {
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
                    i++;
                }

                // palatals
                else if (text[i].equals("m") && text[i + 1].matches("ě")) {
                    transcribedText.append("mɲɛ");
                    i++;
                } else if (text[i].matches("[dtn]") && text[i + 1].matches("[iíě]")) {
                    transcribedText.append(ipaPalatalConsonants[getIndexOfString(toBePalatalConsonants, text[i])]);
                    if (text[i + 1].equals("ě")) {
                        transcribedText.append("ɛ");
                        i++;
                    }
                }

                // pair consonants
                else if (text[i].matches("[pbtdťďkgcčřfvszšžh]|dz|dž")) {
                    boolean voiced = getIndexOfString(normalConsonantPairs, text[i]) % 2 == 1;

                    // voiceless end of word
                    if (text[i + 1].equals(" ") && voiced) {          // pozor na zápis dat z OCR
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i]) - 1]);
                    } else {
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                    }
                }

                // articulatory assimilation of mn around kg
                else if (text[i].matches("[mn]") && (text[i + 1].matches("[kg]"))) {
                    if (text[i].equals("n") && text[i + 1].matches("[kg]")) {
                        transcribedText.append("ŋ");
                    } else {
                        transcribedText.append("ɱ");
                    }

                } else {
                    transcribedText.append(text[i]);
                }
            }
        }
        return transcribedText.toString();
    }
    public int getIndexOfString(String[] list, String match){
        int index = -1;
        for (int i = 0; i < list.length; i++){
            if (match.equals(list[i])){
                index = i;
                break;
            }
        }
        return index;
    }


/*
    public String regressiveAssimilation(String transcribedText){
        String[] ipaVoicedConsonant = {"b","d","ɟ","g","r̝","v","z","ʒ","ɦ","m","n","r","j","l","ŋ","ɱ","ɲ"};
        String[] ipaVoicelessConsonant = {"p","t","c","k","r̝̊","f","s","ʃ","x"};



        String[] text = transcribedText.split("");
        StringBuilder assimilatedText = new StringBuilder();
        String typeI = " "; // text[i] -- voiced, voiceless, vocal
        String typeII = " "; // text [i+1] -- voiced, voiceless, vocal

        for (int i = text.length; i > 0; i--){
            typeI = consonantType(text[i]);
            typeII = consonantType(text[i+1]);
            if (typeI.equals(typeII)){
                assimilatedText.append(text[i]);
            }
            else if (typeI.equals("voiced") && typeII.equals("voiceless")){
                assimilatedText.append(ipaVoicelessConsonant[getIndexOfString(ipaVoicedConsonant, text[i])]);
            }
            else if (typeI.equals("voiceless") && typeII.equals("voiced")){
                assimilatedText.append(ipaVoicedConsonant[getIndexOfString(ipaVoicelessConsonant, text[i])]);
            }
            // if (text[i].equals(":")) podívej se ještě o jedno vedle
            else{
                assimilatedText.append(text[i]);
            }

        }

        return assimilatedText.reverse().toString();
    }

    public String consonantType(String ch) {
        if (ch.matches("[bdɟgr̝vzʒɦmnrjlŋɱɲ]")){
            return "voiced";
        }
        else if (ch.matches("[ptckr̝̊fsʃx]")){
            return "voiceless";
        }
        else if (ch.matches("[aɛɪoui]")){
            return "vocal";
        }
        else if (ch.matches(":")){
            return ":";
        }
        return "_";
    }
*/
}