package co.nos.noswallet.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import co.nos.noswallet.BuildConfig;
import timber.log.Timber;

/**
 * Preconfigured representatives to choose from
 */

public class PreconfiguredRepresentatives {
    private static List<String> representatives = Arrays.asList(
            BuildConfig.REPRESENTATIVE
    );

    public static String getRepresentative() {
        int size = representatives.size();
        if (size == 1) {
            return representatives.get(0);
        } else {
            int index = new Random().nextInt(size);
            String rep = representatives.get(index);
            Timber.d("Representative: %s", rep);
            return rep;
        }
    }
}
