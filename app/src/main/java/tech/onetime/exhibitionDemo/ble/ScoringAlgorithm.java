package tech.onetime.exhibitionDemo.ble;

import android.util.Log;

import java.util.ArrayList;

import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by joe on 2018/4/7.
 */

public class ScoringAlgorithm {
    private final String TAG = "ScoringAlgorithm";
    private ArrayList<BeaconObject> beacons = new ArrayList<>();
    private  ArrayList<String> positionList = new ArrayList<>();

    public ScoringAlgorithm(ArrayList<BeaconObject> beacons) {
        this.beacons = beacons;
        mappingPosition();
    }
    public String getCurrentPosition(){
        Log.d(TAG, "-----------------------");
        String candidate = null;
        for(int i = 0,count = 0; i < positionList.size(); i++){
            Log.d(TAG,positionList.get(i));
            if(count == 0){
                candidate = positionList.get(i);
                count = 1;
            }else if(candidate != positionList.get(i)){
                count--;
            }else{
                count++;
            }
        }
        if(isMajorityElement(candidate))
            return candidate;
        else
            return "not majority";

        //return candidate;

    }

    private Boolean isMajorityElement(String candidate){
        int count = 0;
        for(int i = 0; i < positionList.size(); i++){
            if(candidate == positionList.get(i))
                count++;
        }
        if(count > positionList.size()/2)
            return true;
        else
            return false;
    }
    private void mappingPosition (){



        for(int i = 0; i < beacons.size(); i++ ){
            switch (beacons.get(i).getMajorMinorString()){
                case "(0,0)":
                case "(0,5)":
                case "(0,8)":
                    positionList.add("A");
                    break;
                case "(5,0)":
                case "(5,5)":
                case "(5,8)":
                    positionList.add("B");
                    break;
                case "(8,0)":
                case "(8,5)":
                case "(8,8)":
                    positionList.add("C");
                    break;
            }
        }
    }
}
