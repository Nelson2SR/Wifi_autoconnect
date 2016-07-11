package com.appliedmesh.merchantapp.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.network.SignUpRequest;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ActivitySignUp extends AppCompatActivity {
	Button btnSignUp;
	EditText etUserName;
	EditText etPassword1, etPassword2, etEmail,etEmail1;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public  boolean isEmailValid(String email) {
		boolean isValid = false;

		if (!TextUtils.isEmpty(email)) {
			CharSequence inputStr = email;
			Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (matcher.matches()) {
				isValid = true;
			}
		}
		return isValid;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showDialog(String email) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(ActivitySignUp.this);

		myAlertDialog.setCancelable(false);
       if(!TextUtils.isEmpty(email)){
    	   myAlertDialog.setMessage(R.string.sign_up_email_complete_message);
       }else{
   		myAlertDialog.setMessage(R.string.sign_up_noemail_complete_message);
       }
		myAlertDialog.setTitle(R.string.sign_up_complete_title);
		myAlertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				setResult(Activity.RESULT_OK);
				ActivitySignUp.this.finish();
			}
		});

		myAlertDialog.show();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_signup);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		etUserName =  (EditText) findViewById(R.id.etUser);
		etEmail = (EditText) findViewById(R.id.etLogin);
		etEmail1 = (EditText) findViewById(R.id.etLogin1);
		etPassword1 = (EditText) findViewById(R.id.etPassword1);
		etPassword2 = (EditText) findViewById(R.id.etPassword2);
		btnSignUp = (Button) findViewById(R.id.btnSignUp);

		btnSignUp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String username= etUserName.getText().toString();
				final String userID = etEmail.getText().toString();
				final String userID1 = etEmail1.getText().toString();
				String password1 = etPassword1.getText().toString();
				String password2 = etPassword2.getText().toString();
				if(TextUtils.isEmpty(username) ){
					 Toast.makeText(ActivitySignUp.this, R.string.username_cannot_be_empty, Toast.LENGTH_LONG).show();
					 return;
				}
				if((TextUtils.isEmpty(userID)&&TextUtils.isEmpty(userID1)) || (isEmailValid(userID)&&isEmailValid(userID1)) ){
					if((!TextUtils.isEmpty(userID)&&!TextUtils.isEmpty(userID1)) && !userID.equals(userID1)){
						 Toast.makeText(ActivitySignUp.this, R.string.your_email_doesn_t_match, Toast.LENGTH_LONG).show();
						 return;
					}
					if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(password2) && password1.equals(password2)) {
						SignUpRequest req = new SignUpRequest(ActivitySignUp.this, username,password1, ServerConfigs.MERCHANT_CODE, ServerConfigs.STORE_CODE,userID,new JsonObjectRequestCallback() {
				            @Override
				            public void onRequestSuccess(JSONObject value) {
								SharedPrefHelper.set(ActivitySignUp.this, Constants.NAME_USERNAME, userID);
								showDialog(userID);
				            }

				            @Override
				            public void onRequestFailed(String errorMessage) {
				            	Toast.makeText(ActivitySignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
				            }
				        });

					} else if (!password1.equals(password2)) {
		                Toast.makeText(ActivitySignUp.this, R.string.your_passwords_doesn_t_match, Toast.LENGTH_LONG).show();
					}
				} else {
	                Toast.makeText(ActivitySignUp.this, R.string.please_enter_a_valid_email_address, Toast.LENGTH_LONG).show();
				}
			}
			
		});
	}
}
