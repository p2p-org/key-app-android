package org.p2p.wallet.utils

import androidx.core.content.getSystemService
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment

object MockUtils {

    val resourcesMock: Resources = mockk(relaxed = true)

    val sharedPrefsMock: SharedPreferences = mockk(relaxed = true) {
        every { getString(eq("KEY_BASE_URL"), any()) }.returns(NetworkEnvironment.RPC_POOL.endpoint)
        every { getString(eq("KEY_FEE_RELAYER_BASE_URL"), any()) }
            .returns("https://test-solana-fee-relayer.wallet.p2p.org/")
        every { getString(eq("KEY_NAME_SERVICE_BASE_URL"), any()) }
            .returns("https://name-register.key.app/")
        every { getString(eq("KEY_NOTIFICATION_SERVICE_BASE_URL"), any()) }
            .returns("http://35.234.120.240:9090/")
        every { getString(eq("KEY_MOONPAY_SERVER_SIDE_BASE_URL"), any()) }
            .returns("http://example.com")
        every { getString(eq("KEY_DEPOSIT_TICKER_BALANCE"), any()) }
            .returns("0")
    }

    fun mockContext(additionalMock: Context.() -> Unit = {}): Context = mockk(relaxed = true) {
        every { getSharedPreferences(any(), any()) }.returns(sharedPrefsMock)
        every { resources }.returns(resourcesMock)
        every { theme }.returns(mockk())
        every { packageName }.returns("p2p.wallet")

        val connectivityManagerMock: ConnectivityManager = mockk {
            every { allNetworks }.returns(emptyArray())
        }
        every { getSystemService<ConnectivityManager>() }.returns(connectivityManagerMock)
        every { getSystemService<NotificationManager>() }.returns(mockk())

        val externalDirMock: File = mockk(relaxed = true) {
            every { path }.returns("somepath")
        }
        every { getExternalFilesDir(any()) }.returns(externalDirMock)
        additionalMock.invoke(this)
    }

    fun mockApplication(contextMock: Context): Application = mockk {
        every { applicationContext }.returns(contextMock)
        every { baseContext }.returns(contextMock)
        every { resources }.returns(resourcesMock)
        every { packageName }.returns("org.p2p.wallet")
    }
}
