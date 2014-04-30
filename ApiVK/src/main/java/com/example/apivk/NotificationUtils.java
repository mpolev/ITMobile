package com.example.apivk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.perm.kate.api.Audio;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 01.02.14.
 */
public class NotificationUtils{
    private static final String TAG = NotificationUtils.class.getSimpleName();

    private static NotificationUtils instance;

    private static Context context;
    private NotificationManager manager; // Системная утилита, упарляющая уведомлениями
    private int lastId = 0; //постоянно увеличивающееся поле, уникальный номер каждого уведомления
    //private HashMap<Integer, Notification> notifications; //массив ключ-значение на все отображаемые пользователю уведомления
    //NotificationCompat.Builder nb;
    private HashMap<Integer, NotificationCompat.Builder> notifications; //массив ключ-значение на все отображаемые пользователю уведомления


    //приватный контструктор для Singleton
    private NotificationUtils(Context context){
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notifications = new HashMap<Integer, Notification>();
        notifications = new HashMap<Integer, NotificationCompat.Builder>();
    }
    /**
     * Получение ссылки на синглтон
     */
    public static NotificationUtils getInstance(Context context){
        if(instance==null){
            instance = new NotificationUtils(context);
        } //else{
          //  instance.context = context;
        //}
        return instance;
    }


    public void updateProgress(int id, int progress, String message){

        //notifications.get(id).contentView.setProgressBar(
                //R.id.notification_download_layout_progressbar, 100, progress, false);

        if(message != null)
            notifications.get(id).setContentTitle(message);
        notifications.get(id).setProgress(100, progress, false);
        notifications.get(id).setDefaults(0);

        //Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(id, notifications.get(id).getNotification()); // отображаем его пользователю.

        // Задаем текст
        //notifications.get(id).contentView.setTextViewText(
        //        R.id.notification_download_layout_title, Integer.toString(progress).
        //        concat(" / ").concat(Integer.toString(maxSize)));
        //'\n' + Integer.toString(progress) + " / " + maxSize);
// Уведомляем об изменении

        //manager.notify(id, notifications.get(id));
    }


    public void closeNotification(int id){
        manager.cancel(id);
    }


    public int createInfoNotification(String message, String tittle){
        Intent notificationIntent = new Intent(context, DownloadedFilesActivity.class); // по клику на уведомлении откроется HomeActivity
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
//NotificationCompat.Builder nb = new NotificationBuilder(context) //для версии Android > 3.0
                .setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(message) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle(tittle) //заголовок уведомления
                .setDefaults(Notification.DEFAULT_ALL)// звук, вибро и диодный индикатор выставляются по умолчанию
                .setProgress(100, 0, false);

        Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(lastId, notification); // отображаем его пользователю.
        //notifications.put(lastId, notification); //теперь мы можем обращаться к нему по id
        notifications.put(lastId, nb);
        return lastId++;
    }



    /**
     * Создание уведомления с прогрессбаром о загрузке
     * @param fileName - текст, отображённый в заголовке уведомления.
     */
    /*public int createDownloadNotification(String fileName){
        String text = context.getString(R.string.notification_downloading).concat(" ").concat(fileName); //текст уведомления
        RemoteViews contentView = createProgressNotification(text, context.getString(R.string.notification_downloading)); //View уведомления
        contentView.setImageViewResource(R.id.notification_download_layout_image, R.drawable.ic_launcher); // иконка уведомления
        return lastId++; //увеличиваем id, которое будет соответствовать следующему уведомлению
    }*/

    /**
     * генерация уведомления с ProgressBar, иконкой и заголовком
     *
     * @param text заголовок уведомления
     * @param topMessage сообщение, уотображаемое в закрытом статус-баре при появлении уведомления
     * @return View уведомления.
     */
    /*private RemoteViews createProgressNotification(String text, String topMessage) {
        Notification notification = new Notification(R.drawable.ic_launcher, topMessage, System.currentTimeMillis());
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_download_layout);
        contentView.setProgressBar(R.id.notification_download_layout_progressbar, 100, 0, false);
        contentView.setTextViewText(R.id.notification_download_layout_title, text);

        notification.contentView = contentView;
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;

        Intent notificationIntent = new Intent(context, NotificationUtils.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        manager.notify(lastId, notification);
        notifications.put(lastId, notification);
        return contentView;
    }*/
}
