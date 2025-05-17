// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.inappmessaging.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.BackpressureStrategy

/**
 * The [ForegroundNotifier] notifies listeners via [.foregroundFlowable] when an
 * application comes to the foreground.
 *
 *
 * Supported foreground scenarios
 *
 *
 *  * App resumed phone screen is unlocked
 *  * App starts when app icon is clicked
 *  * App resumes aftercompletion of phone call
 *  * App is chosen from recent apps menu
 *
 *
 *
 * This works as follows
 *
 *
 *  * When an app is foregrounded for the first time after app icon is clicked, it is moved to
 * the foreground state and an event is published
 *  * When any activity in the app is paused and [.onActivityPaused] callback is
 * received, the app is considered to be paused until the next activity starts and the [       ][.onActivityResumed] callback is received. A runnable is simultaneously scheduled
 * to be run after a [.DELAY_MILLIS] which will put the app into background state.
 *  * If some other activity subsequently starts and beats execution of the runnable by invoking
 * the [.onActivityResumed], the app never went out of view for the user and
 * is considered to have never gone to the background. The runnable is removed and the app
 * remains in the foreground.
 *  * Similar to the first step, an event is published in the [       ][.onActivityResumed] callback if the app was deemed to be in the background>
 *
 *
 * @hide
 */
object ForegroundNotifier {
    private val foregroundSubject = BehaviorSubject.create<String>()

    /** @return a [ConnectableFlowable] representing a stream of foreground events
     */
    fun foregroundFlowable(): ConnectableFlowable<String> {
        return foregroundSubject.toFlowable(BackpressureStrategy.BUFFER).publish()
    }

    val list = mutableListOf<String>()


    fun callForeground(name: String): Boolean {
        if (list.contains(name).not()) {
            list.add(name)
            foregroundSubject.onNext(InAppMessageStreamManager.ON_FOREGROUND)
            return true
        }
        return false
    }

}