package org.p2p.wallet.striga.user

import org.p2p.wallet.striga.signup.model.StrigaUserStatus

interface StrigaStorageContract {
    var userStatus: StrigaUserStatus?
}
