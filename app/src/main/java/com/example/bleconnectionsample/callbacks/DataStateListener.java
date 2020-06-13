
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.callbacks;

import android.bluetooth.BluetoothDevice;

import no.nordicsemi.android.ble.data.Data;

public interface DataStateListener {
    void onDataSend(BluetoothDevice device, Data data);

    void onDataRec(BluetoothDevice device, Data data);
}
