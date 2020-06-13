
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class BLEDataCallback implements ProfileDataCallback, DataSentCallback, DataReceivedCallback {
    private static final int STATE_RELEASED = 0x00;
    private static final int STATE_PRESSED = 0x01;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
     /*   if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }

        final int state = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (state == STATE_PRESSED) {
//            onButtonStateChanged(device, true);
        } else if (state == STATE_RELEASED) {
//            onButtonStateChanged(device, false);
        } else {
            onInvalidDataReceived(device, data);
        }
*/
    }

    @Override
    public void onDataSent(@NonNull BluetoothDevice bluetoothDevice, @NonNull Data data) {

    }

    @Override
    public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {

    }
}
