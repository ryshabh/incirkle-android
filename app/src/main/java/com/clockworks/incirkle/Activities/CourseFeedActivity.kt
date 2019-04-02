package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.clockworks.incirkle.Fragments.*
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_course_feed.*


class CourseFeedActivity : AppActivity()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"
        const val IDENTIFIER_IS_USER_TEACHER = "IS_USER_TEACHER"
    }

    private lateinit var courseReference: DocumentReference

    private var isUserTeacher = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_feed)
        setSupportActionBar(toolbar)

        isUserTeacher = intent.getBooleanExtra(IDENTIFIER_IS_USER_TEACHER, false);

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs_courseFeed))
        tabs_courseFeed.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))



        this.courseReference = FirebaseFirestore.getInstance().document(intent.getStringExtra(IDENTIFIER_COURSE_PATH))
        FirebaseAuth.getInstance().currentUser?.let()
        { user ->
            this.showLoadingAlert()
            this.courseReference.get()
                .addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    this.performThrowable { it.serialize(Course::class.java) }?.let()


                    {
                        try
                        {
                            supportActionBar!!.title = it.code + " : " + it.name
                        } catch (e: Exception)
                        {
                        }
//                        user.documentReference() == it.teacher
                        Log.d("User Phone no", "" + user.phoneNumber);
                        var isTeacher = isUserTeacher || it.teachingAssistants.contains(user.phoneNumber)
                        var isTeachingAssistant = it.teachingAssistants.contains(user.phoneNumber)
                        var sectionPagerAdapter = SectionsPagerAdapter(
                            supportFragmentManager,
                            this.courseReference,
                            isTeacher,
                            isTeachingAssistant
                        )
                        container.adapter = sectionPagerAdapter
                        val limit = if (sectionPagerAdapter.getCount() > 1) sectionPagerAdapter.getCount() - 1 else 1
                        container.offscreenPageLimit = limit

                    }
                }
                .addOnCompleteListener { this.dismissLoadingAlert() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.menu_course_feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val id = item.itemId

        if (id == R.id.action_course_info)
        {
            val intent = Intent(this, CourseInfoActivity::class.java)
            intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSES_PATH, this.courseReference.parent.path)
            intent.putExtra(CourseInfoActivity.IDENTIFIER_COURSE_ID, this.courseReference.id)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(
        fm: FragmentManager,
        val courseReference: DocumentReference,
        val isTeacher: Boolean,
        val isTeachingAssistant: Boolean
    ) : FragmentPagerAdapter(fm)
    {
        override fun getItem(position: Int): Fragment
        {
            when (position)
            {
                0 ->
                {
                    val fragment = CourseActivitiesFragment()
                    val bundle = Bundle()
                    bundle.putString(CourseActivitiesFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putBoolean(CourseActivitiesFragment.IDENTIFIER_IS_TEACHER, isTeacher)
                    bundle.putBoolean(CourseActivitiesFragment.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)
                    fragment.arguments = bundle
                    return fragment
                }
                1 ->
                {
                    val fragment = CourseForumFragment()
                    val bundle = Bundle()
                    bundle.putString(CourseForumFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putString(
                        CourseForumFragment.IDENTIFIER_COURSE_TEACHER_PATH, intent.getStringExtra(
                            IDENTIFIER_COURSE_TEACHER_PATH
                        )
                    )
                    bundle.putBoolean(CourseForumFragment.IDENTIFIER_IS_TEACHER, isTeacher)
                    bundle.putBoolean(CourseForumFragment.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)

                    fragment.arguments = bundle
                    return fragment
                }
                2 ->
                {
                    val fragment = CourseDocumentsFragment()
                    val bundle = Bundle()
                    bundle.putString(CourseDocumentsFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putBoolean(CourseDocumentsFragment.IDENTIFIER_IS_TEACHER, isTeacher)
                    bundle.putBoolean(CourseDocumentsFragment.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)
                    fragment.arguments = bundle
                    return fragment
                }
                else ->
                {
                    val fragment = AssignmentFragment()
                    val bundle = Bundle()
                    bundle.putString(AssignmentFragment.IDENTIFIER_COURSE_DOCUMENT_ID, courseReference.id)
                    bundle.putString(AssignmentFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putString(
                        AssignmentFragment.IDENTIFIER_COURSE_TEACHER_PATH, intent.getStringExtra(
                            IDENTIFIER_COURSE_TEACHER_PATH
                        )
                    )
                    bundle.putBoolean(AssignmentFragment.IDENTIFIER_IS_TEACHER, isTeacher)
                    bundle.putBoolean(AssignmentFragment.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)
                    fragment.arguments = bundle
                    return fragment
                }
            }
        }

        override fun getCount(): Int
        {
            return 4
        }
    }
}
