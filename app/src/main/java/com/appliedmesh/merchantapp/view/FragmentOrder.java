package com.appliedmesh.merchantapp.view;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.module.Order;
import com.appliedmesh.merchantapp.module.OrderManager;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.SetWaitingTimeRequest;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.Volley;
import com.appliedmesh.merchantapp.R;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by dongbin on 2015/6/4.
 */
public class FragmentOrder extends android.support.v4.app.Fragment implements View.OnClickListener{
    private final static String TAG = FragmentOrder.class.getSimpleName();

    private static int ORDER_BG_CONFIRMED;
    private static int ORDER_BG_NORMAL;
    private static int ORDER_BG_URGENT;
    private static int ORDER_BG_VERY_URGENT;
    private RelativeLayout mRootView;
    private TextView[] mEstimeViewlist;
    private Order mOrder;
    private int mOrderStatus = Order.STATUS_UNKNOWN;
    private LayoutInflater mInflater;
    private boolean mInSetWaitingTime = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEstimeViewlist = new TextView[4];
        Bundle args = getArguments();
        int index = args.getInt(Constants.ARGS_ORDER_INDEX);
        mOrder = OrderManager.getInstance().getOrders().get(index);

        ORDER_BG_CONFIRMED = getResources().getColor(R.color.white);
        ORDER_BG_NORMAL = getResources().getColor(R.color.order_bg);
        ORDER_BG_URGENT = getResources().getColor(R.color.order_bg_urgent);
        ORDER_BG_VERY_URGENT = getResources().getColor(R.color.order_bg_very_urgent);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        mRootView = (RelativeLayout)inflater.inflate(R.layout.order, container, false);
        initView();
        return mRootView;
    }

    private void initView() {
        Log.d("FRAGMENT ORDER", "Debug order => "+mOrder.getItems());


        if(mRootView == null)
            return;
        //set digital clock typeface
        Typeface fontFace = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Clockopia.ttf");
        TextView tvordertime = (TextView) mRootView.findViewById(R.id.tv_order_time);
        tvordertime.setTypeface(fontFace);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+16"));
        tvordertime.setText(sdf.format(mOrder.getOrderTime()));
        TextView tvStartTime = (TextView)mRootView.findViewById(R.id.tv_start_time);
        tvStartTime.setTypeface(fontFace);

        //set queue number
        TextView tvQueue = (TextView)mRootView.findViewById(R.id.tv_queue_num);
        tvQueue.setText(Integer.toString(mOrder.getQueueNum()));

        //setup item layout
        LinearLayout llproducts = (LinearLayout)mRootView.findViewById(R.id.ll_products);
        List<Map<String,String>> itemList = mOrder.getItems();
        Iterator<Map<String,String>> iter = itemList.iterator();
        int total=0;
        int index = 0;
        while(iter.hasNext()) {
            Map<String, String> item = iter.next();
            RelativeLayout layout = genItemLayout(item.get("name"), item.get("qty"));
            if (index%2 != 0) {
                layout.setBackgroundColor(getResources().getColor(R.color.order_item_bg));
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llproducts.addView(layout,index++,lp);
            if (item.containsKey("qty")) {
                total += Integer.parseInt(item.get("qty"));
            }
        }
        //set total number
        TextView tvtotal = (TextView)mRootView.findViewById(R.id.tv_order_totalnum);
        tvtotal.setText(Integer.toString(total));

        //init estimate selection
        mEstimeViewlist[0] = (TextView)mRootView.findViewById(R.id.tv_estimate_5);
        mEstimeViewlist[1] = (TextView)mRootView.findViewById(R.id.tv_estimate_15);
        mEstimeViewlist[2] = (TextView)mRootView.findViewById(R.id.tv_estimate_30);
        mEstimeViewlist[3] = (TextView)mRootView.findViewById(R.id.tv_estimate_30_plus);
        for (int i=0; i<4; i++){
            mEstimeViewlist[i].setOnClickListener(this);
        }

        //update background color
        updateOrderStatus();
    }

    private long getEstimateMillis(){
        switch (mOrder.getEstimateTime()) {
            case 0:
                return 5*60*1000;
            case 1:
                return 15*60*1000;
            case 2:
                return 30*60*1000;
            case 3:
                return 40*60*1000;
            default:
                break;
        }
        return 0;
    }

    private void setViewConfirmed(){
        //show order start time
        TextView tvStart = (TextView)mRootView.findViewById(R.id.tv_start);
        tvStart.setVisibility(View.VISIBLE);
        TextView tvStartTime = (TextView)mRootView.findViewById(R.id.tv_start_time);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM HH:mm:ss");
        tvStartTime.setText(sdf.format(mOrder.getStartTime()));
        tvStartTime.setVisibility(View.VISIBLE);

        //set backgroundcolor
        mRootView.setBackgroundColor(ORDER_BG_CONFIRMED);

        //show collection time
        TextView tvCollectTime = (TextView)mRootView.findViewById(R.id.tv_collection_time);
        Timestamp et = new Timestamp(mOrder.getStartTime().getTime() + getEstimateMillis());
        SimpleDateFormat sdf2 = new SimpleDateFormat("EEE d MMM HH:mm");
        tvCollectTime.setText(sdf2.format(et));
        View estimate = mRootView.findViewById(R.id.ll_estimate);
        View collect = mRootView.findViewById(R.id.rl_collection_time);
        estimate.setVisibility(View.GONE);
        collect.setVisibility(View.VISIBLE);
    }

    private RelativeLayout genItemLayout(String name, String quantity) {
        RelativeLayout layout = (RelativeLayout)mInflater.inflate(R.layout.productlistitem,null);
        TextView tvname = (TextView)layout.findViewById(R.id.tv_name);
        tvname.setText(Html.fromHtml(name));
        TextView tvqty = (TextView)layout.findViewById(R.id.tv_qty);
        tvqty.setText(quantity);

        return layout;
    }

    @Override
    public void onClick(View view) {
        for (int i=0;i<4;i++) {
            if (mEstimeViewlist[i] == view) {
                String waitingtime = OrderManager.getInstance().getEstimateString(i);
                final int index = i;
                if (waitingtime != null && !mInSetWaitingTime) {
                    SetWaitingTimeRequest req = new SetWaitingTimeRequest(getActivity(), waitingtime, mOrder.getId(), new JsonObjectRequestCallback() {
                        @Override
                        public void onRequestSuccess(JSONObject value) {
                            mOrder.setEstimateTime(index);
                            updateOrderStatus();
                            Intent intent = new Intent(Constants.ACTION_ORDER_CONFIRMED);
                            intent.putExtra("orderid", mOrder.getId());
                            getActivity().sendBroadcast(intent);
                            mInSetWaitingTime = false;
                        }

                        @Override
                        public void onRequestFailed(String errorMessage) {
                            Logger.e(TAG, errorMessage);

                            if (errorMessage == null) {
                                /*
                                 * We don't know what to do with this!
                                 */
                            }
                            //check if this order is confrimed already
                            else  if (errorMessage.contains("has already been processed before")) {
                                Toast.makeText(getActivity(), "Order has been processed", Toast.LENGTH_SHORT).show();
                                mOrder.setEstimateTime(0);
                                updateOrderStatus();
                                Intent intent = new Intent(Constants.ACTION_ORDER_CONFIRMED);
                                intent.putExtra("orderid", mOrder.getId());
                                getActivity().sendBroadcast(intent);
                                mInSetWaitingTime = false;
                            }else {
                                Intent intent = new Intent(Constants.ACTION_ORDER_CONFIRM_ERROR);
                                intent.putExtra("orderid", mOrder.getId());
                                intent.putExtra("error_msg", errorMessage);
                                getActivity().sendBroadcast(intent);
                                mInSetWaitingTime = false;
                            }
                        }
                    });
                    Volley.getInstance().addToRequestQueue(req);
                    mInSetWaitingTime = true;
                }
                break;
            }
        }
    }

    public boolean updateOrderStatus(){
        boolean needAlarm=false;
        if (mOrder != null) {
            int status = mOrder.getStatus();
            if (status != mOrderStatus) {
                mOrderStatus = status;
                switch (mOrderStatus) {
                    case Order.STATUS_CONFIRMED:
                        setViewConfirmed();
                        break;
                    case Order.STATUS_NOT_URGENT:
                        mRootView.setBackgroundColor(ORDER_BG_NORMAL);
                        break;
                    case Order.STATUS_URGENT:
                        mRootView.setBackgroundColor(ORDER_BG_URGENT);
                        needAlarm = true;
                        break;
                    case Order.STATUS_VERY_URGENT:
                        needAlarm = true;
                        mRootView.setBackgroundColor(ORDER_BG_VERY_URGENT);
                        break;
                }
            }
        }
        return needAlarm;
    }

    public Order getRelatedOrder() {
        return mOrder;
    }
}