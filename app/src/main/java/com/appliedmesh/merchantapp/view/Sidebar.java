package com.appliedmesh.merchantapp.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;

import java.io.File;

public class Sidebar extends Fragment implements OnClickListener {
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationDrawerCallbacks mCallbacks;
	private DrawerLayout mDrawerLayout;
	private View mFragmentContainerView;
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	private int mCurrentSelectedPosition = 0;
	TextView tvDev,tvLogin;

	public Sidebar() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			selectItem(mCurrentSelectedPosition);
		}else
			selectItem(1);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.sidebar, container, false);
		TextView tvScanBarcode, tvOrderPage, tvOrderCollection,tvVersion;
		tvScanBarcode = (TextView) v.findViewById(R.id.tvScanBarcode);
		tvOrderPage= (TextView) v.findViewById(R.id.tvOrderPage);
		tvOrderCollection= (TextView) v.findViewById(R.id.tvOrderCollection);
		tvDev = (TextView) v.findViewById(R.id.tvDev);
		tvLogin =  (TextView) v.findViewById(R.id.tvLogin);
		tvVersion = (TextView) v.findViewById(R.id.tvVersion);

		tvScanBarcode.setOnClickListener(this);
		tvOrderPage.setOnClickListener(this);
		tvOrderCollection.setOnClickListener(this);
		tvDev.setOnClickListener(this);
		tvLogin.setOnClickListener(this);
		tvVersion.setText(getVersionName());

		return v;
	}

	private String getVersionName() {
		try {
			PackageManager manager = getActivity().getPackageManager();
			PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// get profile from database

	public boolean isDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 * 
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, final DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.open, R.string.close);

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void selectItem(int position) {
		mCurrentSelectedPosition = position;

		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			mCallbacks.onNavigationDrawerItemSelected(position);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void toggleDrawer() {
		if (isDrawerOpen()) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		} else {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}
	}

	/**
	 * Manually adjust drawer selected position as it might not be in sync with
	 * selected position when Goals or About is selected and orientation is
	 * changed.
	 */

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
	public static interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(int position);
	}

	public void setNavigationDrawerEnabled(boolean enabled) {
		if (enabled) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		} else {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvScanBarcode:
			selectItem(0);
			break;
		case R.id.tvOrderPage:
			selectItem(1);
			break;
        case R.id.tvOrderCollection:
            selectItem(2);
            break;
        case R.id.tvDev:
            selectItem(3);
            break;
        case R.id.tvLogin:
            selectItem(4);
            break;
		default:
			break;
		}
	}
	
	public void updateHostName() {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dev.mypointofpurchase.com");
		if(file.exists()){
			tvDev.setVisibility(View.VISIBLE);
		}else{
			tvDev.setVisibility(View.GONE);
		}
		if (getUrl().equals(ServerConfigs.BASEURL_MYPOINT)) {
			tvDev.setText("Change to DEV");
		} else {
			tvDev.setText("Change to FNB");
		}
	}
	public String getUrl(){
		String url = SharedPrefHelper.getString(getActivity(), Constants.NAME_SERVER_URL);
		if(url==null || url.equals("")){
			setUrl(ServerConfigs.BASEURL_MYPOINT);
			url = ServerConfigs.BASEURL_MYPOINT;
		}
		return url;
	}
	public void setUrl( String url) {
		SharedPrefHelper.set(getActivity(), Constants.NAME_SERVER_URL, url);
	}
	public void updateLoginStatus() {
		if (isUserLogin()) {
			tvLogin.setText(R.string.logout);
		} else {
			tvLogin.setText(R.string.login);
		}
	}
	public  boolean isUserLogin() {
		return (SharedPrefHelper.getString(getActivity(), Constants.REGISTRATION_ID).trim().length() > 0);
	}

	public void setUserName( String username) {
		SharedPrefHelper.set(getActivity(), Constants.NAME_USERNAME, username);
	}
	public String getUserName() {
		String username = SharedPrefHelper.getString(getActivity(), Constants.NAME_USERNAME);
		return username;
	}
	public String getToken() {
		/*
		 * Not used for now; was only use for previous POC app.
		 */
		return "";
	}

}
