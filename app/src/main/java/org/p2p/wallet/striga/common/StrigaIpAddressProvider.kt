package org.p2p.wallet.striga.common

import timber.log.Timber
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

class StrigaIpAddressProvider {

    /**
     * This is a temporary function, until we have a proper way to get the ip address
     */
    fun getIpAddress(): String {
        val defaultOne = "127.0.0.1"
        try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { it is Inet4Address && !it.isLoopbackAddress  }
                ?.hostAddress
                ?: defaultOne
        } catch (e: SocketException) {
            Timber.e(e, "Unable to retrieve ip address")
        }
        return defaultOne
    }
}
