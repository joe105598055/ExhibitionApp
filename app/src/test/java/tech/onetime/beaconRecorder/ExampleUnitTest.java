package tech.onetime.beaconRecorder;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void test123() throws Exception {
        ArrayList<String> array = new ArrayList<>();
        array.add("a");
        array.add("b");
        array.add("b");
        array.add("d");
        array.add("e");
        for(int i = array.size()-1; i >= 0; i--){
            System.out.println("i = " + i);
            if(array.get(i) == "b"){
                array.remove(i);
                array.add(i,"*");
            }
        }

        for(String s : array){
            System.out.println(s);
        }
        assertEquals(4, 2 + 2);
    }


}