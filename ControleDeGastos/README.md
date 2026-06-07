# 💸 Controle de Gastos

Aplicativo Android para controle de gastos pessoais, desenvolvido com as tecnologias mais modernas do ecossistema Android.

---

## 📱 Sobre o Projeto

O **Controle de Gastos** é um app nativo Android que permite ao usuário registrar, visualizar e excluir suas despesas do dia a dia de forma simples e organizada. Os dados são salvos localmente no dispositivo, dispensando qualquer cadastro ou conexão com a internet.

---

## ✨ Funcionalidades

- ✅ **Adicionar despesas** com descrição, valor e categoria
- 📋 **Listar todas as despesas** em ordem cronológica (mais recentes primeiro)
- 🗑️ **Excluir despesas** com um toque
- 💰 **Resumo do total gasto** exibido na tela principal
- 💾 **Persistência local** — os dados ficam salvos mesmo após fechar o app

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| **Kotlin** | Linguagem principal do projeto |
| **Jetpack Compose** | Framework moderno para construção de interfaces declarativas |
| **Material Design 3** | Sistema de design do Google aplicado aos componentes visuais |
| **Room (2.8.4)** | Banco de dados local SQLite com camada de abstração |
| **KSP** | Processamento de anotações moderno (substitui o KAPT) |
| **Kotlin Coroutines** | Programação assíncrona e reativa |
| **Kotlin Flow** | Fluxo de dados reativo para atualização da UI em tempo real |
| **Navigation 3** | Navegação entre telas com a nova API do AndroidX |
| **ViewModel** | Gerenciamento do estado da UI seguindo o padrão MVVM |
| **Android Gradle Plugin 9.0** | Sistema de build mais recente |

---

## 🏗️ Arquitetura

O projeto segue o padrão de arquitetura **MVVM (Model-View-ViewModel)**, recomendado pelo Google para apps Android modernos.

```
app/
└── src/main/java/com/example/controledegastos/
    ├── data/
    │   ├── local/
    │   │   ├── AppDatabase.kt      # Banco de dados Room
    │   │   ├── Expense.kt          # Entidade (modelo de dados)
    │   │   └── ExpenseDao.kt       # Operações no banco (CRUD)
    │   └── DataRepository.kt       # Repositório de dados
    ├── ui/
    │   ├── screens/
    │   │   ├── ExpenseListScreen.kt  # Tela principal com lista de gastos
    │   │   └── ExpenseAddScreen.kt   # Tela para adicionar novo gasto
    │   ├── viewmodel/
    │   │   └── ExpenseViewModel.kt   # Lógica de negócio e estado da UI
    │   └── main/
    │       └── MainScreen.kt         # Tela raiz com navegação
    ├── Navigation.kt                 # Configuração das rotas de navegação
    ├── NavigationKeys.kt             # Chaves de navegação tipadas
    └── MainActivity.kt               # Ponto de entrada do aplicativo
```

---

## 🗃️ Modelo de Dados

Cada despesa registrada contém os seguintes campos:

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Int | Identificador único (gerado automaticamente) |
| `description` | String | Descrição da despesa |
| `value` | Double | Valor em reais |
| `category` | String | Categoria da despesa (ex: Alimentação, Transporte) |
| `timestamp` | Long | Data e hora do registro |

---

## 🚀 Como Executar

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) (versão mais recente)
- JDK 17 ou superior
- Dispositivo Android com **API 24** (Android 7.0) ou superior — ou emulador

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/JoseSilva84/gastospessoais.git
   ```

2. **Abra o projeto no Android Studio:**
   - Vá em `File > Open` e selecione a pasta `ControleDeGastos`

3. **Sincronize o Gradle:**
   - Clique em **"Sync Now"** na barra amarela que aparecer

4. **Execute o app:**
   - Conecte seu dispositivo Android via USB (com Modo Desenvolvedor ativado) ou inicie um emulador
   - Clique no botão **▶ Run** (triângulo verde)

---

## 📋 Requisitos Mínimos

- **Android:** 7.0 Nougat (API 24) ou superior
- **Armazenamento:** ~5 MB
- **Permissões:** Nenhuma permissão especial necessária

---

## 📄 Licença

Este projeto foi desenvolvido para fins de aprendizado.
