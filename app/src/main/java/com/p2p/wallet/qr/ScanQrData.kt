package com.p2p.wallet.qr

sealed class ScanQrData {
    class Success(val data: String) : ScanQrData()
    object IncorrectQR : ScanQrData()
}