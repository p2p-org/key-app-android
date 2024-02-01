package org.p2p.wallet.referral.repository

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc

internal class ReferralRequest(
    @Transient val request: ReferralBodyRequest,
    method: String,
) : JsonRpc<ReferralBodyRequest, Unit>(
    method = method,
    params = request
) {
    @Transient
    override val typeOfResult: Type = Unit::class.java
}
