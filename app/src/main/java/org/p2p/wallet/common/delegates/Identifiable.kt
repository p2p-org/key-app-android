package org.p2p.wallet.common.delegates

/**
 * Identifiable interface to provide long id values, to use as stableId.
 */
interface Identifiable {

    /**
     * @return item id.
     */
    val id: Long
}
