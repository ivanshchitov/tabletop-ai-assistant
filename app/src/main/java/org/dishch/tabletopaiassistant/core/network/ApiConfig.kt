package org.dishch.tabletopaiassistant.core.network

import org.dishch.tabletopaiassistant.BuildConfig

object ApiConfig {
    const val BASE_URL = "https://opencode.ai/zen/v1/"
    const val MODEL = "deepseek-v4-flash"
    const val TEMPERATURE = 0.7
    const val MAX_TOKENS = 1000
    const val REQUEST_TIMEOUT_SECONDS = 30L
    const val MAX_RETRY_ATTEMPTS = 3

    /** Read from `OPENCODE_API_KEY` in local.properties at build time — see app/build.gradle.kts. */
    val API_KEY: String = BuildConfig.OPENCODE_API_KEY
}
