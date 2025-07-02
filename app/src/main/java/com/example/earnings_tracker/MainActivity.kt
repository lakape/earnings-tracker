package com.example.earnings_tracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var wageInput: EditText
    private lateinit var startStopButton: Button
    private lateinit var resetButton: Button
    private lateinit var timerDisplay: TextView
    private lateinit var earningsDisplay: TextView
    private lateinit var hourlyWageDisplay: TextView

    private var isTimerRunning = false
    private var startTime: Long = 0
    private var totalElapsedTime: Long = 0
    private var hourlyWage: Double = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                updateTimer()
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        wageInput = findViewById(R.id.wageInput)
        startStopButton = findViewById(R.id.startStopButton)
        resetButton = findViewById(R.id.resetButton)
        timerDisplay = findViewById(R.id.timerDisplay)
        earningsDisplay = findViewById(R.id.earningsDisplay)
        hourlyWageDisplay = findViewById(R.id.hourlyWageDisplay)
    }

    private fun setupClickListeners() {
        startStopButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        val wageText = wageInput.text.toString().trim()

        if (wageText.isEmpty()) {
            Toast.makeText(this, "Please enter your hourly wage", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            hourlyWage = wageText.toDouble()
            if (hourlyWage <= 0) {
                Toast.makeText(this, "Hourly wage must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid hourly wage", Toast.LENGTH_SHORT).show()
            return
        }

        isTimerRunning = true
        startTime = System.currentTimeMillis()
        startStopButton.text = "STOP"
        wageInput.isEnabled = false

        hourlyWageDisplay.text = "Hourly Wage: ${DecimalFormat("#0.00").format(hourlyWage)}€"

        handler.post(updateRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        totalElapsedTime += System.currentTimeMillis() - startTime
        startStopButton.text = "START"
        handler.removeCallbacks(updateRunnable)
    }

    private fun resetTimer() {
        isTimerRunning = false
        totalElapsedTime = 0
        startTime = 0
        hourlyWage = 0.0

        startStopButton.text = "START"
        wageInput.isEnabled = true
        wageInput.text.clear()
        timerDisplay.text = "00:00:00"
        earningsDisplay.text = "0.00€"
        hourlyWageDisplay.text = "Hourly Wage: --"

        handler.removeCallbacks(updateRunnable)
    }

    private fun updateTimer() {
        val currentElapsedTime = if (isTimerRunning) {
            totalElapsedTime + (System.currentTimeMillis() - startTime)
        } else {
            totalElapsedTime
        }

        // Format time display
        val seconds = (currentElapsedTime / 1000) % 60
        val minutes = (currentElapsedTime / (1000 * 60)) % 60
        val hours = (currentElapsedTime / (1000 * 60 * 60))

        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        timerDisplay.text = timeString

        // Calculate earnings
        val hoursWorked = currentElapsedTime / (1000.0 * 60.0 * 60.0)
        val totalEarnings = hourlyWage * hoursWorked

        val earningsString = DecimalFormat("#0.00").format(totalEarnings) + "€"
        earningsDisplay.text = earningsString
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
