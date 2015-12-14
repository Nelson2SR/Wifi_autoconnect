package com.appliedmesh.merchantapp.view;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appliedmesh.merchantapp.module.BeepManager;
import com.appliedmesh.merchantapp.network.GetMobileAppInfoRequest;
import com.appliedmesh.merchantapp.network.GetStoreSettingsRequest;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Volley;
import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.module.OrderManager;
import com.appliedmesh.merchantapp.module.RemoteServer;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Sidebar.NavigationDrawerCallbacks {
    private static final String TAG = "MainActivity";
    private static final int MENU_GROUP_SETTINGS = 1;
    //private static final int MENU_ID_MERCHANT = 1;
    //private static final int MENU_ID_SECURITY = 2;
    private SharedPreferences mSharedPreferences;
//    private FragmentScanner mScanner;
    private FragmentOrderList mOrder;
    private Sidebar mSideBar;
    private Menu mMenu;
    private boolean mIsWelcomeShowed = false;
    private BeepManager mBeepManager;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if (OrderManager.getInstance().getPendingNum() > 0) {
            Intent newint = new Intent(MainActivity.this, OrderDetailActivity.class);
            startActivity(newint);
        }

        mSideBar = (Sidebar) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mSideBar.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        OrderManager.getInstance().startQuery();
        mBeepManager = new BeepManager(this);

        String userid = SharedPrefHelper.getString(this, Constants.NAME_USERNAME);
        if (userid == null || userid.equals("")) {
            activeLogin();
        }
        // getStoreSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter_main = new IntentFilter(Constants.ACTION_SCAN_RESULT);
        registerReceiver(mMainReceiver, filter_main);
        IntentFilter filter_welcome = new IntentFilter(Constants.ACTION_WELCOME_RESULT);
        registerReceiver(mMainReceiver,filter_welcome);
        IntentFilter filter_order_detail = new IntentFilter(Constants.ACTION_ORDER_DETAIL);
        registerReceiver(mMainReceiver, filter_order_detail);
        IntentFilter filter_order_coming = new IntentFilter(Constants.ACTION_ORDER_COMING);
        registerReceiver(mMainReceiver, filter_order_coming);
        IntentFilter filter_test = new IntentFilter(Constants.ACTION_TEST);
        registerReceiver(mMainReceiver, filter_test);
    }

    @Override
    protected void onResume() {
    	mSideBar.updateHostName();
    	mSideBar.updateLoginStatus();
//        checkUpdate();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mMainReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OrderManager.getInstance().stopQuery();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (mIsWelcomeShowed) {
            return;
        }
        switch (position) {
            case 0:
//                if (mScanner == null)
//                    mScanner = new FragmentScanner();
//                UpdateFragment(mScanner);
                break;
            case 1:
                if (mOrder == null)
                    mOrder = new FragmentOrderList();
                UpdateFragment(mOrder);
                break;
            case 2:
                Intent newint = new Intent(MainActivity.this, CollectionActivity.class);
                startActivity(newint);
                break;
            case 3://Dev
    			changeUrl();
    			mSideBar.updateHostName();
    			break;
            case 4:
    			if (mSideBar.isUserLogin()) {
    				mSideBar.setToken( "");
    				mSideBar.setUserName("");
    				mSideBar.updateLoginStatus();
    			} else {
                    activeLogin();
    			}
    			break;
            default:
                break;
        }
        invalidateOptionsMenu();
    }

    private void activeLogin(){
        Intent i = new Intent(this, ActivityLoginOption.class);
        startActivity(i);
    }

	private void changeUrl(){
		if(mSideBar.getUrl().equals(ServerConfigs.BASEURL_MYPOINT)){
			mSideBar.setUrl( ServerConfigs.DEVURL_MYPOINT);
		}else{
			mSideBar.setUrl( ServerConfigs.BASEURL_MYPOINT);
		}
	}

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Are you sure to quit?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                }
            })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    }).show();
    }

    /* dong_bin: TODO do not check merchant_id and merchant_key anymore
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        menu.add(MENU_GROUP_SETTINGS, MENU_ID_MERCHANT, 1, "Merchant ID");
        menu.add(MENU_GROUP_SETTINGS, MENU_ID_SECURITY, 2, "Security key");
        if (mIsWelcomeShowed) {
            hiddenEditMenu();
        }
        return true;
    }
    */

    private void hiddenEditMenu(){
        if(null != mMenu){
            mMenu.setGroupVisible(MENU_GROUP_SETTINGS, false);
        }
    }

    private void showEditMenu(){
        if(null != mMenu){
            mMenu.setGroupVisible(MENU_GROUP_SETTINGS, true);
        }
    }

    private void settingDialog(String message, String text, String name){
        final EditText input_dialog = new EditText(this);
        final String Key = name;
        input_dialog.setText(text);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(input_dialog)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.cancel), null)
                .setNegativeButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = input_dialog.getText().toString();
                        if (input != null && !input.equals("")) {
                            SharedPreferences.Editor ed = mSharedPreferences.edit();
                            ed.putString(Key, input);
                            ed.commit();
                        }
                    }
                });
        builder.show();

    }

    /* dong_bin: TODO do not check merchant_id and merchant_key anymore
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_MERCHANT:
                settingDialog(getString(R.string.merchant_id_hint),
                        mSharedPreferences.getString(Constants.NAME_MERCHANT_ID, ""),
                        Constants.NAME_MERCHANT_ID);
                return true;
            case MENU_ID_SECURITY:
                settingDialog(getString(R.string.security_key_hint),
                        mSharedPreferences.getString(Constants.NAME_SECURITY_KEY, ""),
                        Constants.NAME_SECURITY_KEY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    private void UpdateFragment(Fragment fg) {
        if (fg == null) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fg);
        transaction.commit();
    }

    private BroadcastReceiver mMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_SCAN_RESULT)) {
                Log.d(TAG, "get scan result: " + intent.getStringExtra("receipt"));
                Intent newint = new Intent(MainActivity.this, WebviewActivity.class);
                newint.putExtra("url", intent.getStringExtra("receipt"));
                startActivity(newint);
            }else if (intent.getAction().equals(Constants.ACTION_ORDER_DETAIL)) {
                Intent newint = new Intent(MainActivity.this, OrderDetailActivity.class);
                newint.putExtra("orderindex", intent.getIntExtra("orderindex", 0));
                startActivity(newint);
            }else if (intent.getAction().equals(Constants.ACTION_ORDER_COMING)) {
                mBeepManager.playBeepSoundAndVibrate(BeepManager.TYPE_NEW_ORDER);
                Intent newint = new Intent(MainActivity.this, OrderDetailActivity.class);
                startActivity(newint);
            }else if (intent.getAction().equals(Constants.ACTION_WELCOME_RESULT)) {
                SharedPreferences.Editor ed = mSharedPreferences.edit();
                ed.putString(Constants.NAME_MERCHANT_ID, intent.getStringExtra(Constants.NAME_MERCHANT_ID));
                ed.putString(Constants.NAME_SECURITY_KEY, intent.getStringExtra(Constants.NAME_SECURITY_KEY));
                ed.commit();
                mIsWelcomeShowed = false;
                showEditMenu();
                //mSideBar.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                mSideBar.selectItem(1);
            }
        }
    };

    private void processResponse(JSONObject response) {
        try {
            if (response.has(RemoteServer.RESPONSE_STATUS) && response.getString(RemoteServer.RESPONSE_STATUS).equals("success")) {
                if (response.has(RemoteServer.RESPONSE_RECEIPTS_URL)) {
                    Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                    intent.putExtra("url", response.getString(RemoteServer.RESPONSE_RECEIPTS_URL));
                    startActivity(intent);
                }
            } else {
                if (response.has(RemoteServer.RESPONSE_ERROR_MSG)) {
                    Toast.makeText(MainActivity.this, "Error " + response.getString(RemoteServer.RESPONSE_ERROR_MSG), Toast.LENGTH_LONG).show();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getStoreSettings(){
        GetStoreSettingsRequest request = new GetStoreSettingsRequest(this, new JsonObjectRequestCallback() {
            @Override
            public void onRequestSuccess(JSONObject value) {
                try{
                    Logger.d(TAG, "OPENING-HOUR: " + value.getString(ServerConfigs.RESPONSE_OPENNG_HOUR));
                    Logger.d(TAG, "CLOSING-HOUR: " + value.getString(ServerConfigs.RESPONSE_CLOSING_HOUR));
                    SharedPrefHelper.set(MainActivity.this,Constants.NAME_OPENING_HOUR, value.getString(ServerConfigs.RESPONSE_OPENNG_HOUR));
                    SharedPrefHelper.set(MainActivity.this,Constants.NAME_CLOSING_HOUR, value.getString(ServerConfigs.RESPONSE_CLOSING_HOUR));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(String errorMessage) {

            }
        });
        Volley.getInstance().addToRequestQueue(request, TAG);
    }

    private int getVersionCode() {
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void checkUpdate(){
        GetMobileAppInfoRequest request = new GetMobileAppInfoRequest(this, "merchant-app", "android", new JsonObjectRequestCallback() {
            @Override
            public void onRequestSuccess(JSONObject value) {
                try {
                    if (!value.has(ServerConfigs.RESPONSE_MOBILE_APP_VER) || !value.has(ServerConfigs.RESPONSE_MOBILE_APP_URL)) {
                        return;
                    }
                    int installedVersionCode =getVersionCode();
                    int updateVersionCode=0;
                    try {
                        updateVersionCode = Integer.valueOf(value.getString(ServerConfigs.RESPONSE_MOBILE_APP_VER));
                    }catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if(installedVersionCode < updateVersionCode) {
                        Logger.d(TAG, installedVersionCode + " < " + updateVersionCode + " pop up upgrade");
                        final String url = value.getString(ServerConfigs.RESPONSE_MOBILE_APP_URL);
                        AlertDialog.Builder updateDialog = new AlertDialog.Builder(MainActivity.this);

                        updateDialog.setCancelable(false);
                        updateDialog.setMessage(R.string.be_to_update);
                        updateDialog.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent i = new Intent(MainActivity.this, ActivityDownload.class);
                                i.putExtra(Constants.ARGS_DOWNLOAD_URL, url);
                                MainActivity.this.startActivity(i);
                            }
                        });
                        updateDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        updateDialog.show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(String errorMessage) {

            }
        });
        Volley.getInstance().addToRequestQueue(request, TAG);
    }
}
