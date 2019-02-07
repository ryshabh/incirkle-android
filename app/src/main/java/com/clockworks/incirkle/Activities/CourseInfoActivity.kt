package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_course_info.*


class CourseInfoActivity : AppCompatActivity()
{
    companion object
    {
        val IDENTIFIER_COURSES_PATH = "Courses Path"
        val IDENTIFIER_COURSE_ID = "Course ID"
    }

    enum class PRIVILEGE { FULL, SEMI, NONE; }

    private lateinit var coursesCollectionReference: CollectionReference
    private var courseID: String? = null
    private lateinit var course: Course
    private var privilege = PRIVILEGE.NONE

    private fun updateCourse(course: Course, user: User)
    {
        this.course = course
        textView_coursePassword.setText(course.password)
        textView_courseCode.setText(course.code)
        textView_courseName.setText(course.name)

        // Set Privilege
        this.privilege =
                if (course.teacher == user.documentReference) PRIVILEGE.FULL
                else if (course.teachingAssistants.contains(user.documentReference)) PRIVILEGE.SEMI
                else PRIVILEGE.NONE

        // Refresh Views
        textView_courseCode.isEnabled = this.privilege == PRIVILEGE.FULL
        textView_courseName.isEnabled = this.privilege == PRIVILEGE.FULL

        // Refresh Menu Items
        this.invalidateOptionsMenu()
    }

    private fun fetchCourse()
    {
        FirebaseAuth.getInstance().currentUser?.currentUserData()
        {
            userData, exception ->

            exception?.let() { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
            ?: userData?.let()
            {
                user ->

                this.courseID?.let()
                {
                    courseID ->

                    this.coursesCollectionReference.document(courseID).get().addOnCompleteListener()
                    {
                        task ->

                        task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                        ?:task.result?.let()
                        {
                            val course = it.toObject(Course::class.java)!!
                            course.reference = it.reference
                            this.updateCourse(course, user)
                        }
                    }

                }?: run()
                {
                    // Create New Course
                    var newPassword = (0..9999).random().toString()
                    while (newPassword.length < 4)
                        newPassword = "0$newPassword"
                    this.updateCourse(Course(newPassword, user.documentReference!!), user)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_info)
        this.coursesCollectionReference = FirebaseFirestore.getInstance().collection(this.intent.getStringExtra(IDENTIFIER_COURSES_PATH)!!)
        this.courseID = this.intent.getStringExtra(IDENTIFIER_COURSE_ID)
        this.fetchCourse()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.activity_course, menu)
        if (this.privilege == PRIVILEGE.NONE)
            return false
        else if (this.privilege == PRIVILEGE.SEMI || (this.privilege == PRIVILEGE.FULL && !this.intent.hasExtra(IDENTIFIER_COURSE_ID)))
            menu.removeItem(R.id.item_teachingAssistants)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.item_teachingAssistants->
            {
                val intent = Intent(this, TeachingAssistantsActivity::class.java)
                intent.putExtra(TeachingAssistantsActivity.IDENTIFIER_CAN_MODIFY, this.privilege == PRIVILEGE.FULL)
                intent.putExtra(TeachingAssistantsActivity.IDENTIFIER_COURSE_PATH, this.course.reference!!.path)
                intent.putExtra(TeachingAssistantsActivity.IDENTIFIER_TEACHING_ASSISTANTS, this.course.teachingAssistants.map { it.path }.toTypedArray())
                startActivityForResult(intent, TeachingAssistantsActivity.REQUEST_CODE)
                return true
            }
            R.id.item_inviteStudents->
            {
                val intent = Intent(this, InviteStudentsActivity::class.java)
                intent.putExtra(InviteStudentsActivity.IDENTIFIER_INVITED_STUDENTS, this.course.invitedStudents)
                startActivityForResult(intent, InviteStudentsActivity.REQUEST_CODE)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == TeachingAssistantsActivity.REQUEST_CODE)
        {
            data?.getStringArrayExtra(TeachingAssistantsActivity.IDENTIFIER_TEACHING_ASSISTANTS)?.let()
            {
                this.course.teachingAssistants = ArrayList(it.map() { FirebaseFirestore.getInstance().document(it) })
            }
        }
        else if (requestCode == InviteStudentsActivity.REQUEST_CODE)
        {
            (data?.getSerializableExtra(InviteStudentsActivity.IDENTIFIER_INVITED_STUDENTS) as? HashMap<String, String>)?.let()
            {
                this.course.invitedStudents = it
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data)
    }

    fun finish(v: View)
    {
        val courseCode = textView_courseCode.text.toString().trim()
        val courseName = textView_courseName.text.toString().trim()

        textView_courseCode.error = null
        textView_courseName.error = null

        if (courseCode.isBlank())
            textView_courseCode.error = "Course Code is empty"
        else if (courseName.isBlank())
            textView_courseName.error = "Course Name is empty"
        else
        {
            this.course.code = courseCode
            this.course.name = courseName

            val courseDocument = this.courseID?.let() { this.coursesCollectionReference.document(it) }
            ?: run() { this.coursesCollectionReference.document() }
            courseDocument.set(this.course).addOnCompleteListener()
            {
                task ->

                task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                ?: run()
                {

                    FirebaseAuth.getInstance().currentUser?.currentUserData()
                    {
                        user, exception ->

                        exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                        ?: run()
                        {
                            user?.courses?.add(courseDocument)
                            user?.documentReference?.set(user)?.addOnCompleteListener()
                            {
                                it.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                                    ?: run()
                                    {
                                        val intent = Intent(this, HomeActivity::class.java)
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}
