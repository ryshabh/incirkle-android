package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ActivityPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_course_activities.*
import kotlinx.android.synthetic.main.list_item_post_activity.view.*
import android.content.Intent
import com.clockworks.incirkle.R


class CourseActivitiesFragment(): FileUploaderFragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }

    class ActivityPostAdapter(private val context: Context, private val isAdmin: Boolean, private var dataSource: List<ActivityPost>): BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var descriptionTextView: TextView
            lateinit var downloadAttachmentButton: Button
        }

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteActivityPost(post: ActivityPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Activity Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
            {
                _, _ ->
                post.reference?.delete()
                    ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?.addOnSuccessListener()
                    {
                        FirebaseStorage.getInstance().getReference("Activity Attachments").child(post.reference!!.id).delete()
                            .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    }
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
                view = inflater.inflate(R.layout.list_item_post_activity, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_activityPost_posterPicture
                viewModel.posterNameTextView = view.textView_activityPost_posterName
                viewModel.timestampTextView = view.textView_activityPost_timestamp
                viewModel.deleteButton = view.button_activityPost_delete
                viewModel.descriptionTextView = view.textView_activityPost_description
                viewModel.downloadAttachmentButton = view.button_activityPost_download_attachment
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

            val date = android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val time = android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$time $date"
            viewModel.timestampTextView.setText(timestamp)
            viewModel.descriptionTextView.setText(post.description)
            viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteActivityPost(post) }
            viewModel.downloadAttachmentButton.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.downloadAttachmentButton.setOnClickListener { post.attachmentPath?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val rootView = inflater.inflate(R.layout.fragment_course_activities, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    override fun storageReference(): StorageReference
    {
        return FirebaseStorage.getInstance().getReference("Activity Attachments")
    }

    override fun didSelectFile()
    {
        button_activity_selectAttachment.text = this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment)
    }

    private fun initialize()
    {
        val isAdmin = arguments?.getBoolean(IDENTIFIER_IS_ADMIN) ?: false
        layout_post_activity_new.visibility = if(isAdmin) View.VISIBLE else View.GONE

        button_activity_selectAttachment.setOnClickListener { this.selectFile() }

        val activityPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Activity Posts")
        button_post_activity.setOnClickListener()
        {
            editText_post_activity_description.error = null

            val description = editText_post_activity_description.text.toString().trim()
            if (description.isBlank())
            {
                editText_post_activity_description.error = "Activity description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    val appActivity = this.activity as AppActivity
                    appActivity.showLoadingAlert()
                    activityPostsReference.add(ActivityPost(description, it.documentReference()))
                        .addOnFailureListener { appActivity.showError (it) }
                        .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                        .addOnSuccessListener()
                        {
                            this.resetPostLayout()
                            this.updateAttachmentPath(it, "attachmentPath")
                        }
                }
            }
        }
        activityPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(ActivityPost::class.java) }?.let()
            { listView_courseFeed_activities.adapter = ActivityPostAdapter(context!!, isAdmin, it) }
        }
    }

    private fun resetPostLayout()
    {
        editText_post_activity_description.setText("")
    }
}