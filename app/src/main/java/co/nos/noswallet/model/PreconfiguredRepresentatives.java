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

    private static List<String> banano_representatives = Arrays.asList(
            "ban_1fomoz167m7o38gw4rzt7hz67oq6itejpt4yocrfywujbpatd711cjew8gjj",
            "ban_1cake36ua5aqcq1c5i3dg7k8xtosw7r9r7qbbf5j15sk75csp9okesz87nfn"
    );

    private static List<String> nano_representatives = Arrays.asList(
            "xrb_3arg3asgtigae3xckabaaewkx3bzsh7nwz7jkmjos79ihyaxwphhm6qgjps4",
            "xrb_1stofnrxuz3cai7ze75o174bpm7scwj9jn3nxsn8ntzg784jf1gzn1jjdkou",
            "xrb_1q3hqecaw15cjt7thbtxu3pbzr1eihtzzpzxguoc37bj1wc5ffoh7w74gi6p",
            "xrb_3dmtrrws3pocycmbqwawk6xs7446qxa36fcncush4s1pejk16ksbmakis78m",
            "xrb_3hd4ezdgsp15iemx7h81in7xz5tpxi43b6b41zn3qmwiuypankocw3awes5k",
            "xrb_1awsn43we17c1oshdru4azeqjz9wii41dy8npubm4rg11so7dx3jtqgoeahy",
            "xrb_1anrzcuwe64rwxzcco8dkhpyxpi8kd7zsjc1oeimpc3ppca4mrjtwnqposrs",
            "xrb_1hza3f7wiiqa7ig3jczyxj5yo86yegcmqk3criaz838j91sxcckpfhbhhra1"
    );

    private static Map<CryptoCurrency, List<String>> representativesMap = new HashMap<>();

    static {
        representativesMap.put(CryptoCurrency.NOLLAR, usd_representatives);
        representativesMap.put(CryptoCurrency.NOS, nos_representatives);
        representativesMap.put(CryptoCurrency.NANO, nano_representatives);
        representativesMap.put(CryptoCurrency.BANANO, banano_representatives);
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
