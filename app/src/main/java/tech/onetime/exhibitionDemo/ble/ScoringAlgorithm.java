package tech.onetime.exhibitionDemo.ble;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by joe on 2018/4/7.
 */

public class ScoringAlgorithm {

    private final String TAG = "ScoringAlgorithm";
    private ArrayList<BeaconObject> beacons = new ArrayList<>();
    private Map<String, Integer> pointClone = new HashMap<>();
    private Map<String, Integer> prePointSet = new HashMap<>();
    private Map<String, Integer> deltaSet = new HashMap<>();

    private ArrayList aList = new ArrayList();
    private ArrayList bList = new ArrayList();
    private ArrayList cList = new ArrayList();

    public ScoringAlgorithm(ArrayList<BeaconObject> beacons) {
        this.beacons = beacons;
    }

    public ScoringAlgorithm(ArrayList<BeaconObject> beacons, Map<String,Integer> prePointSet) {
        this.beacons = beacons;
        this.prePointSet = prePointSet;
    }

    public String getCurrentPosition(){
        Log.d(TAG, "---------[getCurrentPosition]------------[sample] = " + beacons.size());
        initMap();
        return scoring();

    }

    public Map<String, Integer> getPointClone(){
        return pointClone;
    }
    public Map<String ,Integer> getDeltaSet(){
        return deltaSet;
    }


    private void initMap(){

        for(BeaconObject beaconObject : beacons){

            if(beaconObject.getMajor() == "0"){

                aList.add(beaconObject.rssi + 100);

            }else if(beaconObject.getMajor() == "5"){

                bList.add(beaconObject.rssi + 100);

            }else{

                cList.add(beaconObject.rssi + 100);

            }

        }

    }

    private String scoring(){
        int pointA = 0;
        int pointB = 0;
        int pointC = 0;
        int deltaA = 0;
        int deltaB = 0;
        int deltaC = 0;

        if(aList.size() != 0){
            for(Object i : aList) {
                pointA = pointA + (int)i;
            }
            pointA = pointA * (int) Collections.max(aList);
        }

        if(bList.size() != 0){
            for(Object i : bList) {
                pointB = pointB + (int)i;
            }
            pointB = pointB * (int) Collections.max(bList);
        }

        if(cList.size() != 0){
            for(Object i : cList) {
                pointC = pointC + (int)i;
            }
            pointC = pointC * (int) Collections.max(cList);
        }


        pointClone.put("A",pointA);
        pointClone.put("B",pointB);
        pointClone.put("C",pointC);

        if(prePointSet.size() == 3) {
            if(!isRedundant(prePointSet,pointClone)){
                Log.d(TAG, "[MaxPosition Change]");
                deltaA = pointA - prePointSet.get("A");
                deltaB = pointB - prePointSet.get("B");
                deltaC = pointC - prePointSet.get("C");
            }
        }

        deltaSet.put("A",deltaA);
        deltaSet.put("B",deltaB);
        deltaSet.put("C",deltaC);

        int resultA = pointA + deltaA;
        int resultB = pointB + deltaB;
        int resultC = pointC + deltaC;

        Log.d(TAG, "[resultA]" + resultA);
        Log.d(TAG, "[resultB]" + resultB);
        Log.d(TAG, "[resultC]" + resultC);

        if(resultA == Math.max(resultA,Math.max(resultB,resultC))){
            return "A";
        }else if(resultB == Math.max(resultA,Math.max(resultB,resultC))){
            return "B";
        }else{
            return "C";
        }

    }

    private Boolean isRedundant(Map<String, Integer> previous,Map<String, Integer> current){

        String preMaxPosition = getKeyByValue(previous,Math.max(previous.get("C"),Math.max(previous.get("A"),previous.get("B"))));
        String curMaxPosition = getKeyByValue(current,Math.max(current.get("C"),Math.max(current.get("A"),current.get("B"))));

        return preMaxPosition == curMaxPosition;

    }

    private static String getKeyByValue(Map<String, Integer> map, int value) {

        String targetKey = null;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                targetKey =  entry.getKey();
            }
        }
        return targetKey;
    }

}
