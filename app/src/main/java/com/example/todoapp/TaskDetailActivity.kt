package com.example.todoapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.databinding.ActivityTaskDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private var startTime = 0L
    private var timeInMilliseconds = 0L
    private var timeBuffer = 0L
    private var updateTimeHandler: Handler? = null
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val task = intent.getParcelableExtra<Task>("task")

        binding.tvCategory.text = task?.category
        binding.tvTitle.text = task?.title
        binding.tvDescription.text = task?.description
        binding.tvDate.text = task?.date
        binding.tvTime.text = task?.time
        binding.tvReminderTime.text = task?.remindertime
        binding.tvStatus.text = if (task?.isCompleted == true) "Completed" else "Pending"

        updateTimeHandler = Handler()

        setupAlarm(task)

        binding.btnStartStopwatch.setOnClickListener {
            if (!isRunning) {
                startTime = SystemClock.uptimeMillis()
                updateTimeHandler?.postDelayed(updateTimerThread, 0)
                isRunning = true
                binding.btnStartStopwatch.text = "Running..."
            }
        }

        binding.btnStopStopwatch.setOnClickListener {
            if (isRunning) {
                timeBuffer += timeInMilliseconds
                updateTimeHandler?.removeCallbacks(updateTimerThread)
                isRunning = false
                binding.btnStartStopwatch.text = "Start"
            }
        }

        binding.btnResetStopwatch.setOnClickListener {
            timeBuffer = 0L
            timeInMilliseconds = 0L
            binding.tvStopwatch.text = "00:00"
            binding.btnStartStopwatch.text = "Start"
            updateTimeHandler?.removeCallbacks(updateTimerThread)
            isRunning = false
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun setupAlarm(task: Task?) {
        task?.let {
            val reminderTime = "${task.date} ${task.remindertime}"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val reminderDate = sdf.parse(reminderTime)

            reminderDate?.let { date ->
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java).apply {
                    putExtra("taskTitle", it.title)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    it.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Updated flags
                )

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
            }
        }
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            val timeInSeconds = (timeInMilliseconds + timeBuffer) / 1000
            val seconds = (timeInSeconds % 60)
            val minutes = (timeInSeconds / 60)

            binding.tvStopwatch.text = String.format("%02d:%02d", minutes, seconds)

            updateTimeHandler?.postDelayed(this, 1000)
        }
    }
}
