package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import com.clockworks.incirkle.Adapters.AddedCourseListAdapter
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.*

class HomeActivity : AppActivity(), NavigationView.OnNavigationItemSelectedListener
{
    private var courses = ArrayList<Course>()
    private var isUserTeacher = false

    private fun getCoursesForUser(user: User)
    {
        this.courses = ArrayList<Course>()
        user.courses.forEach()
        {
            courseReference ->
            this.showLoadingAlert()
            courseReference.get()
                .addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    this.performThrowable { it.serialize(Course::class.java) }?.let()
                    {
                        this.courses.add(it)
                        courses_list_view.adapter = AddedCourseListAdapter(this, this.courses)
                    }
                }
                .addOnCompleteListener { this.dismissLoadingAlert() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().currentUser?.let()
        {
            this.showLoadingAlert()
            it.documentReference().get()
                .addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    snapshot ->
                    if (snapshot.data.isNullOrEmpty())
                    {
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        finish()
                    }
                    else
                    {
                        this.performThrowable { snapshot.serialize(User::class.java) }?.let()
                        { user ->
                            textView_user_name.text = user.fullName()
                            textView_user_id.text = user.userID()
                            this.isUserTeacher = user.type == User.Type.TEACHER
                            this.invalidateOptionsMenu()

                            if (user.courses.isEmpty())
                            {
                                this.startActivity(Intent(this, SelectOrganisationActivity::class.java))
                                finish()
                            }
                            else
                                this.getCoursesForUser(user)
                        }
                    }
                }
                .addOnCompleteListener { this.dismissLoadingAlert() }
        }
        ?: run()
        {
           this.startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        courses_list_view.setOnItemClickListener()
        {
            _, _, position, _ ->

            val course = this.courses[position]
            val intent = Intent(this, CourseFeedActivity::class.java)
            intent.putExtra(CourseFeedActivity.IDENTIFIER_COURSE_PATH, course.reference!!.path)
            intent.putExtra(CourseFeedActivity.IDENTIFIER_COURSE_TEACHER_PATH, course.teacher.path)
            startActivity(intent)
        }
    }

    override fun onBackPressed()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.home, menu)
        menu.findItem(R.id.home_action_enrol_course).setTitle(if (this.isUserTeacher) R.string.text_enrol_create_course else R.string.title_activity_enrolCourse)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.home_action_enrol_course ->
            {
                this.startActivity(Intent(this, SelectOrganisationActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        // Handle navigation view item clicks here.
        when (item.itemId)
        {
            R.id.nav_account -> this.startActivity(Intent(this, UserProfileActivity::class.java))
            R.id.nav_logout ->
            {
                FirebaseAuth.getInstance().signOut()
                this.startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
