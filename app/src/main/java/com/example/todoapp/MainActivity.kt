package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.graphics.drawable.GradientDrawable
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private var taskList: MutableList<Task> = mutableListOf()
    private var displayedTaskList: MutableList<Task> = mutableListOf()
    private var selectedCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefHelper = SharedPrefHelper(this)
        taskList = sharedPrefHelper.loadTasks()
        displayedTaskList = taskList.toMutableList()

        taskAdapter = TaskAdapter(displayedTaskList, ::editTask, ::deleteTask)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        binding.fab.setOnClickListener {
            showTaskDialog(null)
        }

        setupCategoryListeners()
    }

    private fun setupCategoryListeners() {
        val categoryViews = listOf(
            binding.headerLayout.findViewById<TextView>(R.id.tvAll),
            binding.headerLayout.findViewById<TextView>(R.id.tvWork),
            binding.headerLayout.findViewById<TextView>(R.id.tvPersonal),
            binding.headerLayout.findViewById<TextView>(R.id.tvWishlist),
            binding.headerLayout.findViewById<TextView>(R.id.tvBirthday),
            binding.headerLayout.findViewById<TextView>(R.id.tvOthers)
        )

        categoryViews.forEach { view ->
            view.setOnClickListener {
                selectCategory(view)
            }
        }
    }

    private fun selectCategory(view: TextView) {
        resetCategoryStyles()
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = (12 * resources.displayMetrics.density)
            setColor(Color.parseColor("#53b6fa"))
        }
        view.background = drawable
        view.setTextColor(Color.WHITE)
        selectedCategory = view.text.toString()
        filterTasks()
    }

    private fun resetCategoryStyles() {
        val categoryViews = listOf(
            binding.headerLayout.findViewById<TextView>(R.id.tvAll),
            binding.headerLayout.findViewById<TextView>(R.id.tvWork),
            binding.headerLayout.findViewById<TextView>(R.id.tvPersonal),
            binding.headerLayout.findViewById<TextView>(R.id.tvWishlist),
            binding.headerLayout.findViewById<TextView>(R.id.tvBirthday),
            binding.headerLayout.findViewById<TextView>(R.id.tvOthers)
        )

        categoryViews.forEach { view ->
            view.setBackgroundResource(R.drawable.category_background)
            (view as TextView).setTextColor(Color.parseColor("#727272"))
        }
    }

    private fun filterTasks() {
        displayedTaskList.clear()
        displayedTaskList.addAll(filterTasksByCategory(selectedCategory))
        taskAdapter.notifyDataSetChanged()
    }

    private fun filterTasksByCategory(category: String): List<Task> {
        return if (category == "All") {
            taskList
        } else {
            taskList.filter { it.category == category }
        }
    }

    private fun showTaskDialog(task: Task?) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        bottomSheetDialog.setContentView(dialogView)

        val titleInput: EditText = dialogView.findViewById(R.id.titleInput)
        val descriptionInput: EditText = dialogView.findViewById(R.id.descriptionInput)
        val dateInput: EditText = dialogView.findViewById(R.id.dateInput)
        val timeInput: EditText = dialogView.findViewById(R.id.timeInput)
        val reminderTimeInput: EditText = dialogView.findViewById(R.id.remindertimeInput)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner)

        val categories = arrayOf("Work", "Personal", "Wishlist", "Birthday", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        titleInput.setText(task?.title ?: "")
        descriptionInput.setText(task?.description ?: "")
        dateInput.setText(task?.date ?: "")
        timeInput.setText(task?.time ?: "")
        reminderTimeInput.setText(task?.remindertime ?: "")
        categorySpinner.setSelection(categories.indexOf(task?.category ?: "Others"))

        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                dateInput.setText("$year-${month + 1}-$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        timeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                timeInput.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePickerDialog.show()
        }

        // Added click listener for reminder time input
        reminderTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                reminderTimeInput.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePickerDialog.show()
        }

        dialogView.findViewById<android.widget.Button>(R.id.saveButton).setOnClickListener {
            val taskTitle = titleInput.text.toString()
            val taskDescription = descriptionInput.text.toString()
            val taskDate = dateInput.text.toString()
            val taskTime = timeInput.text.toString()
            val taskReminderTime = reminderTimeInput.text.toString()
            val taskCategory = categorySpinner.selectedItem.toString()

            if (taskTitle.isNotEmpty() && taskDescription.isNotEmpty()) {
                if (task == null) {
                    val newTask = Task(0, taskTitle, taskDescription, taskDate, taskTime, taskReminderTime, category = taskCategory)
                    addTask(newTask)
                } else {
                    val updatedTask = task.copy(
                        title = taskTitle,
                        description = taskDescription,
                        date = taskDate,
                        time = taskTime,
                        remindertime = taskReminderTime,
                        category = taskCategory
                    )
                    editExistingTask(task, updatedTask)
                }
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun addTask(newTask: Task) {
        taskList.add(newTask)
        filterTasks()
        sharedPrefHelper.saveTasks(taskList)
    }

    private fun editExistingTask(oldTask: Task, newTask: Task) {
        val position = taskList.indexOf(oldTask)
        taskList[position] = newTask
        filterTasks()
        sharedPrefHelper.saveTasks(taskList)
    }

    private fun editTask(task: Task) {
        showTaskDialog(task)
    }

    private fun deleteTask(task: Task) {
        taskList.remove(task)
        filterTasks()
        sharedPrefHelper.saveTasks(taskList)
    }
}