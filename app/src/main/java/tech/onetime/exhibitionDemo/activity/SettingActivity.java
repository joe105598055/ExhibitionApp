package tech.onetime.exhibitionDemo.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;

import tech.onetime.exhibitionDemo.R;
import tech.onetime.exhibitionDemo.ble.BeaconScanCallback;
import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by JianFa on 2017/2/26
 */

@EActivity(R.layout.activity_setting)
public class SettingActivity extends AppCompatActivity implements BeaconScanCallback.iBeaconScanCallback{

    private static final String TAG = "SettingActivity";

    private BeaconScanCallback _beaconCallback;

    static final int REQUEST_ENABLE_BT = 1001; // The request code


    @AfterViews
    void afterViews() {

        Log.d(TAG, "afterViews");
        if(bleInit()){
            System.out.println("[bleInit] true");
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean bleInit() {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bm.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return scanBeacon();

    }

    private boolean scanBeacon() {

        if (_beaconCallback != null)
            _beaconCallback.stopScan();

        _beaconCallback = new BeaconScanCallback(this, this);
//        Log.d(TAG, "scanBeacon __ set beacon mac : " + currentBeaconObject.mac);
        // TO Set beacon filter
//        _beaconCallback.setScanFilter_address(currentBeaconObject.mac);
        _beaconCallback.startScan();

        return true;

    }

    @Override
    public void onBackPressed() {

//        super.onBackPressed();

        setResult(RESULT_CANCELED, SettingActivity.this.getIntent());
        SettingActivity.this.finish();

    }

    @Override
    public void scannedBeacons(BeaconObject beaconObject) {
        /**To do  each beaconObject be scanned */
    }

    @Override
    public void getNearestBeacon(BeaconObject beaconObject) {

        Log.d(TAG, "[getNearestBeacon]" + beaconObject.getMajorMinorString());

    }

    @Override
    public void getCurrentRoundBeacon(ArrayList<BeaconObject> BeaconObjectArray) {

    }

    protected void onDestroy(){

        super.onDestroy();

        if (_beaconCallback != null) _beaconCallback.stopScan();

    }

    @Click(R.id.exit)
    void exit(){
        setResult(RESULT_OK, SettingActivity.this.getIntent());
        SettingActivity.this.finish();
    }

}
