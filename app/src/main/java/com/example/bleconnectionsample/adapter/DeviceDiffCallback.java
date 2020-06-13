
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.adapter;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class DeviceDiffCallback extends DiffUtil.Callback {
    private final List<DiscoveredBluetoothDevice> oldList;
    private final List<DiscoveredBluetoothDevice> newList;

    public DeviceDiffCallback(final List<DiscoveredBluetoothDevice> oldList,
                              final List<DiscoveredBluetoothDevice> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition);
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        final DiscoveredBluetoothDevice device = oldList.get(oldItemPosition);
        return device.hasRssiLevelChanged();
    }
}
