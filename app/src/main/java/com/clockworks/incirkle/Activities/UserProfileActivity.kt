package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.Organisation
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.util.concurrent.TimeUnit


class UserProfileActivity : AppActivity()
{

    companion object
    {
        const val FROM_SIGNUP = "FROM_SIGNUP"
        const val IS_TEACHER = "IS_TEACHER"
        const val PHONE_NO = "PHONE_NO"
    }

    private var fromSignup: Boolean = false
    private var isTeacher: Boolean = false
    private var phoneNo: String? = null


    private var TAG = UserProfileActivity::class.java.simpleName
    private lateinit var user: User
    private val displayPictureAlertClickListener = DialogInterface.OnClickListener()
    { dialog, button ->

        when (button)
        {
            AlertDialog.BUTTON_POSITIVE ->
            {
                // TODO: Change Picture

                try
                {
                    this.selectFile {

                        /*  this.selectedFileUri?.getName(this)
                                          ?: getString(com.clockworks.incirkle.R.string.text_select_attachment)
                */
                        if (this.selectedFileUri != null)
                        {
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedFileUri)
                            displayPictureImageButton.setImageBitmap(bitmap)
                            if (fromSignup)
                            {
                                updateAttachmentPathWithouDocumentReference(
                                    FirebaseStorage.getInstance().getReference("UserProfiles").child(System.currentTimeMillis().toString())
                                )
                            }
                            else
                            {
                                updateAttachmentPath(
                                    user.reference!!,
                                    FirebaseStorage.getInstance().getReference("UserProfiles").child(user.reference!!.id),
                                    "profilepic"
                                )
                            }

                        }
                    }
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }

            }
            AlertDialog.BUTTON_NEGATIVE ->
            {
                // TODO: Delete Picture
                if (this.user.profilepic == null || this.user.profilepic.isNullOrEmpty())
                    return@OnClickListener

                val photoRef = FirebaseStorage.getInstance().getReference("UserProfiles").storage.getReferenceFromUrl(
                    this.user.profilepic!!
                )
                this.showLoadingAlert()
                photoRef.delete().addOnSuccessListener(OnSuccessListener<Void> {
                    // File deleted successfully
                    Log.d(TAG, "onSuccess: deleted file")
                    this.user.profilepic = null
                    displayPictureImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_user))
                    this.user.reference?.update("profilepic", null)
                        ?.addOnFailureListener(::showError)
                        ?.addOnSuccessListener() {}
                        ?.addOnCompleteListener { this.dismissLoadingAlert() }
                }).addOnFailureListener(OnFailureListener {
                    // Uh-oh, an error occurred!
                    this.dismissLoadingAlert()
                    Log.d(TAG, "onFailure: did not delete file")
                })

            }
            AlertDialog.BUTTON_NEUTRAL -> dialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(com.clockworks.incirkle.R.layout.activity_user_profile)
        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        //get value from previous page
        fromSignup = intent.getBooleanExtra(FROM_SIGNUP, false)

        if (fromSignup)
        {
            // user signup
            isTeacher = intent.getBooleanExtra(IS_TEACHER, false)
            phoneNo = intent.getStringExtra(PHONE_NO)
            btnSubmit.text = getString(R.string.submit)
            if (isTeacher)
            {
                radioButton_teacher.isChecked = true
            }
            else
            {
                radioButton_student.isChecked = true
            }

            user = User()
        }
        else
        {
            FirebaseAuth.getInstance().currentUser?.let()
            { firebaseUser ->
                this.showLoadingAlert()
                firebaseUser.documentReference().get()
                    .addOnFailureListener(::showError)
                    .addOnSuccessListener()
                    {
                        if (it.data?.isEmpty() != false)
                        {
                            val userID = firebaseUser.phoneNumber
                            val newUser = User(
                                "",
                                "",
                                this@UserProfileActivity.selectedGender(),
                                null,
                                firebaseUser.phoneNumber,
                                this@UserProfileActivity.selectedType()
                            )
                            newUser.phoneNumber = firebaseUser.phoneNumber
                            newUser.reference = FirebaseAuth.getInstance().currentUser?.documentReference()

                            this.update(newUser)

                            this.showLoadingAlert()
                            Organisation.reference.get()
                                .addOnFailureListener(::showError)
                                .addOnSuccessListener()
                                {
                                    it.forEach()
                                    {
                                        this.showLoadingAlert()
                                        it.reference.collection("Courses").get()
                                            .addOnFailureListener(::showError)
                                            .addOnSuccessListener()
                                            {
                                                it.forEach()
                                                {
                                                    this.performThrowable { it.serialize(Course::class.java) }?.let()
                                                    {
                                                        if ((it.invitedStudents + it.teachingAssistants).contains(userID))
                                                            newUser.courses.add(it.reference!!)
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener { this.dismissLoadingAlert() }
                                    }
                                }
                                .addOnCompleteListener { this.dismissLoadingAlert() }
                        }
                        else
                        {
                            this.performThrowable { it.serialize(User::class.java) }?.let()
                            {
                                this.update(it)
                                for (index in 0 until gender.childCount) gender.getChildAt(index).isEnabled = false
                                for (index in 0 until type.childCount) type.getChildAt(index).isEnabled = false
                            }
                        }
                    }
                    .addOnCompleteListener { this.dismissLoadingAlert() }
            }

        }
    }

    fun selectedGender(): User.Gender
    {
        return if (gender.checkedRadioButtonId == radioButton_male.id) User.Gender.MALE else User.Gender.FEMALE
    }

    fun selectedType(): User.Type
    {
        return if (type.checkedRadioButtonId == radioButton_teacher.id) User.Type.TEACHER else User.Type.STUDENT
    }

    fun update(user: User)
    {
        try
        {
            this.user = user
            editText_first_name.setText(user.firstName)
            editText_last_name.setText(user.lastName)
            gender.check(if (user.gender == User.Gender.MALE) radioButton_male.id else radioButton_female.id)
            type.check(if (user.type == User.Type.TEACHER) radioButton_teacher.id else radioButton_student.id)

            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
            if (user.profilepic != null)
            {
                user.profilepic?.let {

                    Glide
                        .with(this)
                        .load(it)
                        .apply(requestOptions)
                        .into(displayPictureImageButton);

                }
            }
            else
            {
                Glide
                    .with(this)
                    .load(ContextCompat.getDrawable(this, R.drawable.ic_user))
                    .apply(requestOptions)
                    .into(displayPictureImageButton);
            }
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun changeDisplayPicture(v: View)
    {
        AlertDialog.Builder(this)
            .setTitle("Change Display Picture")
            .setPositiveButton("Change", this.displayPictureAlertClickListener)
            .setNegativeButton("Delete", this.displayPictureAlertClickListener)
            .setNeutralButton("Cancel", this.displayPictureAlertClickListener)
            .create()
            .show()

    }

    fun updateProfile(v: View)
    {
        this.user.firstName = editText_first_name.text.toString()
        this.user.lastName = editText_last_name.text.toString()
        this.user.gender = this.selectedGender()
        this.user.type = this.selectedType()
        // check image changed
        if (changedUrl != null)
            this.user.profilepic = this.changedUrl;

        if (fromSignup)
        {
            // first time user created
            verifyNo(phoneNo)
        }
        else
        {
            updateUserDetail()

        }
    }

    private fun updateUserDetail()
    {
        this.showLoadingAlert()
        this.user.reference?.set(this.user)
            ?.addOnFailureListener(::showError)
            ?.addOnSuccessListener()
            {
                val homeActivityIntent = Intent(this, HomeActivity::class.java)
                homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(homeActivityIntent)
                finish()
            }
            ?.addOnCompleteListener { this.dismissLoadingAlert() }
    }


    private fun verifyNo(phoneNo: String?)
    {
        this.showLoadingAlert()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNo!!,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
            {
                override fun onVerificationCompleted(credential: PhoneAuthCredential)
                {
                    this@UserProfileActivity.dismissLoadingAlert()
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener()
                        { task ->
                            task.exception?.let {
                                this@UserProfileActivity.showLongToast(
                                    this@UserProfileActivity,
                                    it.message.toString()
                                )
                            }

                                ?: run {
                                    Toast.makeText(
                                        this@UserProfileActivity,
                                        "Successfully Verified",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException)
                {
                    this@UserProfileActivity.dismissLoadingAlert()
                    showLongToast(this@UserProfileActivity, e.message.toString())
                }

                override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?)
                {
                    this@UserProfileActivity.dismissLoadingAlert()
                    verificationId?.let()
                    { code ->
                        val phoneVerificationIntent =
                            Intent(this@UserProfileActivity, LoginPhoneVerificationActivity::class.java)
                        phoneVerificationIntent.putExtra(
                            LoginPhoneVerificationActivity.IDENTIFIER_VERIFICATION_CODE,
                            code
                        )
                        phoneVerificationIntent.putExtra("isFromUserProfile",true)
                        startActivityForResult(phoneVerificationIntent, 1001)
                    }
                        ?: run { this@UserProfileActivity.showError(Exception("No Verification Code found")) }
                }
            })


    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK)
        {
            user.reference = FirebaseAuth.getInstance().currentUser?.documentReference()
            user.phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
            updateUserDetail()
        }
    }

}
