package com.gammadesv.todo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gammadesv.todo.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Logger
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val database by lazy {
        Firebase.database("https://todo-30caa-default-rtdb.firebaseio.com/")
            .reference.child("tasks").also {
                Log.d("FirebaseInit", "Database reference initialized")
            }
    }
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de logging para Firebase
        Firebase.database.setLogLevel(Logger.Level.DEBUG)
        Log.d("FirebaseInit", "Initializing Firebase connection")

        setupBinding()
        setupRecyclerView()
        setupUiListeners()
        setupDataListener()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(emptyList()) { updatedTask ->
            Log.d("FirebaseUpdate", "Updating task: ${updatedTask.id}")
            database.child(updatedTask.id).setValue(updatedTask)
                .addOnFailureListener { e ->
                    showError("Error al actualizar la tarea", e)
                }
        }

        with(binding.recyclerViewTasks) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupUiListeners() {
        binding.apply {
            buttonAdd.setOnClickListener {
                Log.d("UIEvent", "Add button clicked")
                handleAddTask()
            }
        }
    }

    private fun setupDataListener() {
        Log.d("FirebaseListener", "Setting up data listener")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseData", "Data changed, snapshot: $snapshot")
                val tasks = snapshot.children.mapNotNull {
                    it.getValue(Task::class.java)?.also { task ->
                        Log.v("FirebaseData", "Loaded task: ${task.id} - ${task.title}")
                    }
                }
                adapter.updateTasks(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Error cargando tareas", error.toException())
                Log.e("FirebaseError", "Database error: ${error.message}", error.toException())
            }
        })
    }

    private fun handleAddTask() {
        val taskTitle = binding.editTextTask.text.toString().trim()
        Log.d("TaskInput", "Handling new task: '$taskTitle'")

        if (taskTitle.isEmpty()) {
            binding.editTextTask.error = getString(R.string.error_empty_task)
            Log.w("TaskInput", "Empty task not allowed")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        Log.d("TaskProgress", "Showing progress bar")

        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = taskTitle,
            isCompleted = false
        )

        Log.d("FirebaseWrite", "Writing new task to Firebase: ${newTask.id}")
        database.child(newTask.id).setValue(newTask)
            .addOnSuccessListener {
                Log.i("FirebaseWrite", "Task successfully written")
                binding.editTextTask.text?.clear()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                showError("Error al agregar tarea", e)
                Log.e("FirebaseWrite", "Failed to write task", e)
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun showError(message: String, exception: Exception? = null) {
        runOnUiThread {
            val errorMsg = if (exception != null) {
                "$message: ${exception.localizedMessage ?: "Error desconocido"}"
            } else {
                message
            }

            Log.e("AppError", errorMsg, exception)
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            binding.textError.apply {
                text = errorMsg
                visibility = View.VISIBLE
            }
        }
    }
}