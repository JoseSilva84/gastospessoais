# 💸 Controle de Gastos Pessoais — Projeto Final (Módulo Avançado Android)

Aplicativo Android desenvolvido com **Jetpack Compose**, **Navigation Compose**, **Room Database**, **Retrofit (GET, POST, PUT, DELETE)**, **Recursos Nativos do Dispositivo (Câmera, Galeria, GPS, Arquivos)** e **Notificações Locais**, atendendo integralmente aos requisitos obrigatórios e opcionais do **Projeto Final — Módulo Avançado - Turma Android (Capacita iRede / MCTI Futuro)**.

---

## ✅ Checklist de Atendimento aos Requisitos do PDF

### 1. Requisitos Obrigatórios

| Requisito | Como foi implementado no Projeto | Arquivos Principais |
|---|---|---|
| **1. Interface Gráfica (Jetpack Compose)**<br>• Mínimo de 3 telas distintas<br>• Navegação utilizando Navigation Compose<br>• Componentes Compose e organização visual | O app possui **4 telas distintas** integradas com `NavHost` e `NavigationBar` (Bottom Bar) do **Navigation Compose**:<br>1. **Meus Gastos (`ExpenseListScreen`)**: Dashboard com total em BRL/USD/EUR, filtro por categorias e lista de despesas.<br>2. **Adicionar / Editar Gasto (`ExpenseAddScreen`)**: Formulário com seletor de categorias, captura de foto/GPS e validação.<br>3. **Detalhes da Despesa (`ExpenseDetailScreen`)**: Visualização detalhada, foto do comprovante, conversão cambial e ações de sincronização.<br>4. **API Retrofit & Cotações (`CloudSyncScreen`)**: Painel de cotações em tempo real e console de sincronização HTTP. | `Navigation.kt`<br>`ExpenseListScreen.kt`<br>`ExpenseAddScreen.kt`<br>`ExpenseDetailScreen.kt`<br>`CloudSyncScreen.kt` |
| **2. Persistência Local (Room Database)**<br>• Pelo menos 1 entidade<br>• Pelo menos 1 DAO<br>• Operações de leitura e gravação | Banco SQLite local gerenciado pelo **Room Database**:<br>• Entidade `@Entity(tableName = "expenses") data class Expense`<br>• Interface `@Dao interface ExpenseDao` com `@Query` (Flow reativo), `@Insert`, `@Update` e `@Delete`. | `Expense.kt`<br>`ExpenseDao.kt`<br>`AppDatabase.kt` |
| **3. Consumo de API (Retrofit)**<br>• Uso do Retrofit para comunicação externa<br>• Pelo menos duas requisições entre `GET`, `POST`, `PUT`, `DELETE` | Implementadas **todas as 4 requisições (`@GET`, `@POST`, `@PUT`, `@DELETE`)** via **Retrofit 2 + Gson**:<br>• **`ExpenseRemoteApiService`** (`JSONPlaceholder REST API`): `@GET("posts")`, `@POST("posts")`, `@PUT("posts/{id}")` e `@DELETE("posts/{id}")`.<br>• **`CurrencyApiService`** (`AwesomeAPI Economia`): `@GET("json/last/USD-BRL,EUR-BRL")` para cotação do Dólar e Euro em tempo real. | `ExpenseApiService.kt`<br>`RetrofitClient.kt`<br>`ExpenseViewModel.kt` |

---

### 2. Requisitos Opcionais (Implementados)

| Requisito Opcional | Como foi implementado no Projeto | Arquivos Principais |
|---|---|---|
| **1. Recurso Nativo do Dispositivo** | Foram integrados **4 recursos nativos** do Android:<br>• 📷 **Câmera Nativa (`TakePicture` + `FileProvider`)**: Captura de foto do comprovante/nota fiscal.<br>• 🖼️ **Galeria de Fotos (`GetContent`)**: Anexo de imagem da galeria do aparelho.<br>• 📍 **Geolocalização (`LocationManager` / GPS)**: Registro das coordenadas/endereço onde a despesa foi realizada.<br>• 📁 **Arquivos do Dispositivo (`Exportação CSV`)**: Geração e compartilhamento de planilha `.csv` com os gastos. | `DeviceResourceHelper.kt`<br>`ExpenseAddScreen.kt`<br>`ExpenseListScreen.kt` |
| **2. Notificações Locais** | Implementado canal de notificações (`NotificationChannel` + `NotificationCompat`):<br>• **Aviso de item salvo/atualizado** disparado automaticamente ao salvar uma despesa.<br>• **Resumo Financeiro Diário** acionável na barra superior.<br>• **Alerta de Sincronização com a Nuvem**. | `NotificationHelper.kt` |

---

## 🏗️ Arquitetura do Projeto (MVVM)

```
app/src/main/java/com/example/controledegastos/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Configuração do Room Database (SQLite)
│   │   ├── Expense.kt              # Entidade Room (@Entity)
│   │   └── ExpenseDao.kt           # DAO Room (@Query, @Insert, @Update, @Delete)
│   └── remote/
│       ├── ExpenseApiService.kt    # Interfaces Retrofit (@GET, @POST, @PUT, @DELETE)
│       └── RetrofitClient.kt       # Cliente HTTP Retrofit + Gson + OkHttp Logging
├── ui/
│   ├── screens/
│   │   ├── ExpenseListScreen.kt    # Tela 1: Lista, Resumo em R$/USD/EUR e Filtros
│   │   ├── ExpenseAddScreen.kt     # Tela 2: Cadastro/Edição + Câmera, Galeria e GPS
│   │   ├── ExpenseDetailScreen.kt  # Tela 3: Detalhes da Despesa e Comprovante
│   │   └── CloudSyncScreen.kt      # Tela 4: Painel API Retrofit e Cotações
│   └── viewmodel/
│       └── ExpenseViewModel.kt     # Estado reativo (StateFlow) integrando Room + Retrofit
├── util/
│   ├── DeviceResourceHelper.kt     # Câmera, Galeria, GPS e Exportação de Arquivo CSV
│   └── NotificationHelper.kt       # Notificações Locais do Android
├── Navigation.kt                   # Rotas Navigation Compose + Bottom Navigation Bar
└── MainActivity.kt                 # Activity principal
```

---

## 🚀 Como Executar

1. Abra a pasta `ControleDeGastos` no **Android Studio**.
2. Aguarde a sincronização do **Gradle** (`Sync Now`).
3. Execute o aplicativo em um emulador ou dispositivo físico Android (**API 24+**).
