package org.p2p.wallet.debugdrawer

import android.app.Application
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.base.DebugModuleAdapter
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.timber.data.LumberYard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.common.AppRestarter
import timber.log.Timber

object DebugDrawer : KoinComponent {

    private val appRestarter: AppRestarter by inject()
    private val authLogoutInteractor: AuthLogoutInteractor by inject()

    fun init(application: Application) {
        val lumberYard = LumberYard.getInstance(application)
        lumberYard.cleanUp()
        Timber.plant(lumberYard.tree())
    }

    fun install(activity: FragmentActivity): DebugDrawer {
        val drawer = DebugDrawer.Builder(activity)
            .modules(*getDefaultModules())
            .withTheme(R.style.WalletTheme)
            .build()

        val content = activity.findViewById<ViewGroup>(R.id.dd_content_layout).apply {
            isFocusableInTouchMode = false
            isFocusable = false
            isClickable = false
        }
        val parent = (content.parent as ViewGroup).apply {
            isFocusableInTouchMode = false
            isFocusable = false
            isClickable = false
        }
        val drawerContent = activity.findViewById<ViewGroup>(R.id.dd_debug_view)

        drawerContent.setBackgroundColor(activity.getColor(R.color.backgroundPrimary))

        return drawer
    }

    private fun getDefaultModules(): Array<DebugModuleAdapter> {
        val restartCallback = { appRestarter.restartApp() }
        val clearDataCallback = { authLogoutInteractor.clearAppData() }

        return arrayOf(
            ConfigurationModule(),
            CustomTimberModule(),
            WipeDataModule(restartCallback, clearDataCallback),
            BuildModule(),
            DeviceModule()
        )
    }
}
