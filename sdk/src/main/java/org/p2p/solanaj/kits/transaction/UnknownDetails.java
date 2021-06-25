package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class UnknownDetails extends TransactionDetails {
    private Object rawData;
    private String data;

    public UnknownDetails(String signature, long blockTime, Map<String, Object> rawData) {
        super(signature, blockTime);
        this.rawData = rawData;
    }

    public UnknownDetails(String signature, long blockTime, String data) {
        super(signature, blockTime);
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