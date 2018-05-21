package tech.onetime.exhibitionDemo.ble;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tech.onetime.exhibitionDemo.schema.BeaconObject;

/**
 * Created by joe on 2018/4/7.
 */

public class ScoringAlgorithmV2 {
    private final String TAG = "ScoringAlgorithm";
    private ArrayList<BeaconObject> beacons = new ArrayList<>();
    private Map<String, Integer> beaconMap = new HashMap<>();
    private Map<String, Integer> scoringClone = new HashMap<>();
    private Map<String, Integer> preScoringSet = new HashMap<>();
    private ArrayList<String> resultList = new ArrayList<>();
    private Map<String, Integer> offSet = new HashMap<>();



    public ScoringAlgorithmV2(ArrayList<BeaconObject> beacons) {
        this.beacons = beacons;
    }

    public ScoringAlgorithmV2(ArrayList<BeaconObject> beacons,Map<String,Integer> preScoringSet) {
        this.beacons = beacons;
        this.preScoringSet = preScoringSet;
    }

    public String getCurrentPosition(){
        Log.d(TAG, "---------[getCurrentPosition]------------[sample] = " + beacons.size());
        initMap();

        for(int i = 0; i < beacons.size(); i++){ // 1...10 round
            beaconMap.put(beacons.get(i).getMajorMinorString(),beacons.get(i).rssi);
        }


        return scoring();

    }

    public Map<String, Integer> getScoringSet(){
        return scoringClone;
    }
    public Map<String ,Integer> getOffset(){
        return offSet;
    }

    private String scoring(){

        Log.d(TAG, "***************[Scoring]**************");
        int ResultA = 0, ResultB = 0, ResultC = 0;
        int offSetA = 0, offSetB = 0, offSetC = 0;

        int weightA = Math.max(beaconMap.get("(0,0)"),Math.max(beaconMap.get("(0,2)"),beaconMap.get("(0,4)"))) + 100;
        int weightB = Math.max(beaconMap.get("(5,11)"),Math.max(beaconMap.get("(5,13)"),beaconMap.get("(5,15)"))) + 100;
        int weightC = Math.max(beaconMap.get("(8,22)"),Math.max(beaconMap.get("(8,24)"),beaconMap.get("(8,26)"))) + 100;
        int ScoringA = (beaconMap.get("(0,0)") + beaconMap.get("(0,2)") + beaconMap.get("(0,4)") + 300)  * weightA;
        int ScoringB = (beaconMap.get("(5,11)") + beaconMap.get("(5,13)") + beaconMap.get("(5,15)") + 300) * weightB;
        int ScoringC = (beaconMap.get("(8,22)") + beaconMap.get("(8,24)") + beaconMap.get("(8,26)") + 300) * weightC;
        scoringClone.put("A",ScoringA);
        scoringClone.put("B",ScoringB);
        scoringClone.put("C",ScoringC);

        if(preScoringSet.get("B")!=null) {
            if(!isRedundant(preScoringSet,scoringClone)){
                Log.d(TAG, "[MaxPosition Change!!!!!!!!!!!!!!!!!!!!!!!!!]");
                offSetA = ScoringA - preScoringSet.get("A");
                offSetB = ScoringB - preScoringSet.get("B");
                offSetC = ScoringC - preScoringSet.get("C");
            }
        }

        offSet.put("A",offSetA);
        offSet.put("B",offSetB);
        offSet.put("C",offSetC);

        ResultA = ScoringA + offSetA;
        ResultB = ScoringB + offSetB;
        ResultC = ScoringC + offSetC;

        Log.d(TAG, "weightA = " + weightA + ", " + "ScoringA = " + ScoringA + ", " + "preA = " + preScoringSet.get("A") +  ", " + "resultA = " + ResultA);
        Log.d(TAG, "weightB = " + weightB + ", " + "ScoringB = " + ScoringB + ", " + "preB = " + preScoringSet.get("B") +  ", " + "resultB = " + ResultB) ;
        Log.d(TAG, "weightC = " + weightC + ", " + "ScoringC = " + ScoringC + ", " + "preC = " + preScoringSet.get("C") +  ", " + "resultC = " + ResultC);


        if(ResultA == Math.max(ResultA,Math.max(ResultB,ResultC))){
            return "A";
        }else if(ResultB == Math.max(ResultA,Math.max(ResultB,ResultC))){
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
    private void initMap(){
        beaconMap.clear();
        beaconMap.put("(0,0)",-100);
        beaconMap.put("(0,2)",-100);
        beaconMap.put("(0,4)",-100);
        beaconMap.put("(5,11)",-100);
        beaconMap.put("(5,13)",-100);
        beaconMap.put("(5,15)",-100);
        beaconMap.put("(8,22)",-100);
        beaconMap.put("(8,24)",-100);
        beaconMap.put("(8,26)",-100);

    }


    private Boolean isMajorityElement(String candidate){
        int count = 0;
        for(int i = 0; i < resultList.size(); i++){
            if(candidate == resultList.get(i))
                count++;
        }
        if(count > resultList.size()/2)
            return true;
        else
            return false;
    }
//    private void mappingPosition (){
//
//        for(int i = 0; i < beacons.size(); i++ ){
//            switch (beacons.get(i).getMajorMinorString()){
//                case "(0,0)":
//                case "(0,5)":
//                case "(0,8)":
//                    positionList.add("A");
//                    break;
//                case "(5,0)":
//                case "(5,5)":
//                case "(5,8)":
//                    positionList.add("B");
//                    break;
//                case "(8,0)":
//                case "(8,5)":
//                case "(8,8)":
//                    positionList.add("C");
//                    break;
//            }
//        }
//    }
}
