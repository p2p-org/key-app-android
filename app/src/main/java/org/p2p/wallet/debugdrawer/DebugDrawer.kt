package org.p2p.wallet.debugdrawer

import android.app.Application
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.edgetoedge.isInsetConsumed
import org.p2p.wallet.utils.edgetoedge.navigationBarInset
import org.p2p.wallet.utils.edgetoedge.redispatchWindowInsetsToAllChildren
import org.p2p.wallet.utils.edgetoedge.statusBarInset
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.base.DebugModuleAdapter
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.timber.data.LumberYard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

object DebugDrawer : KoinComponent {

    private val appRestarter: AppRestarter by inject()
    private val authInteractor: AuthInteractor by inject()

    fun init(application: Application) {
        val lumberYard = LumberYard.getInstance(application)
        lumberYard.cleanUp()
        Timber.plant(lumberYard.tree())
    }

    fun install(activity: FragmentActivity) {
        DebugDrawer.Builder(activity)
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

        drawerContent.setBackgroundColor(activity.colorFromTheme(R.attr.colorBackgroundPrimary))

        ViewCompat.setOnApplyWindowInsetsListener(drawerContent) { v, insets ->
            if (insets.isInsetConsumed()) {
                return@setOnApplyWindowInsetsListener insets
            }
            v.updatePadding(
                top = insets.statusBarInset(),
                bottom = insets.navigationBarInset()
            )
            WindowInsetsCompat.CONSUMED
        }
        parent.redispatchWindowInsetsToAllChildren()
        content.redispatchWindowInsetsToAllChildren()
    }

    private fun getDefaultModules(): Array<DebugModuleAdapter> {
        val restartCallback = { appRestarter.restartApp() }
        val clearDataCallback = { authInteractor.clear() }

        return arrayOf(
            ConfigurationModule(),
            CustomTimberModule(),
            WipeDataModule(restartCallback, clearDataCallback),
            BuildModule(),
            DeviceModule()
        )
    }
}