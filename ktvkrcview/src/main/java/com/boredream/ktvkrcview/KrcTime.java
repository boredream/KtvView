package com.boredream.ktvkrcview;

/**
 * krc歌词时间
 */
public class KrcTime {
    /**
     * 开始时间
     */
    public long startTime;

    /**
     * 时长
     */
    public long duration;

    public KrcTime() {
    }

    public KrcTime(long startTime, long spanTime) {
        this.startTime = startTime;
        this.duration = spanTime;
    }
}
