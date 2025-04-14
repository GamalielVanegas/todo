package com.gammadesv.todo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gammadesv.todo.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot  // Importación añadida
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val database by lazy { Firebase.database.reference.child("tasks") }
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial (typo corregido)
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
        adapter = TaskAdapter(emptyList()) { updatedTask: Task ->
            database.child(updatedTask.id).setValue(updatedTask)
                .addOnFailureListener { e ->
                    showError("Error al actualizar la tarea", e)  // Typos corregidos
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
            buttonAdd.setOnClickListener { handleAddTask() }
            // Puedes agregar más listeners aquí si es necesario (typos corregidos)
        }
    }

    private fun setupDataListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {  // DataSnapshot ahora reconocido
                val tasks = snapshot.children.mapNotNull {
                    it.getValue<Task>()  // Usando la extensión KTX
                }
                adapter.updateTasks(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Error loading tasks", error.toException())
            }
        })
    }

    private fun handleAddTask() {
        val taskTitle = binding.editTextTask.text.toString().trim()

        if (taskTitle.isEmpty()) {
            binding.editTextTask.error = "Ingresa una tarea"
            return
        }

        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = taskTitle,
            isCompleted = false
        )

        database.child(newTask.id).setValue(newTask)
            .addOnSuccessListener {
                binding.editTextTask.text?.clear()
            }
            .addOnFailureListener { e ->
                showError("Error al agregar tarea", e)
            }
    }

    private fun showError(message: String, exception: Exception? = null) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("TodoApp", message, exception)
    }
}