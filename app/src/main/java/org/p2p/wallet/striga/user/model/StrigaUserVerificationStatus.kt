package org.p2p.wallet.striga.user.model

/**
 *
 * @property INITIATED The "Start KYC" endpoint has been called and the SumSub token has been fetched
 * @property PENDING_REVIEW Documents have been submitted and are pending review
 * @property ON_HOLD Requires manual review from the compliance team
 * @property APPROVED User approved
 * @property REJECTED User rejected - Can be final or not
 */
enum class StrigaUserVerificationStatus {
    // please, keep order, ordinal is used in comparative operations
    UNKNOWN,
    NOT_STARTED,
    INITIATED,
    PENDING_REVIEW,
    ON_HOLD,
    REJECTED,
    REJECTED_FINAL,
    APPROVED;

    companion object {
        fun from(status: String): StrigaUserVerificationStatus {
            return values().firstOrNull { it.name.lowercase() == status.lowercase() } ?: UNKNOWN
        }
    }
}
