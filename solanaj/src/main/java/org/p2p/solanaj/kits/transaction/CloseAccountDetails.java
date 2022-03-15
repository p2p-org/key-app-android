package org.p2p.solanaj.kits.transaction;

import org.jetbrains.annotations.Nullable;

public class CloseAccountDetails extends TransactionDetails {
    private String account;
    private String mint;

    public CloseAccountDetails(
            @Nullable String signature,
            long blockTime,
            int slot,
            @Nullable String account,
            @Nullable String mint) {
        super(signature, blockTime, slot);
        this.account = account;
        this.mint = mint;
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

    public String getMint() {
        return mint;
    }
}