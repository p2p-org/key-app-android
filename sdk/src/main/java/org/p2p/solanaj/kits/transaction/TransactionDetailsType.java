package org.p2p.solanaj.kits.transaction;

public enum TransactionDetailsType {
    UNKNOWN("unknown"), SWAP("swap"), TRANSFER("transfer"), CLOSE_ACCOUNT("closeAccount");

    private String type;

    TransactionDetailsType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}