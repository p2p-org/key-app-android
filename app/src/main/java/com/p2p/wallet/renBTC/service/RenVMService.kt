package com.p2p.wallet.renBTC.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.p2p.wallet.renBTC.interactor.RenBTCInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

private const val TAG = "RenVMService"

class RenVMService : Service(), CoroutineScope {

    companion object {
        private const val ACTION_CHECK = "ACTION_CHECK"
        private const val ACTION_NEW_SESSION = "ACTION_NEW_SESSION"

        fun startWithCheck(context: Context) {
            val intent = Intent(context, RenVMService::class.java).setAction(ACTION_CHECK)
            context.startService(intent)
        }

        fun startWithNewSession(context: Context) {
            val intent = Intent(context, RenVMService::class.java).setAction(ACTION_NEW_SESSION)
            context.startService(intent)
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val interactor: RenBTCInteractor by inject()

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
            ACTION_CHECK -> checkActiveSession()
            ACTION_NEW_SESSION -> startNewSession()
        }

        return START_NOT_STICKY
    }

    /**
     * This will not start new session if no valid session found in database
     * This can be called to draw current state of the screen
     * */
    private fun checkActiveSession() {
        if (checkJob?.isActive == true) {
            Timber.tag(TAG).d("Session is active, skipping check")
            return
        }

        Timber.tag(TAG).d("Searching for existing session")

        checkJob = launch {
            try {
                val session = interactor.findActiveSession()
                if (session != null && session.isValid) startNewSession()
            } catch (e: Throwable) {
                Timber.e(e, "Error starting session")
            }
        }
    }

    /**
     * This will start new session anyway
     * This can be called when user clicked [show address] and wants to make transaction
     * */
    private fun startNewSession() {
        if (renVMJob?.isActive == true) {
            Timber.tag(TAG).d("Service is already active, skipping new session generate")
            return
        }

        Timber.tag(TAG).d("Generating new session")

        renVMJob = launch {
            try {
                val session = interactor.generateSession()
                interactor.startPolling(session)
            } catch (e: Throwable) {
                Timber.e(e, "Error generating session")
            }
        }
    }
}