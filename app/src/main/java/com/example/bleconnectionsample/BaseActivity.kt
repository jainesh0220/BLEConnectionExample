/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */


package com.example.bleconnectionsample

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bleconnectionsample.adapter.DiscoveredBluetoothDevice
import com.example.bleconnectionsample.other.Logging
import com.example.bleconnectionsample.viewModel.BleViewModel
import kotlinx.android.synthetic.main.base_activity.*
import no.nordicsemi.android.ble.data.Data
import java.util.*

class BaseActivity : AppCompatActivity() {
    private val TAG = "BaseActivity";
    private lateinit var mContext: Context
    private val EXTRA_DEVICE = "com.example.bleconnectionsample"
    private var mViewModel: BleViewModel? = null
    private var discoveredBluetoothDevice: DiscoveredBluetoothDevice? = null

    @SuppressLint("SourceLockedOrientationActivity")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.base_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mContext = this
        if (intent.extras?.get(EXTRA_DEVICE) != null) {
            discoveredBluetoothDevice =
                (intent.extras?.get(EXTRA_DEVICE) as DiscoveredBluetoothDevice?)!!
        }
        mViewModel = ViewModelProvider(this).get(BleViewModel::class.java)
        mViewModel!!.connect(discoveredBluetoothDevice)
        initViewModels()
    }

    override fun onResume() {
        super.onResume()
        mViewModel = ViewModelProvider(this).get(BleViewModel::class.java)
        mViewModel!!.connect(discoveredBluetoothDevice)
    }

    private fun initViewModels() {
        mViewModel?.isConnected?.observe(this, Observer { connected ->
            Log.e(TAG, "Connected")
            if (connected) {
                textStatus.text = "Connected"
                textAddress.text =
                    discoveredBluetoothDevice?.name + discoveredBluetoothDevice?.address
            }
        })

        mViewModel?.isDeviceReady?.observe(
            this,
            Observer { Logging.getInstance().e(TAG, "initViewModels: Started") })
        mViewModel?.isSupported?.observe(this, Observer { supported ->
            if (!supported)
                Logging.getInstance().e(TAG, "initViewModels: Started")
        })
        mViewModel?.mOnDataReceved?.observe(this, Observer<Data> { data ->
            Toast.makeText(mContext, "Data" + Arrays.toString(data.value), Toast.LENGTH_LONG)
                .show();
        })
        mViewModel?.isDisconnected?.observe(this, Observer {
            Logging.getInstance().e(TAG, "initViewModels: Disconnected")
        }
        )
    }
}