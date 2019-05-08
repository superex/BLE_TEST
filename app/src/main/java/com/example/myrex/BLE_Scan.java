package com.example.myrex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.*;

import android.content.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class BLE_Scan extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    String[] _items = {};
    List<String> _listTmp = new ArrayList();
    HashMap<String,BluetoothDevice> _hmBtDevice = new HashMap();
    HashMap<String,Integer> _hmBtRssi = new HashMap();

    private static final String TAG = "rex_test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listview = (ListView) findViewById(R.id.ble_items);
        listview.setOnItemClickListener(this);

        Button btn_scan = findViewById(R.id.button_scan);
        Button btn_ok = findViewById(R.id.button_ok);
        btn_scan.setOnClickListener(this);
        btn_ok.setOnClickListener(this);

        showItems();

        boolean b = checkIsOK2Run();
        btn_scan.setEnabled(b);
        btn_ok.setEnabled(b);

        if (b)
        {
            BLEObj.getBLEObj().scanLeDevice(true, mLeScanCallback, btn_scan);
        }
    }

    private boolean checkIsOK2Run()
    {
        try
        {
            BLEObj.getBLEObj().isOKtoRun(this);
            return  true;
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage(),ex);
            Toast.makeText(this,ex.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v instanceof Button)
        {
            Button btn = (Button)v;
            if (btn.getId()==R.id.button_scan)
            {
                _listTmp.clear();
                showItems();

                btn.setEnabled(false);

                Button btn_scan = findViewById(R.id.button_scan);
                BLEObj.getBLEObj().scanLeDevice(true, mLeScanCallback, btn_scan);
            }
            else if (btn.getId()==R.id.button_ok)
            {
                doOK();
            }
        }
    }

    private void doOK()
    {
        ListView listview = (ListView) findViewById(R.id.ble_items);
        int cntChoice = listview.getCount();

        if (cntChoice==0)
        {
            Toast.makeText(this,"No BLE device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        SparseBooleanArray sparseBooleanArray = listview.getCheckedItemPositions();

        List<String> listSelectName = new ArrayList();
        for(int i = 0; i < cntChoice; i++)
        {
            if(sparseBooleanArray.get(i))
            {
                //listSelectName.add(listview.getItemAtPosition(i).toString());
                listSelectName.add(_listTmp.get(i));
            }
        }

        if (listSelectName.size()==0)
        {
            Toast.makeText(this,"NO BLE device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG,"listSelectName="+listSelectName);

        BluetoothDevice dev1 = null;
        BluetoothDevice dev2 = null;

        if (listSelectName.size()>0) dev1 = _hmBtDevice.get(listSelectName.get(0));
        if (listSelectName.size()>1) dev2 = _hmBtDevice.get(listSelectName.get(1));

        //Toast.makeText(this,listSelectName.toString(), Toast.LENGTH_LONG).show();

        BLEObj.getBLEObj()._listSelectedBD.clear();
        if (dev1!=null) BLEObj.getBLEObj()._listSelectedBD.add(dev1);
        if (dev2!=null) BLEObj.getBLEObj()._listSelectedBD.add(dev2);

        Intent data = new Intent();
        //data.putExtra("device1_name",dev1.getName());
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    protected void onStop() {

        super.onStop();

        try
        {
            BLEObj.getBLEObj().stopScan();
        }
        catch (Exception ex)
        {
            Log.d(TAG,ex.getMessage(),ex);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(TAG,parent.getAdapter().getClass().getName());

        Log.d(TAG,"onItemClick, position="+position+", id="+id);

        //Toast.makeText(this,"點選第 "+(position +1) +" 個 \n內容："+_items[position], Toast.LENGTH_SHORT).show();
    }

//    void showItems2()
//    {
//        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
//        for(String key :_listTmp)
//        {
//            HashMap<String,String> hm = new HashMap<String,String>();
//            hm.put("name", _hmBtDevice.get(key).getName());
//            hm.put("bd_addr", _hmBtDevice.get(key).getAddress());
//            hm.put("rssi", "-13");
//            list.add(hm);
//        }
//
//        if (_listTmp.size()==0)
//        {
//            HashMap<String,String> hm = new HashMap<String,String>();
//            hm.put("name", "AAA");
//            hm.put("bd_addr", "12:19:33");
//            hm.put("rssi", "-33");
//            list.add(hm);
//
//            hm = new HashMap<String,String>();
//            hm.put("name", "BB");
//            hm.put("bd_addr", "12:19:33:44");
//            hm.put("rssi", "-53");
//            list.add(hm);
//
//            hm = new HashMap<String,String>();
//            hm.put("name", "CC");
//            hm.put("bd_addr", "12:19:33");
//            hm.put("rssi", "-45");
//            list.add(hm);
//        }
//
//        SimpleAdapter adapter = new SimpleAdapter(
//                this,
//                list,
//                R.layout.list_item_ble_scan_result,
//                new String[] {"name","bd_addr","rssi", "checked"},
//                new int[] {R.id.textView_name,R.id.textView_bd_addr, R.id.textView_rssi, R.id.checkedTextView_ble_checked}
//        );
//
//
//        ListView listview = (ListView) findViewById(R.id.ble_items);
//        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        listview.setAdapter(adapter);
//    }

    void showItems()
    {
        _items = new String[_listTmp.size()];
        for(int i=0;i<_items.length;i++)
        {
            String key = _listTmp.get(i);
            _items[i] = key+", RSSI:"+_hmBtRssi.get(key);
        }

        ListView listview = (ListView) findViewById(R.id.ble_items);

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_multiple_choice,
                _items);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG,"onActivityResult,requestCode="+requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        Log.d(TAG,"onRequestPermissionsResult, requestCode="+requestCode);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG,"onScanResult="+result.getDevice().getName());

            BluetoothDevice bd = result.getDevice();
            String name = bd.getName();
            if (name==null) return;

            String key = name +" ("+bd.getAddress()+")";
            _hmBtRssi.put(key, result.getRssi());

            if (!_listTmp.contains(key))
            {
                _listTmp.add(key);
                _hmBtDevice.put(key, bd);
            }

            showItems();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG,"onBatchScanResults="+results.size());
            showText("onBatchScanResults="+results.size());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG,"onScanFailed="+errorCode);
            showText("onScanFailed="+errorCode);
        }
    };

    void showText(String txt)
    {
        Log.i(TAG,txt);
    }
}
