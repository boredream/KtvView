package com.boredream.ktvkrcview;

import java.util.ArrayList;
import java.util.List;

/**
 * 一行歌词
 */
public class KrcLine {

    public KrcTime lineTime;
    public List<KrcWord> wordTimes;
    public String lineStr;

    public KrcLine() {
        lineTime = new KrcTime();
        wordTimes = new ArrayList<>();
        lineStr = "";
    }
}
