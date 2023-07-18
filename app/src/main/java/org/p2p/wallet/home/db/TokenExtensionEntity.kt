package org.p2p.wallet.home.db

import androidx.room.ColumnInfo

data class TokenExtensionEntity(
    @ColumnInfo(name = COLUMN_RULE_OF_PROCESSING_TOKEN_PRICE_WS)
    val ruleOfProcessingTokenPriceWs: String?,
    @ColumnInfo(name = COLUMN_IS_POSITION_ON_WS)
    val isPositionOnWs: Boolean?,
    @ColumnInfo(name = COLUMN_IS_TOKEN_CELL_VISIBLE_ON_WS)
    val isTokenCellVisibleOnWs: Boolean?,
    @ColumnInfo(name = COLUMN_PERCENT_DIFFERENCE_TO_SHOW_BY_PRICE_ON_WS)
    val percentDifferenceToShowByPriceOnWs: Int?,
    @ColumnInfo(name = COLUMN_CALCULATION_OF_FINAL_BALANCE_ON_WS)
    val calculationOfFinalBalanceOnWs: Boolean?,
    @ColumnInfo(name = COLUMN_RULE_OF_FRACTIONAL_PART_ON_WS)
    val ruleOfFractionalPartOnWs: String?,
    @ColumnInfo(name = COLUMN_CAN_BE_HIDDEN)
    val canBeHidden: Boolean?
) {
    companion object {
        const val COLUMN_RULE_OF_PROCESSING_TOKEN_PRICE_WS = "rule_of_processing_token_price"
        const val COLUMN_IS_POSITION_ON_WS = "is_position_on_ws"
        const val COLUMN_IS_TOKEN_CELL_VISIBLE_ON_WS = "is_token_visible_on_ws"
        const val COLUMN_PERCENT_DIFFERENCE_TO_SHOW_BY_PRICE_ON_WS = "percent_diff_to_show_by_price_on_ws"
        const val COLUMN_CALCULATION_OF_FINAL_BALANCE_ON_WS = "calculation_of_final_balance_on_ws"
        const val COLUMN_RULE_OF_FRACTIONAL_PART_ON_WS = "rule_of_fractional_part_on_ws"
        const val COLUMN_CAN_BE_HIDDEN = "can_be_hidden"
    }
}
