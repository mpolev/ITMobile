package com.example.apivk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.perm.kate.api.Audio;
import com.perm.kate.api.Video;

import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Administrator on 26.01.14.
 */
public class DownloadActivity extends Activity {

    ListView downloadListView;
    ArrayAdapter<StringBuilder> adapter;
    ArrayList<Audio> audioInfoList;
    //ArrayList<Video> videoInfoList;
    ArrayList<StringBuilder> audioNamesList;
    //ArrayList<StringBuilder> videoNamesList;



    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);

        downloadListView = (ListView)findViewById(R.id.downloadListView);
        registerForContextMenu(downloadListView);

        downloadListView.setOnItemClickListener(soundClick);

        audioInfoList = new ArrayList<Audio>();
        audioNamesList = new ArrayList<StringBuilder>();
        //videoInfoList = new ArrayList<Video>();
        //videoNamesList = new ArrayList<StringBuilder>();


        ShowSoundList fm = new ShowSoundList();
        fm.execute();

    }


    ListView.OnItemClickListener soundClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            DownloadSound da = new DownloadSound();
            da.run(position);
        }
    };



    class ShowSoundList extends AsyncTask<Void, Void, Void> {

        StringBuilder sb = new StringBuilder();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //File myFolder = new File("sdcard/vkmusic/");
            //if(!myFolder.exists())
            //    myFolder.mkdirs();
            File f = new File("sdcard/vkmusic/");
            File[] arr = f.listFiles();
            String[] strarr = f.list();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                audioInfoList = DataOfSession.getInstance().api.getAudio(DataOfSession.getInstance().account.user_id,
                        null, null, null, "", "");
                //videoInfoList = DataOfSession.getInstance().api.getVideo("", DataOfSession.getInstance().account.user_id,
                //        0l, "", 10l, 0l, "");
                for(Audio c : audioInfoList){
                    sb = new StringBuilder();

                    sb.append(c.artist);
                    sb.append("-");
                    sb.append(c.title);

                    audioNamesList.add(sb);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            adapter = new ArrayAdapter<StringBuilder>(getApplicationContext(),
                    R.layout.custom_list_item, audioNamesList);
            downloadListView.setAdapter(adapter);

            //MediaPlayer mp = new MediaPlayer();
            //mp.set
        }
    }



    class DownloadSound extends AsyncTask<Void, Integer, Void> {

        StringBuilder sb = new StringBuilder();
        private int index;
        int downloadNotificationId, infoNotificationId;
        int sizeSound = 0;
        NotificationUtils notifUtils;

        public void run(int ind){
            index = ind;

            execute();
        }


        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();


        }*/

        @Override
        protected void onProgressUpdate(Integer... progress) {

            NotificationUtils.getInstance(getApplicationContext()).updateProgress(infoNotificationId, progress[0],
                    null);
        }


        @Override
        protected Void doInBackground(Void... params) {

            try {
                String[] forbiddenSymbols = new String[] {"<", ">", ":", "\"", "/", "\\", "|", "?", "*"}; // для windows
                String nameMusic = audioNamesList.get(index).toString();
                for (String forbiddenSymbol: forbiddenSymbols) {
                    nameMusic = StringUtils.replace(nameMusic, forbiddenSymbol, "");
                }

                String pathname = "sdcard/vkmusic/" + nameMusic;
                File destination = new File(pathname + ".mp3");

                URL source = new URL(audioInfoList.get(index).url);

                checkFilePath(destination);

                sizeSound = calculateFileSize(source);

                downloadFile(destination, nameMusic, pathname, source);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //adapter = new ArrayAdapter<Audio>(getApplicationContext(),
            //        R.layout.custom_list_item, a);
            //downloadListView.setAdapter(adapter);


            return null;
        }

        /*@Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }*/


        private void checkFilePath(File destination){

            try{
                if (destination.exists()) {
                    if (destination.isDirectory()) {
                        throw new IOException("File '" + destination + "' exists but is a directory");
                    }
                    if (destination.canWrite() == false) {
                        throw new IOException("File '" + destination + "' cannot be written to");
                    }
                } else {
                    File parent = destination.getParentFile();
                    if (parent != null) {
                        if (!parent.mkdirs() && !parent.isDirectory()) {
                            throw new IOException("Directory '" + parent + "' could not be created");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        private int calculateFileSize(URL source){

            HttpURLConnection conn = null;
            int result = 0;

            try {
                conn = (HttpURLConnection) source.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                result = conn.getContentLength();
            } catch (IOException e) {
                //return -1;
            } finally {
                conn.disconnect();
            }

            return result;
        }


        private void downloadFile(File destination, String nameMusic, String pathname, URL source){
            try {
                //destination.createNewFile();

                InputStream input = source.openStream();
                try {
                    FileOutputStream output = new FileOutputStream(destination, false);
                    try {
                        //IOUtils.copy(input, output);//Не находит этот класс и метод!!!

                        long count = 0;
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int n = 0;

                        createNotification(nameMusic);

                        int onePrecent = sizeSound / 100;
                        int countPrecent = 0;

                        while (-1 != (n = input.read(buffer))) {
                            output.write(buffer, 0, n);
                            count += n;
                            //co++;

                            if(count > onePrecent * 5)
                            {
                                count = 0;
                                countPrecent += 5;
                                //NotificationUtils.getInstance(getApplicationContext()).updateProgress(infoNotificationId, countPrecent,
                                //        sizeSound/1024);
                                publishProgress(countPrecent);
                            }
                        }
                        closeNotificationAfterDownload(100);


                        output.close(); // don't swallow close Exception if copy completes normally
                    } finally {
                        //IOUtils.closeQuietly(output);
                        try {
                            if ((Closeable)output != null) {
                                ((Closeable)output).close();
                            }
                        } catch (IOException ioe) {
                            // ignore
                        }
                    }
                } finally {
                    //IOUtils.closeQuietly(input);
                    try {
                        if ((Closeable)input != null) {
                            ((Closeable)input).close();
                        }
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }catch (FileNotFoundException e) {
                System.out.print("ERROR " + pathname);
            }
            catch (IOException e) {

                closeNotificationForError(0);

                e.printStackTrace();
            }
        }


        private void createNotification(String nameMusic){
            notifUtils = NotificationUtils.getInstance(getApplicationContext());
            infoNotificationId = notifUtils.createInfoNotification(nameMusic, "Загрузка");
            //downloadNotificationId = NotificationUtils.getInstance(getApplicationContext()).createDownloadNotification(nameMusic);
        }


        private void closeNotificationAfterDownload(int progress){
            NotificationUtils.getInstance(getApplicationContext()).updateProgress(infoNotificationId, progress,
                    "Загрузка завершена!");
            //NotificationUtils.getInstance(getApplicationContext()).closeNotification(downloadNotificationId);
            //notifUtils.createInfoNotification(nameMusic, "Загрузка завершена!");
        }


        private void closeNotificationForError(int progress){
            //NotificationUtils.getInstance(getApplicationContext()).closeNotification(downloadNotificationId);
            NotificationUtils.getInstance(getApplicationContext()).updateProgress(infoNotificationId, progress,
                    "Ошибка при загрузке");
        }
    }
}
