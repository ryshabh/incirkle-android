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
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.currentUserData
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_teaching_assistants.*

class TeachingAssistantsActivity : AppCompatActivity()
{
    companion object
    {
        val REQUEST_CODE = 2
        val IDENTIFIER_CAN_MODIFY = "Can Modify"
        val IDENTIFIER_TEACHING_ASSISTANTS = "Teaching Assistants"
    }

    private var teachingAssistants = ArrayList<String>()

    private fun updateTeachingAssistantsListView()
    {
        listView_teachingAssistants.adapter = DetailedListAdapter(this, ArrayList())
        val students = ArrayList<Pair<String, String>>()
        User.iterate(this.teachingAssistants)
        {
            index, key, task ->
            task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
            task.result?.let()
            {
                val student = Pair(this.teachingAssistants[index], it.firstOrNull()?.toObject(User::class.java)?.fullName() ?: "")
                students.add(student)
                if (students.size == this.teachingAssistants.size)
                    listView_teachingAssistants.adapter = DetailedListAdapter(this, students)
            }
        }

        /*
        for (index in 0 until this.teachingAssistants.size)
        {
            val userID = this.teachingAssistants[index]
            (if (Patterns.PHONE.matcher(userID).matches()) "phoneNumber"
            else if (Patterns.PHONE.matcher(userID).matches()) "emailAddress"
            else null)?.let()
            {
                    key ->

                User.collectionReference().whereEqualTo(key, userID).get().addOnCompleteListener()
                {
                        task ->
                    task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                    task.result?.let()
                    {
                        val student = Pair(userID, it.firstOrNull()?.toObject(User::class.java)?.fullName() ?: "")
                        students.add(student)
                        if (students.size == this.teachingAssistants.size)
                            listView_teachingAssistants.adapter = DetailedListAdapter(this, students)
                    }
                }
            }
                ?: run() { Toast.makeText(this, "Found invalid User ID type: $userID", Toast.LENGTH_LONG).show() }
        }
        */
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teaching_assistants)
        supportActionBar?.let { it.title = getString(R.string.text_teachingAssistants) }

        this.teachingAssistants = intent.getStringArrayListExtra(IDENTIFIER_TEACHING_ASSISTANTS)
        this.updateTeachingAssistantsListView()
        if (this.intent.getBooleanExtra(IDENTIFIER_CAN_MODIFY, false))
        {
            button_add_teachingAssistant.visibility = View.VISIBLE
            registerForContextMenu(listView_teachingAssistants)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)
    {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.listView_teachingAssistants)
        {
            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            menu?.setHeaderTitle(this.teachingAssistants[info.position])
            menu?.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean
    {
        val menuInfo = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        this.teachingAssistants.removeAt(menuInfo.position)
        this.updateTeachingAssistantsListView()
        return true
    }

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
                                this.updateTeachingAssistantsListView()
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
        intent.putExtra(IDENTIFIER_TEACHING_ASSISTANTS, this.teachingAssistants)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
