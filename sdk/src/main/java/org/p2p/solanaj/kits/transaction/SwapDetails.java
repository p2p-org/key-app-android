package org.p2p.solanaj.kits.transaction;

import java.util.Arrays;
import java.util.List;

public class SwapDetails extends TransactionDetails {

    public static List<String> KNOWN_SWAP_PROGRAM_IDS = Arrays.asList("DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1");

    private String mintA;
    private String mintB;
    private String amountA;
    private String amountB;

    public SwapDetails(String data) {
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.SWAP;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    public String getMintA() {
        return mintA;
    }

    public String getMintB() {
        return mintB;
    }

    public String getAmountA() {
        return amountA;
    }

    public String getAmountB() {
        return amountB;
    }

}