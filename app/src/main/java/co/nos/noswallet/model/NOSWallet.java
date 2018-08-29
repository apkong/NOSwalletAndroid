package co.nos.noswallet.model;

import java.math.BigDecimal;

import javax.inject.Inject;

public class NOSWallet {

    static final BigDecimal tenPowerTo30 = new BigDecimal(10).pow(30);


    private volatile String neuros = "0"; // for example 2 (which is equivalent for 2 with 30 zeros
    private volatile String rawAmount = "0"; //for example 2 with 30 zeros

    @Inject
    public NOSWallet() {
    }

    public void setRawAmount(String rawAmount) {
        this.rawAmount = rawAmount;
        this.neuros = rawToNeuros(rawAmount);
        System.out.println("setRawAmount(: " + rawAmount + ") == " + neuros);
    }

    public static String rawToNeuros(String rawAmount) {

        String result;
        int diff = rawAmount.length() - 30;
        if (rawAmount.length() < 30) {
            result = "0";
        } else {
            result = rawAmount.substring(0, diff);
        }
        System.out.println("rawToNeuros(" + rawAmount + ") == " + result);
        return result;
    }

    public void setNeuros(String neuros) {
        System.out.println("neuros: " + neuros + " NEURO");
        this.neuros = neuros;
        this.rawAmount = neurosToRaw(neuros);
    }

    public static String neurosToRaw(String neuros) {
        String thirtyZeros = zeros(30);
        return neuros + thirtyZeros;
    }

    public static String zeros(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }


    public boolean canTransferNeuros(String neurosAmount) {
        System.out.println("canTransferNeuros");
        System.out.println("neurosAmount: " + neurosAmount);
        System.out.println("neuros: " + neuros);
        BigDecimal difference = new BigDecimal(this.neuros).subtract(new BigDecimal(neurosAmount));
        return difference.compareTo(BigDecimal.ZERO) >= 0;
    }


    public String getTotalRawAmount() {
        return rawAmount;
    }

    public String getTotalNeurosAmount() {
        return neuros;
    }

    public String getRawToTransfer(String coinsAmount) {

        String result = new BigDecimal(coinsAmount).multiply(tenPowerTo30).toString();
        System.out.println("coins amount:  " + coinsAmount + ", result: " + result);
        return result;
    }
}
