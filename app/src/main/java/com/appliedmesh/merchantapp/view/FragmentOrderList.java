package com.appliedmesh.merchantapp.view;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appliedmesh.merchantapp.module.Order;
import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.module.OrderManager;

import java.util.*;

/**
 * Created by Home on 2015/6/4.
 */
public class FragmentOrderList extends Fragment implements AdapterView.OnItemClickListener{
    OrderAdapter mAdapter;
    TextView mTvNoItem;
    List<Map<String,Object>> mData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.orderpage, container,false);
        ListView listView = (ListView) v.findViewById(R.id.orderList);
        mTvNoItem = (TextView)v.findViewById(R.id.tv_noitem);
        mData = getData();
        if (mData.size()>0) {
            mTvNoItem.setVisibility(View.GONE);
        }

        mAdapter = new OrderAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        return v;
    }

    private List<Map<String,Object>> getData(){
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        List<Order> orderList = OrderManager.getInstance().getOrders();
        Iterator<Order> iter = orderList.iterator();
        String order_id = getResources().getString(R.string.order_id);
        String queue_num = getResources().getString(R.string.order_queue);
        String queue_qty = getResources().getString(R.string.order_qty);
        while (iter.hasNext()) {
            Order order = iter.next();
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("order_id", String.format(order_id, order.getId()));
            map.put("order_queue", String.format(queue_num, order.getQueueNum()));
            map.put("order_qty", String.format(queue_qty, order.getTotalProducts()));
            map.put("order_pending",R.drawable.pending);
            map.put("order",order);
            list.add(map);
        }

        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(getActivity().getLocalClassName(),"test");
        Intent intent = new Intent(Constants.ACTION_ORDER_DETAIL);
        intent.putExtra("orderindex", i);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        //OrderManager.getInstance().getNewOrder();
        mData = getData();
        if (mData.size()>0) {
            mTvNoItem.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    private class OrderAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        public OrderAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Map<String,Object> map = mData.get(i);
            ImageView img;
            TextView tv_orderid;
            TextView tv_queuenum;
            TextView tv_qty;
            if (view != null) {
                img = (ImageView)view.findViewById(R.id.iv_order_pending);
                tv_orderid = (TextView)view.findViewById(R.id.order_id);
                tv_queuenum = (TextView)view.findViewById(R.id.order_queue);
                tv_qty = (TextView)view.findViewById(R.id.tv_qty);
            }else{
                view = mInflater.inflate(R.layout.orderlistitem, null);
                tv_orderid = (TextView)view.findViewById(R.id.order_id);
                tv_queuenum = (TextView)view.findViewById(R.id.order_queue);
                tv_qty = (TextView)view.findViewById(R.id.tv_qty);
                img = (ImageView)view.findViewById(R.id.iv_order_pending);
                img.setImageResource((Integer) map.get("order_pending"));
            }
            Order order = (Order)map.get("order");
            tv_orderid.setText(map.get("order_id").toString());
            tv_queuenum.setText(map.get("order_queue").toString());
            tv_qty.setText(map.get("order_qty").toString());
            if (order.getEstimateTime() < 0) {
                img.setVisibility(View.VISIBLE);
            }else{
                img.setVisibility(View.INVISIBLE);
            }
            return view;
        }
    }
}
