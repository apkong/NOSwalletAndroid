package co.nos.noswallet.util;

import java.math.BigInteger;

import javax.inject.Inject;

public class LargeNumbersHelper {

    @Inject
    LargeNumbersHelper() {
    }

    public String substract(String left,
                            String right) {
        return new BigInteger(left).subtract(new BigInteger(right)).toString();
    }

    public String add(String left, String right) {
        return new BigInteger(left).add(new BigInteger(right)).toString();
    }
}
