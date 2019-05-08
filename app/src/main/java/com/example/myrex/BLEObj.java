package com.example.myrex;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEObj
{
    private BluetoothAdapter _adapter;
    private BluetoothLeScanner _leScanner;

    private static final String TAG = "rex_test";

    private boolean mScanning;
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    private ScanCallback _leScanCallback;

    private List<BluetoothDevice> _listBD = new ArrayList<>();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public List<BluetoothDevice> _listSelectedBD = new ArrayList<BluetoothDevice>();



    private static BLEObj _obj = new BLEObj();

    private BLEObj()
    {
    }

    public static BLEObj getBLEObj()
    {
        return  _obj;
    }

    public BluetoothDevice getBD(int index)
    {
        if (index>=_listSelectedBD.size()) return null;

        BluetoothDevice bd = _listSelectedBD.get(index);
        return  bd;
    }

    public void isOKtoRun(Context context) throws Exception
    {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            throw new Exception("BLE NOT supported");
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        _adapter = bluetoothManager.getAdapter();
        if (_adapter ==null) throw  new Exception("Bluetooth Adapter not found");
        if (!_adapter.isEnabled()) throw new Exception("Bluetooth Adapter not enabled");

        if (ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            //throw new Exception("can NOT ACCESS_FINE_LOCATION");
        }

        if (ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            throw new Exception("can NOT ACCESS_COARSE_LOCATION");
        }
    }

    public void scanLeDevice(boolean enable, ScanCallback leScanCallback, final Button btn_scan) {

        Log.i(TAG, "scanLeDevice, enable="+enable);

        _leScanCallback = leScanCallback;

        if (_leScanner ==null)
        {
            _leScanner = _adapter.getBluetoothLeScanner();
        }

        if (enable) {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    try
                    {
                        _leScanner.stopScan(_leScanCallback);
                        Log.i(TAG,"stopScan, timeout "+SCAN_PERIOD);
                        btn_scan.setEnabled(true);
                    }
                    catch (Exception ex)
                    {
                        Log.d(TAG,ex.getMessage(),ex);
                    }

                }
            }, SCAN_PERIOD);

            btn_scan.setEnabled(false);
            mScanning = true;
            _leScanner.startScan(_leScanCallback);

            Log.i(TAG,"startScan");
        } else {
            mScanning = false;
            _leScanner.stopScan(_leScanCallback);
        }
    }

    public void stopScan()
    {
        if (_leScanner !=null && _leScanCallback!=null)
        {
            _leScanner.stopScan(_leScanCallback);
        }

        _leScanCallback = null;
        mScanning = false;
    }
}
