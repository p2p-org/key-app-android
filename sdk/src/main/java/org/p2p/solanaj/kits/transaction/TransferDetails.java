package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class TransferDetails extends TransactionDetails {
    private String destination;
    private String source;
    private String mint;
    private String amount;
    private int decimals;

    public TransferDetails(String signature, long blockTime, String type, Map<String, Object> rawData) {
        super(signature, blockTime);
        this.destination = (String) rawData.get("destination");
        this.source = (String) rawData.get("source");

        if (type.equals("transferChecked")) {
            this.mint = (String) rawData.get("mint");
            this.amount = (String) ((Map) rawData.get("tokenAmount")).get("amount");
            this.decimals = ((Double) ((Map) rawData.get("tokenAmount")).get("decimals")).intValue();
        } else {
            this.amount = (String) rawData.get("amount");
        }
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.TRANSFER;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public String getMint() {
        return mint;
    }

    public String getAmount() {
        return amount;
    }

    public int getDecimals() {
        return decimals;
    }

}