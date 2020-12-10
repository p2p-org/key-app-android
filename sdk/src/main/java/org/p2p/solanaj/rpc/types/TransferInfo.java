package org.p2p.solanaj.rpc.types;

public class TransferInfo {

    String from;
    String to;
    long lamports;
    long slot;
    String signature;

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double fee;

    public TransferInfo(String from, String to, long lamports) {
        this.from = from;
        this.to = to;
        this.lamports = lamports;
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
