package com.gammadesv.todo

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

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase
        database = Firebase.database.reference.child("tasks")

        // Configurar RecyclerView
        adapter = TaskAdapter(tasks) { updatedTask ->
            database.child(updatedTask.id).setValue(updatedTask)
        }
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = adapter

        // Botón para agregar tarea
        buttonAdd.setOnClickListener {
            val taskTitle = editTextTask.text.toString()
            if (taskTitle.isNotEmpty()) {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = taskTitle
                )
                database.child(newTask.id).setValue(newTask)
                editTextTask.text.clear()
            }
        }

        // Escuchar cambios en Firebase
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