package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class TransferDetails extends TransactionDetails {
    private final String destination;
    private final String source;
    private final String authority;
    private String mint;
    private final String amount;
    private int decimals;
    private final String transferType;
    private final long fee;

    public TransferDetails(String signature, long blockTime, int slot, long fee, String type, Map<String, Object> rawData) {
        super(signature, blockTime, slot);
        this.destination = (String) rawData.get("destination");
        this.source = (String) rawData.get("source");
        this.authority = (String) rawData.get("authority");
        this.transferType = type;
        this.fee = fee;

        if (type.equals("transferChecked")) {
            this.mint = (String) rawData.get("mint");
            this.amount = (String) ((Map) rawData.get("tokenAmount")).get("amount");
            this.decimals = ((Double) ((Map) rawData.get("tokenAmount")).get("decimals")).intValue();
        } else {
            Double lamports = (Double) rawData.get("lamports");
            String finalAmount;
            if (lamports != null) {
                finalAmount = lamports.toString();
            } else {
                finalAmount = (String) rawData.get("amount");
            }
            this.amount = finalAmount;
        }
    }

    public Boolean isSimpleTransfer() {
        return transferType.equals("transfer");
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.TRANSFER;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public String getTransferType() {
        return transferType;
    }

    public String getAuthority() {
        return authority;
    }

    public String getMint() {
        return mint;
    }

    public String getAmount() {
        return amount;
    }

    public long getFee() {
        return fee;
    }

    public int getDecimals() {
        int result;
        if (decimals == 0) {
            // if there is no decimals, then putting SOL decimals instead
            result = 9;
        } else {
            result = decimals;
        }
        return result;
    }

}