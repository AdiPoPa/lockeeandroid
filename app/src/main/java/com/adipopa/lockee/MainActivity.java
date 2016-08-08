package com.adipopa.lockee;

import android.animation.Animator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String registerNotification = "hide";
    TextView registerNotif;
    Button switchButton;
    ListView mainList;
    TextView noLocks;
    List<Data> dataList;
    Data[] simpleArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(LoginActivity.active) {
            LoginActivity.login.finish();
        }

        if(SaveSharedPreference.getLoginStatus(MainActivity.this).equals("not logged"))
        {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        
        String email = "adipopa@gmail.com";
        getList(email);

        registerNotif = (TextView) findViewById(R.id.registerNotification);
        switchButton = (Button) findViewById(R.id.switchButton);

        if(registerNotification.equals("show")){
            registerNotif.setVisibility(View.VISIBLE);
        }
        else{
            registerNotif.setVisibility(View.GONE);
        }
        
        mainList = (ListView) findViewById(R.id.mainList);
        noLocks = (TextView) findViewById(R.id.noLocks);

        dataList = new ArrayList<>();

        mainList.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ImageView listIcon = (ImageView) view.findViewById(R.id.listIcon);
                        listIcon.setImageResource(R.mipmap.lockicon);
                    }
                }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch(item.getItemId()){
                case R.id.menu_logout: {
                    SaveSharedPreference.setLoginStatus(MainActivity.this, "not logged");
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if(featureId == Window.FEATURE_ACTION_BAR && menu != null){
            if(menu.getClass().getSimpleName().equals("MenuBuilder")){
                try{
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
    
    public void onSwitch(View view){
        String lock_inner_id = "B55HH471C28f646G";
        String type = "switch";
        LocksWorker locksWorker = new LocksWorker(this);
        locksWorker.execute(type, lock_inner_id);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (LocksWorker.lockStatus) {
                    case "unlocked":
                        Toast.makeText(MainActivity.this, "unlocked", Toast.LENGTH_SHORT).show();
                        break;
                    case "locked":
                        Toast.makeText(MainActivity.this, "locked", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "the lock was not found", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, 750);
    }

    public void onHideNotification(View view){
        registerNotif.animate()
                .setDuration(500)
                .translationY(0 - registerNotif.getHeight())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        registerNotif.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
    }
    
    // Method to acquire the lock list from the database

    public void getList(String email) {
        String type = "getlist";
        LocksWorker locksWorker = new LocksWorker(this);
        locksWorker.execute(type, email);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String cardinal = "";
                int i = 0;
                while (LocksWorker.list[i] != '#') {
                    cardinal += LocksWorker.list[i];
                    i++;
                }
                i++;
                int cardinal_parse = Integer.parseInt(cardinal);
                if (cardinal_parse > 0) {
                    for (int n = 1; n <= cardinal_parse; n++) {
                        String nickname = "";
                        String shareID = "";
                        String is_open = "";
                        while (LocksWorker.list[i] != '#') {
                            nickname += LocksWorker.list[i];
                            i++;
                        }
                        i++;
                        while (LocksWorker.list[i] != '#') {
                            shareID += LocksWorker.list[i];
                            i++;
                        }
                        i++;
                        while (LocksWorker.list[i] != '#') {
                            is_open += LocksWorker.list[i];
                            i++;
                        }
                        i++;
                        Data data = new Data(nickname, shareID, is_open);
                        dataList.add(data);
                    }
                } else {
                    noLocks.setVisibility(View.VISIBLE);
                }

                // 08.08.2016 - 22:51 : Am rezolvaaaaat, trebuia tot in handler... ca handlerul ruleaza in paralel cu interfata, Jeeeesus

                simpleArray = new Data[dataList.size()];
                dataList.toArray(simpleArray);

                ListAdapter listAdapter = new LocksListAdapter(MainActivity.this, simpleArray);

                mainList.setAdapter(listAdapter);
            }
        }, 1000);
    }

    // Method to require double press on the back button to close the app

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press the back button again to close the app", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
