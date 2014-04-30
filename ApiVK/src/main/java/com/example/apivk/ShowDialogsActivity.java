package com.example.apivk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.Message;
import com.perm.kate.api.User;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by USER on 06.01.14.
 */
public class ShowDialogsActivity extends Activity {

    ArrayList<Message> c;

    ListView showDialogs;
    ArrayAdapter<StringBuilder> adapter;
    List<StringBuilder> listAll;

    public static final int IDM_UPDATE = 101;
    public static final int IDM_RELOAD = 102;
    public static final int IDM_FRONLINE = 103;

    private int countDialog = 10;
    private int dialogPosition;


    UpdateDialogs mt;
    FriendsOnline showFriendsOnline;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_dialogs);

        showDialogs = (ListView)findViewById(R.id.showDialogsListView);
        registerForContextMenu(showDialogs);

        updateDialogs();


        showDialogs.setOnItemClickListener(dialogClick);

    }


    ListView.OnItemClickListener dialogClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            dialogPosition = position;

            Intent intent = new Intent(ShowDialogsActivity.this, ShowMessagesActivity.class);

            if(c.get(position).chat_members == null){
                intent.putExtra("type_dialog", 0);
                intent.putExtra("id_dialog", c.get(position).uid);
            }
            else{
                intent.putExtra("type_dialog", 1);
                intent.putExtra("id_dialog", c.get(position).chat_id);
            }

            startActivity(intent);

            //finish();
        }
    };


    @Override
    public void onRestart(){
        super.onRestart();

        updateDialogs();
        showDialogs.setSelection(dialogPosition);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, IDM_UPDATE, Menu.NONE, "Обновить");
        menu.add(Menu.NONE, IDM_RELOAD, Menu.NONE, "Загрузить еще");
        menu.add(Menu.NONE, IDM_FRONLINE, Menu.NONE, "Друзья Online");
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
                updateDialogs();

                toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                break;
            case IDM_RELOAD:
                message = "Подгружено";
                changeCountDialog();
                updateDialogs();

                toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                break;
            case IDM_FRONLINE:
                friendsOnline();

                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }


    private void updateDialogs(){

        mt = new UpdateDialogs();
        mt.execute();
    }


    private void changeCountDialog(){
        countDialog += countDialog;
    }


    private void friendsOnline(){
        showFriendsOnline = new FriendsOnline();
        showFriendsOnline.execute();
    }



    class UpdateDialogs extends AsyncTask<Void, Void, Void> {

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvInfo.setText("Begin");
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            try {
                c = DataOfSession.getInstance().api.getMessagesDialogs(0l, countDialog, "", "");

                listAll = new ArrayList<StringBuilder>(c.size());
                //StringBuilder sb = new StringBuilder();
                for(com.perm.kate.api.Message a : c)
                {
                    StringBuilder sb = new StringBuilder();
                    if(a.read_state == false && !a.is_out){
                        sb.insert(sb.length(), "NEW MESSAGE!!!");
                        sb.insert(sb.length(), '\n');
                    }
                    if(a.chat_members != null){
                        for(Long user : a.chat_members){
                            sb.insert(sb.length(), DataOfSession.getInstance().friendsMap.get(user.longValue()));
                            sb.insert(sb.length(), ", ");
                        }
                        sb.delete(sb.length()-2, sb.length());
                    }
                    else sb.insert(sb.length(), DataOfSession.getInstance().friendsMap.get(a.uid));
                    listAll.add(sb);

                    //adapter = new ArrayAdapter<StringBuilder>(getApplicationContext(),
                    //        R.layout.custom_list_item, listAll);

                    //showDialogs.setAdapter(adapter);
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
                    R.layout.custom_list_item, listAll);
            showDialogs.setAdapter(adapter);
        }
    }


    class FriendsOnline extends AsyncTask<Void, Void, Void> {

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
                for(Long friend : friendsOnline){
                    sb.insert(sb.length(), DataOfSession.getInstance().friendsMap.get(friend));
                    sb.insert(sb.length(), " ");
                    sb.insert(sb.length(), "Online");
                    sb.insert(sb.length(), '\n');
                }

            } catch (Exception e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            AlertDialog.Builder builder = new AlertDialog.Builder(ShowDialogsActivity.this);
            builder.setTitle("Friends online")
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
}