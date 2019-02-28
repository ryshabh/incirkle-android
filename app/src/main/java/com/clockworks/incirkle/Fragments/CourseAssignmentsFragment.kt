package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.AssignmentPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_course_assignments.*
import kotlinx.android.synthetic.main.list_item_post_assignment.view.*
import java.util.*
import kotlin.properties.Delegates

class CourseAssignmentsFragment(): Fragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }
    private val calendar = Calendar.getInstance()

    class AssignmentPostAdapter(private val context: Context, private val isAdmin: Boolean, private var dataSource: List<AssignmentPost>): BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var nameTextView: TextView
            lateinit var detailsTextView: TextView
            lateinit var dueDateTextView: TextView
        }

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteAssignmentPost(post: AssignmentPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Assignment Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
            {
                _, _ ->
                post.reference?.delete()?.addOnFailureListener() { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
            })
            builder.setNegativeButton("Cancel", null)
            builder.create().show()
        }

        override fun getCount(): Int
        {
            return this.dataSource.size
        }

        override fun getItem(p0: Int): Any
        {
            return this.dataSource[p0]
        }

        override fun getItemId(p0: Int): Long
        {
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
        {
            val view: View
            val viewModel: ViewModel

            val post = this.dataSource[position]

            if (convertView == null)
            {
                view = inflater.inflate(R.layout.list_item_post_assignment, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_assignmentPost_posterPicture
                viewModel.posterNameTextView = view.textView_assignmentPost_posterName
                viewModel.timestampTextView = view.textView_assignmentPost_timestamp
                viewModel.deleteButton = view.button_assignmentPost_delete
                viewModel.nameTextView= view.textView_assignmentPost_name
                viewModel.detailsTextView = view.textView_assignmentPost_details
                viewModel.dueDateTextView = view.textView_assignmentPost_dueDate
                view.tag = viewModel
            }
            else
            {
                view = convertView
                viewModel = convertView.tag as ViewModel
            }

            post.poster.get().addOnCompleteListener()
            {
                task ->

                task.exception?.let { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                ?: task.result?.serialize(User::class.java)?.let()
                {
                    viewModel.posterNameTextView.setText(it.fullName())
                    // TODO: Set Display Picture
                }
            }

            val dueDateDate = android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.dueDate.toDate())
            val dueDateTime = android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.dueDate.toDate())
            val dueDate = "$dueDateTime, $dueDateDate"

            val timestampDate = android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestampTime = android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$timestampTime, $timestampDate"

            viewModel.timestampTextView.text = timestamp
            viewModel.nameTextView.text = post.name
            viewModel.detailsTextView.text = post.details
            viewModel.dueDateTextView.text = dueDate
            viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener { this.deleteAssignmentPost(post) }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val rootView = inflater.inflate(R.layout.fragment_course_assignments, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    private fun initialize()
    {
        val isAdmin = arguments?.getBoolean(IDENTIFIER_IS_ADMIN) ?: false
        layout_post_assignment_new.visibility = if(isAdmin) View.VISIBLE else View.GONE


        this.button_post_assignment_dueDate.text = android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueDate.setOnClickListener()
        {
            val listener = DatePickerDialog.OnDateSetListener()
            {
                _, year, month, day ->
                this.calendar.set(Calendar.YEAR, year)
                this.calendar.set(Calendar.MONTH, month)
                this.calendar.set(Calendar.DAY_OF_MONTH, day)
                this.button_post_assignment_dueDate.text = android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
            }
            val year = this.calendar.get(Calendar.YEAR)
            val month = this.calendar.get(Calendar.MONTH)
            val day = this.calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, listener, year, month, day).show()
        }

        button_post_assignment_dueTime.text = android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueTime.setOnClickListener()
        {
            val listener = TimePickerDialog.OnTimeSetListener()
            {
                _, hour, minute ->
                this.calendar.set(Calendar.HOUR_OF_DAY, hour)
                this.calendar.set(Calendar.MINUTE, minute)
                button_post_assignment_dueTime.text = android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
            }
            val hour = this.calendar.get(Calendar.HOUR_OF_DAY)
            val minute = this.calendar.get(Calendar.MINUTE)
            TimePickerDialog(context, listener, hour, minute, false).show()
        }

        val assignmentPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Assignment Posts")

        button_post_assignment.setOnClickListener()
        {
            editText_post_assignment_name.error = null
            editText_post_assignment_details.error = null

            val name = editText_post_assignment_name.text.toString().trim()
            val details = editText_post_assignment_details.text.toString().trim()

            if (name.isBlank())
            {
                editText_post_assignment_name.error = "Assignment name cannot be empty"
                return@setOnClickListener
            }
            else if (details.isBlank())
            {
                editText_post_assignment_details.error = "Assignment description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    (this.activity as AppActivity).showLoadingAlert()
                    assignmentPostsReference.add(AssignmentPost(name, details, this.calendar.time, it.documentReference()))
                        .addOnSuccessListener { this.resetPostLayout() }
                        .addOnFailureListener { (this.activity as AppActivity).showError(it) }
                        .addOnCompleteListener { (this.activity as AppActivity).dismissLoadingAlert() }
                }
            }
        }
        assignmentPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(AssignmentPost::class.java) }?.let()
            { listView_courseFeed_assignments.adapter = AssignmentPostAdapter(context!!, isAdmin, it) }
        }
    }

    private fun resetPostLayout()
    {
        editText_post_assignment_name.setText("")
        editText_post_assignment_details.setText("")
        this.button_post_assignment_dueDate.text = android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueTime.text = android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
    }
}