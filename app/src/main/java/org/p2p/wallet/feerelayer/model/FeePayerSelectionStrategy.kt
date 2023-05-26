package org.p2p.wallet.feerelayer.model

/**
 * Selection strategy limits our action when [FeePayerState] is calculated
 * For example: we cannot correct the amount if user enter the amount but we can correct it
 * when user selects the new source token or clicked on [MAX] value available
 * */
enum class FeePayerSelectionStrategy {
    CORRECT_AMOUNT,
    SELECT_FEE_PAYER,
    MANUAL;
}
