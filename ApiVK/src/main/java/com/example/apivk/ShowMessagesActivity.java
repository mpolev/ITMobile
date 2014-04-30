package com.example.apivk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.Message;
import com.perm.kate.api.User;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by USER on 06.01.14.
 */
public class ShowMessagesActivity extends Activity {

    ArrayList<Message> c;

    ListView showMessages;
    ArrayAdapter<StringBuilder> adapter;
    private Button sendMessageButton;
    private TextView newMessageTextView;

    public static final int IDM_UPDATE = 101;
    public static final int IDM_RELOAD = 102;
    public static final int IDM_STATUSUSER = 104;


    private int ident;
    private Long id;
    private int countMessage = 10;
    private int messagePosition;

    UpdateMessages um;
    OnlineUser ou;
    NewMessage nm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_messages);

        showMessages = (ListView)findViewById(R.id.showMessagesListView);
        sendMessageButton = (Button)findViewById(R.id.sendMessageButton);
        newMessageTextView = (TextView)findViewById(R.id.newMessageEditText);
        sendMessageButton.requestFocus();

        registerForContextMenu(showMessages);

        ident = getIntent().getExtras().getInt("type_dialog");
        id = getIntent().getExtras().getLong("id_dialog");

        sendMessageButton.setOnClickListener(newMessage);
        showMessages.setOnScrollListener(scrollListView);

        messagePosition = countMessage;
        updateMessages();
    }

    @Override
    public void onRestart(){
        super.onRestart();

        messagePosition = countMessage;
        updateMessages();
        //showMessages.setSelection(showMessages.getCount());
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, IDM_UPDATE, Menu.NONE, "Обновить");
        menu.add(Menu.NONE, IDM_RELOAD, Menu.NONE, "Загрузить еще");
        menu.add(Menu.NONE, IDM_STATUSUSER, Menu.NONE, "Online | Offline");
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        CharSequence message;
        Toast toast;
        switch (item.getItemId())
        {
            case IDM_UPDATE:
                message = "Обновлено";

                messagePosition = showMessages.getCount();
                updateMessages();
                //showMessages.setSelection(messagePosition);

                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                break;
            case IDM_RELOAD:
                message = "Подгружено";

                messagePosition = showMessages.getFirstVisiblePosition() + countMessage;
                changeCountMessage();
                updateMessages();
                //showMessages.setSelection(messagePosition);

                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                break;
            case IDM_STATUSUSER:

                showOnlineUser();

                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }


    private View.OnClickListener newMessage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(newMessageTextView.getText().toString().compareTo("") == 0) return;

            newMessage();
        }
    };


    private OnScrollListener scrollListView = new OnScrollListener(){

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(totalItemCount > 5){
                if(firstVisibleItem == 0){
                    if(countMessage != totalItemCount)
                        return;

                    messagePosition = countMessage;
                    changeCountMessage();
                    updateMessages();
                }
            }
        }
    };


    private void updateMessages(){

        um = new UpdateMessages();
        um.execute();
    }


    private void changeCountMessage(){
        countMessage += countMessage;
    }


    private void newMessage(){

        nm = new NewMessage();
        nm.execute();
    }


    private void showOnlineUser(){

        ou = new OnlineUser();
        ou.execute();
    }



    class UpdateMessages extends AsyncTask<Void, Void, Void> {

        List<StringBuilder> listAll;

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvInfo.setText("Begin");
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(ident == 0){
                    c = DataOfSession.getInstance().api.getMessagesHistory(id,
                            0l, DataOfSession.getInstance().account.user_id, 0l, countMessage);
                }
                else{
                    c = DataOfSession.getInstance().api.getMessagesHistory(0l,
                            id, DataOfSession.getInstance().account.user_id, 0l, countMessage);
                }

                listAll = new ArrayList<StringBuilder>();
                ArrayList<Long> isNotReadMessages = new ArrayList<Long>();

                for(com.perm.kate.api.Message a : c)
                {
                    if(!a.read_state) isNotReadMessages.add(a.mid);

                    StringBuilder sb = new StringBuilder();
                    if(a.is_out)
                        sb.insert(sb.length(), DataOfSession.getInstance().friendsMap
                                .get(DataOfSession.getInstance().account.user_id));
                    else
                        sb.insert(sb.length(), DataOfSession.getInstance().friendsMap.get(a.uid));

                    sb.insert(sb.length(), ":");
                    sb.insert(sb.length(), '\n');

                    if(!a.read_state){
                        sb.insert(sb.length(), "Не прочитано");
                        sb.insert(sb.length(), '\n');
                    }

                    sb.insert(sb.length(), a.body);
                    listAll.add(sb);
                }

                Collections.reverse(listAll);

                DataOfSession.getInstance().api.markAsNewOrAsRead(isNotReadMessages, true);


            } catch (Exception e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            adapter = new ArrayAdapter<StringBuilder>(getApplicationContext(),
                    //android.R.layout.simple_list_item_1, listAll);
                    R.layout.custom_list_item_for_messages, listAll);

            showMessages.setAdapter(adapter);

            showMessages.setSelection(messagePosition);
        }
    }



    class OnlineUser extends AsyncTask<Void, Void, Void> {

        StringBuilder sb;

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvInfo.setText("Begin");
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ArrayList<Long> friendsOnline = new ArrayList<Long>();

                friendsOnline = DataOfSession.getInstance().api.
                                getOnlineFriends(DataOfSession.getInstance().account.user_id);


                sb = new StringBuilder();
                if(ident == 0){
                    Boolean found = false;

                    sb.insert(sb.length(), DataOfSession.getInstance().friendsMap.get(id));
                    for(Long a : friendsOnline){
                        if(a.longValue() == id){
                            found = true;

                            sb.insert(sb.length(), " Online");

                            break;
                        }
                    }

                    if(!found) sb.insert(sb.length(), " Offline");
                }
                else {
                    ArrayList<User> chatUsers = DataOfSession.getInstance().api.
                                getChatUsers(id, "first_name, last_name, online, online_mobile");

                    for(User a : chatUsers){
                        if(a.uid != DataOfSession.getInstance().account.user_id){
                            sb.insert(sb.length(), a.first_name);
                            sb.insert(sb.length(), " ");
                            sb.insert(sb.length(), a.last_name);
                            sb.insert(sb.length(), " ");

                            if(a.online || a.online_mobile)
                                sb.insert(sb.length(), "Online");
                            else sb.insert(sb.length(), "Offline");

                            sb.insert(sb.length(), '\n');
                        }
                    }
                    sb.delete(sb.length()-1, sb.length());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            AlertDialog.Builder builder = new AlertDialog.Builder(ShowMessagesActivity.this);
            builder.setTitle("Online | Offline")
                    .setMessage(sb)
                    .setCancelable(false)
                    .setNegativeButton("Круто",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }



    class NewMessage extends AsyncTask<Void, Void, Void> {

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvInfo.setText("Begin");
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(ident == 0){
                    DataOfSession.getInstance().api.sendMessage(id, 0l, newMessageTextView.getText().toString(),
                            "", "", null, null, null, null, null, null);
                }
                else{
                    DataOfSession.getInstance().api.sendMessage(0l, id, newMessageTextView.getText().toString(),
                            "", "", null, null, null, null, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Toast toast = Toast.makeText(getApplicationContext(), "Сообщение отправлено", Toast.LENGTH_SHORT);
            toast.setGravity(1, 0, 0);
            toast.show();

            newMessageTextView.setText("");

            messagePosition = showMessages.getCount();
            updateMessages();
        }
    }
}
