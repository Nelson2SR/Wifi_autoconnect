package com.appliedmesh.merchantapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.LoginRequest;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Utils;
import com.appliedmesh.merchantapp.utils.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ActivityLoginOption  extends AppCompatActivity {
	Button btnLogin, btnGuestCheckout, btnSignUp;
	RadioButton rbLogin, rbGuestCheckout;
	RadioGroup rbLoginOption;
	TextView tvGuestCheckout, tvLogin, tvForgot, tvLoginText;
	EditText etLogin, etPassword, etEmail;
	int checkedId;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_login_option);
		

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		etEmail = (EditText) findViewById(R.id.etEmail);
		etPassword = (EditText) findViewById(R.id.etPassword);
		etLogin = (EditText) findViewById(R.id.etLogin);
		tvLoginText = (TextView) findViewById(R.id.tvLoginText);
		tvForgot = (TextView) findViewById(R.id.tvForgot);
		tvGuestCheckout = (TextView) findViewById(R.id.tvGuestCheckout);
		tvLogin = (TextView) findViewById(R.id.tvLogin);
		btnSignUp = (Button) findViewById(R.id.btnSignUp);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		rbLoginOption = (RadioGroup) findViewById(R.id.rbLoginOption);
		RadioButton rbGuestCheckout = (RadioButton) findViewById(R.id.rbGuestCheckout);
		rbGuestCheckout.setVisibility(View.GONE);

		String username = SharedPrefHelper.getString(this, Constants.NAME_USERNAME);
		if (username!=null && !username.equals("")) {
			etLogin.setText(username);
		}

		rbLoginOption.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ActivityLoginOption.this.checkedId = checkedId;
				if (checkedId == R.id.rbLogin) {
					tvLogin.setVisibility(View.VISIBLE);
					tvGuestCheckout.setVisibility(View.GONE);
					tvForgot.setVisibility(View.VISIBLE);
					etEmail.setVisibility(View.GONE);
					etLogin.setVisibility(View.VISIBLE);
					etPassword.setVisibility(View.VISIBLE);
				} else if (checkedId == R.id.rbGuestCheckout) {
					tvLogin.setVisibility(View.GONE);
					tvGuestCheckout.setVisibility(View.VISIBLE);
					tvForgot.setVisibility(View.GONE);
					etEmail.setVisibility(View.VISIBLE);
					etLogin.setVisibility(View.GONE);
					etPassword.setVisibility(View.GONE);
				}

			}

		});
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rbLoginOption.getCheckedRadioButtonId() == R.id.rbLogin) {
					final String userID = etLogin.getText().toString();
					String password = etPassword.getText().toString();
					if (!TextUtils.isEmpty(userID)) {
						LoginRequest req = new LoginRequest(ActivityLoginOption.this, userID, password, ServerConfigs.MERCHANT_CODE, new JsonObjectRequestCallback() {
							@Override
							public void onRequestSuccess(JSONObject value) {
								try {
                                    String deviceId = Utils.getDeviceId(ActivityLoginOption.this);
									String regId = value.getString("registration_id");
                                    String regSecret = value.getString("registration_secret");

                                    String regIdEncrypted = Utils.encrypt(deviceId, regId);
                                    String regSecretEncrypted = Utils.encrypt(deviceId, regSecret);

                                    SharedPrefHelper.set(ActivityLoginOption.this, Constants.REGISTRATION_ID, regIdEncrypted);
                                    SharedPrefHelper.set(ActivityLoginOption.this, Constants.REGISTRATION_SECRET, regSecretEncrypted);
									SharedPrefHelper.set(ActivityLoginOption.this, Constants.MERCHANT_NAME, value.getString("merchant_name"));
									SharedPrefHelper.set(ActivityLoginOption.this, Constants.MERCHANT_OUTLET, value.getString("outlet_name"));
									SharedPrefHelper.set(ActivityLoginOption.this, Constants.NAME_USERNAME, userID);
									Toast.makeText(ActivityLoginOption.this, "Login Successful", Toast.LENGTH_LONG).show();
									finish();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onRequestFailed(String errorMessage) {
								Toast.makeText(ActivityLoginOption.this, "Invalid User Name or Password", Toast.LENGTH_SHORT).show();
							}
						});
						Volley.getInstance().addToRequestQueue(req);
					} else {
						Toast.makeText(ActivityLoginOption.this, R.string.please_enter_a_username, Toast.LENGTH_LONG).show();
					}
				} else {
					// Call Purchae API and get the URL
					// TODO check if both edittext are the same or not
					String userID = etEmail.getText().toString();
					if (TextUtils.isEmpty(userID) && isEmailValid(userID))
						return;
				}
			}
		});
		btnSignUp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(ActivityLoginOption.this, ActivitySignUp.class);
				startActivity(i);

			}
		});
		tvForgot.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String userID = etLogin.getText().toString();
				if (TextUtils.isEmpty(userID)){
					Toast.makeText(ActivityLoginOption.this, R.string.please_enter_a_username, Toast.LENGTH_LONG).show();
					return;
				}else{
					//TODO
				}
			}
		});

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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
}
