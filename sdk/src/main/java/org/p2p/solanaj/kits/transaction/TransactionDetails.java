package org.p2p.solanaj.kits.transaction;

public abstract class TransactionDetails {

    public abstract TransactionDetailsType getType();

    public abstract Object getInfo();

    public String getData() {
        return null;
    }

}