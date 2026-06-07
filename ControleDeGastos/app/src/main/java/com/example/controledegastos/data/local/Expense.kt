package com.example.controledegastos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val value: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
