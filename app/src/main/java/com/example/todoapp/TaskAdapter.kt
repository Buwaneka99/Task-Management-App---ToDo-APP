package com.example.todoapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.radioButton.isChecked = task.isCompleted

            if (task.isCompleted) {
                binding.tvTitle.paint.isStrikeThruText = true
                binding.btnEdit.visibility = View.GONE
                binding.tvCountdown.text = "Completed"
            } else {
                binding.tvTitle.paint.isStrikeThruText = false
                binding.btnEdit.visibility = View.VISIBLE

                val remainingTime = getRemainingTime(task)
                binding.tvCountdown.text = remainingTime
            }

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, TaskDetailActivity::class.java)
                intent.putExtra("task", task)
                context.startActivity(intent)
            }

            binding.btnEdit.setOnClickListener { onEditClick(task) }
            binding.btnDelete.setOnClickListener { onDeleteClick(task) }

            binding.radioButton.setOnClickListener {
                task.isCompleted = !task.isCompleted
                notifyItemChanged(adapterPosition)
            }
        }

        private fun getRemainingTime(task: Task): String {
            val currentTime = Calendar.getInstance().timeInMillis
            val taskDueDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${task.date} ${task.time}")?.time ?: return "Expired"

            return if (currentTime > taskDueDate) {
                "Expired"
            } else {
                val remainingMillis = taskDueDate - currentTime
                val days = (remainingMillis / (1000 * 60 * 60 * 24)).toInt()
                val hours = (remainingMillis / (1000 * 60 * 60) % 24).toInt()
                val minutes = (remainingMillis / (1000 * 60) % 60).toInt()
                val seconds = (remainingMillis / 1000 % 60).toInt()

                String.format(
                    "Remaining: %d days, %02d:%02d",
                    days, hours, minutes, seconds
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount() = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    fun updateTasks(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }
}
