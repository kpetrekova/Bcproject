package com.android.transcribeczech;


import android.util.Log;


public class PhoneticTranscription {
    private static final String[] normalVocals = {"a", "e", "i", "o", "u", "y", "ě"};
    private static final String[] longVocals = {"á", "é", "í", "ó", "ú", "ý"};
    private static final String[] ipaVocals = {"a", "ɛ", "ɪ", "o", "u", "ɪ", "jɛ"};
    private static final String[] toBePalatalConsonants = {"d", "t", "n"};
    private static final String[] ipaPalatalConsonants = {"ɟ", "c", "ɲ"};
    private static final String[] normalConsonantPairs = {"p", "b", "t", "d", "ť", "ď", "k", "g", "c", "č", "ř", "f", "v", "s", "z", "š", "ž", "h", "w","q","x", "m", "n", "r", "j", "l"};
    private static final String[] ipaConsonantPairs = {"p", "b", "t", "d", "c", "ɟ", "k", "g", "t͜s", "t͜ʃ", "r̝", "f", "v", "s", "z", "ʃ", "ʒ", "ɦ", "v","kv","ks", "m", "n", "r", "j", "l"};
    private static final String[] ipaVoicedConsonants = {"b", "d", "ɟ", "g", "r̝", "v", "z", "ʒ", "ɦ", "m", "n", "r", "j", "l", "ŋ", "ɱ", "ɲ"};
    private static final String[] ipaVoicelessConsonants = {"p", "t", "c", "k", "r̝̊", "f", "s", "ʃ", "x"};

    enum CharType {VOICED, VOICELESS, VOCAL, OTHER}


    public static String handleText(String origText) {

        try{
            return transcribe(origText.toLowerCase().replaceAll("[^\\sa-z0-9ěščřžýáíéťďňůúó]", ""));
        } catch (Exception e){
            Log.e("PhoneticTranscription", "Could not handle text. : "+ origText, e);
        } return origText;
    }
    public static String transcribe(String origText) {

        StringBuilder transcribedText = new StringBuilder();

        for (String word : origText.split(" ")) {

            if (MainActivity.map.containsKey(word)){
                word = MainActivity.map.get(word);
            }
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
                else if (text[i].equals("d") && word.matches("dce[rř].*|srdc.*")){
                    transcribedText.append("t͜s");
                    i++;   // skip "c" in original text
                }

                // simplification when two same consonants
                else if (text.length >= i+2 && (text[i].equals(text[i + 1]) && text[i].matches("[nkrtpsflvbmš]"))){
                    if (text[i].matches("[tn]") & text[i+2].matches("ií")){
                        transcribedText.append(ipaPalatalConsonants[getIndexOfString(toBePalatalConsonants, text[i])]);
                        i++;
                    } else if (text[i].matches("[tn]") & text[i+2].matches("ě")){
                        transcribedText.append(ipaPalatalConsonants[getIndexOfString(toBePalatalConsonants, text[i])]);
                        transcribedText.append("ɛ");
                        i+=2;
                    } else {
                        transcribedText.append(ipaConsonantPairs[getIndexOfString(normalConsonantPairs, text[i])]);
                    }
                    i++;
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
                else if (text[i].matches("m") && (text[i + 1].matches("[vf]"))) {
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

    public static int getIndexOfString(String[] list, String match) {
        int index = -1;
        for (int i = 0; i < list.length; i++) {
            if (match.equals(list[i])) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static String regressiveAssimilation(String transcribedText) {

        String[] words = transcribedText.split(" ");
        String[] text = transcribedText.split("");

        int wordIndex = words.length+1;
        boolean prep; // prep as preposition
        for (int i = text.length-2; i > -1 ; i--){
            prep  = false;
            CharType charI = getCharacterType(text[i]);

            CharType charII;

            // check one-syllable prepositions
            if (wordIndex <= words.length&&  wordIndex >= 2 && words[wordIndex-2].matches("v|k|s|z|bez|od|pod|nad|pr̝ɛs") && !words[wordIndex-1].matches("va:s|va:m|va:mɪ")){
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

            // process based on next letter
            if (charI.equals(CharType.VOICELESS) && charII.equals(CharType.VOICED) && !text[i].matches("[x ]") && !text[i+1].matches("v")) {  // ch and space can't become voiced, v doesn't make consonants voiced
                text[i] = ipaVoicedConsonants[getIndexOfString(ipaVoicelessConsonants, text[i])];
            } else if (prep && charI.equals(CharType.VOICED) && charII.equals(CharType.VOCAL)){ // one-syllable voiced preposition reaction to word starting with vocal
                text[i] = ipaVoicelessConsonants[getIndexOfString(ipaVoicedConsonants, text[i])];
            } else if (charI.equals(CharType.VOICED) && charII.equals(CharType.VOICELESS)) {
                text[i] = ipaVoicelessConsonants[getIndexOfString(ipaVoicedConsonants, text[i])];
            } else if (text[i].equals(" ")){
                wordIndex--;
            }
        }
        return arrayToString(text);
    }


    private static CharType getCharacterType(String ch){
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

    public static String arrayToString(String[] list){
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s);
        }
        return result.toString();
    }
}