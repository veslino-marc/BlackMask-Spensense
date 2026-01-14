package com.example.spendsense

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class TransactionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_TRANSACTIONS = "transactions"
    }

    fun saveTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        transactions.add(0, transaction) // Add to beginning (newest first)
        saveAllTransactions(transactions)
    }

    fun getAllTransactions(): List<Transaction> {
        val jsonString = prefs.getString(KEY_TRANSACTIONS, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val transactions = mutableListOf<Transaction>()
        
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            transactions.add(
                Transaction(
                    id = obj.getLong("id"),
                    type = obj.getString("type"),
                    amount = obj.getDouble("amount"),
                    category = obj.getString("category"),
                    categoryIcon = obj.getString("categoryIcon"),
                    note = obj.getString("note"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }
        
        return transactions
    }

    fun getRecentTransactions(limit: Int = 5): List<Transaction> {
        return getAllTransactions().take(limit)
    }

    fun getExpenses(): List<Transaction> {
        return getAllTransactions().filter { it.type == "expense" }
    }

    fun getIncome(): List<Transaction> {
        return getAllTransactions().filter { it.type == "income" }
    }

    fun getTotalExpenses(): Double {
        return getExpenses().sumOf { it.amount }
    }

    fun getTotalIncome(): Double {
        return getIncome().sumOf { it.amount }
    }

    fun deleteTransaction(id: Long) {
        val transactions = getAllTransactions().filter { it.id != id }
        saveAllTransactions(transactions)
    }

    fun clearAllTransactions() {
        prefs.edit().remove(KEY_TRANSACTIONS).apply()
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val jsonArray = JSONArray()
        
        for (transaction in transactions) {
            val obj = JSONObject().apply {
                put("id", transaction.id)
                put("type", transaction.type)
                put("amount", transaction.amount)
                put("category", transaction.category)
                put("categoryIcon", transaction.categoryIcon)
                put("note", transaction.note)
                put("timestamp", transaction.timestamp)
            }
            jsonArray.put(obj)
        }
        
        prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).apply()
    }

    fun generateId(): Long {
        return System.currentTimeMillis()
    }
}
