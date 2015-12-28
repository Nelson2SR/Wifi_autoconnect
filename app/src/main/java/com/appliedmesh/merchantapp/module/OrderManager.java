package com.appliedmesh.merchantapp.module;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.appliedmesh.merchantapp.network.GetNewOrdersRequest;
import com.appliedmesh.merchantapp.network.JsonObjectRequestCallback;
import com.appliedmesh.merchantapp.network.ServerConfigs;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dongbin on 2015/6/5.
 */
public class OrderManager {
    private static String TAG = "OrderManger";
    private static OrderManager sOrderManager;
    private final static long QUERY_DURATION = 1000;
    private final static long QUERY_PERIOD = 10 * 1000;
    private Context mCtx;
    private List<Order> mOrderList;
    private List<Order> mPendingOrderList;
    private Map<String, String> mProductTable;
    private String[] mProducts;
    private String[] mEstimateTime;
    private final static int PRODUCT_NUM = 27;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean mInited = false;

    private OrderManager() {
        mOrderList = new ArrayList<Order>();
        mPendingOrderList = new LinkedList<Order>();
        mProductTable = new HashMap<String, String>();
        mProducts = new String[PRODUCT_NUM];
        mEstimateTime = new String[4];
        mTimer = new Timer(true);
    }

    public static OrderManager getInstance() {
        if (sOrderManager == null) {
            sOrderManager = new OrderManager();
        }
        return sOrderManager;
    }

    //TODO: just for test, pls remove after get data from remote server
    public void init(Context context) {
        if (!mInited) {
            mCtx = context;
            mProducts[0] = "Tea-O";
            mProducts[1] = "Coffee-O";
            mProducts[2] = "Tea";
            mProducts[3] = "Coffee";
            mProducts[4] = "Tea-C";
            mProducts[5] = "Coffee-C";
            mProducts[6] = "Yuan Yang";
            mProducts[7] = "Gula Melaka Tea";
            mProducts[8] = "Gula Melaka Coffee";
            mProducts[9] = "Almond Tea";
            mProducts[10] = "Almond Coffee";
            mProducts[11] = "Almond Milk";
            mProducts[12] = "Ginger Tea";
            mProducts[13] = "Honey Tea-O";
            mProducts[14] = "Honey Milk Tea";
            mProducts[15] = "Honey-O";
            mProducts[16] = "Lemon Tea";
            mProducts[17] = "Honey Lemon Tea";
            mProducts[18] = "Honey Lemon";
            mProducts[19] = "Lime Tea";
            mProducts[20] = "Honey Lime Tea";
            mProducts[21] = "Honey Lime";
            mProducts[22] = "Iced Salted Lemon";
            mProducts[23] = "Milo";
            mProducts[24] = "Horlicks";
            mProducts[25] = "Iced Milo Dino";
            mProducts[26] = "Iced Horlicks Dino";

            //product-name  ===> Base Code
            mProductTable.put("Tea-O", "TO");
            mProductTable.put("Coffee-O", "KO");
            mProductTable.put("Tea", "T");
            mProductTable.put("Coffee", "K");
            mProductTable.put("Tea-C", "TC");
            mProductTable.put("Coffee-C", "KC");
            mProductTable.put("Yuan Yang", "YY");
            mProductTable.put("Gula Melaka Tea", "GMT");
            mProductTable.put("Gula Melaka Coffee", "GMK");
            mProductTable.put("Almond Tea", "AT");
            mProductTable.put("Almond Coffee", "AK");
            mProductTable.put("Almond Milk", "AM");
            mProductTable.put("Ginger Tea", "GT");
            mProductTable.put("Honey Tea-O", "HTO");
            mProductTable.put("Honey Milk Tea", "HTC");
            mProductTable.put("Honey-O", "HO");
            mProductTable.put("Lemon Tea", "LeT");
            mProductTable.put("Honey Lemon Tea", "HLeT");
            mProductTable.put("Honey Lemon", "HLe");
            mProductTable.put("Lime Tea", "LiT");
            mProductTable.put("Honey Lime Tea", "HLiT");
            mProductTable.put("Honey Lime", "HLi");
            mProductTable.put("Iced Salted Lemon", "SL");
            mProductTable.put("Milo", "M");
            mProductTable.put("Horlicks", "HOR");
            mProductTable.put("Iced Milo Dino", "MD");
            mProductTable.put("Iced Horlicks Dino", "HORD");

            //product-name  ===> Hot Cold
            mProductTable.put("Hot", "hot");
            mProductTable.put("Cold", "iced");

            //Sweetness Level
            mProductTable.put("No Sweetener", "O");
            mProductTable.put("Less Less Sweet", "SST");
            mProductTable.put("Less Sweet", "ST");
            mProductTable.put("Normal", "");
            mProductTable.put("More Sweet", "+T");
            mProductTable.put("More More Sweet", "++T");

            //Beverage Thickness
            mProductTable.put("Thinner", "P");
            mProductTable.put("Thicker", "K");
            mProductTable.put("Extra Thick", "��");

            //Milk Content
            mProductTable.put("Less Milk", "-C");
            mProductTable.put("More Milk", "+C");
            mProductTable.put("No Milk", "xC");

            //Ice Content
            mProductTable.put("Less Less Ice", "--i");
            mProductTable.put("Less Ice", "-i");
            mProductTable.put("More Ice", "+i");
            mProductTable.put("No Ice", "Noice");

            //Other Additions
            mProductTable.put("Add Honey", "+H");
            mProductTable.put("Add Gula Melaka", "+GM");
            mProductTable.put("Add Almond", "+A");
            mProductTable.put("Add Lemon", "+Le");
            mProductTable.put("Add Lime", "+Li");
            mProductTable.put("Add Ginger", "+G");
            mProductTable.put("Add Horlicks", "+Hor");
            mProductTable.put("Add Milo", "+M");

            mEstimateTime[0] = "5";
            mEstimateTime[1] = "15";
            mEstimateTime[2] = "30";
            mEstimateTime[3] = "40";
            mInited = true;
        }

    }

