package com.android.transcribeczech;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {

    @Test
    public void test1() {
        assertEquals(PhoneticTranscription.handleText("Nechť již hříšné saxofony ďáblů rozezvučí síň úděsnými tóny waltzu, tanga a quickstepu."),
                "nɛxc jɪʃ ɦr̝i:ʃnɛ: saksofonɪ ɟa:blu: rozɛzvut͜ʃi: si:ň u:ɟɛsni:mɪ to:nɪ valdzu taŋga a kvuɪt͜skstɛpu ");
    }

    @Test
    public void test2() {
        assertEquals(PhoneticTranscription.handleText("Příliš žluťoučký kůň úpěl ďábelské ódy."),
                "pr̝i:lɪʃ ʒlucout͜ʃki: ku:ň u:pjɛl ɟa:bɛlskɛ: o:dɪ ");
    }

    @Test
    public void test3() {
        assertEquals(PhoneticTranscription.handleText("Lenka a Jakub rádi chodí do knihovny."),
                "lɛŋka a jakup ra:ɟɪ xoɟi: do kɲɪɦovnɪ ");
    }

    @Test
    public void test4() {
        assertEquals(PhoneticTranscription.handleText("enigma"),
                "ɛnɪgma ");
    }

    @Test
    public void test5() {
        assertEquals(PhoneticTranscription.handleText("dceřino srdce"),
                "t͜sɛr̝ɪno srt͜sɛ ");
    }

    @Test
    public void test6() {
        assertEquals(PhoneticTranscription.handleText("nejjednodušší měkký polštář"),
                "nɛjjɛdnoduʃi: mɲɛki: polʃta:r̝ ");
    }


    @Test
    public void test7() {
        assertEquals(PhoneticTranscription.handleText("mě nehledej"),
                "mɲɛ nɛɦlɛdɛj ");
    }

    @Test
    public void test8() {
        assertEquals(PhoneticTranscription.handleText("tramvaj pod taškou"),
                "traɱvaj pot taʃkou ");
    }

    @Test
    public void test9() {
        assertEquals(PhoneticTranscription.handleText("beztvarý obraz"),
                "bɛstvari: obras ");
    }

}