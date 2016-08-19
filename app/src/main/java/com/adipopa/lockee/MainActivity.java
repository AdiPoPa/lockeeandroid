package com.adipopa.lockee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
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

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    public static String registerNotification = "hide";
    ListView mainList;
    TextView noLocks;
    String email;

    private static final String TAG_LOCKINFO = "locks_info";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_LOCK_ID = "lock_inner_id";
    private static final String TAG_SHARE_ID = "share_id";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ICON = "icon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginActivity loginActivity = new LoginActivity();
        loginActivity.finish();

        if(SaveSharedPreference.getLoginStatus(this).equals("not logged")) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            email = SaveSharedPreference.getLoginStatus(this);
            noLocks = (TextView) findViewById(R.id.noLocks);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            TextView registerSuccessful = (TextView)findViewById(R.id.registerNotification);

            if (registerNotification.equals("show")) {
                
                final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(registerSuccessful, "alpha", 0f, 1f);
                fadeIn.setDuration(2000);
                final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(registerSuccessful, "alpha",  1f, 0f);
                fadeOut.setDuration(2000);

                final AnimatorSet mAnimationSet = new AnimatorSet();

                mAnimationSet.play(fadeIn);

                mAnimationSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mAnimationSet.setStartDelay(3000);
                        mAnimationSet.play(fadeOut);
                        mAnimationSet.start();
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                mAnimationSet.end();
                                new getLocks().execute(email);
                            }
                        }, 5000);
                    }
                });
                mAnimationSet.start();
            } else {
                registerSuccessful.setVisibility(View.GONE);
            }
            
            // TODO: Make the list gone when there are no locks

            mainList = (ListView) findViewById(R.id.mainList);
            mainList.addFooterView(new View(MainActivity.this));

            mainList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            TextView nicknameText = (TextView) view.findViewById(R.id.nicknameText);
                            TextView lockIDText = (TextView) view.findViewById(R.id.lockIDText);
                            TextView shareIDText = (TextView) view.findViewById(R.id.shareIDText);
                            TextView statusText = (TextView) view.findViewById(R.id.statusText);
                            Intent intent = new Intent(MainActivity.this, ControlActivity.class);

                            String nickname = nicknameText.getText().toString();
                            String lockID = lockIDText.getText().toString();
                            String shareID = shareIDText.getText().toString();
                            String status = statusText.getText().toString();
                            intent.putExtra("nickname", nickname);
                            intent.putExtra("lockID", lockID);
                            intent.putExtra("shareID", shareID);
                            intent.putExtra("status", status);

                            startActivity(intent);
                        }
                    }
            );

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
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
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /** Async task class to get json by making HTTP call **/

    private class getLocks extends AsyncTask<String, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> locksList;

        android.app.AlertDialog progressDialog = new SpotsDialog(MainActivity.this, R.style.loadingDialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            // Creating service handler class instance
            LocksListWorker webreq = new LocksListWorker();

            // Making a request to url and getting response
            String locks_url = "https://lockee-cloned-andrei-b.c9users.io/android/get_locks/";

            String email = params[0];

            String jsonStr = webreq.makeWebServiceCall(locks_url, LocksListWorker.POST, email);

            Log.d("Response: ", "> " + jsonStr);

            locksList = ParseJSON(jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    progressDialog.dismiss();

                    /**
                     * Updating parsed JSON data into ListView
                     * */

                    if(locksList != null) {
                        if (locksList.size() > 0) {
                           ListAdapter adapter = new SimpleAdapter(
                                    MainActivity.this, locksList,
                                    R.layout.main_list, new String[]{TAG_NICKNAME, TAG_LOCK_ID, TAG_SHARE_ID, TAG_STATUS, TAG_ICON},
                                    new int[]{R.id.nicknameText, R.id.lockIDText, R.id.shareIDText, R.id.statusText, R.id.lockIcon}
                            );
                            mainList.setAdapter(adapter);
                        } else {
                            noLocks.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("LocksListHandler", "There was an error getting the list of locks, please check connection");
                    }
                }
            }, 1000);
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
                    String lock_id = c.getString(TAG_LOCK_ID);
                    String share_id = c.getString(TAG_SHARE_ID);
                    String status = "";
                    int icon = 0;
                    if(c.getString(TAG_STATUS).equals("true")) {
                        status = "unlocked";
                        icon = R.drawable.ic_lock_open_white_48dp;
                    } else if (c.getString(TAG_STATUS).equals("false")){
                        status = "locked";
                        icon = R.drawable.ic_lock_outline_white_48dp;
                    }

                    // tmp hashmap for single lock
                    HashMap<String, String> lock = new HashMap<>();

                    // adding each child node to HashMap key => value
                    lock.put(TAG_NICKNAME, nickname);
                    lock.put(TAG_LOCK_ID, lock_id);
                    lock.put(TAG_SHARE_ID, share_id);
                    lock.put(TAG_STATUS, status);
                    lock.put(TAG_ICON, Integer.toString(icon));

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
    protected void onResume() {
        super.onResume();
        new getLocks().execute(email);
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
