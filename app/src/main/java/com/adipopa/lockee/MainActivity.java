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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    public static String registerNotification = "hide";
    TextView registerNotif;

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

        registerNotif = (TextView) findViewById(R.id.registerNotification);

        if(registerNotification.equals("show")){
            registerNotif.setVisibility(View.VISIBLE);
        }
        else{
            registerNotif.setVisibility(View.GONE);
        }

        Data data1 = new Data("Turda", "Str. Lotus 19");
        Data data2 = new Data("Cluj-Napoca", "Str. Meteor 19");
        Data data3 = new Data("Bucuresti", "Str. Basarabiei");

        Data[] dataArray = {data1, data2, data3};

        ListAdapter CustomAdapter = new CustomAdapter(this, dataArray);
        ListView myListView = (ListView) findViewById(R.id.mainList);

        myListView.setAdapter(CustomAdapter);

                myListView.setOnItemClickListener(
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
