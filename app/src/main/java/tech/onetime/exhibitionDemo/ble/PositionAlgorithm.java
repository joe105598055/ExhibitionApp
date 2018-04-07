package tech.onetime.exhibitionDemo.ble;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by joe on 2018/3/22.
 */

public class PositionAlgorithm {
    private final String TAG = "PositionAlgorithm";

    private ArrayList<BeaconObject> beacons = new ArrayList<>();
    private int logarithm, power;

    public PositionAlgorithm(ArrayList<BeaconObject> beacons) {
        this.beacons = beacons;
//        for (int i = 0; i < beacons.size(); i++)
//            Log.d(TAG,Integer.toString(i) + Integer.toString(beacons.get(i).rssi));
    }

    /**
     * 回傳估算的座標 和 平均RSSI
     */
    public double[] getCurrentPositionAndAVGRssi() throws IndexOutOfBoundsException {


        for(int i = 0 ;i < beacons.size(); i++){
            System.out.println("[" + beacons.get(i).major + "," + beacons.get(i).minor + "] =" + beacons.get(i).rssi);
        }
        BeaconObject base = beacons.get(0);
        //double xSum = 0, ySum = 0;
        int avgRSSI = beacons.get(0).rssi;

        DecimalFormat df = new DecimalFormat("##.00");
        double ret[] = new double[3];
        ret[0] = Double.parseDouble(df.format(base.major));
        ret[1] = Double.parseDouble(df.format(base.minor));
        ret[2] = Double.parseDouble(df.format(avgRSSI));

        return ret;
    }

    /**
     * 轉換 RSSI
     */
    private double getTransRSSI(int rssi) {
        return Math.abs(rssi) * Math.pow(power, Math.log(Math.abs(rssi) / Math.log(logarithm)));
    }

    /**
     * 由大到小排序 RSSI
     */
    private void sortBeacons() {
        Collections.sort(beacons, new Comparator() {

            @Override
            public int compare(Object lhs, Object rhs) {
                return ((BeaconObject) rhs).rssi - ((BeaconObject) lhs).rssi;
            }
        });
    }
}
