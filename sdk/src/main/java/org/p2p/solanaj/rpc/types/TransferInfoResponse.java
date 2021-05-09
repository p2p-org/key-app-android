package org.p2p.solanaj.rpc.types;

public class TransferInfoResponse {

    private String from;
    private String to;
    private long lamports;
    private long slot;
    private String signature;
    private long fee;

    public TransferInfoResponse(String from, String to, long lamports, long slot, String signature, long fee) {
        this.from = from;
        this.to = to;
        this.lamports = lamports;
        this.slot = slot;
        this.signature = signature;
        this.fee = fee;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }


    public long getSlot() {
        return slot;
    }

    public void setSlot(long slot) {
        this.slot = slot;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getLamports() {
        return lamports;
    }

    @Override
    public String toString() {
        return "To: " + to.toString() + " From: " + from.toString() + " lamports: " + lamports + "\n";
    }

}
