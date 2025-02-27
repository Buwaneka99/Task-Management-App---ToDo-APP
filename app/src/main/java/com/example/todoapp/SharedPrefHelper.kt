package com.example.todoapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TodoApp", Context.MODE_PRIVATE)

    fun saveTasks(taskList: List<Task>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(taskList)
        editor.putString("taskList", json)
        editor.apply()
    }

    fun loadTasks(): MutableList<Task> {
        val gson = Gson()
        val json = sharedPreferences.getString("taskList", null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}
