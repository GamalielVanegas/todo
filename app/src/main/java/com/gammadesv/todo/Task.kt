package com.gammadesv.todo

data class Task(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false
)