package org.p2p.wallet.striga.user

import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails

interface StrigaStorageContract {
    var userStatus: StrigaUserStatusDetails?
}
