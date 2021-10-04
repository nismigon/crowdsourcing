package fr.stormlab.crowdsourcing;

import java.util.Random;

public class Utils {


    /**
     * Generate a random int between min and max
     * @param min Minimum value
     * @param max Maximum value
     * @return An integer between min and max
     */
    public static int generateRandomInt(int min, int max) {
        return new Random().nextInt(max-min) + min;
    }
}
