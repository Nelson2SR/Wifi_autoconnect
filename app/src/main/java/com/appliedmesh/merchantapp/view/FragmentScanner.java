//package com.appliedmesh.merchantapp.view;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.appliedmesh.merchantapp.R;
//import com.appliedmesh.merchantapp.module.Constants;
//import com.google.zxing.Result;
//import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;
//
///**
// * Created by mito on 9/17/13.
// */
//public class FragmentScanner extends BarCodeScannerFragment implements View.OnClickListener{
//	boolean torchState = false;
//	TextView tvFlashLight;
//    TextView tvLoadImage;
//    TextView tvEnterBarcode;
//	public void onCreate(Bundle savedInstanceState) {
//
//		super.onCreate(savedInstanceState);
//
//		this.setmCallBack(new IResultCallback() {
//			@Override
//			public void result(Result lastResult) {
//				//Toast.makeText(getActivity(), "Scan: " + lastResult.toString(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Constants.ACTION_SCAN_RESULT);
//                intent.putExtra("receipt", lastResult.toString());
//                getActivity().sendBroadcast(intent);
//			}
//		});
//	}
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		tvFlashLight = (TextView) getView().findViewById(R.id.tvFlashLight);
//		tvFlashLight.setOnClickListener(this);
//        tvEnterBarcode = (TextView) getView().findViewById(R.id.tvEnterReceipt);
//        tvEnterBarcode.setOnClickListener(this);
//        tvLoadImage  = (TextView) getView().findViewById(R.id.tvLoadImage);
//        tvLoadImage.setOnClickListener(this);
//		super.onActivityCreated(savedInstanceState);
//	}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
//        View ret = super.onCreateView(inflater, container, savedInstance);
//        final TextView tvScanBarcode = (TextView)ret.findViewById(R.id.TvBtnScan);
//        tvScanBarcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                restartPreviewAfterDelay(0);
//            }
//        });
//        return ret;
//    }
//
//    @Override
//    public void onClick(View view) {
//        if (view == tvFlashLight) {
//            toggleTorch();
//        }else if (view  == tvEnterBarcode) {
//            final EditText input_receipt = new EditText(getActivity());
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(getString(R.string.enter_receipt))
//                    .setIcon(android.R.drawable.ic_dialog_info)
//                    .setView(input_receipt)
//                    .setCancelable(false)
//                    .setPositiveButton(getString(R.string.cancel), null)
//                    .setNegativeButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            String receipt = input_receipt.getText().toString();
//                            Intent intent = new Intent(Constants.ACTION_SCAN_RESULT);
//                            intent.putExtra("receipt", receipt);
//                            getActivity().sendBroadcast(intent);
//                        }
//                    });
//            builder.show();
//
//        }else if (view == tvLoadImage) {
//            Toast.makeText(getActivity(), "Feature currently not available", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void toggleTorch() {
//		torchState = !torchState;
//		setTorch(torchState);
//	}
//
//	public FragmentScanner() {
//
//	}
//}