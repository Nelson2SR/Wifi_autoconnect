package com.appliedmesh.merchantapp.view;

import java.io.File;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.network.DownloadManager;


public class ActivityDownload extends AppCompatActivity {
	ProgressDialog mProgressDlg;
	IntentFilter updateFilter;
	DownloadManager mDownloadManager;
	public static String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Merchant-app.apk";
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        updateFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_PROGRESS);
	        mDownloadManager = DownloadManager.getInstance(this);
	        String url = getIntent().getStringExtra(Constants.ARGS_DOWNLOAD_URL);
	        File apkfile = new  File(apkPath);
	        if(apkfile.exists()){
	        	apkfile.delete();
	        }
	        mDownloadManager.DownloadFile(url,apkPath);
	        mProgressDlg = new ProgressDialog(ActivityDownload.this);
	        mProgressDlg.setTitle(getString(R.string. app_update));
	        mProgressDlg.setMessage(getString(R.string.download_progress));
	        mProgressDlg.setIcon(R.drawable.oth_logo);
	        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        mProgressDlg.setCancelable(false);
	        mProgressDlg.setProgress(0);
	        mProgressDlg.show();
	    }
		private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent!=null){
					int process = intent.getIntExtra(DownloadManager.EXTRA_PROGRESS,0);
			        mProgressDlg.setProgress(process);
			        if(process==100){
			        	Intent install = new Intent(Intent.ACTION_VIEW);
			        	install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        	install.setDataAndType(Uri.parse("file://" + apkPath),"application/vnd.android.package-archive");
			        	ActivityDownload.this.startActivity(install);
			        	ActivityDownload.this.finish();
			        }
				}
			};
		};
		@Override
		public void onStart() {
			LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, updateFilter);
			super.onStart();
		}
		@Override
		public void onStop() {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
			super.onStop();
		}
		@Override
		protected void onDestroy() {
			super.onDestroy();
		}
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
//			super.onBackPressed();
		}
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				return super.onOptionsItemSelected(item);

			default:
				break;
			}
			return super.onOptionsItemSelected(item);
		}
}
