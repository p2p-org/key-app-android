import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import org.p2p.wallet.infrastructure.network.ServerException
import org.p2p.wallet.infrastructure.network.feerelayer.ErrorTypeConverter
import org.p2p.wallet.infrastructure.network.feerelayer.FeeRelayerErrorType
import org.p2p.wallet.infrastructure.network.feerelayer.FeeRelayerServerError
import timber.log.Timber
import java.io.IOException

class FeeRelayerInterceptor(
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            response
        } else {
            throw extractException(response.body!!.string())
        }
    }

    private fun extractException(bodyString: String): Throwable = try {
        val fullMessage = JSONObject(bodyString).toString(1)

        Timber.tag("ServerErrorInterceptor").d("Handling exception: $fullMessage")

        val serverError = gson.fromJson(bodyString, FeeRelayerServerError::class.java)

        ServerException(
            errorCode = ErrorTypeConverter.fromFeeRelayer(serverError.data?.type ?: FeeRelayerErrorType.UNKNOWN),
            fullMessage = fullMessage,
            errorMessage = serverError.message
        )
    } catch (e: Throwable) {
        IOException("Error reading response error body", e)
    }
}