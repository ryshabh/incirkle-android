package com.clockworks.incirkle.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.clockworks.incirkle.R
import com.clockworks.incirkle.utils.AppConstantsValue
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login_phone.*
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppActivity()
{

    companion object
    {
        const val FROM_SIGNUP = "FROM_SIGNUP"
        const val IS_TEACHER = "IS_TEACHER"
    }

    private var fromSignup: Boolean = false
    private var isTeacher: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone)
        setSupportActionBar(toolbar)
        //get value from previous page
        fromSignup = intent.getBooleanExtra(FROM_SIGNUP, false)
        isTeacher = intent.getBooleanExtra(IS_TEACHER, false)

        if (fromSignup)
        {
            supportActionBar?.let { it.title = getString(R.string.signup) }
        }

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.editText_phone_number.filters = arrayOf(InputFilter.LengthFilter(10))
        this.editText_phone_number.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                this@LoginPhoneActivity.button_next.isEnabled = p0?.length == 10
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
        var user = FirebaseAuth.getInstance().currentUser;

        if (user != null)
        {
            signIn()
            // User is signed in.
        }
    }

    private fun setProgress(case: Boolean)
    {
        editText_phone_number.isEnabled = !case
        button_next.isEnabled = !case
    }

    @Suppress("UNUSED_PARAMETER")
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
            val countryCode = getString(R.string.phone_country_code)
            // check mobile no is available or not if not then go to user profile page
            showLoadingAlert()
            AppConstantsValue.userCollectionRef.whereEqualTo("phoneNumber", "$countryCode$number").get()
                .addOnCompleteListener {
                    dismissLoadingAlert()
                    if (it.isSuccessful)
                    {
                        if (it.result?.documentChanges?.isNotEmpty()!!)
                        {
                            // user exist
                            Log.d("LoginPhoneActivity", "User exist")
                            verifyNo(countryCode, number)
                        }
                        else
                        {
                            Log.d("LoginPhoneActivity", "User doesn't exist")
                            if (fromSignup)
                            {
                                startActivity(
                                    Intent(
                                        this,
                                        UserProfileActivity::class.java
                                    ).putExtra(
                                        UserProfileActivity.FROM_SIGNUP,
                                        fromSignup
                                    ).putExtra(
                                        UserProfileActivity.IS_TEACHER,
                                        isTeacher
                                    ).putExtra(UserProfileActivity.PHONE_NO, "$countryCode$number")
                                )
                            }
                            else
                            {
                                openErrorDialog(this, "No user found please sign up")
                            }
                        }
                    }
                    else
                    {
                        this.showLongToast(this, "Something went wrong")
                    }
                }

        }
    }

    fun openErrorDialog(context: Context, msg: String)
    {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(msg)
        alertDialogBuilder.setPositiveButton(
            "ok"
        ) { dialog, arg1 ->
            finish()
            dialog.dismiss()
        }

        //        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialog, int which) {
        //                dialog.dismiss();
        //            }
        //        });

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun verifyNo(countryCode: String, number: String)
    {
        this.setProgress(true)
        this.showLoadingAlert()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "$countryCode$number",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
            {
                override fun onVerificationCompleted(credential: PhoneAuthCredential)
                {
                    this@LoginPhoneActivity.dismissLoadingAlert()
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener()
                        {

                                task ->
                            task.exception?.let { this@LoginPhoneActivity.showError(it) }

                                ?: run {
                                    Toast.makeText(
                                        this@LoginPhoneActivity,
                                        "Successfully Verified",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            setProgress(false)
                            val homeActivityIntent = Intent(applicationContext, HomeActivity::class.java)
                            homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(homeActivityIntent)
                            finish()
                        }
                }

                override fun onVerificationFailed(e: FirebaseException)
                {
                    this@LoginPhoneActivity.dismissLoadingAlert()
                    this@LoginPhoneActivity.showError(e)
                }

                override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?)
                {
                    this@LoginPhoneActivity.dismissLoadingAlert()
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
                        ?: run { this@LoginPhoneActivity.showError(Exception("No Verification Code found")) }
                }
            })


    }


    private fun signIn()
    {
        val homeActivityIntent = Intent(this, HomeActivity::class.java)
        homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(homeActivityIntent)
        finish()
    }
}