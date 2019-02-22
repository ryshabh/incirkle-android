package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.clockworks.incirkle.Adapters.AvailableCourseListAdapter
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.*
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_enrol_course.*

class EnrolCourseActivity : AppActivity()
{
    companion object
    {
        const val IDENTIFIER_SELECTED_ORGANISATION= "Selected Organisation"
    }

    var availableCourses = ArrayList<Course>()

    private fun coursesPath(): String
    {
        return Organisation.reference
            .document(this.intent.getStringExtra(IDENTIFIER_SELECTED_ORGANISATION)!!)
            .collection("Courses")
            .path
    }

    private fun fetchAvailableCoursesForUser(user: User)
    {
        val organisationID = intent.getStringExtra(EnrolCourseActivity.IDENTIFIER_SELECTED_ORGANISATION)
        Organisation.reference.document(organisationID).collection("Courses").get()
            .addOnFailureListener()
            {
                this.showError(it)
                this.availableCourses = ArrayList<Course>()
                listView_available_courses.adapter = AvailableCourseListAdapter(this, this.availableCourses)
            }
            .addOnSuccessListener()
            {
                val courses = ArrayList<Course>()
                it.documents.forEach()
                {
                    snapshot ->

                    if (user.courses.contains(snapshot.reference))
                        return@forEach

                    this.performThrowable { snapshot.serialize(Course::class.java) }?.let()
                    {
                        if (it.teacher != user.reference)
                        {
                            it.reference = snapshot.reference
                            courses.add(it)
                        }
                    }
                }

                this.availableCourses = courses
                listView_available_courses.adapter = AvailableCourseListAdapter(this, this.availableCourses)
            }

        listView_available_courses.setOnItemClickListener()
        {
            _, _, position, _ ->
            val intent = Intent(this, CourseInfoActivity::class.java)
            intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
            intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSE_ID, this.availableCourses[position].reference!!.id)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrol_course)
        supportActionBar?.let { it.title = "Available Courses" }

        FirebaseAuth.getInstance().currentUser?.let()
        {
            it.documentReference().get()
                .addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    this.performThrowable { it.serialize(User::class.java) }?.let()
                    {
                        this.button_create_course.visibility = if (it.type == User.Type.TEACHER) View.VISIBLE else View.GONE
                        this.fetchAvailableCoursesForUser(it)
                    }
                }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun createCourse(v: View)
    {
        val intent = Intent(this, CourseInfoActivity::class.java)
        intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSES_PATH, this.coursesPath())
        startActivity(intent)
    }
}
