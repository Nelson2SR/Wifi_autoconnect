package com.appliedmesh.merchantapp.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.BeepManager;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.module.Order;
import com.appliedmesh.merchantapp.module.OrderManager;
import com.appliedmesh.merchantapp.module.RemoteServer;
import com.appliedmesh.merchantapp.network.GetMobileAppInfoRequest;
import com.appliedmesh.merchantapp.network.GetStoreSettingsRequest;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Utils;
import com.appliedmesh.merchantapp.utils.Volley;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.server.converter.StringToIntConverter;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;


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

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;
    private MediaRouter.Callback mMediaRouterCallback;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private CollectionQueueChannel mCollectionQueueChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    private LinkedList<Map<String, String>> mData;


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

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getResources()
                        .getString(R.string.app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();

        Log.d(TAG,"SelectDevice:" + mSelectedDevice);
        Log.d(TAG,"SelectedRoute:" + mMediaRouter.getSelectedRoute());
        Log.d(TAG,"RouteSize:" + mMediaRouter.getRoutes().size());


    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("route.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("route.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
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

        // Start media router discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        mMediaRouter.selectRoute(mMediaRouter.getSelectedRoute());

    }

    @Override
    protected void onResume() {
        if(!Utils.isEmpty(SharedPrefHelper.getString(this, Constants.MERCHANT_NAME))){
            getSupportActionBar().setTitle(SharedPrefHelper.getString(this, Constants.MERCHANT_NAME) +" | "+ SharedPrefHelper.getString(this, Constants.MERCHANT_OUTLET));
        }
    	mSideBar.updateHostName();
    	mSideBar.updateLoginStatus();

        SharedPreferences mainPre = getSharedPreferences("queueSave",Context.MODE_PRIVATE);
        String mQueue = mainPre.getString("queue","");
        Log.d(TAG,"Resume Back" + mQueue);
        String mDisplay = mainPre.getString("display","");

        if(mDisplay.equals("yes")){
            addNewData(mQueue);
            mainPre.edit().putString("display","no").apply();
        }

        Log.d(TAG,"RouteSize:" + mMediaRouter.getRoutes().size());
        Log.d(TAG,"After Resume, the Route is:" + mMediaRouter.getSelectedRoute());

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork != null){
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWiFi) {
                Log.d(TAG,"Here is WiFi");
            }else {
                Log.d(TAG,"There is no WiFi");
            }
        }else {
            Log.d(TAG,"There is no network");
        }


        try {
            File file = new File(getFilesDir(),"device");
            FileInputStream fis = new FileInputStream(file);
            byte[] array = new byte[(int) fis.getChannel().size()];
            fis.read(array, 0, array.length);
            fis.close();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(array, 0, array.length);
            parcel.setDataPosition(0);
            Bundle out = parcel.readBundle();
            if(out != null){
                Log.d(TAG,"OutFile have content");
                mSelectedDevice = CastDevice.getFromBundle(out);
                Log.d(TAG,"Device:" + mSelectedDevice.toString());
                launchReceiver();
            }else {
                Log.d(TAG,"OutFile don't have content");
            }
//            out.putAll(out);
        } catch (FileNotFoundException fnfe) {
        } catch (IOException ioe) {
        } finally {
        }
        //        checkUpdate();
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"Application has stop");
        super.onStop();
        unregisterReceiver(mMainReceiver);
        // End media router discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
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
            case 4:
    			if (mSideBar.isUserLogin()) {
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
                Log.d(TAG,"Receive detail");
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



    /**
     * Callback for MediaRouter events
     */
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG,"RouteInfo is stored");
            //attempt to autoconnect to chromecast
            if(mSelectedDevice == null) {
                Log.d(TAG,  "Test for stored: info=" + route);
                mMediaRouter.selectRoute(route);
            }
        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected");
            // Handle the user route selection.
            Log.d(TAG, "onRouteSelected: " + info.getName());
            Log.d(TAG, "Default Route:" + mMediaRouter.getDefaultRoute());
            Log.d(TAG, "Selected Route:" + mMediaRouter.getSelectedRoute());
//            Log.d(TAG, "See selected Route:" + mMediaRouter.getRoutes().get(1));

