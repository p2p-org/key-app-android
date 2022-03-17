package org.p2p.solanaj.ws

class SignatureNotification(val error: Any?) {
    fun hasError(): Boolean {
        return error != null
    }
}