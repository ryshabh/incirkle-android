package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login_phone_verification.*


class LoginPhoneVerificationActivity : AppActivity()
{
    companion object
    {
        const val IDENTIFIER_VERIFICATION_CODE = "Verification Code"
    }

    private lateinit var mVerificationId: String
    private var isFromUserProfile = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone_verification)
        setSupportActionBar(toolbar)

        this.mVerificationId = intent.getStringExtra(IDENTIFIER_VERIFICATION_CODE) ?: ""
        this.isFromUserProfile = intent.getBooleanExtra("isFromUserProfile",false)

//        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.editText_verification_code.filters = arrayOf(InputFilter.LengthFilter(6))
        this.editText_verification_code.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                this@LoginPhoneVerificationActivity.button_continue.isEnabled = p0?.length == 6
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
    }

    fun verify(v: View)
    {

        val credential =
            PhoneAuthProvider.getCredential(this.mVerificationId, this.editText_verification_code.text.toString())
        this.showLoadingAlert()
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnFailureListener { showLongToast(this@LoginPhoneVerificationActivity, it.localizedMessage) }
            .addOnSuccessListener {
                if(isFromUserProfile)
                {
                    setResult(Activity.RESULT_OK)
                    finish()
                }else{
                    signIn()
                }
            }
            .addOnCompleteListener { this.dismissLoadingAlert() }
    }

    private fun signIn()
    {
        val homeActivityIntent = Intent(this, HomeActivity::class.java)
        homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(homeActivityIntent)
        finish()
    }

}
