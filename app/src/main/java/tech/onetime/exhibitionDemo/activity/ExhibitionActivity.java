package tech.onetime.exhibitionDemo.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.InputStream;

import tech.onetime.exhibitionDemo.R;
import tech.onetime.exhibitionDemo.ble.BeaconScanCallback;
import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by joe on 2018/04/16
 */

@EActivity(R.layout.activity_setting)
public class ExhibitionActivity extends AppCompatActivity implements BeaconScanCallback.iBeaconScanCallback,SensorEventListener {

    private static final String TAG = "ExhibitionActivity";

    private BeaconScanCallback _beaconCallback;
    @ViewById ImageView areaImage;
    @ViewById TextView times;
    // for AcceleroMeter
    @ViewById TextView active;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
    private float mAccelLast = SensorManager.GRAVITY_EARTH;
    private long perScannedTime = 0;
    private int stopCount = 0;
    private int MOTION_THRESHOLD;
    // for AcceleroMeter End
    private Boolean isMoving = true;

    private SensorManager sensorManager;


    static final int REQUEST_ENABLE_BT = 1001; // The request code


    @AfterViews
    void afterViews() {

        Log.d(TAG, "afterViews");
        if(bleInit()){
            Log.d(TAG, "[bleInit] true");
        }
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

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

        setResult(RESULT_CANCELED, ExhibitionActivity.this.getIntent());
        ExhibitionActivity.this.finish();

    }

    @Override
    public void scannedBeacons(BeaconObject beaconObject) {
        /**To do  each beaconObject be scanned */
    }

    int roundTimes = 0;
    @Override
    public void getNearestBeacon(BeaconObject beaconObject) {

        times.setText(Integer.toString(++roundTimes));

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(perScannedTime == 0){
            perScannedTime = sensorEvent.timestamp;
        }
        if(perScannedTime !=  sensorEvent.timestamp){
            System.out.println("[delta] =" + (sensorEvent.timestamp - perScannedTime)/1000000L);
            updateThreshold((sensorEvent.timestamp - perScannedTime)/1000000L);
            perScannedTime = sensorEvent.timestamp;
        }

        mGravity = sensorEvent.values.clone();

        float x = mGravity[0];
        float y = mGravity[1];
        float z = mGravity[2];

        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;

        // Make this higher or lower according to how much
        // motion you want to detect
        if(mAccel > 0.6){
            // do something
            stopCount = 0;
        }else{
            stopCount++;
        }
        if(stopCount >= MOTION_THRESHOLD){
            isMoving = false;
            active.setText("stop");
        }else{
            isMoving = true;
            active.setText("moving");
        }
    }

    @UiThread
    private void updateThreshold(long delta){


        if(10 <= delta && delta <= 29){ //20
            MOTION_THRESHOLD = 45;
        }else if(30 <= delta && delta <= 49){ //40
            MOTION_THRESHOLD = 22;
        }else if(50 <= delta && delta <= 69){ //60
            MOTION_THRESHOLD = 15;
        }else if(70 <= delta && delta <= 89){ //80
            MOTION_THRESHOLD = 11;
        }else if(89 <= delta && delta <= 200){
            MOTION_THRESHOLD = 8;
        }else{
            MOTION_THRESHOLD = 7;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private String currentPosition = "";
    @Override
    public void getCurrentPosition(String position) {
        Log.d(TAG, "************************getCurrentPosition: " + position);
        roundTimes = 0;
        if(currentPosition != position && !isMoving){
            currentPosition = position;
            switch (position){
                case "A":
                    new DownloadImageTask((ImageView) findViewById(R.id.areaImage)).execute("http://140.124.181.85:3000/image/A.png");
                    break;
                case "B":
                    new DownloadImageTask((ImageView) findViewById(R.id.areaImage)).execute("http://140.124.181.85:3000/image/B.png");
                    break;
                case "C":
                    new DownloadImageTask((ImageView) findViewById(R.id.areaImage)).execute("http://140.124.181.85:3000/image/C.png");
                    break;
//                case "critical":
//                    new DownloadImageTask((ImageView) findViewById(R.id.areaImage)).execute("http://140.124.181.85:3000/image/Critical.png");
//                    break;
//                case "not majority":
//                    new DownloadImageTask((ImageView) findViewById(R.id.areaImage)).execute("http://140.124.181.85:3000/image/Moving.png");
//                    break;
            }
        }

}
    protected void onDestroy(){

        super.onDestroy();

        if (_beaconCallback != null) _beaconCallback.stopScan();

        sensorManager.unregisterListener(this);

    }

    @Click(R.id.exit)
    void exit(){
        setResult(RESULT_OK, ExhibitionActivity.this.getIntent());
        ExhibitionActivity.this.finish();
    }

}
