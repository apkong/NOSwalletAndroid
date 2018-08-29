package co.nos.noswallet.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

/**
 * Preconfigured representatives to choose from
 */

public class PreconfiguredRepresentatives {
    private static List<String> representatives = Arrays.asList(
            "eur_39nqgscm7yz7q1demb3zf96ru8bboqcwet14cefhmgbrxbhhozhjwxci1k9m"
    );

    public static String getRepresentative() {
        int index = new Random().nextInt(representatives.size());
        String rep = representatives.get(index);
        Timber.d("Representative: %s", rep);
        return rep;
    }
}
