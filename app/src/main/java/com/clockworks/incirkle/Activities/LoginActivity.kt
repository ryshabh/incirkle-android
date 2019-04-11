package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.clockworks.incirkle.R

class LoginActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.let { it.title = getString(R.string.app_name) }
    }

    fun phoneLogin(view: View)
    {
        startActivity(Intent(this, LoginPhoneActivity::class.java))
    }

    fun emailLogin(view: View)
    {
        startActivity(Intent(this, LoginEmailActivity::class.java))
    }

    fun signupForTeacher(view: View)
    {
        startActivity(Intent(this, LoginPhoneActivity::class.java).putExtra(LoginPhoneActivity.FROM_SIGNUP,true).putExtra(LoginPhoneActivity.IS_TEACHER,true))
    }

    fun signupForStudent(view: View)
    {
        startActivity(Intent(this, LoginPhoneActivity::class.java).putExtra(LoginPhoneActivity.FROM_SIGNUP,true).putExtra(LoginPhoneActivity.IS_TEACHER,false))
    }
}
