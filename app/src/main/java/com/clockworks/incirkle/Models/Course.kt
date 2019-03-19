package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.model.value.IntegerValue
import java.io.Serializable
import java.lang.Exception
import kotlin.math.sign

class Course(): FirebaseDocument
{
    class Timing(): Serializable
    {
        enum class Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY; }

        class Time(): Serializable
        {
            var hour: Int = 0
            var minute: Int = 0

            constructor(hour: Int, minute: Int) : this()
            {
                this.hour = hour
                this.minute = minute
            }

            fun totalMinutes() : Int
            {
                return (this.hour * 60) + this.minute
            }

            operator fun minus(b: Time) : Time
            {
                val totalDuration = this.totalMinutes() - b.totalMinutes()
                return Time(totalDuration.div(60), totalDuration.rem(60))
            }

            override fun toString(): String
            {
                val minutes = "${if (minute.div(10) == 0) "0" else ""}$minute"
                if (this.hour < 12) // AM
                    return "${if (hour == 0) 12 else hour}:$minutes am"
                else // PM
                    return "${if (hour == 12) hour else (hour - 12)}:$minutes pm"
            }
        }

        companion object: Serializable
        {
            val InvalidTimeException = Exception("Start Time cannot be before End Time")
            fun checkTimeValidity(startTime: Time, endTime: Time) : Boolean
            {
                if (endTime.minus(startTime).totalMinutes().sign == -1)
                    throw Timing.InvalidTimeException
                else
                    return true
            }
        }

        var day: Day = Day.values().first()
        var startTime = Time(8, 0)
        var endTime = Time(9,0)

        fun duration() : String
        {
            val durationTime = (this.endTime - this.startTime)
            return "${durationTime.hour} Hour(s), ${durationTime.minute} Minute(s)"
        }

        fun timePeriod() : String
        {
            return "${this.startTime} to ${this.endTime}"
        }

        override fun toString(): String
        {
            return "${this.day}, ${this.timePeriod()}"
        }
    }

    lateinit var password: String
    lateinit var name: String
    lateinit var code: String
    lateinit var teacher: DocumentReference

     var activitypostsize : Int = 0
     var forumpostsize : Int = 0
     var assignmentpostsize : Int = 0
     var documentpostsize : Int = 0

    var timings = ArrayList<Timing>()
    var teachingAssistants = ArrayList<String>()
    var invitedStudents = ArrayList<String>()

    @Exclude
    override var reference: DocumentReference? = null

    constructor(password: String, teacher: DocumentReference): this()
    {
        this.password = password
        this.name = ""
        this.code = ""
        this.teacher = teacher
    }
}