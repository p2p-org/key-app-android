package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class CloseAccountDetails extends TransactionDetails {
    private String account;
    private String destination;
    private String owner;
    private String signature;

    public CloseAccountDetails(Map<String, Object> rawData) {
        this.account = (String) rawData.get("account");
        this.destination = (String) rawData.get("destination");
        this.owner = (String) rawData.get("owner");
        this.signature = "123"; // fixme: add valid signature
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.CLOSE_ACCOUNT;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public String getAccount() {
        return account;
    }

    public String getDestination() {
        return destination;
    }

    public String getOwner() {
        return owner;
    }

} 