package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.clockworks.incirkle.Adapters.AvailableCourseListAdapter
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.Organisation
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_course.*

class AvailableCoursesActivity : AppCompatActivity()
{
    companion object
    {
        val IDENTIFIER_SELECTED_ORGANISATION= "Selected Organisation"
    }

    var showMenu = false
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
        val courseIDEditText = EditText(this)
        courseIDEditText.inputType = InputType.TYPE_CLASS_NUMBER
        courseIDEditText.filters = arrayOf(InputFilter.LengthFilter(4))
        courseIDEditText.setPadding(10, 10, 10, 10)

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Add ${course.name}")
        alertBuilder.setMessage("Enter Course ID")
        alertBuilder.setView(courseIDEditText)
        alertBuilder.setPositiveButton("Add", null)
        alertBuilder.setNeutralButton("Cancel", null)

        val alert = alertBuilder.create()

        courseIDEditText.addTextChangedListener(object : TextWatcher
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

                courseIDEditText.error = null

                val courseIDString = courseIDEditText.text.toString().trim()
                courseIDString.toIntOrNull()?.let()
                {
                    courseID ->

                    if (courseIDString.equals(course.id, false))
                    {
                        dialogInterface.dismiss()
                        val intent = Intent(this, CourseActivity::class.java)
                        intent.putExtra(CourseActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
                        intent.putExtra(CourseActivity.IDENTIFIER_COURSE_ID, courseID)
                        startActivity(intent)
                    }
                    else
                        courseIDEditText.error = "Incorrect Course ID"
                }
                ?: run()
                {
                    courseIDEditText.error = "Course ID must be numeric"
                }
            }
        }

        alert.show()
    }

    private fun fetchAvailableCoursesForUser(user: User)
    {
        val organisationID = intent.getStringExtra(AvailableCoursesActivity.IDENTIFIER_SELECTED_ORGANISATION)
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

                        snapshot.toObject(Course::class.java)?.let() { courses.add(it) }
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
        setContentView(R.layout.activity_add_course)

        FirebaseAuth.getInstance().currentUser?.currentUserData()
        {
                user, exception ->

            exception?.let()
            {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
                ?: user?.let()
            {
                this.showMenu = it.type == User.Type.TEACHER
                this.invalidateOptionsMenu()
                this.fetchAvailableCoursesForUser(user)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.activity_add_course, menu)
        return this.showMenu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.item_create_course ->
            {
                val intent = Intent(this, CourseActivity::class.java)
                intent.putExtra(CourseActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
