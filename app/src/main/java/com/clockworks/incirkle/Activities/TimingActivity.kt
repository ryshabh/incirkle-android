package com.clockworks.incirkle.Activities

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.activity_timing.*

class TimingActivity : AppCompatActivity()
{
    companion object
    {
        val REQUEST_CODE = 4
        val IDENTIFIER_TIMING = "Timing"
        val IDENTIFIER_TIMING_INDEX = "Timing Index"
    }

    private lateinit var timing: Course.Timing
    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)

        this.timing = (intent.getSerializableExtra(IDENTIFIER_TIMING) as? Course.Timing)?.
            let { it } ?: run() { Course.Timing() }
        this.index = intent.getIntExtra(IDENTIFIER_TIMING_INDEX, -1)

        // Initialize Views
        spinner_day.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Course.Timing.Day.values().map { it.toString() })
        spinner_day.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                this@TimingActivity.timing.day = Course.Timing.Day.values()[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?)
            {

            }
        }
        spinner_day.setSelection(Course.Timing.Day.values().indexOf(this.timing.day), true)
        button_startTime.text = this.timing.startTime.toString()
        button_endTime.text = this.timing.endTime.toString()
        textView_duration.text = this.timing.duration()
    }

    private fun timePickerDialog(context: Context, time: Course.Timing.Time, onChange: (Course.Timing.Time) -> Unit) : TimePickerDialog
    {
        val listener = TimePickerDialog.OnTimeSetListener()
        {
            timePicker, hour, minute ->
            try
            {
                onChange(Course.Timing.Time(hour, minute))
            }
            catch (e: Exception)
            {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                timePicker.hour = time.hour
                timePicker.minute = time.minute
            }
        }
        val dialog = TimePickerDialog(context, listener, time.hour, time.minute, false)
        return dialog
    }

    fun selectStartTime(v: View)
    {
        this.timePickerDialog(this, this.timing.startTime)
        {
            if (Course.Timing.checkTimeValidity(it, this.timing.endTime))
            {
                this.timing.startTime = it
                button_startTime.text = this.timing.startTime.toString()
                textView_duration.text = this.timing.duration()
            }
        }.show()
    }

    fun selectEndTime(v: View)
    {
        this.timePickerDialog(this, this.timing.endTime)
        {
            if (Course.Timing.checkTimeValidity(this.timing.startTime, it))
            {
                this.timing.endTime = it
                button_endTime.text = this.timing.endTime.toString()
                textView_duration.text = this.timing.duration()
            }
        }.show()
    }

    fun done(v: View)
    {
        val intent = Intent()
        Log.d("Time on done", this.timing.toString())
        intent.putExtra(IDENTIFIER_TIMING, this.timing)
        intent.putExtra(IDENTIFIER_TIMING_INDEX, this.index)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
