package com.example.taifa.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taifa on 4/11/16.
 */
public class BleActivity extends AppCompatActivity {

    private BluetoothLEController mBLEController;

    private List<String> mList;
    private BaseAdapter mFoundAdapter;

    private ListView lvDevices;
    private Button btnScan, btnDisconnect, btnReconnect, btnSend;
    private TextView tvConnState, tvContent;
    private EditText etSendContent;

    private static final String TAG = "LMBluetoothSdk";

    private BluetoothLEListener mBluetoothLEListener = new BluetoothLEListener() {
        @Override
        public void onReadData(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvContent.append("Read from " + mBLEController.getConnectedDevice().getName()
                            + ": " + parseData(characteristic) + "\n");
                }
            });
        }

        @Override
        public void onWriteData(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvContent.append("Me" + ": " + parseData(characteristic) + "\n");
                }
            });
        }

        @Override
        public void onDataChanged(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvContent.append("Changed from " + mBLEController.getConnectedDevice().getName()
                            + ": " + parseData(characteristic) + "\n");
                }
            });
        }

        @Override
        public void onActionStateChanged(int preState, int state) {
            Log.d(TAG, "onActionStateChanged: " + state);
        }

        @Override
        public void onActionDiscoveryStateChanged(String discoveryState) {
            if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Toast.makeText(BleActivity.this, "scanning!", Toast.LENGTH_SHORT).show();
            } else if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Toast.makeText(BleActivity.this, "scan finished!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActionScanModeChanged(int preScanMode, int scanMode) {
            Log.d(TAG, "onActionScanModeChanged:  " + scanMode);
        }

        @Override
        public void onBluetoothServiceStateChanged(final int state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvConnState.setText("Conn state: " + Utils.transConnStateAsString(state));
                }
            });
        }

        @Override
        public void onActionDeviceFound(final BluetoothDevice device, short rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mList.add(device.getName() + "@" + device.getAddress());
                    mFoundAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        getSupportActionBar().setTitle("BLE Sample");
        init();
    }

    private void init() {
        mBLEController = BluetoothLEController.getInstance().build(this);
        mBLEController.setBluetoothListener(mBluetoothLEListener);

        mList = new ArrayList<String>();
        mFoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);

        lvDevices = (ListView) findViewById(R.id.lv_ble_devices);
        btnScan = (Button) findViewById(R.id.btn_ble_scan);
        btnDisconnect = (Button) findViewById(R.id.btn_ble_disconnect);
        btnReconnect = (Button) findViewById(R.id.btn_ble_reconnect);
        btnSend = (Button) findViewById(R.id.btn_ble_send);
        tvConnState = (TextView) findViewById(R.id.tv_ble_conn_state);
        tvContent = (TextView) findViewById(R.id.tv_ble_chat_content);
        etSendContent = (EditText) findViewById(R.id.et_ble_send_content);

        lvDevices.setAdapter(mFoundAdapter);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.clear();
                mFoundAdapter.notifyDataSetChanged();
                if (mBLEController.startScan()) {
                    Toast.makeText(BleActivity.this, "Scanning!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBLEController.disconnect();
            }
        });
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBLEController.reConnect();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etSendContent.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    mBLEController.write(msg.getBytes());
                }
            }
        });
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = mList.get(position);
                mBLEController.connect(itemStr.substring(itemStr.length() - 17));
            }
        });

        if (!mBLEController.isSupportBLE()) {
            Toast.makeText(BleActivity.this, "Unsupport BLE!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLEController.release();
    }




}
