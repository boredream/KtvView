package com.boredream.ktvkrcview;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * KTV样式双行歌词
 */
public class KrcKtvView extends FrameLayout {

    private KrcLineView krcLineViewTop;
    private TextView tvTimer;
    private View v;
    private KrcLineView krcLineViewBottom;
    private MediaPlayer player;

    public KrcKtvView(Context context) {
        super(context);
        init(context);
    }

    public KrcKtvView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KrcKtvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.live_krc_layout, null);
        v = view.findViewById(R.id.v);

        krcLineViewTop = (KrcLineView) view.findViewById(R.id.krc_top);
        tvTimer = (TextView) view.findViewById(R.id.tv_timer);

        krcLineViewBottom = (KrcLineView) view.findViewById(R.id.krc_bottom);
        krcLineViewBottom.setContainer(this, v);

        addView(view);
    }

    public void setPlayerAndKrc(MediaPlayer player, String krcFilePath) throws IOException {
        this.player = player;
        List<KrcLine> krcLines = KrcParse.parseKrcFile(krcFilePath);
        List<KrcLine> krcLinesTop = new ArrayList<>();
        List<KrcLine> krcLinesBottom = new ArrayList<>();
        for (int i = 0; krcLines != null && i < krcLines.size(); i++) {
            KrcLine krcLine = krcLines.get(i);
            if(i == krcLines.size() -1 && krcLine.lineTime.duration > krcLine.lineTime.startTime
                    && krcLine.wordTimes.size() > 0) {
                // 如果是最后一行特殊的duration（这个duration为歌曲结束时间）
                // 则将其修正为和其他歌词一样duration=最后一个歌词的结束时间
                KrcWord lastWord = krcLine.wordTimes.get(krcLine.wordTimes.size() - 1);
                krcLine.lineTime.duration = lastWord.wordTime.startTime + lastWord.wordTime.duration;
            }
            if (i % 2 == 0) {
                krcLinesTop.add(krcLine);
            } else {
                krcLinesBottom.add(krcLine);
            }
        }

        krcLineViewTop.setKrcLines(krcLinesTop);
        krcLineViewTop.setPlayer(this.player);
        krcLineViewBottom.setKrcLines(krcLinesBottom);
        krcLineViewBottom.setPlayer(this.player);
    }

    /**
     * 开始
     */
    public void start() {
        setVisibility(View.VISIBLE);
        krcLineViewTop.start();
        krcLineViewBottom.setTimer(tvTimer);
        krcLineViewBottom.start();
    }

    public void stop() {
        krcLineViewTop.stop();
        krcLineViewBottom.stop();
        tvTimer.setText("");
        setVisibility(View.GONE);
    }

}
