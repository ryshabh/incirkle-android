package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_teaching_assistants.*
import java.lang.Exception

class TeachingAssistantsActivity : AppActivity(), DetailedListAdapter.DeleteListener
{
    companion object
    {
        const val REQUEST_CODE = 2
        const val IDENTIFIER_CAN_MODIFY = "Can Modify"
        const val IDENTIFIER_TEACHING_ASSISTANTS = "Teaching Assistants"
    }

    private var teachingAssistants = ArrayList<String>()
    private var isAdmin = false
    private var deleteAlert: AlertDialog? = null

    private fun updateTeachingAssistantsListView(list: ArrayList<Pair<String, String>>)
    {
        this.deleteAlert = null
        listView_teachingAssistants.adapter = DetailedListAdapter(this, list, if (this.isAdmin) this else null)
    }

    private fun updateTeachingAssistants()
    {
        this.updateTeachingAssistantsListView(ArrayList())
        val assistants = ArrayList<Pair<String, String>>()
        User.iterate(this.teachingAssistants)
        {
            index, key, task ->
            task.addOnFailureListener(::showError)
                .addOnSuccessListener()
                {
                    val user = this.performThrowable { it.firstOrNull()?.serialize(User::class.java) }
                    val assistant = Pair(this.teachingAssistants[index], user?.fullName() ?: "")
                    assistants.add(assistant)
                    if (assistants.size == this.teachingAssistants.size)
                        this.updateTeachingAssistantsListView(assistants)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teaching_assistants)
        supportActionBar?.let { it.title = getString(R.string.text_teachingAssistants) }

        this.teachingAssistants = intent.getStringArrayListExtra(IDENTIFIER_TEACHING_ASSISTANTS)
        this.isAdmin = this.intent.getBooleanExtra(IDENTIFIER_CAN_MODIFY, false)
        if (this.isAdmin)
            button_add_teachingAssistant.visibility = View.VISIBLE
        this.updateTeachingAssistants()
    }

    override fun onItemDelete(position: Int)
    {
        if (this.isAdmin)
        {
            this.deleteAlert = AlertDialog.Builder(this)
                .setTitle("Remove Teaching Assistant")
                .setMessage("Are you sure you wish to remove User with ID: ${this.teachingAssistants[position]} as Teaching Assistant?")
                .setPositiveButton("Yes")
                {
                    _, _ ->
                    this.teachingAssistants.removeAt(position)
                    this.updateTeachingAssistants()
                }
                .setNegativeButton("No")
                {
                    _, _ ->
                    this.deleteAlert = null
                }
                .create()
            this.deleteAlert?.show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun addTeachingAssistant(v: View)
    {
        val userIDTextView = EditText(this)
        userIDTextView.hint = "Email Address / Phone Number"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Teaching Assistant")
        builder.setView(userIDTextView)
        builder.setPositiveButton("Done", null)
        builder.setNeutralButton("Cancel", null)

        val alert = builder.create()
        alert.setOnShowListener()
        {
                dialogInterface ->

            (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener()
            {

                var userID = userIDTextView.text.toString().trim()
                userIDTextView.error = null
                if (userID.isBlank())
                    userIDTextView.error = "User cannot be empty"
                else if (this.teachingAssistants.contains(userID))
                    userIDTextView.error = "User with login ID $userID is already present"
                else if (!Patterns.PHONE.matcher(userID).matches() && !Patterns.PHONE.matcher(userID).matches())
                    userIDTextView.error = "Invalid User ID"
                else
                {
                    if (Patterns.PHONE.matcher(userID).matches())
                    {
                        if (userID.length == 10)
                            userID = "+91$userID"
                        else if (userID.length != 13)
                        {
                            userIDTextView.error = "Invalid Mobile Phone Number"
                            return@setOnClickListener
                        }
                    }

                    FirebaseAuth.getInstance().currentUser?.currentUserData()
                    {
                        userData, exception ->
                        exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                        ?: userData?.let()
                        {
                            user ->
                            if (user.userID().equals(userID, true))
                                Toast.makeText(this, "Cannot add self", Toast.LENGTH_LONG).show()
                            else
                            {
                                this.teachingAssistants.add(userID)
                                this.updateTeachingAssistants()
                                alert.dismiss()
                            }
                        }
                    }
                }
            }
        }
        alert.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun done(view: View)
    {
        val intent = Intent()
        intent.putExtra(IDENTIFIER_TEACHING_ASSISTANTS, this.teachingAssistants)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
