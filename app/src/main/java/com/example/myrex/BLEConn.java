package com.example.myrex;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BLEConn
{
    private static final String TAG = "rex_test";

    private BluetoothGatt _bluetoothGatt;
    private BluetoothGattCharacteristic _writeto;
    private BluetoothGattCharacteristic _notifyback;

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private boolean _isConnectionOK = false;
    private Object _waitConnStateOK = new Object();

    public BLEConn(Context context, BluetoothDevice bd) throws Exception
    {
        _bluetoothGatt = bd.connectGatt(context, false , mGattCallback);

        if (_isConnectionOK) return;

        synchronized (_waitConnStateOK)
        {
            _waitConnStateOK.wait(5000);
        }

        if (!_isConnectionOK) throw new Exception("connection fail");
    }

    public void disconnect()
    {
        _bluetoothGatt.disconnect();
        _bluetoothGatt = null;
        Log.i(TAG,"_bluetoothGatt_disconnect");
    }

    public void writeCmd(String data) throws Exception
    {
        if (_writeto==null) throw new Exception("no Characteristics to write");

        _writeto.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        _writeto.setValue(data);
        _bluetoothGatt.writeCharacteristic(_writeto);
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange, from " + status + " to " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        _bluetoothGatt.discoverServices());

                _isConnectionOK = true;
                synchronized (_waitConnStateOK)
                {
                    _waitConnStateOK.notify();
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i(TAG, "Disconnected from GATT server.");

            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "ACTION_GATT_SERVICES_DISCOVERED");

                List<BluetoothGattService> listS = gatt.getServices();


                for (BluetoothGattService bgs : listS) {
                    List<BluetoothGattCharacteristic> listC = bgs.getCharacteristics();

                    for (BluetoothGattCharacteristic ch : listC) {
                        UUID uuid = ch.getUuid();

                        Log.d(TAG,"UUID="+uuid.toString());

                        if (uuid.toString().startsWith("00000511")) {
                            _writeto = ch;
                        } else if (uuid.toString().startsWith("00000512")) {
                            _notifyback = ch;
                        }

                        Log.i(TAG, "uuid.toString()=" + uuid.toString());
                    }
                }

                Log.i(TAG, "writeto=" + _writeto);
                Log.i(TAG, "notifyback=" + _notifyback);

                if (_notifyback != null) {
                    Log.i(TAG, "setCharacteristicNotification1");

                    _bluetoothGatt.setCharacteristicNotification(_notifyback, true);

                    BluetoothGattDescriptor descriptor = _notifyback.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    _bluetoothGatt.writeDescriptor(descriptor);

                    Log.i(TAG, "setCharacteristicNotification2");
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead");
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            Log.i(TAG, "onDescriptorWrite=" + descriptor.getUuid().toString() + ",status=" + status);

//            if (_writeto != null) {
//                _writeto.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//                _writeto.setValue("1219");
//                _bluetoothGatt.writeCharacteristic(_writeto);
//                Log.i(TAG, "writeCharacteristic_OK");
//            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            byte [] data = characteristic.getValue();

            Log.i(TAG,"onCharacteristicChanged");

            for (byte b : data)
            {
                //Log.i(TAG, "data="+b);
            }

            Log.i(TAG, "data="+characteristic.getStringValue(0));

        }
    };
}
