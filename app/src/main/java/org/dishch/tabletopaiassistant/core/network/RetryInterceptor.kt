package org.dishch.tabletopaiassistant.core.network

import java.net.SocketTimeoutException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Retries a request up to [ApiConfig.MAX_RETRY_ATTEMPTS] times with exponential backoff
 * (2^n seconds) when the request times out. Non-timeout IO errors (e.g. no connection)
 * are not retried and are propagated immediately.
 */
class RetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastError: SocketTimeoutException

        while (true) {
            try {
                return chain.proceed(chain.request())
            } catch (e: SocketTimeoutException) {
                lastError = e
                if (attempt >= ApiConfig.MAX_RETRY_ATTEMPTS - 1) throw lastError
                Thread.sleep((1L shl attempt) * 1000L)
                attempt++
            }
        }
    }
}
