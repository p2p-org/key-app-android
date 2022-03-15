package org.p2p.solanaj.kits.transaction;

import androidx.annotation.NonNull;

public enum TransactionDetailsType {
    UNKNOWN("unknown"),
    SWAP("swap"),
    TRANSFER("transfer"),
    CREATE_ACCOUNT("create"),
    CLOSE_ACCOUNT("closeAccount");

    private String type;

    TransactionDetailsType(String type) {
        this.type = type;
    }

    @NonNull
    public String getType() {
        return type;
    }

}