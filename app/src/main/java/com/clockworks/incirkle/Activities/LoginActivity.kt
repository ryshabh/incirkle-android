package com.clockworks.incirkle.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.clockworks.incirkle.R

class LoginActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun phoneLogin(view: View)
    {
        startActivity(Intent(this, LoginPhoneActivity::class.java))
    }

    fun emailLogin(view: View)
    {
        startActivity(Intent(this, LoginEmailActivity::class.java))
    }
}
