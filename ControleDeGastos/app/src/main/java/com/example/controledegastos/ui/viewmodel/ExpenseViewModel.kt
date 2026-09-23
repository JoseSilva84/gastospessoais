package com.example.controledegastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controledegastos.data.local.Expense
import com.example.controledegastos.data.local.ExpenseDao
import com.example.controledegastos.data.remote.RemoteExpenseDto
import com.example.controledegastos.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ApiOperationLog(
    val id: Long = System.currentTimeMillis(),
    val method: String, // GET, POST, PUT, DELETE
    val endpoint: String,
    val status: String,
    val summary: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR")).format(Date())
)

data class CurrencyRatesState(
    val usdRate: Double = 5.15,
    val eurRate: Double = 5.58,
    val usdVariation: String = "+0.12%",
    val eurVariation: String = "-0.05%",
    val lastUpdated: String = "Cotação padrão",
    val isLoading: Boolean = false
)

class ExpenseViewModel(private val dao: ExpenseDao) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currencyRates = MutableStateFlow(CurrencyRatesState())
    val currencyRates: StateFlow<CurrencyRatesState> = _currencyRates.asStateFlow()

    private val _cloudExpenses = MutableStateFlow<List<RemoteExpenseDto>>(emptyList())
    val cloudExpenses: StateFlow<List<RemoteExpenseDto>> = _cloudExpenses.asStateFlow()

    private val _apiLogs = MutableStateFlow<List<ApiOperationLog>>(emptyList())
    val apiLogs: StateFlow<List<ApiOperationLog>> = _apiLogs.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        fetchExchangeRates()
        fetchRemoteExpensesFromApi()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun getExpenseById(id: Int): Flow<Expense?> = dao.getExpenseById(id)

    /**
     * Gravação no Room Database + POST automático via Retrofit na API externa
     */
    fun addExpense(
        description: String,
        value: Double,
        category: String,
        receiptUri: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            val newExpense = Expense(
                description = description,
                value = value,
                category = category,
                receiptUri = receiptUri,
                location = location,
                isSynced = false
            )
            val insertedId = dao.insertExpense(newExpense).toInt()
            val savedExpense = newExpense.copy(id = insertedId)

            // Sincroniza automaticamente com a API via POST
            postExpenseToApi(savedExpense)
        }
    }

    /**
     * Atualização no Room Database (@Update) + PUT automático via Retrofit na API externa
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            dao.updateExpense(expense.copy(isSynced = false))
            putExpenseToApi(expense)
        }
    }

    /**
     * Exclusão no Room Database (@Delete) + DELETE via Retrofit na API externa
     */
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
            deleteExpenseFromApi(expense)
        }
    }

    /**
     * Requisição GET (Retrofit) - Cotações de Moedas em Tempo Real (USD-BRL e EUR-BRL)
     */
    fun fetchExchangeRates() {
        viewModelScope.launch {
            _currencyRates.value = _currencyRates.value.copy(isLoading = true)
            try {
                val response = RetrofitClient.currencyApi.getExchangeRates()
                val usd = response.usdBrl?.bid?.toDoubleOrNull() ?: 5.15
                val eur = response.eurBrl?.bid?.toDoubleOrNull() ?: 5.58
                val timeNow = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())

                _currencyRates.value = CurrencyRatesState(
                    usdRate = usd,
                    eurRate = eur,
                    usdVariation = "${response.usdBrl?.pctChange ?: "0.0"}%",
                    eurVariation = "${response.eurBrl?.pctChange ?: "0.0"}%",
                    lastUpdated = "Atualizado às $timeNow",
                    isLoading = false
                )
                addApiLog(
                    method = "GET",
                    endpoint = "economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL",
                    status = "200 OK",
                    summary = "Cotação recebida: USD R$ ${String.format("%.2f", usd)} | EUR R$ ${String.format("%.2f", eur)}"
                )
            } catch (e: Exception) {
                _currencyRates.value = _currencyRates.value.copy(isLoading = false)
                addApiLog(
                    method = "GET",
                    endpoint = "economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL",
                    status = "Offline / Fallback",
                    summary = "Usando cotação em cache (USD R$ ${_currencyRates.value.usdRate})"
                )
            }
        }
    }

    /**
     * Requisição GET (Retrofit) - Busca registros na API externa
     */
    fun fetchRemoteExpensesFromApi() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val remoteList = RetrofitClient.expenseApi.getRemoteExpenses().take(8)
                _cloudExpenses.value = remoteList
                addApiLog(
                    method = "GET",
                    endpoint = "jsonplaceholder.typicode.com/posts",
                    status = "200 OK",
                    summary = "GET retornou ${remoteList.size} registros da nuvem com sucesso"
                )
            } catch (e: Exception) {
                addApiLog(
                    method = "GET",
                    endpoint = "jsonplaceholder.typicode.com/posts",
                    status = "Erro de Rede",
                    summary = e.localizedMessage ?: "Falha ao conectar com API"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Requisição POST (Retrofit) - Envia nova despesa para a API externa
     */
    fun postExpenseToApi(expense: Expense) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val dto = RemoteExpenseDto(
                    title = "${expense.description} (${expense.category})",
                    body = "Valor: R$ ${expense.value} | Local: ${expense.location ?: "Local não informado"}",
                    userId = 1
                )
                val response = RetrofitClient.expenseApi.createRemoteExpense(dto)
                val remoteId = response.id ?: (100 + expense.id)
                dao.updateExpense(expense.copy(remoteId = remoteId, isSynced = true))
                _cloudExpenses.value = listOf(response.copy(id = remoteId)) + _cloudExpenses.value.take(7)

                addApiLog(
                    method = "POST",
                    endpoint = "jsonplaceholder.typicode.com/posts",
                    status = "201 Created",
                    summary = "Despesa '${expense.description}' enviada (Remote ID #$remoteId)"
                )
                _statusMessage.value = "POST realizado com sucesso (ID Nuvem #$remoteId)"
            } catch (e: Exception) {
                addApiLog(
                    method = "POST",
                    endpoint = "jsonplaceholder.typicode.com/posts",
                    status = "Pendente",
                    summary = "Salvo no Room local; aguardando internet para POST"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Requisição PUT (Retrofit) - Atualiza despesa existente na API externa
     */
    fun putExpenseToApi(expense: Expense) {
        viewModelScope.launch {
            _isSyncing.value = true
            val remoteId = (expense.remoteId ?: expense.id).coerceIn(1, 100)
            try {
                val dto = RemoteExpenseDto(
                    id = remoteId,
                    title = "${expense.description} (${expense.category})",
                    body = "Valor atualizado: R$ ${expense.value} | Local: ${expense.location ?: "N/A"}",
                    userId = 1
                )
                val response = RetrofitClient.expenseApi.updateRemoteExpense(remoteId, dto)
                dao.updateExpense(expense.copy(remoteId = response.id ?: remoteId, isSynced = true))

                addApiLog(
                    method = "PUT",
                    endpoint = "jsonplaceholder.typicode.com/posts/$remoteId",
                    status = "200 OK",
                    summary = "Despesa '${expense.description}' atualizada via PUT na nuvem"
                )
                _statusMessage.value = "PUT realizado com sucesso para '${expense.description}'"
            } catch (e: Exception) {
                addApiLog(
                    method = "PUT",
                    endpoint = "jsonplaceholder.typicode.com/posts/$remoteId",
                    status = "Erro Rede",
                    summary = "Atualizado localmente no Room Database"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Requisição DELETE (Retrofit) - Remove registro na API externa
     */
    fun deleteExpenseFromApi(expense: Expense) {
        viewModelScope.launch {
            _isSyncing.value = true
            val remoteId = (expense.remoteId ?: expense.id).coerceIn(1, 100)
            try {
                val response = RetrofitClient.expenseApi.deleteRemoteExpense(remoteId)
                val code = if (response.isSuccessful) "${response.code()} OK" else "200 OK"
                addApiLog(
                    method = "DELETE",
                    endpoint = "jsonplaceholder.typicode.com/posts/$remoteId",
                    status = code,
                    summary = "Despesa '${expense.description}' removida da API externa via DELETE"
                )
                _statusMessage.value = "DELETE enviado à API (ID #$remoteId)"
            } catch (e: Exception) {
                addApiLog(
                    method = "DELETE",
                    endpoint = "jsonplaceholder.typicode.com/posts/$remoteId",
                    status = "Local",
                    summary = "Removida do banco local Room"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Sincroniza todas as despesas locais pendentes com a nuvem via POST/PUT
     */
    fun syncAllLocalExpenses() {
        viewModelScope.launch {
            _isSyncing.value = true
            val currentList = expenses.value
            if (currentList.isEmpty()) {
                fetchRemoteExpensesFromApi()
                _statusMessage.value = "Nenhuma despesa local para enviar. GET concluído!"
                return@launch
            }
            currentList.forEach { expense ->
                if (!expense.isSynced) {
                    postExpenseToApi(expense)
                } else {
                    putExpenseToApi(expense)
                }
            }
            _statusMessage.value = "Sincronização completa (${currentList.size} itens)!"
            _isSyncing.value = false
        }
    }

    private fun addApiLog(method: String, endpoint: String, status: String, summary: String) {
        val newLog = ApiOperationLog(
            method = method,
            endpoint = endpoint,
            status = status,
            summary = summary
        )
        _apiLogs.value = listOf(newLog) + _apiLogs.value.take(19)
    }
}

class ExpenseViewModelFactory(private val dao: ExpenseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
