
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bleconnectionsample.MainActivity;
import com.example.bleconnectionsample.R;
import com.example.bleconnectionsample.viewModel.DevicesLiveData;

import java.util.List;

@SuppressWarnings("unused")
public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {
    private final Context mContext;
    private List<DiscoveredBluetoothDevice> mDevices;
    private OnItemClickListener mOnItemClickListener;

    @SuppressWarnings("ConstantConditions")
    public DevicesAdapter(@NonNull final MainActivity activity, @NonNull final DevicesLiveData devicesLiveData) {
        mContext = activity;
        setHasStableIds(true);
        devicesLiveData.observeForever(devices -> {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DeviceDiffCallback(mDevices, devices), false);
            mDevices = devices;
            result.dispatchUpdatesTo(DevicesAdapter.this);
        });
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext)
                .inflate(R.layout.device_item, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final DiscoveredBluetoothDevice device = mDevices.get(position);
        final String deviceName = device.getName();

        if (!TextUtils.isEmpty(deviceName))
            holder.deviceName.setText(deviceName);
        else
            holder.deviceName.setText(R.string.unknown_device);
        holder.deviceAddress.setText(device.getAddress());
        final int rssiPercent = (int) (100.0f * (127.0f + device.getRssi()) / (127.0f + 20.0f));
        holder.rssi.setImageLevel(rssiPercent);
    }

    @Override
    public long getItemId(final int position) {
        return mDevices.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mDevices != null ? mDevices.size() : 0;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(@NonNull final DiscoveredBluetoothDevice device);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView rssi;

        private ViewHolder(@NonNull final View view) {
            super(view);
            deviceName = view.findViewById(R.id.device_name);
            deviceAddress = view.findViewById(R.id.device_address);
            rssi = view.findViewById(R.id.rssi);
            view.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mDevices.get(getAdapterPosition()));
                }
            });
        }
    }
}
