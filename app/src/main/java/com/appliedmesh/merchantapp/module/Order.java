package com.appliedmesh.merchantapp.module;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dongbin on 2015/6/5.
 */
public class Order implements Serializable{
    public static final String PRODUCT_ID = "id";
    public static final String PRODUCT_QTY = "qty";
    public static final String PRODUCT_OPTION_HOT_ICE = "hc";
    public static final String PRODUCT_OPTION_SWEETNESS = "sweetness";
    public static final String PRODUCT_OPTION_THICKNESS = "thick";
    public static final String PRODUCT_OPTION_MILK = "milk";
    public static final String PRODUCT_OPTION_ICE = "ice";
    public static final String PRODUCT_OPTION_OTHER = "other";
    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_CONFIRMED = 0;
    public static final int STATUS_NOT_URGENT = 1;
    public static final int STATUS_URGENT = 2;
    public static final int STATUS_VERY_URGENT = 3;
    public static final int TIME_DURATION_TO_URGENT = 30*1000;
    public static final int TIME_DURATION_TO_VERY_URGENT = 60*1000;
    private String mId;
    private Timestamp mOrderTime;
    private Timestamp mStartTime;
    private int mQueueNum;
    private List<Map<String, String>> mItems;
    private int mEstimateTime;
    private int mStatus;

    public Order(String id) {
        this(id, new Timestamp(0), new Timestamp(0), -1, null, -1);
    }

    public Order(String id, Timestamp ordertime, Timestamp starttime, int queuenum, List<Map<String, String>> itemlist, int estimatetime) {
        mId = id;
        mOrderTime = ordertime;
        mStartTime = starttime;
        mQueueNum = queuenum;
        mItems = itemlist;
        mEstimateTime = estimatetime;
        updateStatus();
    }

    public String getId() {
        return mId;
    }
    public int getQueueNum() {
        return mQueueNum;
    }
    public int getEstimateTime() {
        return mEstimateTime;
    }
    public Timestamp getOrderTime() {
        return mOrderTime;
    }
    public Timestamp getStartTime() {
        return mStartTime;
    }
    public List<Map<String, String>> getItems(){
        return mItems;
    }
    public void setEstimateTime(int estimateTime) {
        mEstimateTime = estimateTime;
        if (mStartTime.equals(new Timestamp(0))) {
            mStartTime = new Timestamp(System.currentTimeMillis());
        }
        OrderManager.getInstance().orderConfirmed(this);
        updateStatus();
    }
    public void updateStatus(){
        if (mEstimateTime >= 0) {
            mStatus = STATUS_CONFIRMED;
        }else {
            /*
             * Wen Yen added a 16 * 3600 seconds to the mOrderTime in order to make it Singpapore.
             * We have no idea why we need to add 16 instead of 8 hours.
             * This change will only affect this block, i.e. we did not fix the root of the problem
             */
//            long duration = System.currentTimeMillis() - mOrderTime.getTime();
            long duration = System.currentTimeMillis() - (mOrderTime.getTime() + (8 * 3600 * 1000));
            if (duration >= TIME_DURATION_TO_VERY_URGENT) {
                mStatus = STATUS_VERY_URGENT;
            }else if (duration >= TIME_DURATION_TO_URGENT) {
                mStatus = STATUS_URGENT;
            }else{
                mStatus = STATUS_NOT_URGENT;
            }
        }
    }

    public int getStatus() {
        updateStatus();
        return mStatus;
    }

    public long getWaitingTime(){
        if (mEstimateTime >= 0) {
            return 0;
        }else{
            return System.currentTimeMillis() - mOrderTime.getTime();
        }
    }

    public int getTotalProducts(){
        Iterator<Map<String,String>> iter = mItems.iterator();
        int total=0;
        while(iter.hasNext()) {
            Map<String, String> item = iter.next();
            if (item.containsKey(PRODUCT_QTY)) {
                total += Integer.parseInt(item.get(PRODUCT_QTY));
            }
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Order) {
            final Order order = (Order) o;
            if (this.mId.equals(order.mId)) {
                return true;
            }
        }
        return false;
    }
}
