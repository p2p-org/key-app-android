package org.p2p.solanaj.kits.transaction;

public abstract class TransactionDetails {

    private String signature;
    private long blockTime;

    public TransactionDetails(String signature, long blockTime) {
        this.signature = signature;
        this.blockTime = blockTime;
    }

    public abstract TransactionDetailsType getType();

    public abstract Object getInfo();

    public String getData() {
        return null;
    }

    public String getSignature() {
        return signature;
    }

    public long getBlockTime() {
        return blockTime;
    }
}