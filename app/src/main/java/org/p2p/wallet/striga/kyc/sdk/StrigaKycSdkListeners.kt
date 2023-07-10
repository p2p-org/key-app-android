package org.p2p.wallet.striga.kyc.sdk

import com.sumsub.sns.core.SNSActionResult
import com.sumsub.sns.core.data.listener.SNSEvent
import com.sumsub.sns.core.data.model.SNSCompletionResult
import com.sumsub.sns.core.data.model.SNSException
import com.sumsub.sns.core.data.model.SNSSDKState

/**
 * Listener to know about errors that occur within the SDK
 *
 * [documentation](https://developers.sumsub.com/msdk/android/#on-sdk-errors)
 */
typealias StrigaSdkErrorListener = (error: SNSException) -> Unit
/**
 * Listener to get notified about changes in the flow of the verification process.
 *
 * [documentation](https://developers.sumsub.com/msdk/android/#on-sdk-state-changes)
 */
typealias StrigaSdkStateListener = (oldState: SNSSDKState, newState: SNSSDKState) -> Unit

/**
 * An optional callback to get notified when the SDK is closed
 *
 * - [SNSCompletionResult.SuccessTermination] - A user clicks on the cancel button.
 * - [SNSCompletionResult.AbnormalTermination] - an error occurred.
 * Look at the exception object if you want to get more information
 * @param result [SNSCompletionResult]
 * @param state [SNSSDKState] the state at which the SDK was closed
 */
typealias StrigaSdkCompletionListener = (result: SNSCompletionResult, state: SNSSDKState) -> Unit

/**
 * An optional handler for getting liveness result and controlling action scenario.
 *
 * The handler takes two parameters:
 * - actionId - Action ID
 * - answer - Liveness module answer.
 * Possible values: "GREEN", "YELLOW", "RED", "ERROR" or null
 *
 * [documentation](https://developers.sumsub.com/msdk/android/#on-action-result)
 */
typealias StrigaSdkActionResultListener = (actionId: String, actionType: String?) -> SNSActionResult

/**
 * Providing events callback allows you to be aware of the events happening along the processing.
 *
 * Events are passed into the callback as instances of a class inherited from the base `SNSEvent` class,
 * this way each event has its eventType and some parameters packed into payload dictionary.
 * You can get event parameters either by examining the payload directly
 * or by casting the given event instance to a specific SNSEvent* class according to its type.
 */
typealias StrigaSdkEventListener = (event: SNSEvent) -> Unit
