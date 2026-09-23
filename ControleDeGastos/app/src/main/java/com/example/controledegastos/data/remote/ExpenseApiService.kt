package com.example.controledegastos.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * DTO para sincronização de gastos na nuvem via API REST (JSONPlaceholder).
 */
data class RemoteExpenseDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("userId") val userId: Int = 1
)

/**
 * DTOs para cotações de moedas em tempo real (AwesomeAPI Economia).
 */
data class CurrencyResponseDto(
    @SerializedName("USDBRL") val usdBrl: CurrencyQuoteDto? = null,
    @SerializedName("EURBRL") val eurBrl: CurrencyQuoteDto? = null
)

data class CurrencyQuoteDto(
    @SerializedName("code") val code: String = "",
    @SerializedName("codein") val codeIn: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("bid") val bid: String = "5.00",
    @SerializedName("pctChange") val pctChange: String = "0.0",
    @SerializedName("create_date") val createDate: String = ""
)

/**
 * Serviço Retrofit com as 4 requisições exigidas no Projeto Final:
 * GET, POST, PUT e DELETE.
 */
interface ExpenseRemoteApiService {

    @GET("posts")
    suspend fun getRemoteExpenses(): List<RemoteExpenseDto>

    @POST("posts")
    suspend fun createRemoteExpense(@Body expense: RemoteExpenseDto): RemoteExpenseDto

    @PUT("posts/{id}")
    suspend fun updateRemoteExpense(
        @Path("id") id: Int,
        @Body expense: RemoteExpenseDto
    ): RemoteExpenseDto

    @DELETE("posts/{id}")
    suspend fun deleteRemoteExpense(@Path("id") id: Int): Response<Unit>
}

/**
 * Serviço Retrofit para buscar a cotação do Dólar (USD) e Euro (EUR) em tempo real via GET.
 */
interface CurrencyApiService {

    @GET("json/last/USD-BRL,EUR-BRL")
    suspend fun getExchangeRates(): CurrencyResponseDto
}
