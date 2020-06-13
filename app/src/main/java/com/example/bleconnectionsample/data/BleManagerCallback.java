
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.data;

import android.bluetooth.BluetoothDevice;

import com.example.bleconnectionsample.callbacks.DataStateListener;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.data.Data;

public interface BleManagerCallback extends BleManagerCallbacks, DataStateListener {
    void onDataRec(BluetoothDevice device, Data data);

    void onDataSend(BluetoothDevice device, Data data);
}