//            writeToFile(mMediaRouter.getSelectedRoute().toString());
//            String routeString = readFromFile();
//            Log.d(TAG,"Route file string:" + routeString);
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            Bundle device_bundle = new Bundle();
            mSelectedDevice.putInBundle(device_bundle);

            File file = new File(getFilesDir(),"device");

            try {
               // deviceFile.createNewFile();0
                FileOutputStream fos = new FileOutputStream(file,false);
                Parcel parcel = Parcel.obtain();
                device_bundle.writeToParcel(parcel,0);
                fos.write(parcel.marshall());
                fos.flush();
                fos.close();
                Log.d("Store Device","Trying outputing file");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG,"DeviceId:" + mSelectedDevice);
            launchReceiver();


        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            teardown(false);
            mSelectedDevice = null;
        }


    }
    /**
     * Start the receiver app
     */


    private void launchReceiver() {
        Log.d(TAG,"Have launched");
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown(true);
                }

            };
            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();
            mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();


            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        teardown(true);
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(
                                    mApiClient,
                                    mCollectionQueueChannel.getNamespace(),
                                    mCollectionQueueChannel);
                        } catch (IOException e) {
                            //exception
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {
                    // Launch the receiver app
                    Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult:"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                mSessionId = result.getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d(TAG, "application name: "
                                                        + applicationMetadata.getName()
                                                        + ", status: " + applicationStatus
                                                        + ", sessionId: " + mSessionId
                                                        + ", wasLaunched: " + wasLaunched);
                                                mApplicationStarted = true;

                                                // Create the custom message
                                                // channel
                                                mCollectionQueueChannel = new CollectionQueueChannel();
                                                try {
                                                    Cast.CastApi.setMessageReceivedCallbacks(
                                                            mApiClient,
                                                            mCollectionQueueChannel.getNamespace(),
                                                            mCollectionQueueChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Exception while creating channel", e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                sendMessage("Talk to me");
                                            } else {
                                                Log.e(TAG, "application could not launch");
                                                teardown(true);
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended" + cause);
            mWaitingForReconnect = true;
            Log.d(TAG,"After disconnect, the selected device is:" + mSelectedDevice.toString());
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed " );//+ result.getErrorMessage()
            teardown(false);
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown(boolean selectDefaultRoute) {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mCollectionQueueChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mCollectionQueueChannel.getNamespace());
                            mCollectionQueueChannel = null;
                        }
                    } catch (IOException e) {
                        // Exception
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        if (selectDefaultRoute) {
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }

    /**
     * Send a text message to the receiver
     */
    public void sendMessage(String message) {
        Log.d(TAG,"oh yeah,sendMessage");
        if (mApiClient != null && mCollectionQueueChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                        mCollectionQueueChannel.getNamespace(), message).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                // Exception
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Custom message channel
     */
    class CollectionQueueChannel implements Cast.MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG,"Receive resultCode");
//        if (requestCode == Constants.REQUEST_CODE_SEND_TO_DISPLAY) {
//            if (resultCode == RESULT_OK) {
//                Order order = (Order)data.getSerializableExtra("order");
//                addNewData(String.valueOf(order.getQueueNum()));
//                mBeepManager.playBeepSoundAndVibrate(BeepManager.TYPE_ALARM);
//            }
//        }else if (requestCode == Constants.REQUEST_CODE_LOGIN) {
//            if (resultCode == RESULT_OK) {
//                invalidateOptionsMenu();
//            }
//        }
//
//    }

    private void getData(){
        if (mData == null) {
            mData = new LinkedList<Map<String, String>>();
        }
    }

    public void addNewData(String queue){
        Log.d(TAG,"lol,addNewData");
        Map<String, String> map = new HashMap<String, String>();
        map.put("queue_num", queue);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        map.put("queue_time", sdf.format(System.currentTimeMillis()));
        Log.d(TAG,queue);
//        Iterator<Map<String,String>> iter = mData.iterator();
//        while(iter.hasNext()) {
//            Map<String,String> q = iter.next();
//            if (q.get("queue_num").equals(queue)) {
//                iter.remove();
//                break;
//            }
//        }
//        mData.addFirst(map);
//        mAdapter.notifyDataSetChanged();
        sendMessage(queue+" "+map.get("queue_time"));
    }

    private class QueueAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private final WeakReference<ListView> mListView;

        public QueueAdapter(Context context, ListView listView){
            this.mInflater = LayoutInflater.from(context);
            this.mListView = new WeakReference<ListView>(listView);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Map<String,String> map = mData.get(i);
            TextView tv_queue;
            TextView tv_time;
            if (view != null) {
                tv_queue = (TextView)view.findViewById(R.id.tv_queue);
                tv_time = (TextView)view.findViewById(R.id.tv_time);
            }else{
                view = mInflater.inflate(R.layout.collection_queue_item, null);
                tv_queue = (TextView)view.findViewById(R.id.tv_queue);
                tv_time = (TextView)view.findViewById(R.id.tv_time);
            }
            tv_queue.setText(map.get("queue_num"));
            tv_time.setText(map.get("queue_time"));
            if ( i==0 ) {
                tv_queue.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_firstitem_size));
                tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_firstitem_size));
            }else{
                tv_queue.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_item_size));
                tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_item_size));
            }
            if (i%2 != 0) {
                view.setBackgroundColor(getResources().getColor(R.color.blue));
            }else{
                view.setBackgroundColor(getResources().getColor(R.color.white));
            }
            return view;
        }
    }
}
