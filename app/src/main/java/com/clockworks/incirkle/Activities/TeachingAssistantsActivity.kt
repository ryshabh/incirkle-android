package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_teaching_assistants.*

class TeachingAssistantsActivity : AppCompatActivity()
{
    companion object
    {
        val REQUEST_CODE = 2
        val IDENTIFIER_CAN_MODIFY = "Can Modify"
        val IDENTIFIER_COURSE_PATH = "Course Path"
        val IDENTIFIER_TEACHING_ASSISTANTS = "Teaching Assistants"
    }

    private var teachingAssistants = ArrayList<User>()

    private fun updateTeachingAssistants()
    {
        listView_teachingAssistants.adapter = DetailedListAdapter(this, this.teachingAssistants.map { Pair(it.fullName(), it.userID()) })
        val intent = Intent()
        intent.putExtra(IDENTIFIER_TEACHING_ASSISTANTS, this.teachingAssistants.map { it.documentReference!!.path }.toTypedArray())
        setResult(Activity.RESULT_OK, intent)
    }

    fun fetchTeachingAssistants()
    {
        val assistantsPaths = intent.getStringArrayExtra(IDENTIFIER_TEACHING_ASSISTANTS)
        val size = assistantsPaths.size
        val assistants = ArrayList<User>()
        for (index in 0..size)
        {
            FirebaseFirestore.getInstance().document(assistantsPaths[index]).get().addOnCompleteListener()
            {
                result ->
                result.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                ?: result.result?.let()
                {
                    val teachingAssistant = it.toObject(User::class.java)!!
                    teachingAssistant.documentReference = it.reference
                    assistants.add(teachingAssistant)
                }
                if (index == size - 1)
                {
                    this.teachingAssistants = assistants
                    this.updateTeachingAssistants()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teaching_assistants)
        if (this.intent.getBooleanExtra(IDENTIFIER_CAN_MODIFY, false))
        {
            button_add_teachingAssistant.visibility = View.VISIBLE
            registerForContextMenu(listView_teachingAssistants)
        }
        this.fetchTeachingAssistants()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)
    {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.listView_invitedStudents)
        {
            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            menu?.setHeaderTitle(this.teachingAssistants[info.position].fullName())
            menu?.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean
    {
        val menuInfo = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        this.teachingAssistants.removeAt(menuInfo.position)
        this.updateTeachingAssistants()
        return true
    }

    fun addTeachingAssistant(v: View)
    {
        val courseReference = FirebaseFirestore.getInstance().document(intent.getStringExtra(IDENTIFIER_COURSE_PATH))
        User.collectionReference().whereArrayContains("courses", courseReference).get().addOnCompleteListener()
        {
                task ->
            task.exception?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show() }
                ?: task.result?.documents?.let()
                {
                    val availableUsers = it.filter { !this.teachingAssistants.map() { it.documentReference!! }.contains(it.reference) }.map()
                    {
                        val user = it.toObject(User::class.java)!!
                        user.documentReference = it.reference
                        return@map user
                    }

                    val spinner = Spinner(this)
                    spinner.adapter = DetailedListAdapter(this, availableUsers.map { Pair(it.fullName(), it.userID()) })

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Teaching Assistant")
                    builder.setView(spinner)
                    builder.setPositiveButton("Done", null)
                    builder.setNeutralButton("Cancel", null)
                    val alert = builder.create()
                    alert.setOnShowListener()
                    {
                            dialogInterface ->

                        (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener()
                        {
                            if (spinner.selectedItemPosition == Spinner.INVALID_POSITION)
                            {
                                Toast.makeText(this, "Please select Assistant", Toast.LENGTH_LONG).show()
                                return@setOnClickListener
                            }
                            val newAssistant = availableUsers[spinner.selectedItemPosition]
                            this.teachingAssistants.add(newAssistant)
                            this.updateTeachingAssistants()
                            dialogInterface.dismiss()
                        }
                    }
                    alert.show()
                }
        }
    }
}
