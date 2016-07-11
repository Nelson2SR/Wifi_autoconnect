package com.appliedmesh.merchantapp.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;

/**
 * Created by Home on 2015/4/4.
 */
public class FragmentWelcome extends Fragment{
    EditText mMerchantId;
    EditText mSecurityKey;
    Button mBtnConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.welcome, container,false);
        mMerchantId = (EditText) v.findViewById(R.id.editText_merchant_id);
        mSecurityKey = (EditText) v.findViewById(R.id.editText_security_key);
        mBtnConfirm = (Button) v.findViewById(R.id.button_confirmation);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String merchantid = mMerchantId.getText().toString();
                String securitykey = mSecurityKey.getText().toString();
                if (merchantid == null || merchantid.equals("")) {
                    showDialog(getString(R.string.merchant_id_hint));
                    return;
                }
                if (securitykey == null || securitykey.equals("")) {
                    showDialog(getString(R.string.security_key_hint));
                    return;
                }
                Intent intent = new Intent(Constants.ACTION_WELCOME_RESULT);
                intent.putExtra(Constants.NAME_MERCHANT_ID, merchantid);
                intent.putExtra(Constants.NAME_SECURITY_KEY, securitykey);
                getActivity().sendBroadcast(intent);
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), null);
        builder.create().show();
    }
}
