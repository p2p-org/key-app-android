package org.p2p.wallet.renbtc.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.model.RenBtcSession
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

private const val TAG = "RenVMService"

class RenVMService : Service(), CoroutineScope {

    companion object {
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_START_SESSION = "ACTION_START_SESSION"

        fun startWithCheck(context: Context) {
            val intent = Intent(context, RenVMService::class.java).setAction(ACTION_START_SESSION)
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RenVMService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val renBtcInteractor: RenBtcInteractor by inject()

    private var checkJob: Job? = null
    private var renVMJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        renVMJob?.cancel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        Timber.tag(TAG).d("Received new action $action")
        when (action) {
            ACTION_START_SESSION -> checkActiveOrStarNewSession()
            ACTION_STOP -> stopServiceAndCleanSession()
        }

        return START_NOT_STICKY
    }

    private fun stopServiceAndCleanSession() {
        launch {
            renBtcInteractor.clearSession()
            stopSelf()
        }
    }

    /**
     * This will not start new session if no valid session found in database
     * This can be called to draw current state of the screen
     * */
    private fun checkActiveOrStarNewSession() {
        if (checkJob?.isActive == true) {
            Timber.tag(TAG).d("Session is active, skipping check")
            return
        }

        Timber.tag(TAG).d("Searching for existing session")

        checkJob = launch(coroutineContext) {
            try {
                val session = renBtcInteractor.findActiveSession()
                if (session != null && session.isValid) {
                    renBtcInteractor.startSession(session)
                    renBtcInteractor.startPolling(session)
                } else {
                    renBtcInteractor.setSessionSate(RenBtcSession.Loading)
                    val session = renBtcInteractor.generateSession()
                    renBtcInteractor.startPolling(session)
                }
            } catch (e: Throwable) {
                renBtcInteractor.setSessionSate(RenBtcSession.Error(e))
                Timber.e(e, "Error starting  session")
            }
        }
    }
}
