package co.nos.noswallet.model;

import java.math.BigDecimal;

public class CurrentAccountBalance {

    public final BigDecimal value;

    public CurrentAccountBalance(BigDecimal currentAccountBalance) {
        this.value = currentAccountBalance;
    }

    public CurrentAccountBalance(String currentAccountBalance) {
        this(new BigDecimal(currentAccountBalance));
    }
}
