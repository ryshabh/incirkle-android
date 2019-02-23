package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.design.widget.TabLayout

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.clockworks.incirkle.Fragments.CourseActivitiesFragment
import com.clockworks.incirkle.Fragments.CourseDocumentsFragment
import com.clockworks.incirkle.Fragments.CourseForumFragment
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.documentReference

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
    }

    private lateinit var courseReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_feed)
        setSupportActionBar(toolbar)

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs_courseFeed))
        tabs_courseFeed.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        this.courseReference = FirebaseFirestore.getInstance().document(intent.getStringExtra(IDENTIFIER_COURSE_PATH))
        FirebaseAuth.getInstance().currentUser?.let()
        {
            user ->
            this.showLoadingAlert()
            this.courseReference.get()
                .addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    this.performThrowable { it.serialize(Course::class.java) }?.let()
                    { container.adapter = SectionsPagerAdapter(supportFragmentManager, this.courseReference, user.documentReference() == it.teacher) }
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

    inner class SectionsPagerAdapter(fm: FragmentManager, val courseReference: DocumentReference, val isAdmin: Boolean) : FragmentPagerAdapter(fm)
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
                    bundle.putBoolean(CourseActivitiesFragment.IDENTIFIER_IS_ADMIN, isAdmin)
                    fragment.arguments = bundle
                    return fragment
                }
                1 ->
                {
                    val fragment = CourseForumFragment()
                    val bundle = Bundle()
                    bundle.putString(CourseForumFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putBoolean(CourseForumFragment.IDENTIFIER_IS_ADMIN, isAdmin)
                    fragment.arguments = bundle
                    return fragment
                }
                2 ->
                {
                    val fragment = CourseDocumentsFragment()
                    val bundle = Bundle()
                    bundle.putString(CourseDocumentsFragment.IDENTIFIER_COURSE_PATH, courseReference.path)
                    bundle.putBoolean(CourseDocumentsFragment.IDENTIFIER_IS_ADMIN, isAdmin)
                    fragment.arguments = bundle
                    return fragment
                }
                else ->
                {
                    return Fragment()
                }
            }
        }

        override fun getCount(): Int
        {
            return 4
        }
    }
}
