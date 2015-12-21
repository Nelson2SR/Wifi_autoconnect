package com.appliedmesh.merchantapp.view;

import android.content.*;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.appliedmesh.merchantapp.utils.FileUtil;
import com.appliedmesh.merchantapp.utils.KeyboardUtil;
import com.appliedmesh.merchantapp.R;
import com.appliedmesh.merchantapp.module.Constants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by dongbin on 2015/6/11.
 */
public class CollectionActivity extends AppCompatActivity implements View.OnTouchListener{
    private LinkedList<Map<String, String>> mData;
    private QueueAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final EditText inputQueue = (EditText)findViewById(R.id.et_queue_num);
        inputQueue.setInputType(InputType.TYPE_NULL);
        inputQueue.setOnTouchListener(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        getData();
        mAdapter = new QueueAdapter(this,listView);
        listView.setAdapter(mAdapter);

    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filterInputQueue = new IntentFilter(Constants.ACTION_INPUT_QUEUE);
        registerReceiver(mMainReceiver, filterInputQueue);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mMainReceiver);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final EditText inputQueue = (EditText)findViewById(R.id.et_queue_num);
        new KeyboardUtil(this, this, inputQueue).showKeyboard();
        return false;
    }

    private void getData(){
        if (mData == null) {
            mData = new LinkedList<Map<String, String>>();
        }
    }

    private void addNewData(String queue){
        Map<String, String> map = new HashMap<String, String>();
        map.put("queue_num", queue);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        map.put("queue_time", sdf.format(System.currentTimeMillis()));

        Iterator<Map<String,String>> iter = mData.iterator();
        while(iter.hasNext()) {
            Map<String,String> q = iter.next();
            if (q.get("queue_num").equals(queue)) {
                iter.remove();
                break;
            }
        }
        mData.addFirst(map);
        mAdapter.notifyDataSetChanged();
    }

    private class QueueAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private final WeakReference<ListView> mListView;

        public QueueAdapter(Context context, ListView listView){
            this.mInflater = LayoutInflater.from(context);
            this.mListView = new WeakReference<ListView>(listView);
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
            Map<String,String> map = mData.get(i);
            TextView tv_queue;
            TextView tv_time;
            if (view != null) {
                tv_queue = (TextView)view.findViewById(R.id.tv_queue);
                tv_time = (TextView)view.findViewById(R.id.tv_time);
            }else{
                view = mInflater.inflate(R.layout.collection_queue_item, null);
                tv_queue = (TextView)view.findViewById(R.id.tv_queue);
                tv_time = (TextView)view.findViewById(R.id.tv_time);
            }
            tv_queue.setText(map.get("queue_num"));
            tv_time.setText(map.get("queue_time"));
            if ( i==0 ) {
                tv_queue.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_firstitem_size));
                tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_firstitem_size));
            }else{
                tv_queue.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_item_size));
                tv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.collection_list_item_size));
            }
            if (i%2 == 1) {
                view.setBackgroundColor(getResources().getColor(R.color.order_item_bg));
            }else{
                view.setBackgroundColor(getResources().getColor(R.color.white));
            }
            return view;
        }
    }

    private BroadcastReceiver mMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_INPUT_QUEUE)) {
                final EditText et_queue = (EditText)findViewById(R.id.et_queue_num);
                String confirm_msg = String.format(getString(R.string.collection_confirm_send_to_display), et_queue.getText().toString());
                new AlertDialog.Builder(context).setTitle(getResources().getString(R.string.collection_send_to_display))
                        .setMessage(confirm_msg)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addNewData(et_queue.getText().toString());
                                et_queue.setText("");//clear text
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        }).show();
            }
        }
    };

}