package org.p2p.solanaj.kits.transaction;

import java.util.Arrays;
import java.util.List;

public class SwapDetails extends TransactionDetails {

    public static List<String> KNOWN_SWAP_PROGRAM_IDS = Arrays.asList("DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1");

    private final String mintA;
    private final String mintB;
    private final String amountA;
    private final String amountB;
    private final String destination;
    private final Long fee;

    public SwapDetails(
            String signature,
            long blockTime,
            int slot,
            long fee,
            String destination,
            String amountA,
            String amountB,
            String mintA,
            String mintB
    ) {
        super(signature, blockTime, slot);
        this.amountA = amountA;
        this.amountB = amountB;
        this.mintA = mintA;
        this.mintB = mintB;
        this.fee = fee;
        this.destination = destination;
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

    public String getDestination() {
        return destination;
    }

    public Long getFee() {
        return fee;
    }
}