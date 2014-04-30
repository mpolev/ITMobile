package com.example.apivk;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 08.01.14.
 */
public class DataOfSession {

    public HashMap<Long, StringBuilder> friendsMap;
    public Account account;
    public Api api;

    private CreateFriendsMap fm;


    private static DataOfSession instance = null;
    public static DataOfSession getInstance() {
        if(instance == null) {
            instance = new DataOfSession();
        }
        return instance;
    }


    private DataOfSession() {
        account=new Account();
    }


    public void connect(Context context){
        //Восстановление сохранённой сессии
        account.restore(context);

        //Если сессия есть создаём API для обращения к серверу
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
    }


    public void logOut(Context context) {
        api=null;
        account.access_token=null;
        account.user_id=0;
        account.save(context);
    }


    public void CreateFriendsMap(){

        fm = new CreateFriendsMap();
        fm.execute();
    }



    class CreateFriendsMap extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            friendsMap = new HashMap<Long, StringBuilder>();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                ArrayList<User> b = api.getFriends(account.user_id, "first_name, last_name", 0, "", "");
                for(User a : b){
                    StringBuilder sb = new StringBuilder();

                    sb.insert(sb.length(), a.first_name);
                    sb.insert(sb.length(), " ");
                    sb.insert(sb.length(), a.last_name);

                    friendsMap.put(a.uid, sb);
                }

                StringBuilder sb = new StringBuilder();
                sb.insert(sb.length(), "Я");
                friendsMap.put(account.user_id, sb);

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        /*@Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }*/
    }
}
