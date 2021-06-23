package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class UnknownDetails extends TransactionDetails {
    private Object rawData;
    private String data;

    public UnknownDetails(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    public UnknownDetails(String data) {
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