package com.example.myrex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.bluetooth.BluetoothDevice;

import android.net.*;

import java.util.HashMap;
import java.util.List;

import java.io.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "rex_test";

    final int ACTIVITY_SCAN_BLE = 0;
    final int ACTIVITY_CHOOSE_FILE = 1;

    String [] _keys;
    String [] _items;

    HashMap<String,BLEConn> _hmBLEConn = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_scan = findViewById(R.id.button_scan);
        Button btn_select_file = findViewById(R.id.button_select_file);
        btn_scan.setOnClickListener(this);
        btn_select_file.setOnClickListener(this);

        ListView listview = (ListView) findViewById(R.id.ble_connection);
        listview.setOnItemClickListener(this);

        showItems();
    }

    @Override
    public void onClick(View v) {

        if (v.getId()==R.id.button_scan)
        {
            Intent intent = new Intent(this, BLE_Scan.class);
            startActivityForResult(intent, ACTIVITY_SCAN_BLE);
        }
        else if (v.getId()==R.id.button_select_file)
        {
            selectFile();
        }
        else
        {
            Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectFile()
    {
        try
        {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");

            Intent intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        }
        catch (Exception ex)
        {
            Log.e(TAG,ex.getMessage(),ex);
        }
    }

    private void writeTest()
    {
        try
        {
            java.util.Iterator<BLEConn> it = _hmBLEConn.values().iterator();
            while(it.hasNext())
            {
                BLEConn blec = it.next();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HH:mm:ss");
                String data = sdf.format(java.util.Calendar.getInstance().getTime());
                blec.writeCmd("Hi="+data);
                Log.i(TAG,"Hi+"+data);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG,ex.getMessage(),ex);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SCAN_BLE)
        {
            if (resultCode == RESULT_OK)
            {
                showItems();
            }
        }
        else if (requestCode == ACTIVITY_CHOOSE_FILE)
        {

            showSelectFile(data);
        }
    }

    void showSelectFile(Intent data)
    {
        try
        {
            Uri uri = data.getData();

            String filePath = uri.getPath();
            TextView tv_fileinfo = findViewById(R.id.textView_fileinfo);

            tv_fileinfo.setText(filePath);

            InputStream iStream =   getContentResolver().openInputStream(uri);
            byte[] inputData = getBytes(iStream);
            tv_fileinfo.setText(filePath +" :　"+inputData.length+" bytes");

//            File file = new File(filePath);
//            int size = (int) file.length();
//            byte[] bytes = new byte[size];
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(file);
//                BufferedInputStream buf = new BufferedInputStream(fis);
//                buf.read(bytes, 0, bytes.length);
//                buf.close();
//                tv_fileinfo.setText(filePath +" :　"+size+" bytes");
//            } catch (Exception ex) {
//                Log.e(TAG,ex.getMessage(),ex);
//            }
//            finally {
//                if (fis!=null) fis.close();
//            }
        }
        catch (Exception ex)
        {
            Log.e(TAG,ex.getMessage(),ex);
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    void showItems()
    {
        List<BluetoothDevice> list = BLEObj.getBLEObj()._listSelectedBD;
        _keys = new String[list.size()];
        _items = new String[list.size()];
        for(int i=0;i<list.size();i++)
        {
            BluetoothDevice bd = list.get(i);
            String key = bd.getName()+" ("+bd.getAddress()+")";
            _keys[i] = key;
            _items[i] = key;
            if (_hmBLEConn.containsKey(key))
            {
                _items[i]  = key +" connected";
            }
        }

        ListView listview = (ListView) findViewById(R.id.ble_connection);

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                _items);
        listview.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(TAG,parent.getAdapter().getClass().getName());

        Log.d(TAG,"onItemClick, position="+position+", id="+id);

        try
        {
            String key = _keys[position];

            if (_hmBLEConn.containsKey(key))
            {
                BLEConn blec = _hmBLEConn.get(key);
                blec.disconnect();
                _hmBLEConn.remove(key);
                Toast.makeText(this,key +" disconnected", Toast.LENGTH_SHORT).show();
            }
            else
            {
                BluetoothDevice bd = BLEObj.getBLEObj().getBD(position);
                BLEConn blec = new BLEConn(this, bd);
                _hmBLEConn.put(key, blec);

                Toast.makeText(this,key +" connected", Toast.LENGTH_SHORT).show();
            }

            showItems();
        }
        catch (Exception ex)
        {
            Log.e(TAG,ex.getMessage(), ex);
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}
