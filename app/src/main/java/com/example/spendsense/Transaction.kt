package com.example.spendsense

data class Transaction(
    val id: Long,
    val type: String,        // "expense" or "income"
    val amount: Double,
    val category: String,
    val categoryIcon: String,
    val note: String,
    val timestamp: Long
)
