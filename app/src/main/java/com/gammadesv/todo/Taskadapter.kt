package com.gammadesv.todo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: List<Task>,  // Cambiado a List inmutable
    private val onTaskUpdated: (Task) -> Unit  // Nombre más descriptivo
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewTask)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxTask)

        // Limpia listeners para evitar fugas de memoria
        fun clearListeners() {
            checkBox.setOnCheckedChangeListener(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Limpiar listeners anteriores
        holder.clearListeners()

        // Configurar vista
        holder.textView.text = task.title
        holder.checkBox.isChecked = task.isCompleted

        // Configurar listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskUpdated(task.copy(isCompleted = isChecked))
        }
    }

    override fun getItemCount(): Int = tasks.size

    // Función para actualizar la lista completa
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    // Función para obtener tarea por posición
    fun getTaskAt(position: Int): Task? {
        return if (position in 0 until itemCount) tasks[position] else null
    }
}