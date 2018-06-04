package tech.onetime.exhibitionDemo.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tech.onetime.exhibitionDemo.schema.BeaconObject;


/**
 * Created by Alexandro on 2016/7/5.
 */

public class BeaconScanCallback implements KitkatScanCallback.iKitkatScanCallback, LollipopScanCallback.iLollipopScanCallback {

    private final String TAG = "OPBeaconScanCallback";

    private iBeaconScanCallback scanCallback;

    private LollipopScanCallback lollipopScanCallback;
    private KitkatScanCallback kitkatLeScanCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanFilter.Builder _filterBuilder;
    private long lastScannedTime = 0;
//    private ArrayList<BeaconObject> eachRoundBeacon = new ArrayList<BeaconObject>();
    private ArrayList<ArrayList<BeaconObject>> eachRoundBeacon = new ArrayList<>();
    private Boolean isScanning = false;
    private Timer timer = null;


    public BeaconScanCallback(Context ctx, iBeaconScanCallback scanCallback) {

        this.scanCallback = scanCallback;

        BluetoothManager bm = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bm.getAdapter();

    }

    public void startTimerTask(){
        timer = new Timer();
        timer.schedule(new timerTask(), 2000L,2000L);
    }

    public void closeTimerTask(){
        if(timer!=null){
            timer.cancel();
        }
    }

    class timerTask extends TimerTask {

        @Override
        public void run() {
            ScoringAlgorithm scoringAlgorithm;
            if(_preScroingSet!=null){
                scoringAlgorithm = new ScoringAlgorithm(syncBeacons.getIns().getBeacons(),_preScroingSet);
            }else {
                scoringAlgorithm = new ScoringAlgorithm(syncBeacons.getIns().getBeacons());
            }
            String currentPosition = scoringAlgorithm.getCurrentPosition();
            Map<String,Integer> scoringSet = scoringAlgorithm.getPointClone();
            Map<String,Integer> deltaSet = scoringAlgorithm.getDeltaSet();


            scanCallback.getCurrentPosition(currentPosition,scoringSet,deltaSet);
        }
    }
    public interface iBeaconScanCallback {

        void scannedBeacons(BeaconObject beaconObject);

        void getCurrentPosition(String position,Map<String,Integer> scoringSet,Map<String,Integer> offSet);

    }


    public void startScan() {

        int apiVersion = Build.VERSION.SDK_INT;

        if (apiVersion > Build.VERSION_CODES.KITKAT)
            scan_lollipop();
        else scan_kitkat();

        isScanning = true;

    }

    private Map <String,Integer> _preScroingSet = null;
    public void setPreviousScoringSet(Map<String,Integer> previousScroingSet) {
        _preScroingSet = new HashMap<>(previousScroingSet);
    }

    public Boolean isScanning(){
        return isScanning;
    }
    /**
     * android 4.4
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void scan_kitkat() {

        Log.d(TAG, "scan_kitkat");

        kitkatLeScanCallback = new KitkatScanCallback(this);
        mBluetoothAdapter.startLeScan(kitkatLeScanCallback);

    }

    /**
     * android 5.0
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void scan_lollipop() {

        Log.d(TAG, "scan_lollipop");

        lollipopScanCallback = new LollipopScanCallback(this);

        List<ScanFilter> scanFilters = new ArrayList<>();
        if(_filterBuilder == null) _filterBuilder = new ScanFilter.Builder();
        _filterBuilder.setDeviceName("USBeacon");
        scanFilters.add(_filterBuilder.build());



        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
//        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
//        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanFilters, scanSettingsBuilder.build(), lollipopScanCallback);

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setScanFilter_address(String deviceAddress) {

        if(_filterBuilder == null) _filterBuilder = new ScanFilter.Builder();
        _filterBuilder.setDeviceAddress(deviceAddress);

    }


    /**
     * kitkat - 偵測到 beacon
     *
     * @param beaconObject is the object include beacon attribute
     */
    @Override
    public void kitkat_beaconScanned(BeaconObject beaconObject) {
            syncBeacons.getIns().addBeacon(beaconObject);
            scanCallback.scannedBeacons(beaconObject);
            returnCallback();

    }

    /**
     * lollipop - 偵測到 beacon
     *
     * @param beaconObject  is the object include beacon attribute
     */
    @Override
    public void lollipop_beaconScanned(BeaconObject beaconObject) {

        syncBeacons.getIns().addBeacon(beaconObject);
        scanCallback.scannedBeacons(beaconObject);
        returnCallback();


    }

    public void stopScan() {

        Log.d(TAG, "stopScan");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
                scanner.stopScan(lollipopScanCallback);
            } else {
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.stopLeScan(kitkatLeScanCallback);
                if (kitkatLeScanCallback != null)
                    kitkatLeScanCallback.stopDetect();
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        isScanning = false;

    }

    public void clearBeacons(){
        Log.d(TAG, "[clearBeacons]");
        syncBeacons.getIns().removeAllBeacons();
    }

    private  void returnCallback(){
//        if (!canReturnCallback()){
//            return;
//        }
//        scanCallback.getNearestBeacon(syncBeacons.getIns().getNearest());
//        eachRoundBeacon.add(syncBeacons.getIns().getBeacons());
//        if(eachRoundBeacon.size() == 10){ // 10 round後，根據scoring result 判斷位置
//            String currentPosition = new ScoringAlgorithm(eachRoundBeacon).getCurrentPosition();
//            scanCallback.getCurrentPosition(currentPosition);
//            Log.d(TAG,"currentPosition = " + currentPosition);
//            eachRoundBeacon.clear();
//        }
//        syncBeacons.getIns().removeAllBeacons();

    }


    private static class syncBeacons {

        private static syncBeacons ins = null;

        private ArrayList<BeaconObject> beacons = new ArrayList<>();

        public static synchronized syncBeacons getIns() {
            if (ins == null)
                ins = new syncBeacons();
            return ins;
        }

        public ArrayList<BeaconObject> getBeacons() {
            return (ArrayList<BeaconObject>) beacons.clone();
        }

        public synchronized void addBeacon(BeaconObject beaconObject) {
            for (int i = 0; i < beacons.size(); i++) {
                if (beacons.get(i).mac.equals(beaconObject.mac)) {

                    if(beacons.get(i).rssi <= beaconObject.rssi){
                        beacons.remove(i);
                        beacons.add(i, beaconObject);
                    }
// else{
//                        beacons.get(i).time = beaconObject.time;
//                    }
                    return;
                }
            }

            beacons.add(beaconObject);
        }

        public synchronized void removeAllBeacons() {
//            long currentTime = System.currentTimeMillis();
//            for(int i = 0; i < beacons.size(); i++){
//                if(currentTime - beacons.get(i).time  > 3000)
//                    beacons.remove(i);
////                    i--;
//            }
            beacons.clear();
        }

        public synchronized BeaconObject getNearest() {
            int maxRSSI = -1000;
            BeaconObject nearest = null;
            for (BeaconObject object : beacons) {
                if (maxRSSI < object.rssi) {
                    nearest = object;
                    maxRSSI = object.rssi;
                }
            }

            return nearest;
        }
    }

    private boolean canReturnCallback() {
        long currentScannedTime = System.currentTimeMillis();
        if (lastScannedTime == 0) {
            lastScannedTime = currentScannedTime;
            return false;
        }

        if (currentScannedTime - lastScannedTime > 100) { // updateTime
            lastScannedTime = currentScannedTime;
            return true;
        } else {
            return false;
        }
    }

}
