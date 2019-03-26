package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.clockworks.incirkle.Adapters.AddedCourseListAdapter
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*





class HomeActivity : AppActivity(), NavigationView.OnNavigationItemSelectedListener
{
    private var courses = ArrayList<Course>()
    private var isUserTeacher = false
    private var listenerRegistrations = ArrayList<ListenerRegistration>()

    private var userListenerRegistration: ListenerRegistration? = null
    lateinit var  addedCourseListAdapter : AddedCourseListAdapter
    private fun autofetchCourses(user: User)
    {
        this.courses = ArrayList()
        this.listenerRegistrations.forEach { it.remove() }
        this.courses.clear()
        this.listenerRegistrations = ArrayList(user.courses.map()
        {
            it.addSnapshotListener()
            {
                result, exception ->
                exception?.let { this.showError(it) }
                ?: result?.let()
                {


                    this.performThrowable { it.serialize(Course::class.java) }?.let()
                    {
                        course ->


                        if(!this.courses.any{ tempcourse -> tempcourse.reference == course.reference })
                        {




                            course.reference!!.collection("Activity Posts").get()
                                .addOnFailureListener(::showError)
                                .addOnSuccessListener()
                                {
                                   course.activitypostsize =  it.size()
                                    if(!addedCourseListAdapter.isEmpty)
                                    {
                                        addedCourseListAdapter.notifyDataSetChanged()
                                    }
                                    Log.d("activitypostsize",course.activitypostsize.toString())
                                }
                                .addOnCompleteListener()
                                {
                                //    Log.d("completed","completed")
                                }
                            course.reference!!.collection("Assignment Posts").get()
                                .addOnFailureListener(::showError)
                                .addOnSuccessListener()
                                {
                                    course.assignmentpostsize =  it.size()
                                    if(!addedCourseListAdapter.isEmpty)
                                    {
                                        addedCourseListAdapter.notifyDataSetChanged()
                                    }
                                    Log.d("assignmentpostsize",course.assignmentpostsize.toString())
                                }
                                .addOnCompleteListener()
                                {
                                 //   Log.d("completed","completed")
                                }
                            course.reference!!.collection("Forum Posts").get()
                                .addOnFailureListener(::showError)
                                .addOnSuccessListener()
                                {
                                    course.forumpostsize =  it.size()
                                    if(!addedCourseListAdapter.isEmpty)
                                    {
                                        addedCourseListAdapter.notifyDataSetChanged()
                                    }
                                    Log.d("forumpostsize",course.forumpostsize.toString())
                                }
                                .addOnCompleteListener()
                                {
                            //        Log.d("completed","completed")
                                }
                            course.reference!!.collection("Document Posts").get()
                                .addOnFailureListener(::showError)
                                .addOnSuccessListener()
                                {
                                    course.documentpostsize =  it.size()
                                    if(!addedCourseListAdapter.isEmpty)
                                    {
                                        addedCourseListAdapter.notifyDataSetChanged()
                                    }

                                    Log.d("documentpostsize",course.documentpostsize.toString())
                                }
                                .addOnCompleteListener()
                                {
                           //         Log.d("completed","completed")
                                }
                            this.courses.add(course)
                            addedCourseListAdapter  = AddedCourseListAdapter(this, this.courses)
                            courses_list_view.adapter = addedCourseListAdapter
                        }

                       /* else
                            existingCourses.forEach { this.courses[this.courses.indexOf(it)] = course }
                        courses_list_view.adapter = AddedCourseListAdapter(this, this.courses)*/
                    }
                }

                Log.d("end","end")
            }
        })
    }

    lateinit var textView_user_name : TextView
    lateinit var textView_user_id : TextView
    lateinit var profileimageview : ImageView
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContentView(com.clockworks.incirkle.R.layout.activity_home)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            com.clockworks.incirkle.R.string.navigation_drawer_open,
            com.clockworks.incirkle.R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        val headerView = nav_view.getHeaderView(0)
        textView_user_name = headerView.findViewById(com.clockworks.incirkle.R.id.textView_user_name) as TextView
        textView_user_id = headerView.findViewById(com.clockworks.incirkle.R.id.textView_user_id) as TextView
        profileimageview = headerView.findViewById(com.clockworks.incirkle.R.id.imageView_diplayPicture) as ImageView


        FirebaseAuth.getInstance().currentUser?.let()
        {
            this.userListenerRegistration = it.documentReference().addSnapshotListener()
            {
                result, exception ->

                exception?.let { this.showError(it) }
                result?.let()
                {
                    if (it.data.isNullOrEmpty())
                    {
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        finish()
                    }
                    else
                    {
                        this.performThrowable { it.serialize(User::class.java) }?.let()
                        { user ->
                            textView_user_name.text = user.fullName()
                            textView_user_id.text = user.userID()
                            this.isUserTeacher = user.type == User.Type.TEACHER
                            this.invalidateOptionsMenu()
                            if (user.profilepic != null)
                            {
                                user.profilepic?.let {

                                    var requestOptions = RequestOptions()
                                    requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))

                                    Glide
                                        .with(this)
                                        .apply { requestOptions }
                                        .load(it)

                                        .into(profileimageview);

                                }
                            }
                            if (user.courses.isEmpty())
                            {
                                this.startActivity(Intent(this, SelectOrganisationActivity::class.java))
                                finish()
                            }
                            else
                            {
                                this.autofetchCourses(user)
                                Log.d("end fetch","end fetch");
                            }
                        }
                    }
                }
            }
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

    override fun onDestroy()
    {
        super.onDestroy()
        this.userListenerRegistration?.remove()
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
        menuInflater.inflate(com.clockworks.incirkle.R.menu.home, menu)
        menu.findItem(com.clockworks.incirkle.R.id.home_action_enrol_course).setTitle(if (this.isUserTeacher) com.clockworks.incirkle.R.string.text_enrol_create_course else com.clockworks.incirkle.R.string.title_activity_enrolCourse)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            com.clockworks.incirkle.R.id.home_action_enrol_course ->
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
            com.clockworks.incirkle.R.id.nav_account -> this.startActivity(Intent(this, UserProfileActivity::class.java))
            com.clockworks.incirkle.R.id.nav_logout ->
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
