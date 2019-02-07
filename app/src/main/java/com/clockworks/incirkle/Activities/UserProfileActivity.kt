package com.clockworks.incirkle.Activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.view.View
import android.widget.Toast
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.Organisation
import com.clockworks.incirkle.Models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_user_profile.*
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R

class UserProfileActivity : AppCompatActivity()
{
    private lateinit var user: User
    private val displayPictureAlertClickListener = DialogInterface.OnClickListener()
    {
        dialog, button ->

        when (button)
        {
            AlertDialog.BUTTON_POSITIVE ->
            {
                // TODO: Change Picture
            }
            AlertDialog.BUTTON_NEGATIVE ->
            {
                // TODO: Delete Picture
            }
            AlertDialog.BUTTON_NEUTRAL -> dialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        FirebaseAuth.getInstance().currentUser?.let()
        {
            firebaseUser ->

            firebaseUser.currentUserData()
            {
                user, exception ->

                exception?.let()
                {
                        e ->
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
                ?: user?.let()
                {
                    this.update(it)
                    for (index in 0 until gender.childCount) gender.getChildAt(index).isEnabled = false
                    for (index in 0 until type.childCount) type.getChildAt(index).isEnabled = false
                }
                ?:
                run()
                {
                    val userID = firebaseUser.phoneNumber

                    val newUser = User(firebaseUser.uid, "", "", this@UserProfileActivity.selectedGender(), null, firebaseUser.phoneNumber, this@UserProfileActivity.selectedType())
                    newUser.phoneNumber = firebaseUser.phoneNumber
                    this.update(newUser)

                    Organisation.reference.get().addOnCompleteListener()
                    {
                        it.exception?.let { Toast.makeText(this@UserProfileActivity, it.toString(), Toast.LENGTH_LONG).show() }
                        ?: it.result?.forEach()
                        {
                            it.reference.collection("Courses").get().addOnCompleteListener()
                            {
                                it.exception?.let { Toast.makeText(this@UserProfileActivity, it.toString(), Toast.LENGTH_LONG).show() }
                                    ?: it.result?.forEach()
                                {
                                    courseSnapshot ->
                                    courseSnapshot.toObject(Course::class.java).let()
                                    {
                                        if ((it.invitedStudents + it.teachingAssistants).contains(userID))
                                            newUser.courses.add(courseSnapshot.reference)
                                    }
                                }
                            }
                        }
                    }
                }

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
        this.user = user
        editText_first_name.setText(user.firstName)
        editText_last_name.setText(user.lastName)
        gender.check(if (user.gender == User.Gender.MALE) radioButton_male.id else radioButton_female.id)
        type.check(if (user.type == User.Type.TEACHER) radioButton_teacher.id else radioButton_student.id)
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

        this.user.update()
        {
            it?.let()
            {
                exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
            }
            ?: run()
            {
                val homeActivityIntent = Intent(this, HomeActivity::class.java)
                homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(homeActivityIntent)
                finish()
            }
        }
    }
}
