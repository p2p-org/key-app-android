package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class UnknownDetails extends TransactionDetails {
    private Object rawData;
    private String data;

    public UnknownDetails(String signature, long blockTime, int slot, Map<String, Object> rawData) {
        super(signature, blockTime, slot);
        this.rawData = rawData;
    }

    public UnknownDetails(String signature, long blockTime, int slot, String data) {
        super(signature, blockTime, slot);
        this.data = data;
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.UNKNOWN;
    }

    @Override
    public Object getInfo() {
        return rawData;
    }

    @Override
    public String getData() {
        return data;
    }

} 