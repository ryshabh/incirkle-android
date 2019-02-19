package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.clockworks.incirkle.R
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login_phone.*
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.editText_phone_number.filters = arrayOf(InputFilter.LengthFilter(10))
        this.editText_phone_number.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                this@LoginPhoneActivity.button_next.isEnabled = p0?.length == 10
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun setProgress(case: Boolean)
    {
        editText_phone_number.isEnabled = !case
        button_next.isEnabled = !case
        progressBar_get_code.visibility = if (case) View.VISIBLE else View.INVISIBLE
    }

    fun getCode(v: View)
    {
        editText_phone_number.error = null

        val number = editText_phone_number.text.trim().toString()
        if (TextUtils.isEmpty(number))
        {
            editText_phone_number.error = "Phone Number is empty"
            editText_phone_number.requestFocus()
        }
        else if (!Patterns.PHONE.matcher(number).matches())
        {
            editText_phone_number.error = "Phone Number is Invalid"
            editText_phone_number.requestFocus()
        }
        else
        {
            this.setProgress(true)
            val countryCode = getString(R.string.phone_country_code)
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "$countryCode$number",
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        this@LoginPhoneActivity.setProgress(false)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener()
                            {
                                    task ->
                                if (task.isSuccessful)
                                {
                                    Toast.makeText(this@LoginPhoneActivity, "Successfully Verified", Toast.LENGTH_LONG).show()
                                }
                                else
                                    Toast.makeText(this@LoginPhoneActivity, task.exception.toString(), Toast.LENGTH_LONG).show()
                            }
                    }

                    override fun onVerificationFailed(e: FirebaseException)
                    {
                        this@LoginPhoneActivity.setProgress(false)
                        Toast.makeText(this@LoginPhoneActivity, e.toString(), Toast.LENGTH_LONG).show()
                    }

                    override fun onCodeSent(
                        verificationId: String?,
                        token: PhoneAuthProvider.ForceResendingToken?
                    ) {
                        this@LoginPhoneActivity.setProgress(false)
                        verificationId?.let()
                        { code ->
                            val phoneVerificationIntent =
                                Intent(this@LoginPhoneActivity, LoginPhoneVerificationActivity::class.java)
                            phoneVerificationIntent.putExtra(
                                LoginPhoneVerificationActivity.IDENTIFIER_VERIFICATION_CODE,
                                code
                            )
                            startActivity(phoneVerificationIntent)
                        }
                            ?: run()
                            {
                                Toast.makeText(this@LoginPhoneActivity, "No Verification Code found", Toast.LENGTH_LONG).show()
                            }
                    }
                })
        }
    }
}