    public List<Order> getOrders() {
        return mOrderList;
    }

    public int getIndexbyId(String id) {
        if (id==null||id.equals("")) {
            return -1;
        }
        return mOrderList.indexOf(new Order(id));
    }

    /*
    public Order getOrderbyId(String id) {
        int index = mOrderList.indexOf(new Order(id));
        if (index !=-1) {
            return mOrderList.get(index);
        }
        return null;
    }
    */

    public int getPendingNum() {
        return mPendingOrderList.size();
    }

    public int getOldestPendingIndex() {
        int index = 0;
        int ret = -1;
        long oldest = 0;
        for (Order order:mOrderList) {
            long waitingtime = order.getWaitingTime();
            if (waitingtime > oldest) {
                oldest = waitingtime;
                ret = index;
            }
            index++;
        }
        return ret;
    }

    public int getPendingTotalProducts(){
        int total=0;
        for(Order order:mPendingOrderList) {
            total += order.getTotalProducts();
        }
        return total;
    }

    private String parseName(JSONObject product) {
        try {
            StringBuilder name = new StringBuilder();
            if (product.has(Order.PRODUCT_OPTION_HOT_ICE)) {
                String hotcold = product.getString(Order.PRODUCT_OPTION_HOT_ICE);
                if (hotcold.equalsIgnoreCase(mProductTable.get("Hot"))) {
                    name.append("<font color='#ff0000'>");
                    name.append(product.getString(Order.PRODUCT_OPTION_HOT_ICE));
                }else{
                    name.append("<font color='#0000ff'>");
                    name.append(product.getString(Order.PRODUCT_OPTION_HOT_ICE));
                }
                name.append("</font>");
            }

//                    String productName = product.getString("code");
                    String shortcode =  product.getString("shortcode");

                    name.append(" ");
                    name.append(shortcode);

            if (product.has(Order.PRODUCT_OPTION_THICKNESS)) {
                name.append(product.getString(Order.PRODUCT_OPTION_THICKNESS));
            }
            if (product.has(Order.PRODUCT_OPTION_MILK)) {
                name.append(" ");
                name.append(product.getString(Order.PRODUCT_OPTION_MILK));
            }
            if (product.has(Order.PRODUCT_OPTION_SWEETNESS)) {
                name.append(" ");
                name.append(product.getString(Order.PRODUCT_OPTION_SWEETNESS));
            }
            if (product.has(Order.PRODUCT_OPTION_ICE)) {
                name.append(" ");
                name.append(product.getString(Order.PRODUCT_OPTION_ICE));
            }
            if (product.has(Order.PRODUCT_OPTION_OTHER)) {
                name.append(" ");
                name.append(product.getString(Order.PRODUCT_OPTION_OTHER));
            }
            return name.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean parseReceipts(JSONObject receipt) {
        try {
            String orderid = null;
            if (receipt.has("order_id")) {
                orderid = receipt.getString("order_id");
                //check if order already in list
                if (mPendingOrderList.contains(new Order(orderid))) {
                    return false;
                }
            }
            Timestamp createtime = null;
            if (receipt.has("create_time")) {
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(receipt.getString("create_time"));
                createtime = new Timestamp(date.getTime());
            }
            int queuenumber = -1;
            if (receipt.has("queue_number")) {
                queuenumber = Integer.parseInt(receipt.getString("queue_number"));
            }
            List<Map<String, String>> productlist = new ArrayList<Map<String, String>>();
            if (receipt.has("receipt_data")) {
                JSONObject receiptData = receipt.getJSONObject("receipt_data");
                if (receiptData.has("product")) {
                    JSONArray productarray = receiptData.getJSONArray("product");
                    for (int i = 0; i < productarray.length(); i++) {
                        JSONObject product = (JSONObject) productarray.get(i);
                        Map<String, String> item = new HashMap<String, String>();
                        item.put("name", parseName(product));
                        if (product.has("count")) {
                            item.put(Order.PRODUCT_QTY, product.getString("count"));
                        }
                        productlist.add(item);
                    }
                }
            }
            Order order = new Order(orderid, createtime, new Timestamp(0), queuenumber, productlist, -1);
            mOrderList.add(order);
            mPendingOrderList.add(order);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void getNewOrder(){
        /*
         * We will only get new orders when we are logged in.
         * So, if we are not logged in, we will exit this function
         */
        String userid = SharedPrefHelper.getString(mCtx, Constants.REGISTRATION_ID);
        if (userid == null || userid.equals("")) {
            return;
        }

        GetNewOrdersRequest req = new GetNewOrdersRequest(mCtx, new JsonObjectRequestCallback() {
            @Override
            public void onRequestSuccess(JSONObject value) {
                try {
                    JSONArray receiptArray = value.getJSONArray("receipts");
                    boolean newOrder = false;
                    for (int i = 0; i < receiptArray.length(); i++) {
                        JSONObject receipt = (JSONObject) receiptArray.get(i);
                        if (parseReceipts(receipt)) {
                            newOrder = true;
                        }
                    }
                    if (newOrder) {
                        Intent intent = new Intent(Constants.ACTION_ORDER_COMING);
                        mCtx.sendBroadcast(intent);
                    }
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(String errorMessage) {

            }
        });
        Volley.getInstance().addToRequestQueue(req);
    }

    private boolean isWorkingTIme(){
        String opening_hour=SharedPrefHelper.getStringDefault(mCtx,Constants.NAME_OPENING_HOUR, Constants.DEFAULT_OPENING_HOUR);
        String closing_hour=SharedPrefHelper.getStringDefault(mCtx,Constants.NAME_CLOSING_HOUR, Constants.DEFAULT_CLOSING_HOUR);
        Date dnow = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date d;
        long startL = 0,endL = 0,nowL=0;
        try{
            d = sdf.parse(opening_hour);
            startL = d.getTime();
            d = sdf.parse(closing_hour);
            endL = d.getTime();
            d = sdf.parse(sdf.format(dnow));
            nowL = d.getTime();
        }catch(ParseException e){
            e.printStackTrace();
        }
        if(startL<=nowL && nowL<=endL){
            return true;
        }else{
            return false;
        }
    }

    private class QueryTask extends TimerTask {
        @Override
        public void run() {
            String url = SharedPrefHelper.getString(mCtx, Constants.NAME_SERVER_URL);

            if (isWorkingTIme()) {
                getNewOrder();
            }else{
                if (url.equals(ServerConfigs.DEVURL_MYPOINT)) {
                    getNewOrder();
                }
                else {
                    Logger.d(TAG,"off work now");
                }
            }
        }
    }

    public void startQuery() {
        if (mTimerTask == null) {
            mTimerTask = new QueryTask();
        }
        mTimer.schedule(mTimerTask,QUERY_DURATION,QUERY_PERIOD);
    }

    public void stopQuery() {
        if (mTimerTask!=null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public String getEstimateString(int index) {
        if (index <0 || index > (mEstimateTime.length-1)) {
            return null;
        }
        return mEstimateTime[index];
    }

    public void orderConfirmed(Order order){
        mPendingOrderList.remove(order);
    }
}
