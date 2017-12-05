package com.boredream.ktvkrcview;

/**
 * 一个歌词
 */
public class KrcWord {

    public KrcTime wordTime;
    public String wordStr;
    public int wordStartIndex;

    public KrcWord() {
        wordTime = new KrcTime();
        wordStr = "";
    }
}
