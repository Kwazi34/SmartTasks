package com.example.smarttasks

data class FirebaseData(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val userId: String? = null // removed trailing comma
)
data class Task(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var dueTime: Long? = null // timestamp for reminder
)
