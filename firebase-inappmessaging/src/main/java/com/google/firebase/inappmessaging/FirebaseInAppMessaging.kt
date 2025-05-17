package com.google.firebase.inappmessaging

import androidx.annotation.VisibleForTesting
import com.google.firebase.FirebaseApp
import com.google.firebase.inappmessaging.internal.*
import com.google.firebase.inappmessaging.internal.ForegroundNotifier.callForeground
import com.google.firebase.inappmessaging.model.TriggeredInAppMessage
import com.google.firebase.installations.FirebaseInstallationsApi
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.combine
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * The entry point of the Firebase In App Messaging headless SDK.
 */
class FirebaseInAppMessaging @VisibleForTesting @Inject constructor(
    private val inAppMessageStreamManager: InAppMessageStreamManager,
    private val programaticContextualTriggers: ProgramaticContextualTriggers,
    private val dataCollectionHelper: DataCollectionHelper,
    private val firebaseInstallations: FirebaseInstallationsApi,
    private val displayCallbacksFactory: DisplayCallbacksFactory,
    private val developerListenerManager: DeveloperListenerManager,
    @com.google.firebase.annotations.concurrent.Lightweight
    private var lightWeightExecutor: Executor
) {
    private var areMessagesSuppressed: Boolean = false
    private var fiamDisplay: FirebaseInAppMessagingDisplay? = null

    fun runAdvanceFirebaseCheck() {
        val unused = inAppMessageStreamManager
            .createFirebaseInAppMessageStream()
            .subscribe { content: List<TriggeredInAppMessage> -> triggerInAppMessage(content) }
    }

    fun callNotifier(name: String?) : Boolean {
        return callForeground(name!!)
    }
    var minArticleViews: Long = 0L
    lateinit var userSegments: List<String>
    fun setPrefAndSegments(minViews: Long, segments: List<String>) {
        userSegments = segments
        minArticleViews = minViews
    }
    init {
        firebaseInstallations.id.addOnSuccessListener(lightWeightExecutor) { id ->
            Logging.logi("Starting InAppMessaging runtime with Installation ID $id")
        }
        val unused: Disposable = inAppMessageStreamManager
            .createFirebaseInAppMessageStream()
            .subscribe { triggerInAppMessage(it) }
    }

    companion object {
        @JvmStatic
        val instance: FirebaseInAppMessaging
            get() = FirebaseApp.getInstance().get(FirebaseInAppMessaging::class.java)
        private const val SEGMENT = "segment"
        private const val MIN_ARTICLE_VIEWS = "min_article_views"
        fun getInstance(): FirebaseInAppMessaging {
            return instance
        }
    }

    val isAutomaticDataCollectionEnabled: Boolean
        get() = dataCollectionHelper.isAutomaticDataCollectionEnabled

    fun setAutomaticDataCollectionEnabled(isAutomaticCollectionEnabled: Boolean?) {
        dataCollectionHelper.setAutomaticDataCollectionEnabled(isAutomaticCollectionEnabled)
    }

    fun setAutomaticDataCollectionEnabled(isAutomaticCollectionEnabled: Boolean) {
        dataCollectionHelper.setAutomaticDataCollectionEnabled(isAutomaticCollectionEnabled)
    }

    fun setMessagesSuppressed(areMessagesSuppressed: Boolean) {
        this.areMessagesSuppressed = areMessagesSuppressed
    }

    fun areMessagesSuppressed(): Boolean = areMessagesSuppressed

    fun setMessageDisplayComponent(messageDisplay: FirebaseInAppMessagingDisplay) {
        Logging.logi("Setting display event component")
        this.fiamDisplay = messageDisplay
    }

    fun clearDisplayListener() {
        Logging.logi("Removing display event component")
        this.fiamDisplay = null
    }

    fun addImpressionListener(impressionListener: FirebaseInAppMessagingImpressionListener) {
        developerListenerManager.addImpressionListener(impressionListener)
    }

    fun addClickListener(clickListener: FirebaseInAppMessagingClickListener) {
        developerListenerManager.addClickListener(clickListener)
    }

    fun addDismissListener(dismissListener: FirebaseInAppMessagingDismissListener) {
        developerListenerManager.addDismissListener(dismissListener)
    }

    fun addDisplayErrorListener(displayErrorListener: FirebaseInAppMessagingDisplayErrorListener) {
        developerListenerManager.addDisplayErrorListener(displayErrorListener)
    }

    fun addImpressionListener(
        impressionListener: FirebaseInAppMessagingImpressionListener,
        executor: Executor
    ) {
        developerListenerManager.addImpressionListener(impressionListener, executor)
    }

    fun addClickListener(
        clickListener: FirebaseInAppMessagingClickListener,
        executor: Executor
    ) {
        developerListenerManager.addClickListener(clickListener, executor)
    }

    fun addDismissListener(
        dismissListener: FirebaseInAppMessagingDismissListener,
        executor: Executor
    ) {
        developerListenerManager.addDismissListener(dismissListener, executor)
    }

    fun addDisplayErrorListener(
        displayErrorListener: FirebaseInAppMessagingDisplayErrorListener,
        executor: Executor
    ) {
        developerListenerManager.addDisplayErrorListener(displayErrorListener, executor)
    }

    fun removeImpressionListener(impressionListener: FirebaseInAppMessagingImpressionListener) {
        developerListenerManager.removeImpressionListener(impressionListener)
    }

    fun removeClickListener(clickListener: FirebaseInAppMessagingClickListener) {
        developerListenerManager.removeClickListener(clickListener)
    }

    fun removeDisplayErrorListener(displayErrorListener: FirebaseInAppMessagingDisplayErrorListener) {
        developerListenerManager.removeDisplayErrorListener(displayErrorListener)
    }

    fun removeDismissListener(dismissListener: FirebaseInAppMessagingDismissListener) {
        developerListenerManager.removeDismissListener(dismissListener)
    }

    fun removeAllListeners() {
        developerListenerManager.removeAllListeners()
    }

    fun triggerEvent(eventName: String) {
        programaticContextualTriggers.triggerEvent(eventName)
    }

    private fun triggerInAppMessage(inAppMessage: TriggeredInAppMessage) {
        fiamDisplay?.displayMessage(
            inAppMessage.inAppMessage,
            displayCallbacksFactory.generateDisplayCallback(
                inAppMessage.inAppMessage, inAppMessage.triggeringEvent
            )
        )
    }

    private fun triggerInAppMessage(
        inAppMessages: List<TriggeredInAppMessage>
    ) {
        val segmentsList = inAppMessages.filter {
            (it.inAppMessage?.data?.containsKey(SEGMENT) == true
                    && userSegments.contains(it.inAppMessage.data?.get(SEGMENT)))
                    || it.inAppMessage?.data?.containsKey(SEGMENT) == false
        }

        val minArticleList = segmentsList.filter {
            it.inAppMessage?.data?.containsKey(MIN_ARTICLE_VIEWS) == true
        }.filter {
            (it.inAppMessage.data?.get(MIN_ARTICLE_VIEWS)?.toInt()
                ?: 0) >= minArticleViews
        }
        val noMinArticleList = segmentsList.filter {
            it.inAppMessage?.data?.containsKey(MIN_ARTICLE_VIEWS) == false
        }

        val mergedList = minArticleList + noMinArticleList

        mergedList.firstOrNull()?.let {
            triggerInAppMessage(it)
        }
    }
}