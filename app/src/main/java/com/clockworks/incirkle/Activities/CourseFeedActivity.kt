package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.clockworks.incirkle.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_course_feed.*

class CourseFeedActivity : AppCompatActivity()
{
    companion object
    {
        val IDENTIFIER_COURSE_PATH = "Course Path"
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
        container.adapter = SectionsPagerAdapter(supportFragmentManager, this.courseReference)
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


    inner class SectionsPagerAdapter(fm: FragmentManager, courseReference: DocumentReference) : FragmentPagerAdapter(fm)
    {
        override fun getItem(position: Int): Fragment
        {
            return Fragment()
        }

        override fun getCount(): Int
        {
            return 4
        }
    }
}
