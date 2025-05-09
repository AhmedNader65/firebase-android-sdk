/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.sessions

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.annotations.concurrent.Background
import com.google.firebase.app
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Datastore for sessions information */
internal data class FirebaseSessionsData(val sessionId: String?)

/** Handles reading to and writing from the [DataStore]. */
internal interface SessionDatastore {
  /** Stores a new session ID value in the [DataStore] */
  fun updateSessionId(sessionId: String)

  /**
   * Gets the currently stored session ID from the [DataStore]. This will be null if no session has
   * been stored previously.
   */
  fun getCurrentSessionId(): String?

  companion object {
    val instance: SessionDatastore
      get() = Firebase.app[FirebaseSessionsComponent::class.java].sessionDatastore
  }
}

@Singleton
internal class SessionDatastoreImpl
@Inject
constructor(
  @Background private val backgroundDispatcher: CoroutineContext,
  @SessionDetailsDataStore private val dataStore: DataStore<Preferences>,
) : SessionDatastore {

  /** Most recent session from datastore is updated asynchronously whenever it changes */
  private val currentSessionFromDatastore = AtomicReference<FirebaseSessionsData>()

  private object FirebaseSessionDataKeys {
    val SESSION_ID = stringPreferencesKey("session_id")
  }

  private val firebaseSessionDataFlow: Flow<FirebaseSessionsData> =
    dataStore.data
      .catch { exception ->
        Log.e(TAG, "Error reading stored session data.", exception)
        emit(emptyPreferences())
      }
      .map { preferences -> mapSessionsData(preferences) }

  init {
    CoroutineScope(backgroundDispatcher).launch {
      firebaseSessionDataFlow.collect { currentSessionFromDatastore.set(it) }
    }
  }

  override fun updateSessionId(sessionId: String) {
    CoroutineScope(backgroundDispatcher).launch {
      try {
        dataStore.edit { preferences ->
          preferences[FirebaseSessionDataKeys.SESSION_ID] = sessionId
        }
      } catch (e: IOException) {
        Log.w(TAG, "Failed to update session Id: $e")
      }
    }
  }

  override fun getCurrentSessionId() = currentSessionFromDatastore.get()?.sessionId

  private fun mapSessionsData(preferences: Preferences): FirebaseSessionsData =
    FirebaseSessionsData(preferences[FirebaseSessionDataKeys.SESSION_ID])

  private companion object {
    private const val TAG = "FirebaseSessionsRepo"
  }
}
