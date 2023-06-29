package org.p2p.wallet.user.worker

import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import android.content.Context
import com.google.gson.Gson
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.interactor.TOKENS_FILE_NAME
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository

private const val TAG = "TokensDataWorker"
private const val THREE_DAYS_INTERVAL = 3L

class TokensDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        fun schedulePeriodicWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<TokensDataWorker>(THREE_DAYS_INTERVAL, TimeUnit.DAYS)
                .setInitialDelay(THREE_DAYS_INTERVAL, TimeUnit.DAYS)
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private val externalStorageRepository: ExternalStorageRepository by inject()
    private val userRepository: UserRepository by inject()
    private val userLocalRepository: UserLocalRepository by inject()
    private val coroutineDispatchers: CoroutineDispatchers by inject()
    private val gson: Gson by inject()

    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        try {
            Timber.tag(TAG).d("Starting loading solana tokens data")
            val data = userRepository.loadAllTokens()
            userLocalRepository.setTokenData(data)

            externalStorageRepository.saveJson(json = gson.toJson(data), fileName = TOKENS_FILE_NAME)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading tokens")
            return@withContext Result.failure()
        }

        Timber.tag(TAG).d("Tokens data is loaded successfully")
        Result.success()
    }
}
