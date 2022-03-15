package org.p2p.solanaj.kits.transaction;

import java.util.Map;

public class BurnOrMintDetails extends TransactionDetails {
    private final String account;
    private final String authority;
    private final String amount;
    private int decimals;
    private final long fee;

    public BurnOrMintDetails(String signature, long blockTime, int slot, long fee, Map<String, Object> rawData) {
        super(signature, blockTime, slot);
        this.account = (String) rawData.get("account");
        this.authority = (String) rawData.get("authority");
        this.fee = fee;
        this.amount = (String) ((Map) rawData.get("tokenAmount")).get("uiAmountString");
    }

    public BurnOrMintDetails(
            String signature,
            long blockTime,
            int slot,
            String account,
            String authority,
            String amount,
            int decimals,
            long fee
    ) {
        super(signature, blockTime, slot);
        this.account = account;
        this.authority = authority;
        this.amount = amount;
        this.decimals = decimals;
        this.fee = fee;
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

    public String getAuthority() {
        return authority;
    }

    public String getAccount() {
        return account;
    }

    public String getMint() {
        return "CDJWUqTcYTVAKXAVXoQZFes5JUFc7owSeq7eMQcDSbo5";
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