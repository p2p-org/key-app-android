package org.p2p.wallet.history.interactor.buffer

object TokenHistoryBufferBuilder {
    private lateinit var tokenPublicKey: String
    private var lastSignature: String? = null
    private var payloadOffset: Int = 10

    fun getInstance(tokenPublicKey: String): TokenHistoryBufferBuilder {
        this.tokenPublicKey = tokenPublicKey
        return this
    }

    fun setLastTransactionSignature(lastSignature: String?): TokenHistoryBufferBuilder {
        this.lastSignature = lastSignature
        return this
    }

    // if buffered items count = offset, we need to request new butch of transactions
    fun setBufferPayloadOffset(offset: Int): TokenHistoryBufferBuilder {
        this.payloadOffset = offset
        return this
    }

    fun build(): TokenHistoryBuffer = TokenHistoryBuffer(tokenPublicKey, lastSignature, payloadOffset)
}
