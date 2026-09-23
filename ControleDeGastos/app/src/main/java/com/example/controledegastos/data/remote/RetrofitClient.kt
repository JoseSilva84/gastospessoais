package com.example.controledegastos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val CLOUD_BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val CURRENCY_BASE_URL = "https://economia.awesomeapi.com.br/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val expenseApi: ExpenseRemoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CLOUD_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseRemoteApiService::class.java)
    }

    val currencyApi: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CURRENCY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }
}
