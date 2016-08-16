package com.adipopa.lockee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements ToolTipView.OnToolTipViewClickedListener {

    public static String registerNotification = "hide";
    ListView mainList;
    TextView noLocks;
    String email;

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

        if(SaveSharedPreference.getLoginStatus(this).equals("not logged")) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            email = SaveSharedPreference.getLoginStatus(this);
            noLocks = (TextView) findViewById(R.id.noLocks);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            ToolTip toolTip = new ToolTip()
                    .withText(getString(R.string.registerGuide))
                    .withTextColor(Color.WHITE)
                    .withColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .withShadow()
                    .withAnimationType(ToolTip.AnimationType.NONE);
            ToolTipRelativeLayout toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.registerGuide);
            final ToolTipView myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, fab);
            myToolTipView.setOnToolTipViewClickedListener(MainActivity.this);

            TextView registerSuccessful = (TextView)findViewById(R.id.registerNotification);

            if (registerNotification.equals("show")) {
                // Custom animation on image
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
                        mAnimationSet.setStartDelay(5000);
                        mAnimationSet.play(fadeOut);
                        mAnimationSet.start();
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                mAnimationSet.end();
                                new getLocks().execute(email);
                            }
                        }, 7000);
                    }
                });
                mAnimationSet.start();
            } else {
                myToolTipView.remove();
                new getLocks().execute(email);
                registerSuccessful.setVisibility(View.GONE);
            }

            mainList = (ListView) findViewById(R.id.mainList);

            mainList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ImageView listIcon = (ImageView) view.findViewById(R.id.listIcon);
                            listIcon.setImageResource(R.mipmap.lockicon);
                        }
                    }
            );

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myToolTipView.remove();
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

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        toolTipView.remove();
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
                                    R.layout.main_list, new String[]{TAG_NICKNAME, TAG_SHARE_ID, TAG_IS_OPENED},
                                    new int[]{R.id.nicknameText, R.id.shareIDText, R.id.statusText});

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
