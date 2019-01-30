package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login_email.*

class LoginEmailActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_email)
        setSupportActionBar(toolbar)
    }

    private fun showError(message: String, editText: EditText)
    {
        editText.error = message
        editText.requestFocus()
    }

    fun login(v: View)
    {
        // Reset errors.
        textView_email_address.error = null
        textView_password.error = null

        // Store values at the time of the login attempt.
        val emailAddress = textView_email_address.text.toString()
        val password = textView_password.text.toString()

        if (TextUtils.isEmpty(emailAddress))
            this.showError("No Email Address entered", textView_email_address)
        else if (TextUtils.isEmpty(password))
            this.showError("No Password entered", textView_password)
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches())
            this.showError("Invalid Email Address", textView_email_address)
        else
        {
            progressBar_sign_in.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener()
            {
                    task ->

                progressBar_sign_in.visibility = View.INVISIBLE
                if (task.isSuccessful)
                {
                    val homeActivityIntent = Intent(this, HomeActivity::class.java)
                    homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(homeActivityIntent)
                }
                else
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}
