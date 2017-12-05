package com.boredream.ktvkrcview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * 单行歌词
 */
@SuppressLint("AppCompatCustomView")
public class KrcLineView extends TextView {

    //线性渲染器
    private LinearGradient mLinearGradient;
    //相对位置信息
    private float positionX;

    /**
     * 音乐播放器
     */
    private MediaPlayer player;

    /**
     * 歌词列表
     */
    private List<KrcLine> mKrcLines;

    /**
     * 是否正在播放
     */
    private boolean isPlaying;

    /**
     * 歌词颜色
     */
    private int color = Color.WHITE;

    /**
     * 歌词阴影颜色
     */
    private int shadowColor = 0xFF14ECEC;

    private TextView timer;

    private KrcKtvView container;
    private View view;

    public void setContainer(KrcKtvView container, View view) {
        this.container = container;
        this.view = view;
    }

    public void setTimer(TextView timer) {
        this.timer = timer;
    }

    public void setKrcLines(List<KrcLine> krcLines) {
        mKrcLines = krcLines;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public KrcLineView(Context context) {
        super(context);
        init(context);
    }

    public KrcLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setSingleLine(true);
    }

    private Handler handler = new Handler();
    private Runnable update = new Runnable() {
        public void run() {

            if (mKrcLines == null || mKrcLines.size() == 0) {
                // 无歌词时
                setText("歌词获取失败");

                if (isPlaying) {
                    handler.postDelayed(update, 200);
                }
                return;
            }

            // 整个音乐的播放进度
            long progressOfMusic = player.getCurrentPosition();

            // 遍历每一行歌词
            KrcLine kl;
            for (int i = 0; i < mKrcLines.size(); i++) {
                kl = mKrcLines.get(i);
                // 重置当前行进度位置
                positionX = 0.0f;

                // 根据当前播放时间，获取正在播放的那行歌词
                if (kl.lineTime.startTime <= progressOfMusic &&
                        progressOfMusic < kl.lineTime.startTime + kl.lineTime.duration) {
                    setCurText(kl);

                    // 针对当前行的播放进度
                    long progressOfLine = progressOfMusic - kl.lineTime.startTime;

                    // 遍历这行歌词里每一个字
                    KrcWord word;
                    for (int j = 0; j < kl.wordTimes.size(); j++) {
                        word = kl.wordTimes.get(j);

                        if (word.wordTime.startTime <= progressOfLine &&
                                progressOfLine < word.wordTime.startTime + word.wordTime.duration) {
                            // 之前word长度
                            float preWordsLength = word.wordStartIndex;
                            // 当前word长度，用时间算出当word进度，然后除以word文字长度算出word长度
                            float curWordsLength = (progressOfLine - word.wordTime.startTime) * 1.0f / word.wordTime.duration * word.wordStr.length();
                            // 最后算出正在播放的进度
                            positionX = (preWordsLength + curWordsLength) / kl.lineStr.length();
                            break;
                        } else if (j < kl.wordTimes.size() - 1 && word.wordTime.startTime + word.wordTime.duration > progressOfLine
                                && progressOfLine < kl.wordTimes.get(j + 1).wordTime.startTime) {
                            // 播放完一个字，如果有下一个字，且尚未播放时
                            float preWordsLength = word.wordStartIndex;
                            positionX = preWordsLength / kl.lineStr.length();
                            break;
                        }
                    }
                    break;
                } else if (i == mKrcLines.size() - 1) {
                    // 如果已经是最后一行
                    if (progressOfMusic > kl.lineTime.startTime + kl.lineTime.duration) {
                        // 如果已经播放完，则一直停留在高亮状态
                        setCurText(kl);
                        positionX = 1f;
                    }
                    break;
                } else if (progressOfMusic > kl.lineTime.startTime + kl.lineTime.duration &&
                        progressOfMusic < mKrcLines.get(i + 1).lineTime.startTime) {
                    // 如果本行已经播放完，还没播放下一行
                    if (progressOfMusic - (kl.lineTime.startTime + kl.lineTime.duration) < 500) {
                        // 本行先停留半秒
                        setCurText(kl);
                        positionX = 1f;
                    } else {
                        // 停留完5秒后，若有下一行则先显示下一行文字
                        setCurText(mKrcLines.get(i + 1));
                    }
                    break;
                } else if (i == 0) {
                    // 如果是第一行, 则音乐开始之前就set
                    if (progressOfMusic < kl.lineTime.startTime) {
                        setCurText(kl);
                    }
                }
            }

            postInvalidate();

            if (isPlaying) {
                handler.postDelayed(update, 50);
            }
        }
    };

    private String lastString = null;

    private void setCurText(KrcLine kl) {
        if (lastString != null && lastString.equals(kl.lineStr)) {
            // 重复行
            return;
        }

        lastString = kl.lineStr;

        setText(kl.lineStr);

        if (container == null) {
            // 第一行不处理
            return;
        }

        if (needScroll()) {
            if (view != null) {
                view.getLayoutParams().width = 0;
                view.requestLayout();
            }
        } else {
            if (view != null) {
                view.getLayoutParams().width = (int) (getNeedScrollWidth() - getTextLen());
                view.requestLayout();
            }
        }
    }

    /**
     * 开始
     */
    public void start() {
        isPlaying = true;
        handler.post(update);
    }

    public void stop() {
        isPlaying = false;
    }

    private int getNeedScrollWidth() {
        int width;
        if (container == null) {
            width = getWidth();
        } else {
            width = container.getWidth() - dp2px(48);
        }
        return width;
    }

    private boolean needScroll() {
        // 明显超过一小段的时候才需要滚动
        return getTextLen() - getNeedScrollWidth() > dp2px(6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 怎么样不在onDraw里new LinearGradient对象？？？
        mLinearGradient = new LinearGradient(
                0, 0,
                getTextLen(), 0,
                new int[]{shadowColor, color},
                new float[]{positionX, positionX},
                Shader.TileMode.CLAMP);

        // 如果当前文字宽度大于空间宽度，则字幕播放到中间位置时，让字幕开始滚动
        float currentTextPosition = getTextLen() * positionX;

        // 滚动偏移量
        int offset = 0;

        if (needScroll()) {
            // 如果文字明显超出
            if (currentTextPosition > getNeedScrollWidth() / 2) {
                // 如果当前文字播放超过一半，则开始滚动
                offset = (int) (currentTextPosition - getNeedScrollWidth() / 2);
                if (offset > getTextLen() - getNeedScrollWidth()) {
                    // 滚动到最右边的时候，停止滚动
                    offset = (int) (getTextLen() - getNeedScrollWidth());
                }
            }
        }

        if ((offset == 0 && currentTextPosition == 0) ||
                (offset > 0 && currentTextPosition > 0)) {
            scrollTo(offset, 0);
        }

        getPaint().setShader(mLinearGradient);

        if (player != null && timer != null) {
            int duration = player.getDuration();
            int position = player.getCurrentPosition();

            int m = (int) ((1.0f * duration - position) / 1000 / 60);
            int s = (duration - position - m * 60 * 1000) / 1000;
            String timerText = getTimeStr(m) + ":" + getTimeStr(s);
            if (timer.getText() == null || !timer.getText().toString().equals(timerText)) {
                timer.setText(timerText);
            }
        }
    }

    private String getTimeStr(int time) {
        if (time < 0) {
            return "00";
        } else if (time < 10) {
            return "0" + time;
        } else {
            return "" + time;
        }
    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private float getTextLen() {
        Rect bounds = new Rect();
        String text = getText().toString();
        Paint textPaint = getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

}
