package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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
    private var isCourseEnrolled = false

    private fun updateCourse(course: Course, user: User)
    {
        this.course = course
        textView_coursePassword.setText(course.password)
        textView_courseCode.setText(course.code)
        textView_courseName.setText(course.name)

        // Set Privilege
        this.privilege =
                if (course.teacher == user.documentReference) PRIVILEGE.FULL
                else if (course.teachingAssistants.contains(user.userID())) PRIVILEGE.SEMI
                else PRIVILEGE.NONE

        // Refresh Views
        linearLayout_coursePassword.visibility = if (this.privilege == PRIVILEGE.FULL) View.VISIBLE else View.GONE
        textView_courseCode.isEnabled = this.privilege == PRIVILEGE.FULL
        textView_courseName.isEnabled = this.privilege == PRIVILEGE.FULL
        linearLayout_teacherName.visibility = if (this.privilege == PRIVILEGE.FULL) View.GONE else View.VISIBLE
        button_teachingAssistant.visibility = if (this.privilege == PRIVILEGE.NONE) View.GONE else View.VISIBLE
        button_inviteStudents.visibility = if (this.privilege == PRIVILEGE.NONE) View.GONE else View.VISIBLE
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
                            // Update Button
                            this.isCourseEnrolled = user.courses.contains(it.reference)
                            button_finish.setText(if (this.isCourseEnrolled) R.string.text_finish else R.string.title_activity_enrolCourse)

                            val course = it.toObject(Course::class.java)!!
                            course.reference = it.reference
                            course.teacher.get().addOnCompleteListener()
                            {
                                it.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                                    ?:it.result?.let()
                                    {
                                        it.toObject(User::class.java)?.let { textView_teacherName.text = it.fullName() }
                                    }
                            }
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

    private fun saveUser(user: User, courseReference: DocumentReference)
    {
        fun goHome()
        {
            val intent = Intent(this, HomeActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        user.documentReference?.set(user)?.addOnCompleteListener()
        {
            it.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                ?: run()
                {

                    // Update all Invited Students & Teachers
                    val allUsers = (this.course.teachingAssistants + this.course.invitedStudents)

                    if (allUsers.isEmpty())
                        goHome()
                    else
                        User.iterate(ArrayList(allUsers))
                        {
                            index, key, task ->

                            task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                            task.result?.forEach()
                            {
                                it.toObject(User::class.java).let()
                                {
                                    if (!it.courses.contains(courseReference))
                                        it.courses.add(courseReference)
                                }
                            }
                            if (index == (allUsers.size - 1))
                                goHome()
                        }
                }
        }
    }

    private fun saveCourse()
    {
        val courseReference = this.courseID?.let() { this.coursesCollectionReference.document(it) }
            ?: run()
            { this.coursesCollectionReference.document() }
        courseReference.set(this.course).addOnCompleteListener()
        {
                task ->

            task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                ?: run()
            {
                FirebaseAuth.getInstance().currentUser?.currentUserData()
                {
                        user, exception ->
                    exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                    ?: user?.let()
                    {
                        if (this.courseID == null)
                            it.courses.add(courseReference)
                        this.saveUser(it, courseReference)
                    }
                }
            }
        }
    }

    private fun showEnrolmentAlert()
    {
        val coursePasswordEditText = EditText(this)
        coursePasswordEditText.inputType = InputType.TYPE_CLASS_NUMBER
        coursePasswordEditText.filters = arrayOf(InputFilter.LengthFilter(4))
        coursePasswordEditText.setPadding(10, 10, 10, 10)

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Enrol ${this.course.name}")
        alertBuilder.setMessage("Enter Course Password")
        alertBuilder.setView(coursePasswordEditText)
        alertBuilder.setPositiveButton("Enrol", null)
        alertBuilder.setNeutralButton("Cancel", null)

        val alert = alertBuilder.create()

        coursePasswordEditText.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).isEnabled = p0?.length == 4
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
            alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).isEnabled = false
            (dialogInterface as android.support.v7.app.AlertDialog).getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener()
            {
                button ->

                coursePasswordEditText.error = null
                val courseIDString = coursePasswordEditText.text.toString().trim()
                courseIDString.toIntOrNull()?.let()
                {
                    courseID ->
                    if (courseIDString.equals(course.password, false))
                    {
                        saveCourse()
                        dialogInterface.dismiss()
                    }
                    else
                        coursePasswordEditText.error = "Incorrect Course Password"
                }
                ?: run() { coursePasswordEditText.error = "Course Password must be numeric" }
            }
        }
        alert.show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_info)
        supportActionBar?.let { it.title = getString(R.string.title_activity_courseInfo) }

        this.coursesCollectionReference = FirebaseFirestore.getInstance().collection(this.intent.getStringExtra(IDENTIFIER_COURSES_PATH)!!)
        this.courseID = this.intent.getStringExtra(IDENTIFIER_COURSE_ID)
        this.fetchCourse()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == TimingsActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            (data?.getSerializableExtra(TimingsActivity.IDENTIFIER_TIMINGS) as? ArrayList<Course.Timing>)?.
                let { this.course.timings = it }
        }
        else if (requestCode == TeachingAssistantsActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(TeachingAssistantsActivity.IDENTIFIER_TEACHING_ASSISTANTS)?.
                let { this.course.teachingAssistants = it }
        }
        else if (requestCode == InviteStudentsActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(InviteStudentsActivity.IDENTIFIER_INVITED_STUDENTS)?.
                let { this.course.invitedStudents = it }
        }
        else
            super.onActivityResult(requestCode, resultCode, data)
    }

    fun timings(v: View)
    {
        val intent = Intent(this, TimingsActivity::class.java)
        intent.putExtra(TimingsActivity.IDENTIFIER_CAN_MODIFY, this.privilege == PRIVILEGE.FULL)
        intent.putExtra(TimingsActivity.IDENTIFIER_TIMINGS, this.course.timings)
        startActivityForResult(intent, TimingsActivity.REQUEST_CODE)
    }

    fun teachingAssistants(v: View)
    {
        val intent = Intent(this, TeachingAssistantsActivity::class.java)
        intent.putExtra(TeachingAssistantsActivity.IDENTIFIER_CAN_MODIFY, this.privilege == PRIVILEGE.FULL)
        intent.putExtra(TeachingAssistantsActivity.IDENTIFIER_TEACHING_ASSISTANTS, this.course.teachingAssistants)
        startActivityForResult(intent, TeachingAssistantsActivity.REQUEST_CODE)
    }

    fun inviteStudents(v: View)
    {
        val intent = Intent(this, InviteStudentsActivity::class.java)
        intent.putExtra(InviteStudentsActivity.IDENTIFIER_INVITED_STUDENTS, this.course.invitedStudents)
        startActivityForResult(intent, InviteStudentsActivity.REQUEST_CODE)
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

            if (this.isCourseEnrolled)
                saveCourse()
            else
                showEnrolmentAlert()
        }
    }
}
