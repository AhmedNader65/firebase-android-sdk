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

package com.google.firebase.vertexai.type

import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.common.FirebaseCommonAIException
import kotlinx.coroutines.TimeoutCancellationException

/** Parent class for any errors that occur from the [FirebaseVertexAI] SDK. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public abstract class FirebaseVertexAIException
internal constructor(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {

  internal companion object {

    /**
     * Converts a [Throwable] to a [FirebaseVertexAIException].
     *
     * Will populate default messages as expected, and propagate the provided [cause] through the
     * resulting exception.
     */
    internal fun from(cause: Throwable): FirebaseVertexAIException =
      when (cause) {
        is FirebaseVertexAIException -> cause
        is FirebaseCommonAIException ->
          when (cause) {
            is com.google.firebase.vertexai.common.SerializationException ->
              SerializationException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.ServerException ->
              ServerException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.InvalidAPIKeyException ->
              InvalidAPIKeyException(cause.message ?: "")
            is com.google.firebase.vertexai.common.PromptBlockedException ->
              PromptBlockedException(cause.response?.toPublic(), cause.cause)
            is com.google.firebase.vertexai.common.UnsupportedUserLocationException ->
              UnsupportedUserLocationException(cause.cause)
            is com.google.firebase.vertexai.common.InvalidStateException ->
              InvalidStateException(cause.message ?: "", cause)
            is com.google.firebase.vertexai.common.ResponseStoppedException ->
              ResponseStoppedException(cause.response.toPublic(), cause.cause)
            is com.google.firebase.vertexai.common.RequestTimeoutException ->
              RequestTimeoutException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.ServiceDisabledException ->
              ServiceDisabledException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.UnknownException ->
              UnknownException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.ContentBlockedException ->
              ContentBlockedException(cause.message ?: "", cause.cause)
            is com.google.firebase.vertexai.common.QuotaExceededException ->
              QuotaExceededException(cause.message ?: "", cause.cause)
            else -> UnknownException(cause.message ?: "", cause)
          }
        is TimeoutCancellationException ->
          RequestTimeoutException("The request failed to complete in the allotted time.")
        else -> UnknownException("Something unexpected happened.", cause)
      }

    /**
     * Catch any exception thrown in the [callback] block and rethrow it as a
     * [FirebaseVertexAIException].
     *
     * Will return whatever the [callback] returns as well.
     *
     * @see catch
     */
    internal suspend fun <T> catchAsync(callback: suspend () -> T): T {
      try {
        return callback()
      } catch (e: Exception) {
        throw from(e)
      }
    }

    /**
     * Catch any exception thrown in the [callback] block and rethrow it as a
     * [FirebaseVertexAIException].
     *
     * Will return whatever the [callback] returns as well.
     *
     * @see catchAsync
     */
    internal fun <T> catch(callback: () -> T): T {
      try {
        return callback()
      } catch (e: Exception) {
        throw from(e)
      }
    }
  }
}

/** Something went wrong while trying to deserialize a response from the server. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class SerializationException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/** The server responded with a non 200 response code. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class ServerException internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/** The provided API Key is not valid. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class InvalidAPIKeyException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/**
 * A request was blocked.
 *
 * See the [response's][response] `promptFeedback.blockReason` for more information.
 *
 * @property response The full server response.
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class PromptBlockedException
internal constructor(
  public val response: GenerateContentResponse?,
  cause: Throwable? = null,
  message: String? = null,
) :
  FirebaseVertexAIException(
    "Prompt was blocked: ${response?.promptFeedback?.blockReason?.name?: message}",
    cause,
  ) {
  internal constructor(message: String, cause: Throwable? = null) : this(null, cause, message)
}

@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class ContentBlockedException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/**
 * The user's location (region) is not supported by the API.
 *
 * See the documentation for a
 * [list of regions](https://firebase.google.com/docs/vertex-ai/locations?platform=android#available-locations)
 * (countries and territories) where the API is available.
 */
// TODO(rlazo): Add secondary constructor to pass through the message?
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class UnsupportedUserLocationException internal constructor(cause: Throwable? = null) :
  FirebaseVertexAIException("User location is not supported for the API use.", cause)

/**
 * Some form of state occurred that shouldn't have.
 *
 * Usually indicative of consumer error.
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class InvalidStateException internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/**
 * A request was stopped during generation for some reason.
 *
 * @property response The full server response.
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class ResponseStoppedException
internal constructor(public val response: GenerateContentResponse, cause: Throwable? = null) :
  FirebaseVertexAIException(
    "Content generation stopped. Reason: ${response.candidates.first().finishReason?.name}",
    cause,
  )

/**
 * A request took too long to complete.
 *
 * Usually occurs due to a user specified [timeout][RequestOptions.timeout].
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class RequestTimeoutException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/**
 * The specified Vertex AI location is invalid.
 *
 * For a list of valid locations, see
 * [Vertex AI locations.](https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations#available-regions)
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class InvalidLocationException
internal constructor(location: String, cause: Throwable? = null) :
  FirebaseVertexAIException("Invalid location \"${location}\"", cause)

/**
 * The service is not enabled for this Firebase project. Learn how to enable the required services
 * in the
 * [Firebase documentation.](https://firebase.google.com/docs/vertex-ai/faq-and-troubleshooting#required-apis)
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class ServiceDisabledException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/**
 * The request has hit a quota limit. Learn more about quotas in the
 * [Firebase documentation.](https://firebase.google.com/docs/vertex-ai/quotas)
 */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class QuotaExceededException
internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/** Streaming session already receiving. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class SessionAlreadyReceivingException :
  FirebaseVertexAIException(
    "This session is already receiving. Please call stopReceiving() before calling this again."
  )

/** Audio record initialization failures for audio streaming */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class AudioRecordInitializationFailedException(message: String) :
  FirebaseVertexAIException(message)

/** Handshake failed with the server */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class ServiceConnectionHandshakeFailedException(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)

/** Catch all case for exceptions not explicitly expected. */
@Deprecated(
  """The Vertex AI in Firebase SDK (firebase-vertexai) has been replaced with the FirebaseAI SDK (firebase-ai) to accommodate the evolving set of supported features and services.
For migration details, see the migration guide: https://firebase.google.com/docs/vertex-ai/migrate-to-latest-sdk"""
)
public class UnknownException internal constructor(message: String, cause: Throwable? = null) :
  FirebaseVertexAIException(message, cause)
