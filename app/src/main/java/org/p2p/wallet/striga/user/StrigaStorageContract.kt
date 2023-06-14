package org.p2p.wallet.striga.user

import org.p2p.wallet.striga.user.model.StrigaUserStatus

interface StrigaStorageContract {
    var userStatus: StrigaUserStatus?
}
