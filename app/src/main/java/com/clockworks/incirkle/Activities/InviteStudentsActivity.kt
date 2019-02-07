package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.activity_invite_students.*
import android.widget.AdapterView
import android.widget.Toast
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.google.firebase.auth.FirebaseAuth


class InviteStudentsActivity : AppCompatActivity()
{
    companion object
    {
        val REQUEST_CODE = 1
        val IDENTIFIER_INVITED_STUDENTS = "Invited Students"
    }

    private var invitedStudents = ArrayList<String>()

    private fun updateInvitedStudentsListView()
    {
        listView_invitedStudents.adapter = DetailedListAdapter(this, ArrayList())
        val students = ArrayList<Pair<String, String>>()
        User.iterate(this.invitedStudents)
        {
            index, key, task ->
            task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
            task.result?.let()
            {
                val student = Pair(this.invitedStudents[index], it.firstOrNull()?.toObject(User::class.java)?.fullName() ?: "")
                students.add(student)
                if (students.size == this.invitedStudents.size)
                    listView_invitedStudents.adapter = DetailedListAdapter(this, students)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_students)
        supportActionBar?.let { it.title = getString(R.string.text_inviteStudents) }

        this.invitedStudents = intent.getSerializableExtra(IDENTIFIER_INVITED_STUDENTS) as ArrayList<String>
        this.updateInvitedStudentsListView()
        registerForContextMenu(listView_invitedStudents)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)
    {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.listView_invitedStudents)
        {
            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            menu?.setHeaderTitle(this.invitedStudents[info.position])
            menu?.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean
    {
        val menuInfo = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        this.invitedStudents.removeAt(menuInfo.position)
        this.updateInvitedStudentsListView()
        return true
    }

    fun inviteStudent(v: View)
    {
        val userIDTextView = EditText(this)
        userIDTextView.hint = "Email Address / Phone Number"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invite Student")
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
                else if (this.invitedStudents.contains(userID))
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
                                this.invitedStudents.add(userID)
                                this.updateInvitedStudentsListView()
                                alert.dismiss()
                            }
                        }
                    }
                }
            }
        }
        alert.show()
    }

    fun done(view: View)
    {
        val intent = Intent()
        intent.putExtra(IDENTIFIER_INVITED_STUDENTS, this.invitedStudents)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
