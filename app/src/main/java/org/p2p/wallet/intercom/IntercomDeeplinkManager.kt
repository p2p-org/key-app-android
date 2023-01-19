package org.p2p.wallet.intercom

import android.net.Uri
import io.intercom.android.sdk.Intercom
import java.util.Stack

private const val QUERY_SURVEY_ID = "intercom_survey_id"

class IntercomDeeplinkManager {

    private val deeplinkStack = Stack<String>()
    private val intercomClient: Intercom
        get() = Intercom.client()

    fun handleBackgroundDeeplink(data: Uri) {
        val uriQueryParams = data.queryParameterNames
        if (QUERY_SURVEY_ID in uriQueryParams) {
            val surveyId = data.getQueryParameter(QUERY_SURVEY_ID).orEmpty()
            deeplinkStack.push(surveyId)
        }
    }

    fun handleForegroundDeeplink(params: Map<String, String>) {
        if (QUERY_SURVEY_ID in params) {
            val surveyId = params[QUERY_SURVEY_ID].orEmpty()
            deeplinkStack.push(surveyId)
        }
    }

    fun proceedDeeplinkIfExists() {
        if (!isHasDeeplinkToProceed()) return
        val surveyId = deeplinkStack.pop()
        intercomClient.displaySurvey(surveyId)
    }

    private fun isHasDeeplinkToProceed(): Boolean = deeplinkStack.isNotEmpty()
}
