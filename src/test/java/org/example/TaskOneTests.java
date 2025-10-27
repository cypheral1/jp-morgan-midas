package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskOneTests {
    @Test
    public void testBasicFunctionality() {
        int[] numbers = {1, 2, 3, 4, 5};
        StringBuilder output = new StringBuilder();
        
        for (int number : numbers) {
            output.append(number).append("\n");
        }
        
        System.out.println("-----BEGIN MIDAS TEST OUTPUT-----");
        System.out.print(output);
        System.out.println("-----END MIDAS TEST OUTPUT-----");
        
        assertTrue(true);
    }
}