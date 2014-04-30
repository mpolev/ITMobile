package com.example.apivk;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.perm.kate.api.Audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 03.02.14.
 */
public class DownloadedFilesActivity extends Activity {

    ListView musicListView;
    ArrayAdapter<String> adapter;
    static File[] arr;
    String[] strarr;
    MediaPlayer mp;
    //int currentSound = -1;
    //int countClick = 0;
    CustomAdapter customAdapter;
    SeekBar seekBar;


    //SeekBar seekBar;
    TextView tv;
    //MediaPlayer mp;
    ImageView iv, iv1, iv3;
    int imagePlay = R.drawable.playnormal;
    int imagePause = R.drawable.pausenormal;
    int _position = 0;

    ChangeSeekBarDuringPlayMusic changeSeekBarDuringPlayMusic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_list_item_for_music_list);

        musicListView = (ListView)findViewById(R.id.musicListView);
        //registerForContextMenu(musicListView);

        mp = new MediaPlayer();

        File f = new File("sdcard/vkmusic/");
        if(!f.exists()){
            Toast toast = Toast.makeText(getApplicationContext(), "Папка пуста", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }
        arr = f.listFiles();
        strarr = f.list();

        customAdapter = new CustomAdapter(getApplicationContext(),
                R.layout.custom, strarr);
        //adapter = new ArrayAdapter(getApplicationContext(),
        //        android.R.layout.simple_list_item_1, strarr);

        musicListView.setAdapter(customAdapter);

        musicListView.setOnItemClickListener(soundClick);




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
                if(_position == 0)
                    _position = DownloadedFilesActivity.arr.length;

                _position--;
                customAdapter.pos = _position;
                customAdapter.notifyDataSetChanged();
                //seekBar.setProgress(0);

                resetPlayer();
                //changeSeekBarDuringPlayMusic = new ChangeSeekBarDuringPlayMusic();
                //changeSeekBarDuringPlayMusic.execute();
            }
        });


        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_position == DownloadedFilesActivity.arr.length-1)
                    _position = -1;

                _position++;
                customAdapter.pos = _position;
                customAdapter.notifyDataSetChanged();
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

        //resetPlayer();
        tv.setText(DownloadedFilesActivity.arr[_position].getName());
        mp.reset();
        try {
            mp.setDataSource(DownloadedFilesActivity.arr[_position].getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setMax(mp.getDuration());


    }


    private void seekChange(View v){
        if(mp.isPlaying()){
            SeekBar sb = (SeekBar)v;
            mp.seekTo(sb.getProgress());
        }
    }


    ListView.OnItemClickListener soundClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            _position = position;
            customAdapter.pos = position;
            customAdapter.notifyDataSetChanged();
            resetPlayer();
        }
    };


    @Override
    public void onDestroy(){
        super.onDestroy();

        mp.release();
        changeSeekBarDuringPlayMusic.cancel(false);
    }


    private void resetPlayer(){
        if(changeSeekBarDuringPlayMusic != null)
            changeSeekBarDuringPlayMusic.cancel(false);

        tv.setText(DownloadedFilesActivity.arr[_position].getName());
        mp.reset();
        try {
            mp.setDataSource(DownloadedFilesActivity.arr[_position].getPath());
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
