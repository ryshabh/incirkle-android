package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.activity_invite_students.*
import android.widget.AdapterView


class InviteStudentsActivity : AppCompatActivity()
{
    companion object
    {
        val REQUEST_CODE = 1
        val IDENTIFIER_INVITED_STUDENTS = "Invited Students"
    }

    private var invitedStudents = HashMap<String, String>()

    private fun updateInvitedStudents()
    {
        listView_invitedStudents.adapter = DetailedListAdapter(this, this.invitedStudents.toList())
        val intent = Intent()
        intent.putExtra(IDENTIFIER_INVITED_STUDENTS, this.invitedStudents)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun setInvitedStudent(student: Pair<String, String>?)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invite Student")
        builder.setView(R.layout.view_detailed_text)

        builder.setPositiveButton("Done", null)
        builder.setNeutralButton("Cancel", null)
        val alert = builder.create()
        alert.setOnShowListener()
        {
            dialogInterface ->

            val alert = dialogInterface as AlertDialog

            val topTextView = alert.findViewById<EditText>(R.id.textView_top)!!
            val bottomTextView = alert.findViewById<EditText>(R.id.textView_bottom)!!

            topTextView.hint = "Name"
            bottomTextView.hint = "User ID"

            (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener()
            {

                val name = topTextView.text.toString().trim()
                val userID = bottomTextView.text.toString().trim()

                topTextView.error = null
                bottomTextView.error = null

                if (name.isBlank())
                {
                    topTextView.error = "User cannot be empty"
                    return@setOnClickListener
                }
                else if (userID.isBlank())
                {
                    bottomTextView.error = "User ID cannot be empty"
                    return@setOnClickListener
                }

                if (student == null)
                {
                    if (this.invitedStudents.containsKey(name))
                    {
                        topTextView.error = "User named $name is already present"
                        return@setOnClickListener
                    }
                    else if (this.invitedStudents.containsValue(userID))
                    {
                        topTextView.error = "User with id $userID is already present"
                        return@setOnClickListener
                    }
                }

                this.invitedStudents.set(name, userID)
                this.updateInvitedStudents()
                alert.dismiss()
            }
        }
        alert.show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_students)

        this.invitedStudents = intent.getSerializableExtra(IDENTIFIER_INVITED_STUDENTS) as HashMap<String, String>
        listView_invitedStudents.adapter = DetailedListAdapter(this, this.invitedStudents.toList())
        listView_invitedStudents.setOnItemClickListener()
        {
            adapterView, view, position, id ->
            this.setInvitedStudent(adapterView.getItemAtPosition(position) as Pair<String, String>)
        }
        registerForContextMenu(listView_invitedStudents)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)
    {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.listView_invitedStudents)
        {
            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            menu?.setHeaderTitle((listView_invitedStudents.adapter.getItem(info.position) as Pair<String, String>).first)
            menu?.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean
    {
        val menuInfo = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val student = listView_invitedStudents.adapter.getItem(menuInfo.position) as Pair<String, String>
        this.invitedStudents.remove(student.first)
        this.updateInvitedStudents()
        return true
    }

    fun inviteStudent(v: View)
    {
        this.setInvitedStudent(null)
    }
}
