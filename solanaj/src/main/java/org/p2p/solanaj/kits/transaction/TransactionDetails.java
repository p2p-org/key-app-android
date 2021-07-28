package org.p2p.solanaj.kits.transaction;

public abstract class TransactionDetails {

    private final String signature;
    private final long blockTime;
    private final int slot;

    public TransactionDetails(String signature, long blockTime, int slot) {
        this.signature = signature;
        this.blockTime = blockTime;
        this.slot = slot;
    }

    public abstract TransactionDetailsType getType();

    public abstract Object getInfo();

    public String getData() {
        return null;
    }

    public String getSignature() {
        return signature;
    }

    public int getSlot() {
        return slot;
    }

    public long getBlockTime() {
        /*
         * Since blocktime is time of when the transaction was processed in SECONDS
         * we are converting it into milliseconds
         * */
        return blockTime * 1000;
    }
}