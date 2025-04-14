package com.gammadesv.todo

import androidx.appcompat.app.AppCompatActivity  // 👈 Esta línea debe estar al inicio

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.widget.Toast
import java.util.UUID
import androidx.recyclerview.widget.LinearLayoutManager
import com.gammadesv.todo.databinding.ActivityMainBinding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gammadesv.todo.ui.theme.TodoTheme
//import androidx.appcompat.app.AppCompatActivity  // 👈 Esta línea debe estar al inicio

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding  // Declara el binding
    private lateinit var database: DatabaseReference
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)  // Infla el binding
        setContentView(binding.root)  // Usa la vista raíz del binding

        // Configura Firebase
        database = FirebaseDatabase.getInstance().reference.child("tasks")

        // RecyclerView (ahora accedido mediante binding)
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks) { updatedTask ->
            database.child(updatedTask.id).setValue(updatedTask)
        }
        binding.recyclerViewTasks.adapter = adapter

        // Botón y EditText (con binding)
        binding.buttonAdd.setOnClickListener {
            val taskTitle = binding.editTextTask.text.toString()
            if (taskTitle.isNotEmpty()) {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = taskTitle
                )
                database.child(newTask.id).setValue(newTask)
                binding.editTextTask.text.clear()
            }
        }

        // Escucha de Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tasks.clear()
                snapshot.children.forEach { child ->
                    val task = child.getValue(Task::class.java)
                    task?.let { tasks.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}