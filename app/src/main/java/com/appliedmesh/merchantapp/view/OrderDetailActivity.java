package com.appliedmesh.merchantapp.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.appliedmesh.merchantapp.module.*;
import com.appliedmesh.merchantapp.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dongbin on 2015/6/4.
 */
public class OrderDetailActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{
    private final static long ALARM_DURATION=10*1000;
    private final static int MSG_ALARM = 1;
    private ViewPager mViewPager;
    private BeepManager mBeepManagerNewOrder;
    private BeepManager mBeepManagerAlarm;
    private OrderPagerAdapater mOrderPagerAdapater;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private AlphaAnimation mAnimation;
    private final OrderDetailHandler mHandler = new OrderDetailHandler(this);
    private FragmentOrder mCurrentFragment;
    private boolean mReachEnd = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderdetail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.orderpager);
        mOrderPagerAdapater = new OrderPagerAdapater(getSupportFragmentManager());

        mViewPager.setAdapter(mOrderPagerAdapater);
        int oldestOrder = OrderManager.getInstance().getOldestPendingIndex();
        int orderindex = getIntent().getIntExtra("orderindex", oldestOrder>0?oldestOrder:0);
        mViewPager.setOffscreenPageLimit(10);
        mViewPager.setCurrentItem(orderindex);
        mViewPager.addOnPageChangeListener(this);

        Order order = OrderManager.getInstance().getOrders().get(orderindex);
        //String title = getResources().getString(R.string.actionbar_title_orderid);
        //getSupportActionBar().setTitle(String.format(title, order.getId()));
        getSupportActionBar().setTitle(order.getId());

        mBeepManagerNewOrder = new BeepManager(this);
        mBeepManagerAlarm = new BeepManager(this);
        mTimer = new Timer(true);
        mTimerTask = new AlarmTask();
        mTimer.schedule(mTimerTask, 0, ALARM_DURATION);

        mAnimation = new AlphaAnimation(1,0);
        mAnimation.setDuration(200);
        mAnimation.setRepeatCount(10);
        mAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filterOrderConfimed = new IntentFilter(Constants.ACTION_ORDER_CONFIRMED);
        registerReceiver(mMainReceiver, filterOrderConfimed);
        IntentFilter filterOrderComing = new IntentFilter(Constants.ACTION_ORDER_COMING);
        registerReceiver(mMainReceiver, filterOrderComing);
    }

    public class OrderPagerAdapater extends FragmentStatePagerAdapter{
        private int prev_size = -1;
        public OrderPagerAdapater(FragmentManager fm){
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            android.support.v4.app.Fragment fragment = new FragmentOrder();
            Bundle args = new Bundle();
            args.putInt(Constants.ARGS_ORDER_INDEX, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return OrderManager.getInstance().getOrders().size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentFragment = (FragmentOrder)object;
            Order order = mCurrentFragment.getRelatedOrder();
            if (order != null) {
                //String title = getResources().getString(R.string.actionbar_title_orderid);
                //getSupportActionBar().setTitle(String.format(title, order.getId()));
                getSupportActionBar().setTitle(order.getId());
            }
            super.setPrimaryItem(container, position, object);
        }
    }

    @Override
    public void onPageSelected(int i) {
        if(i == mOrderPagerAdapater.getCount() -1) {
            mReachEnd = true;
        }else{
            mReachEnd = false;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    /**
     * This will execute after the along with the displaying of the OrderDetail View to display
     * the Pending XX(NN) text at the top right corner of the view
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_pending);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int oldest = OrderManager.getInstance().getOldestPendingIndex();
                if (oldest >= 0) {
                    mViewPager.setCurrentItem(oldest, true);
                }
                return false;
            }
        });
        int pendingorders = OrderManager.getInstance().getPendingNum();
        int pendingproducts = OrderManager.getInstance().getPendingTotalProducts();
        if (pendingorders > 0) {
            String title = getResources().getString(R.string.order_pending)
                    + " " + Integer.toString(pendingorders)
                    + "(" + Integer.toString(pendingproducts)
                    + ")";
            item.setTitle(title);
            item.setVisible(true);

        }else{
//            item.setVisible(false);
            item.setTitle("");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This will execute after the Pending NN(MM) text at the top right of the ViewAdapter is clicked
     *
     * It will display the Order List page
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class AlarmTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(MSG_ALARM);
        }
    }

    private BroadcastReceiver mMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_ORDER_CONFIRMED)) {
                invalidateOptionsMenu();
                //switch to next page
                int oldest = OrderManager.getInstance().getOldestPendingIndex();
                if (oldest >= 0) {
                    mViewPager.setCurrentItem(oldest, true);
                }else {
                    String orderid = intent.getStringExtra("orderid");
                    int index = OrderManager.getInstance().getIndexbyId(orderid);
                    mViewPager.setCurrentItem(++index, true);
                }

            } else if (intent.getAction().equals(Constants.ACTION_ORDER_COMING)) {
                invalidateOptionsMenu();
                mOrderPagerAdapater.notifyDataSetChanged();
//                mBeepManagerNewOrder.playBeepSoundAndVibrate(BeepManager.TYPE_NEW_ORDER);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                            mBeepManagerNewOrder.playBeepSoundAndVibrate(BeepManager.TYPE_NEW_ORDER);
                    }
                }).start();

                int currentitem = mViewPager.getCurrentItem();
                if (mReachEnd) {
                    mViewPager.setCurrentItem(currentitem+1, true);
                }
            }
            else if (intent.getAction().equals(Constants.ACTION_ORDER_CONFIRM_ERROR)) {
                String error_message = intent.getStringExtra("error_message");
                /*
                 * Do Error Stuff
                 */
                Toast.makeText(context, error_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mTimerTask == null) {
            mTimerTask = new AlarmTask();
            mTimer.schedule(mTimerTask, ALARM_DURATION, ALARM_DURATION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mMainReceiver);
    }

    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class OrderDetailHandler extends Handler {
        private final WeakReference<OrderDetailActivity> mActivity;

        public OrderDetailHandler(OrderDetailActivity activity) {
            mActivity = new WeakReference<OrderDetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OrderDetailActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_ALARM:
                        if (activity.mCurrentFragment != null) {
                            boolean needFlash = activity.mCurrentFragment.updateOrderStatus();
                            if (needFlash) {
                                activity.mViewPager.startAnimation(activity.mAnimation);
                            }
                        }
                        int oldest = OrderManager.getInstance().getOldestPendingIndex();
                        if (oldest >= 0
                                && OrderManager.getInstance().getOrders().get(oldest).getStatus() == Order.STATUS_VERY_URGENT
                        ) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Activity activity = mActivity.get();
                                    if (activity != null) {
                                        mActivity.get().mBeepManagerAlarm.playBeepSoundAndVibrate(BeepManager.TYPE_ALARM);
                                    }
                                }
                            }).start();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}