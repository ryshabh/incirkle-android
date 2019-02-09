package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.clockworks.incirkle.Adapters.AvailableCourseListAdapter
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.Organisation
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_enrol_course.*

class EnrolCourseActivity : AppCompatActivity()
{
    companion object
    {
        val IDENTIFIER_SELECTED_ORGANISATION= "Selected Organisation"
    }

    var availableCourses = ArrayList<Course>()

    fun coursesPath(): String
    {
        return Organisation.reference
            .document(this.intent.getStringExtra(IDENTIFIER_SELECTED_ORGANISATION)!!)
            .collection("Courses")
            .path
    }

    fun showAddAlertForCourse(course: Course)
    {
        val coursePasswordEditText = EditText(this)
        coursePasswordEditText.inputType = InputType.TYPE_CLASS_NUMBER
        coursePasswordEditText.filters = arrayOf(InputFilter.LengthFilter(4))
        coursePasswordEditText.setPadding(10, 10, 10, 10)

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Add ${course.name}")
        alertBuilder.setMessage("Enter Course Password")
        alertBuilder.setView(coursePasswordEditText)
        alertBuilder.setPositiveButton("Add", null)
        alertBuilder.setNeutralButton("Cancel", null)

        val alert = alertBuilder.create()

        coursePasswordEditText.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = p0?.length == 4
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
        
        alert.setOnShowListener()
        {
            dialogInterface ->
            alert.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener()
            {
                button ->

                coursePasswordEditText.error = null

                val courseIDString = coursePasswordEditText.text.toString().trim()
                courseIDString.toIntOrNull()?.let()
                {
                    courseID ->

                    if (courseIDString.equals(course.password, false))
                    {
                        dialogInterface.dismiss()
                        val intent = Intent(this, CourseInfoActivity::class.java)
                        intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
                        intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSE_ID, course.reference!!.id)
                        startActivity(intent)
                    }
                    else
                        coursePasswordEditText.error = "Incorrect Course Password"
                }
                ?: run()
                {
                    coursePasswordEditText.error = "Course Password must be numeric"
                }
            }
        }

        alert.show()
    }

    private fun fetchAvailableCoursesForUser(user: User)
    {
        val organisationID = intent.getStringExtra(EnrolCourseActivity.IDENTIFIER_SELECTED_ORGANISATION)
        Organisation.reference.document(organisationID).collection("Courses").get().addOnCompleteListener()
        {
                task ->

            val courses = ArrayList<Course>()
            task.exception?.let()
            {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
                ?: task.result?.documents?.let()
                {
                    documents ->

                    documents.forEach()
                    {
                        snapshot ->

                        if (user.courses.contains(snapshot.reference))
                            return@forEach

                        snapshot.toObject(Course::class.java)?.let()
                        {

                            if (it.teacher != user.documentReference)
                            {
                                it.reference = snapshot.reference
                                courses.add(it)
                            }
                        }
                        ?: run()
                        {
                            Toast.makeText(this, "Could not deserialize Course", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            this.availableCourses = courses
            listView_available_courses.adapter = AvailableCourseListAdapter(this, this.availableCourses)
        }

        listView_available_courses.setOnItemClickListener()
        {
            adapterView, view, position, id ->
            this.showAddAlertForCourse(this.availableCourses[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrol_course)
        supportActionBar?.let { it.title = "Available Courses" }

        FirebaseAuth.getInstance().currentUser?.currentUserData()
        {
                user, exception ->

            exception?.let()
            {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
                ?: user?.let()
            {
                this.button_create_course.visibility = if (it.type == User.Type.TEACHER) View.VISIBLE else View.GONE
                this.fetchAvailableCoursesForUser(it)
            }
        }
    }

    fun createCourse(v: View)
    {
        val intent = Intent(this, CourseInfoActivity::class.java)
        intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
        startActivity(intent)
    }
}
