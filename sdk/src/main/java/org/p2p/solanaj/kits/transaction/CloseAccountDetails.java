package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class CloseAccountDetails extends TransactionDetails {
    private String account;
    private String destination;
    private String owner;
    private int slot;

    public CloseAccountDetails(String signature, long blockTime, int slot, Map<String, Object> rawData) {
        super(signature, blockTime, slot);
        this.account = (String) rawData.get("account");
        this.destination = (String) rawData.get("destination");
        this.owner = (String) rawData.get("owner");
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.CLOSE_ACCOUNT;
    }

    @Override
    public Object getInfo() {
        return this;
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

    @Override
    public int getSlot() {
        return slot;
    }
}