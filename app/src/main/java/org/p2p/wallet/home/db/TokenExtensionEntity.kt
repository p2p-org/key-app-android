package org.p2p.wallet.home.db

import androidx.room.ColumnInfo

data class TokenExtensionEntity(
    @ColumnInfo(name = COLUMN_RULE_OF_PROCESSING_TOKEN_PRICE)
    val ruleOfProcessingTokenPrice: String?,
    @ColumnInfo(name = COLUMN_IS_TOKEN_VISIBLE_ON_WALLET_SCREEN)
    val isTokenVisibleOnWalletScreen: Boolean?,
    @ColumnInfo(name = COLUMN_IS_TOKEN_CELL_VISIBLE_ON_WALLET_SCREEN)
    val isTokenCellVisibleOnWalletScreen: Boolean?,
    @ColumnInfo(name = COLUMN_TOKEN_PERCENT_DIFFERENCE_ON_WALLET_SCREEN)
    val tokenPercentDifferenceOnWalletScreen: Int?,
    @ColumnInfo(name = COLUMN_CALCULATION_WITH_TOTAL_BALANCE)
    val isCalculateWithTotalBalance: Boolean?,
    @ColumnInfo(name = COLUMN_TOKEN_FRACTION_RULE_ON_WALLET_SCREEN)
    val tokenFractionRuleOnWalletScreen: String?,
    @ColumnInfo(name = COLUMN_CAN_TOKEN_BE_HIDDEN)
    val canTokenBeHidden: Boolean?
) {
    companion object {
        const val COLUMN_RULE_OF_PROCESSING_TOKEN_PRICE = "rule_of_processing_token_price"
        const val COLUMN_IS_TOKEN_VISIBLE_ON_WALLET_SCREEN = "is_token_visible_on_wallet_screen"
        const val COLUMN_IS_TOKEN_CELL_VISIBLE_ON_WALLET_SCREEN = "is_token_cell_visible_on_wallet_screen"
        const val COLUMN_TOKEN_PERCENT_DIFFERENCE_ON_WALLET_SCREEN = "token_percent_difference_on_wallet_screen"
        const val COLUMN_CALCULATION_WITH_TOTAL_BALANCE = "is_calculate_with_total_balance"
        const val COLUMN_TOKEN_FRACTION_RULE_ON_WALLET_SCREEN = "token_fraction_rule_on_wallet_screen"
        const val COLUMN_CAN_TOKEN_BE_HIDDEN = "can_token_be_hidden"
    }
}
