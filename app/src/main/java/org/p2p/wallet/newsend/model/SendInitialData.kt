package org.p2p.wallet.newsend.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.wallet.newsend.ui.SendOpenedFrom

@Parcelize
data class SendInitialData(
    val recipient: SearchResult,
    val openedFrom: SendOpenedFrom,
    val initialToken: Token.Active? = null,
    val inputAmount: BigDecimal? = null
) : Parcelable {

    fun isTokenSelectionEnabled(userTokens: List<Token.Active>): Boolean {
        // enabled token selection if user has more than 1 token
        // and he entered NOT from token details
        return initialToken == null && userTokens.size > 1
    }

    fun isInitialAmountEntered(): Boolean = inputAmount != null

    fun selectInitialToken(userTokens: List<Token.Active>): Token.Active {
        if (initialToken != null) {
            return initialToken
        }

        if (userTokens.isEmpty()) {
            throw SendFatalError("User non-zero tokens can't be empty!")
        }

        return userTokens.first()
    }

    fun findSolToken(sourceToken: Token.Active, solToken: Token.Active?): Token.Active {
        return if (sourceToken.isSOL) {
            sourceToken
        } else {
            solToken ?: throw SendFatalError("Couldn't find user's SOL account!")
        }
    }
}
