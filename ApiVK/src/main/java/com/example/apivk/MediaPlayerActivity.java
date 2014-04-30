package com.example.apivk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 15.02.14.
 */
public class MediaPlayerActivity extends Activity{

    SeekBar seekBar;
    TextView tv;
    MediaPlayer mp;
    ImageView iv, iv1, iv3;
    int imagePlay = R.drawable.playnormal;
    int imagePause = R.drawable.pausenormal;
    int position;

    ChangeSeekBarDuringPlayMusic changeSeekBarDuringPlayMusic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_list_item_for_music_list);

        position = getIntent().getExtras().getInt("position");

        mp = new MediaPlayer();

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tv = (TextView)findViewById(R.id.music_list_layout_title);
        iv = (ImageView)findViewById(R.id.music_list_layout_image);
        //iv.setImageResource(imagePlay);
        iv1 = (ImageView)findViewById(R.id.music_list_layout_image1);
        //iv1.setImageResource(imageBefore);
        iv3 = (ImageView)findViewById(R.id.music_list_layout_image3);
        //iv3.setImageResource(imageAfter);


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                    //if(play)
                    iv.setImageResource(imagePause);
                }
                else{
                    //seekBar.setMax(mp.getDuration());
                    mp.start();
                    changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
                    changeSeekBarDuringPlayMusic.execute();
                    seekChange(seekBar.getProgress());
                    iv.setImageResource(imagePlay);
                }
            }
        });


        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0)
                    position = DownloadedFilesActivity.arr.length;

                position--;
                //seekBar.setProgress(0);

                resetPlayer();
                //changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
                //changeSeekBarDuringPlayMusic.execute();
            }
        });


        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == DownloadedFilesActivity.arr.length-1)
                    position = -1;

                position++;
                //seekBar.setProgress(0);

                resetPlayer();
                //changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
                //changeSeekBarDuringPlayMusic.execute();
            }
        });


        //seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekChange(seekBar.getProgress());
            }
        });

        resetPlayer();
        //changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
        //changeSeekBarDuringPlayMusic.execute();
    }


    private void resetPlayer(){
        if(changeSeekBarDuringPlayMusic != null)
            changeSeekBarDuringPlayMusic.cancel(false);

        tv.setText(DownloadedFilesActivity.arr[position].getName());
        mp.reset();
        try {
            mp.setDataSource(DownloadedFilesActivity.arr[position].getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setMax(mp.getDuration());
        mp.start();
        changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
        changeSeekBarDuringPlayMusic.execute();
    }


    private void seekChange(Integer progress){
        if(mp.isPlaying()){
            mp.seekTo(progress);
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        mp.release();
    }





    class ChangeSeekBarDuringPlayMusic extends AsyncTask<Void, Void, Void> {

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvInfo.setText("Begin");
        }*/

        @Override
        protected Void doInBackground(Void... params) {

            int currentPosition= 0;
            int total = mp.getDuration();
            while (mp!=null && currentPosition<total && mp.isPlaying()) {
                try {
                    Thread.sleep(1000);
                    currentPosition= mp.getCurrentPosition();
                } catch (InterruptedException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
                seekBar.setProgress(currentPosition);
            }

            return null;
        }

        /*@Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }*/
    }
}
