package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
