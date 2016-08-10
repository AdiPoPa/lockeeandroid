package com.adipopa.lockee;

import android.animation.Animator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static String registerNotification = "hide";
    TextView registerNotif;
    Button switchButton;
    ListView mainList;
    
    private static final String TAG_LOCKINFO = "locks_info";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_SHARE_ID = "share_id";
    private static final String TAG_IS_OPENED = "is_opened";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LoginActivity loginActivity = new LoginActivity();
        loginActivity.finish();

        if(SaveSharedPreference.getLoginStatus(MainActivity.this).equals("not logged"))
        {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {

            registerNotif = (TextView) findViewById(R.id.registerNotification);
            switchButton = (Button) findViewById(R.id.switchButton);
            noLocks = (TextView) findViewById(R.id.noLocks);

            if(registerNotification.equals("show")){
            registerNotif.setVisibility(View.VISIBLE);
            } else{
            registerNotif.setVisibility(View.GONE);
            }
        
            mainList = (ListView) findViewById(R.id.mainList);
        
            String email = "adipopa@gmail.com";
            new getLocks().execute(email);

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
    }
    
    private class getLocks extends AsyncTask<String, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> locksList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // Creating service handler class instance
            LocksListWorker webreq = new LocksListWorker();

            // Making a request to url and getting response
            String locks_url = "https://lockee-cloned-andrei-b.c9users.io/portal/android/get_locks/";

            String email = params[0];

            String jsonStr = webreq.makeWebServiceCall(locks_url, LocksListWorker.POST, email);

            Log.d("Response: ", "> " + jsonStr);

            locksList = ParseJSON(jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            /**
             * Updating parsed JSON data into ListView
             * */

            if(result != null) {
                ListAdapter adapter = new SimpleAdapter(
                        MainActivity.this, locksList,
                        R.layout.main_list, new String[]{TAG_NICKNAME, TAG_SHARE_ID, TAG_IS_OPENED},
                        new int[]{R.id.nicknameText, R.id.shareIDText, R.id.statusText});

                mainList.setAdapter(adapter);
            } else {
                noLocks.setVisibility(View.VISIBLE);
            }
        }

    }

    private ArrayList<HashMap<String, String>> ParseJSON(String json) {
        if (json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> locksList = new ArrayList<>();

                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                JSONArray locks = jsonObj.getJSONArray(TAG_LOCKINFO);

                // looping through All Locks
                for (int i = 0; i < locks.length(); i++) {
                    JSONObject c = locks.getJSONObject(i);

                    String nickname = c.getString(TAG_NICKNAME);
                    String share_id = c.getString(TAG_SHARE_ID);
                    String is_opened = c.getString(TAG_IS_OPENED);

                    // tmp hashmap for single lock
                    HashMap<String, String> lock = new HashMap<>();

                    // adding each child node to HashMap key => value
                    lock.put(TAG_NICKNAME, nickname);
                    lock.put(TAG_SHARE_ID, share_id);
                    lock.put(TAG_IS_OPENED, is_opened);

                    // adding lock to locks list
                    locksList.add(lock);
                }
                return locksList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
            return null;
        }
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
