
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.data;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bleconnectionsample.callbacks.BLEDataCallback;
import com.example.bleconnectionsample.callbacks.DataStateListener;
import com.example.bleconnectionsample.other.Logging;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;


public class SecureBLEManager extends BleManager<BleManagerCallback> {
    private static final String TAG = "SecureBLEManager";
    final UUID UUID_SERVICE = UUID.fromString("0000ABF0-0000-1000-8000-00805F9B34FB");
    final UUID UUID_WRITE = UUID.fromString("0000ABF2-0000-1000-8000-00805F9B34FB");
    final UUID UUID_READ = UUID.fromString("0000ABF1-0000-1000-8000-00805F9B34FB");
    /**
     * Nordic Blinky Service UUID.
     */
//	public final static UUID LBS_UUID_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
//    public final static UUID LBS_UUID_SERVICE = UUID.fromString("56657772-418c-e3fd-b2a9-8154f87895a8");
    //	/** BUTTON characteristic UUID. */
//	private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
//    private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("58092b44-eb5f-4517-8118-5728f5ab6485");
//	/** LED characteristic UUID. */
//	private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
//	private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("f868f4ef-dd0a-7656-3753-e88ac6aa6f2d");

    private BluetoothGattCharacteristic mButtonCharacteristic;
    private BluetoothGattCharacteristic mButtonCharacteristicRead;
    private LogSession mLogSession;
    private final MtuCallback mMtuCallBack = new MtuCallback() {
        @Override
        public void onMtuChanged(@NonNull BluetoothDevice device, int mtu) {
            log(Log.ERROR, " MTU callback :" + mtu);
        }
    };
    private boolean mSupported;
    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mButtonCharacteristicRead).with(mBleCallback);
            readCharacteristic(mButtonCharacteristicRead).with(mBleCallback).enqueue();
            enableNotifications(mButtonCharacteristicRead).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            try {
                BluetoothGattService serviceList = gatt.getService(UUID_SERVICE);
                BluetoothGattCharacteristic characteristicWrite = serviceList.getCharacteristic(UUID_WRITE);
                BluetoothGattCharacteristic characteristicRead = serviceList.getCharacteristic(UUID_READ);
                if (characteristicRead != null) {
                    gatt.setCharacteristicNotification(characteristicRead, true);
                    requestMtu(245).with(mMtuCallBack).enqueue();
                    mButtonCharacteristicRead = characteristicRead;
                } else {
                    Logging.getInstance().e(TAG, "isRequiredServiceSupported: NULL");
                }
                mButtonCharacteristic = characteristicWrite;
            } catch (Exception e) {
                //TODO handler other devices to show dialog after some time
                Logging.getInstance().e(TAG, "isRequiredServiceSupported: ");
            }

            boolean writeRequest = false;
            mSupported = mButtonCharacteristic != null;
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            mButtonCharacteristic = null;
        }
    };
    private boolean mLedOn;
    private DataStateListener dataStateListener;
    private final BLEDataCallback mBleCallback = new BLEDataCallback() {


        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            // Data can only invalid if we read them. We assume the app always sends correct data.
            log(Log.WARN, "Invalid data received: " + data.getValue());
        }

        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
//            super.onDataReceived(device, data);
            log(Log.WARN, "data received: " + data.getValue());
            dataStateListener.onDataRec(device, data);
            //dataStateListener.onDataSendReceived(device, data);

        }

        @Override
        public void onDataSent(@NonNull BluetoothDevice bluetoothDevice, @NonNull Data data) {
//            super.onDataSent(bluetoothDevice, data);
            log(Log.WARN, "data sent: " + data);
            dataStateListener.onDataSend(bluetoothDevice, data);
            //dataStateListener.onDataSendReceived(bluetoothDevice, data);
        }
    };

    public SecureBLEManager(@NonNull final Context context, final BleManagerCallback bleManagerCallback) {
        super(context);
        dataStateListener = bleManagerCallback;
    }

    @NonNull
    @Override
    protected BleManager.BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * Sets the log session to be used for low level logging.
     *
     * @param session the session, or null, if nRF Logger is not installed.
     */
    public void setLogger(@Nullable final LogSession session) {
        this.mLogSession = session;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        Logger.log(mLogSession, LogContract.Log.Level.fromPriority(priority), message);
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !mSupported;
    }

    @NonNull

    public void createBond(BluetoothDevice device) {

        device.createBond();
    }

    /**
     * Sends a request to the device to turn the LED on or off.
     *
     * @param on true to turn the LED on, false to turn it off.
     */
    public void send(final byte[] on) {

        writeCharacteristic(mButtonCharacteristic, on)
                .with(mBleCallback).enqueue();
    }


}
