package org.p2p.solanaj.kits.transaction;

public abstract class TransactionDetails {

    private final String signature;
    private final long blockTime;

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
        /*
         * Since blocktime is time of when the transaction was processed in SECONDS
         * we are converting it into milliseconds
         * */
        return blockTime * 1000;
    }
}