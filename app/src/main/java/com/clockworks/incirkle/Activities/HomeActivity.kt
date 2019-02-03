package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.clockworks.incirkle.Adapters.CourseListAdapter
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mAuth: FirebaseAuth
    private var courses = ArrayList<Course>()

    private fun getCoursesForUser(user: User)
    {
        this.courses = ArrayList<Course>()
        user.courses.forEach()
        {
            courseReference ->
            courseReference.get().addOnCompleteListener()
            {
                task ->
                if (task.isSuccessful)
                {
                    task.result?.toObject(Course::class.java)?.let()
                    {
                        course ->
                        this.courses.add(course)
                        courses_list_view.adapter = CourseListAdapter(this, this.courses)
                    }
                    ?: run()
                    {
                        Toast.makeText(this@HomeActivity, "Could not deserialize Course Data", Toast.LENGTH_LONG).show()
                    }
                }
                else
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
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
        this.mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart()
    {
        super.onStart()
        if (this.mAuth.currentUser == null)
            this.startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onResume()
    {
        super.onResume()

        this.mAuth.currentUser?.let()
        {
            firebaseUser ->

            firebaseUser.currentUserData()
            {
                user, exception ->

                exception?.let()
                { e ->
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
                ?: user?.let()
                {
                    userData ->

                    val fullName = userData.firstName + " " + userData.lastName
                    textView_user_name.setText(fullName)
                    val userId = userData.emailAddress.let { it } ?: userData.phoneNumber.let { it }
                    textView_user_id.setText(userId ?: getString(R.string.nav_header_subtitle))

                    // TODO: - Update Courses View

                    if (userData.courses.isEmpty())
                        this.startActivity(Intent(this, SelectOrganisationActivity::class.java))
                    else
                        this.getCoursesForUser(userData)
                }
                ?: run()
                {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                }
            }
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.home_action_add_course ->
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
                this.mAuth.signOut()
                this.startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
