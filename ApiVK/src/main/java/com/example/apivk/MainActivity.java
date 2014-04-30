package com.example.apivk;

/*
Есть:
- Загрузка имеющихся диалогов, обновление в ручную
- Загрузка имеющихся сообщений у диалога, обновление вручную
- Написание сообщения в конкретный диалог
- Управление количеством отображаемых в listView сообщений

Что осталось:
- Загрузка в меню диалогов не только имен друзей, но и имен людей, не являющихся друзьями
- Написание сообщения любому человеку из списка друзей из меню просмотра диалогов
- Возможность поиска ссылок в конкретном диалоге
- Возможность копирования текста сообщений
- Узнать про методику автоматического оповещения о сообщении
- Оптимизация
*/


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.perm.kate.api.Api;
import com.perm.kate.api.Message;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int REQUEST_LOGIN=1;
    
    Button authorizeButton;
    Button logoutButton;
    Button postButton;
    Button downloadMusicButton;
    Button showMusicButton;

    Button showDialogsButton;

    EditText messageEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setupUI();
        
        DataOfSession.getInstance().connect(this);
        DataOfSession.getInstance().CreateFriendsMap();
        
        showButtons();
    }

    private void setupUI() {
        authorizeButton=(Button)findViewById(R.id.authorize);
        logoutButton=(Button)findViewById(R.id.logout);
        postButton=(Button)findViewById(R.id.post);
        downloadMusicButton=(Button)findViewById(R.id.showDownloadMusic);
        showMusicButton=(Button)findViewById(R.id.showMusic);

        showDialogsButton=(Button)findViewById(R.id.showDialogs);

        messageEditText=(EditText)findViewById(R.id.message);
        authorizeButton.setOnClickListener(authorizeClick);
        logoutButton.setOnClickListener(logoutClick);
        postButton.setOnClickListener(postClick);
        downloadMusicButton.setOnClickListener(downloadMusic);
        showMusicButton.setOnClickListener(showMusic);

        showDialogsButton.setOnClickListener(showDialogs);
    }
    
    private OnClickListener authorizeClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            startLoginActivity();
        }
    };
    
    private OnClickListener logoutClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            logOut();
        }
    };
    
    private OnClickListener postClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            postToWall();
        }
    };

    private OnClickListener showDialogs=new OnClickListener() {
        @Override
        public void onClick(View v) {
            startShowDialogsActivity();
        }
    };

    private OnClickListener downloadMusic=new OnClickListener() {
        @Override
        public void onClick(View v) {
            startDownloadActiivty();
        }
    };

    private OnClickListener showMusic=new OnClickListener() {
        @Override
        public void onClick(View v) {
            startMusicListActiivty();
        }
    };

    private void startDownloadActiivty(){
        Intent intent = new Intent(this, DownloadActivity.class);
        //Intent intent = new Intent(this, DownloadedFilesActivity.class);
        startActivity(intent);
    }

    private void startMusicListActiivty(){
        //Intent intent = new Intent(this, DownloadActivity.class);
        Intent intent = new Intent(this, DownloadedFilesActivity.class);
        startActivity(intent);
    }

    private void startShowDialogsActivity() {
        Intent intent = new Intent(this, ShowDialogsActivity.class);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно 
                DataOfSession.getInstance().account.access_token=data.getStringExtra("token");
                DataOfSession.getInstance().account.user_id=data.getLongExtra("user_id", 0);
                DataOfSession.getInstance().account.save(MainActivity.this);
                DataOfSession.getInstance().api=new Api(DataOfSession.getInstance().account.access_token, Constants.API_ID);
                showButtons();
            }
        }
    }
    
    private void postToWall() {
        //Общение с сервером в отдельном потоке чтобы не блокировать UI поток
        new Thread(){
            @Override
            public void run(){
                try {
                    String text=messageEditText.getText().toString();
                    DataOfSession.getInstance().api.createWallPost(DataOfSession.getInstance().
                            account.user_id, text, null, null, false, false, false, null, null, null, (long) 0, null, null);

                    //Показать сообщение в UI потоке
                    runOnUiThread(successRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    Runnable successRunnable=new Runnable(){
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };
    
    private void logOut() {
        DataOfSession.getInstance().logOut(MainActivity.this);
        showButtons();
    }
    
    void showButtons(){
        if(DataOfSession.getInstance().api!=null){
            authorizeButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            postButton.setVisibility(View.VISIBLE);
            messageEditText.setVisibility(View.VISIBLE);

            showDialogsButton.setVisibility(View.VISIBLE);
        }else{
            authorizeButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            postButton.setVisibility(View.GONE);
            messageEditText.setVisibility(View.GONE);

            showDialogsButton.setVisibility(View.GONE);
        }
    }
}