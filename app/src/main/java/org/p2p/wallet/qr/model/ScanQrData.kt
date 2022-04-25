package org.p2p.wallet.qr.model

sealed class ScanQrData {
    class Success(val data: String) : ScanQrData()
    object IncorrectQR : ScanQrData()
}
