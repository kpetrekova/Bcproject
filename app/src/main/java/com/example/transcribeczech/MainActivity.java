package com.example.transcribeczech;


import static android.text.TextUtils.indexOf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleText(View v) {
        EditText et = findViewById(R.id.userInput);
        String origText = et.getText().toString();
        String transcribedText = transcribe(origText.toLowerCase());
        ((TextView)findViewById(R.id.textView)).setText(origText + "\n\n" + transcribedText);
        ((TextView)findViewById(R.id.textView)).setMovementMethod(new ScrollingMovementMethod());
        et.setText("");
    }
    public String transcribe(String origText) {
        if (!origText.endsWith(" ")){
            origText += " ";
        }
        String[] text = origText.split("");
        StringBuilder transcribedText = new StringBuilder();
        String[] normalVocals = {"a","e","i","o","u","y"};
        String[] longVocals = {"á","é","í","ó","ú","ý"};
        String[] ipaVocals = {"a","ɛ","ɪ","o","u","ɪ"};
        String[] normalConsonantPairs = {"p","b","t","d","ť","ď","k","g","c","dz","č","dž","ř2","ř","f","v","s","z","š","ž","h2","h"};
        String[] ipaConsonantPairs = {"p","b","t","d","c","ɟ","k","g","t͜s","d͜z","t͜ʃ","d͜ʒ","r̝̊","r̝","f","v","s","z","ʃ","ʒ","x","ɦ"};

        for (int i = 0; i < text.length; i++) {

            // vocals
            if (text[i].matches("[aeiouy]")) {
                transcribedText.append(ipaVocals[getIndexOfString(normalVocals, text[i])]);
            }
            else if (text[i].matches("[áéíóúýů]")) {
                if (text[i].equals("ů")){
                    transcribedText.append("u:");
                } else if (text[i].equals("í") || text[i].equals("ý")) {
                    transcribedText.append("i:");
                } else {
                    transcribedText.append(ipaVocals[getIndexOfString(longVocals, text[i])]).append(":");
                }
            }

            else if (text[i].equals("c") && text[i+1].equals("h")) {
                transcribedText.append("ɣ");
                i ++;
            }

            // pair consonants
            else if (text[i].matches("[pbtdťďkgcčřfvszšžh]|dz|dž")) {
                boolean voiced = getIndexOfString(normalConsonantPairs, text[i]) % 2 == 1;

                // voiceless end of word
                if (text[i+1].equals(" ") && voiced) {          // i == text.length-1 ||
                    transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])-1]);
                } else {
                    transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                }
            }

            // articulatory assimilation of mn around kg
            else if (text[i].matches("[mn]") &&  (text[i + 1].matches("[kg]"))) {
                if (text[i].equals("n") && text[i + 1].matches("[kg]")) {
                    transcribedText.append("ŋ");
                } else {
                    transcribedText.append("ɱ");
                }
            }
            else if (text[i].equals("n") && text[i + 1].matches("i")) {
                transcribedText.append("ɲ");
            } else {
                transcribedText.append(text[i]);

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
}