package com.jhaiian.kate.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object KateHttp {
    private const val DEFAULT_TIMEOUT_MILLIS = 15_000

    val client: OkHttpClient by lazy { build(OkHttpClient.Builder(), DEFAULT_TIMEOUT_MILLIS) }

    fun client(timeoutMillis: Int): OkHttpClient =
        if (timeoutMillis == DEFAULT_TIMEOUT_MILLIS) client else build(client.newBuilder(), timeoutMillis)

    private fun build(builder: OkHttpClient.Builder, timeoutMillis: Int): OkHttpClient =
        builder
            .connectTimeout(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .build()
}
