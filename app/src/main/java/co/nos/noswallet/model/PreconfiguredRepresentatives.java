package co.nos.noswallet.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import timber.log.Timber;

/**
 * Preconfigured usd_representatives to choose from
 */

public class PreconfiguredRepresentatives {
    private static List<String> usd_representatives = Arrays.asList(
            BuildConfig.REPRESENTATIVE
    );

    private static List<String> nos_representatives = Arrays.asList(
            BuildConfig.NOS_REPRESENTATIVE_0,
            BuildConfig.NOS_REPRESENTATIVE_1,
            BuildConfig.NOS_REPRESENTATIVE_2
    );

    private static Map<CryptoCurrency, List<String>> representativesMap = new HashMap<>();

    static {
        representativesMap.put(CryptoCurrency.NOLLAR, usd_representatives);
        representativesMap.put(CryptoCurrency.NOS, nos_representatives);
    }

    public static String getRepresentative(CryptoCurrency cryptoCurrency) {
        List<String> representativesCollection = representativesMap.get(cryptoCurrency);
        return randomlyPicked(representativesCollection);
    }

    public static String getRepresentative() {
        int size = usd_representatives.size();
        if (size == 1) {
            return usd_representatives.get(0);
        } else {
            int index = new Random().nextInt(size);
            String rep = usd_representatives.get(index);
            Timber.d("Representative: %s", rep);
            return randomlyPicked(usd_representatives);
        }
    }

    public static String randomlyPicked(List<String> list) {
        if (list.size() == 1) return list.get(0);
        int index = new Random().nextInt(list.size());
        return list.get(index);
    }
}
