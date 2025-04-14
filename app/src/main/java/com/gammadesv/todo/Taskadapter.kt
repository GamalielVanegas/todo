package com.gammadesv.todo

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTask: TextView = view.findViewById(R.id.textViewTask)
        val checkBoxTask: CheckBox = view.findViewById(R.id.checkBoxTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.textViewTask.text = task.title
        holder.checkBoxTask.isChecked = task.isCompleted

        holder.checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
            onTaskClick(task.copy(isCompleted = isChecked))
        }
    }

    override fun getItemCount() = tasks.size
}