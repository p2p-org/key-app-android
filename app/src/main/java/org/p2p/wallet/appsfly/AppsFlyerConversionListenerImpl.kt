package org.p2p.wallet.appsfly

import com.appsflyer.AppsFlyerConversionListener
import timber.log.Timber

private const val TAG = "AppsFlyConversionListener"
class AppsFlyerConversionListenerImpl : AppsFlyerConversionListener {

    override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
        Timber.tag(TAG).d("On Conversion Success: data = $data")
    }

    override fun onConversionDataFail(data: String?) {
        Timber.tag(TAG).d("On Conversion Failure: cause = $data")
    }

    override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
        Timber.tag(TAG).d("On App open attribution success: data = $data")
    }

    override fun onAttributionFailure(data: String?) {
        Timber.tag(TAG).d("On App open attribution failure: cause = $data")
    }
}
