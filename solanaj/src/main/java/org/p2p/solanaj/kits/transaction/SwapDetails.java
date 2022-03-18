package org.p2p.solanaj.kits.transaction;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class SwapDetails extends TransactionDetails {

    public static List<String> KNOWN_SWAP_PROGRAM_IDS = Arrays.asList(
            "9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP",
            "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1",
            "9qvG1zUp8xF1Bi4m6UdRNby1BAAuaDrUxSpv4CmRRMjL",
            "SwaPpA9LAaLfeLi3a68M4DjnLqgtticKg6CnyNwgAC8"
    );

    @Nullable
    private final String mintA;
    @Nullable
    private final String mintB;
    private final String amountA;
    private final String amountB;
    private final String source;
    private final String alternateSource;
    private final String destination;
    private final String alternateDestination;
    private final Long fee;

    public SwapDetails(
            String signature,
            long blockTime,
            int slot,
            long fee,
            String source,
            String destination,
            @Nullable String amountA,
            @Nullable String amountB,
            @Nullable String mintA,
            @Nullable String mintB,
            String alternateSource,
            String alternateDestination
    ) {
        super(signature, blockTime, slot);
        this.amountA = amountA;
        this.amountB = amountB;
        this.mintA = mintA;
        this.mintB = mintB;
        this.fee = fee;
        this.destination = destination;
        this.source = source;
        this.alternateSource = alternateSource;
        this.alternateDestination = alternateDestination;
    }

    @Override
    public TransactionDetailsType getType() {
        return TransactionDetailsType.SWAP;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    @Nullable
    public String getMintA() {
        return mintA;
    }

    @Nullable
    public String getMintB() {
        return mintB;
    }

    public String getAmountA() {
        return amountA;
    }

    public String getAmountB() {
        return amountB;
    }

    public String getSource() {
        return source;
    }
    public String getDestination() {
        return destination;
    }

    public String getAlternateSource() {
        return alternateSource;
    }

    public String getAlternateDestination() {
        return alternateDestination;
    }

    public Long getFee() {
        return fee;
    }
}