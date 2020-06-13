
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.bleconnectionsample.adapter.DevicesAdapter;
import com.example.bleconnectionsample.adapter.DiscoveredBluetoothDevice;
import com.example.bleconnectionsample.other.Logging;
import com.example.bleconnectionsample.utils.Utils;
import com.example.bleconnectionsample.viewModel.ScannerStateLiveData;
import com.example.bleconnectionsample.viewModel.ScannerViewModel;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements LifecycleOwner, DevicesAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";

    private String EXTRA_DEVICE = "com.example.bleconnectionsample";
    private RecyclerView recyclerViewBleDevices;
    private Button actionGrantLocationPermission;
    private int REQUEST_ACCESS_COARSE_LOCATION = 1022; // random number
    private int REQUEST_ACCESS_FINE_LOCATION = 1023; // random number
    private ScannerViewModel mScannerViewModel = null;
    private View noLocationPermission, bluetoothOff, noDevices;
    private ContentLoadingProgressBar stateScanningProgressbar;
    private LinearLayout noLocationLayout;
    private Button actionPermissionSettings;
    /*private BleViewModel mViewModel;
    private ProgressDialog progressDialogConnection;
    private CountDownTimer countDownTimerConnection;*/

//    @Override
//     Lifecycle getLifecycle() {
////        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        return null;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerViewBleDevices = findViewById(R.id.recycler_view_ble_devices);
        Button btnEnableLocation = findViewById(R.id.action_enable_location);
        Button btnEnableBt = findViewById(R.id.action_enable_bluetooth);
        actionGrantLocationPermission = findViewById(R.id.action_grant_location_permission);
        actionPermissionSettings = findViewById(R.id.action_permission_settings);
        noLocationPermission = findViewById(R.id.no_location_permission);
        bluetoothOff = findViewById(R.id.bluetooth_off);
        noDevices = findViewById(R.id.no_devices);
        stateScanningProgressbar = findViewById(R.id.state_scanning);
        noLocationLayout = findViewById(R.id.no_location);

        this.setSupportActionBar(toolbar);

//        Create view model containing utility methods for scanning

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        Logging.getInstance().d(TAG, String.valueOf(bluetoothAdapter.getBondedDevices()));

        mScannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel.class);

        mScannerViewModel.getScannerState().observe(this, this::startScan);

        // Configure the recycler view

        recyclerViewBleDevices.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBleDevices.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerViewBleDevices.getItemAnimator())).setSupportsChangeAnimations(false);
        DevicesAdapter adapter = new DevicesAdapter(MainActivity.this, mScannerViewModel.getDevices());
        adapter.setOnItemClickListener(this);
        recyclerViewBleDevices.setAdapter(adapter);
        btnEnableLocation.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });


        btnEnableBt.setOnClickListener(v -> {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        });
        actionGrantLocationPermission.setOnClickListener(v -> {
            Utils.markLocationPermissionRequested(MainActivity.this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION
                );
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ACCESS_COARSE_LOCATION
                );
            }
        });

        actionPermissionSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        clear();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION || requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            mScannerViewModel.refresh();
        }
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     */
    private void startScan(ScannerStateLiveData state) {
        // First, check the Location permission. This is required on Marshmallow onwards in order
        // to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(this)) {
            noLocationPermission.setVisibility(View.GONE);

            // Bluetooth must be enabled
            if (state.isBluetoothEnabled()) {
                recyclerViewBleDevices.setVisibility(View.VISIBLE);
                bluetoothOff.setVisibility(View.GONE);
                Objects.requireNonNull(this.getSupportActionBar()).setTitle(getString(R.string.str_device_list));
                // We are now OK to start scanning
                mScannerViewModel.startScan();
                stateScanningProgressbar.setVisibility(View.VISIBLE);

                if (!state.hasRecords()) {
                    noDevices.setVisibility(View.VISIBLE);

                    if (!Utils.isLocationRequired(this) && Utils.isLocationEnabled(this)) {
                        noLocationLayout.setVisibility(View.INVISIBLE);
                    } else {
                        noLocationLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    noLocationLayout.setVisibility(View.GONE);
                    noDevices.setVisibility(View.GONE);
                }
            } else {
                Objects.requireNonNull(this.getSupportActionBar()).setTitle(getString(R.string.str_enable_bt));
                recyclerViewBleDevices.setVisibility(View.GONE);
                bluetoothOff.setVisibility(View.VISIBLE);
                stateScanningProgressbar.setVisibility(View.INVISIBLE);
                noDevices.setVisibility(View.GONE);
                //clear();
            }
        } else {
            Objects.requireNonNull(this.getSupportActionBar()).setTitle(getString(R.string.str_location_permission));
            noLocationPermission.setVisibility(View.VISIBLE);
            bluetoothOff.setVisibility(View.GONE);
            stateScanningProgressbar.setVisibility(View.INVISIBLE);
            noDevices.setVisibility(View.GONE);

            boolean deniedForever = Utils.isLocationPermissionDeniedForever(this);
            //actionGrantLocationPermission.setVisibility(deniedForever ? View.VISIBLE : View.GONE);
            //actionPermissionSettings.setVisibility(deniedForever ? View.GONE : View.VISIBLE );
            if (deniedForever) {
                actionPermissionSettings.setVisibility(View.VISIBLE);
                actionGrantLocationPermission.setVisibility(View.GONE);
            } else {
                actionGrantLocationPermission.setVisibility(View.VISIBLE);
                actionPermissionSettings.setVisibility(View.GONE);
            }
            //actionGrantLocationPermission.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    public void stopScan() {
        mScannerViewModel.stopScan();
    }

    /**
     * Clears the list of devices, which will notify the observer.
     */
    private void clear() {
        mScannerViewModel.getDevices().clear();
        mScannerViewModel.getScannerState().clearRecords();
    }

    @Override
    public void onItemClick(@NonNull DiscoveredBluetoothDevice device) {
        stopScan();
        startActivity(new Intent(this, BaseActivity.class).putExtra(EXTRA_DEVICE, device));
    }

}