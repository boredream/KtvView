package com.boredream.ktvview;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.boredream.ktvkrcview.KrcKtvView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_play;
    private Button btn_start;
    private Button btn_stop;
    private KrcKtvView ktv;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        ktv = (KrcKtvView) findViewById(R.id.ktv);

        btn_play.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                togglePlay();
                break;
            case R.id.btn_start:
                initPlayer();
                break;
            case R.id.btn_stop:
                player.stop();
                ktv.stop();
                break;
        }
    }

    private void initPlayer() {
        try {
            String musicFile = Environment.getExternalStorageDirectory() + File.separator + "陈奕迅 - 兄妹.mp3";
            player = new MediaPlayer();
            player.setDataSource(musicFile);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                    ktv.start();
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    ktv.stop();
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    ktv.stop();
                    return false;
                }
            });
            player.prepareAsync();

            String krcFile = Environment.getExternalStorageDirectory() + File.separator + "陈奕迅 - 兄妹.krc";
            ktv.setPlayerAndKrc(player, krcFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay() {
        if (player == null) return;
        if (player.isPlaying()) {
            btn_play.setText("播放");
            player.pause();
        } else {
            btn_play.setText("暂停");
            player.start();
        }

    }
}
