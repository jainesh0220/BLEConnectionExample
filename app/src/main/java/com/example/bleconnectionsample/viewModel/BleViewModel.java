
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.viewModel;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bleconnectionsample.R;
import com.example.bleconnectionsample.adapter.DiscoveredBluetoothDevice;
import com.example.bleconnectionsample.data.BleManagerCallback;
import com.example.bleconnectionsample.data.SecureBLEManager;
import com.example.bleconnectionsample.other.Logging;

import java.util.Arrays;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;


public class BleViewModel extends AndroidViewModel implements BleManagerCallback {
    private static final String TAG = "BleViewModel";

    //for data receiving
    public MutableLiveData mOnDataReceved;
    //connection state disconnected
    public MutableLiveData mOnDisConnected;
    //ble manager
    private SecureBLEManager secureBleManager;
    //bt device
    private BluetoothDevice mDevice = null;
    // Connection states Connecting, Connected, Disconnecting, Disconnected etc.
    private MutableLiveData mConnectionState;
    // Flag to determine if the device is connected
    private MutableLiveData mIsConnected;
    // Flag to determine if the device has required services
    private MutableLiveData mIsSupported;
    // Flag to determine if the device is ready
    private MutableLiveData mOnDeviceReady;


    public BleViewModel(@NonNull Application application) {
        super(application);

        mConnectionState = new MutableLiveData<String>();
        mIsConnected = new MutableLiveData<Boolean>();
        mIsSupported = new MutableLiveData<Boolean>();
        mOnDeviceReady = new MutableLiveData<Void>();
        mOnDataReceved = new MutableLiveData<Data>();
        mOnDisConnected = new MutableLiveData<Boolean>();
        secureBleManager = new SecureBLEManager(getApplication(), this);
        secureBleManager.setGattCallbacks(this);
    }

    /**
     * Connect to peripheral.
     */
    public void connect(DiscoveredBluetoothDevice device) {
        // Prevent from calling again when called again (screen orientation changed)
        if (mDevice == null) {
            mDevice = device.getDevice();
            LogSession logSession = Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
            secureBleManager.setLogger(logSession);
            reconnect();
        }
    }

    /**
     * Reconnects to previously connected device.
     * If this device was not supported, its services were cleared on disconnection, so
     * reconnection may help.
     */
    public void reconnect() {
        if (mDevice != null) {
            secureBleManager.connect(mDevice)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    /**
     * Disconnect from peripheral.
     */
    public void disconnect() {
        mDevice = null;
        secureBleManager.disconnect().enqueue();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if (secureBleManager.isConnected()) {
            disconnect();
        }
    }

    @Override
    public void onDataRec(BluetoothDevice device, Data data) {
        mOnDataReceved.setValue(data);
    }

    @Override
    public void onDataSend(BluetoothDevice device, Data data) {
        Logging.getInstance().e(TAG, "onDataSend: " + Arrays.toString(data.getValue()));
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        mConnectionState.postValue(getApplication().getString(R.string.state_connecting));
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        mIsConnected.postValue(true);
        mConnectionState.postValue(getApplication().getString(R.string.str_connectionState));

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        mIsConnected.postValue(false);
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        mIsConnected.postValue(false);
        mOnDisConnected.postValue(false);
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
        mIsConnected.postValue(false);
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
        mConnectionState.postValue(getApplication().getString(R.string.state_initializing));
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        mIsSupported.postValue(true);
        mConnectionState.postValue(null);
        mOnDeviceReady.postValue(null);

    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        if (mDevice == null) {
            mDevice = device;
            LogSession logSession = Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
            secureBleManager.setLogger(logSession);

        }
    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        if (mDevice == null) {
            mDevice = device;
            LogSession logSession = Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
            secureBleManager.setLogger(logSession);

        }
        reconnect();
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
        mConnectionState.postValue(false);
        mIsSupported.postValue(false);
    }

    public LiveData<Void> isDeviceReady() {
        return mOnDeviceReady;
    }

    public LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public LiveData<Boolean> isSupported() {
        return mIsSupported;
    }

    public LiveData<String> isReceved() {
        return mOnDataReceved;
    }

    public LiveData<Boolean> isDisconnected() {
        return mOnDisConnected;
    }

    public void sendData(byte[] bytes) {
        try {
            secureBleManager.send(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
