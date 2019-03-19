package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Activities.CommentsActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ForumPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_course_forum.*
import kotlinx.android.synthetic.main.list_item_post_forum.view.*
import kotlinx.android.synthetic.main.fragment_course_forum.view.*
import kotlinx.android.synthetic.main.list_item_post_activity.view.*
import kotlinx.android.synthetic.main.list_item_post_forum.view.*
import kotlinx.android.synthetic.main.popup_add_forum.*
import kotlinx.android.synthetic.main.popup_add_forum.view.*
import java.lang.Exception


class CourseForumFragment(): FileUploaderFragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }
    private var root: View? = null

    lateinit var dialog: AlertDialog
    class ForumPostAdapter(private val context: Context, private val isAdmin: Boolean, private var teacherPath: String, private var dataSource: List<ForumPost>): BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var instructorTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var nameTextView: TextView
            lateinit var descriptionTextView: TextView
            lateinit var downloadAttachmentButton: Button
            lateinit var popupicon: ImageView
            lateinit var button_activityForum_download_attachment: TextView
            lateinit var textview_forumPost_count: TextView
        }

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteForumPost(post: ForumPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Forum Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete")
            {
                _, _ ->
                post.reference?.delete()
                    ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?.addOnSuccessListener()
                    {
                        FirebaseStorage.getInstance().getReference("Forum Attachments").child(post.reference!!.id).delete()
                            .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    }
            }
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
                view = inflater.inflate(R.layout.list_item_post_forum, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_forumPost_posterPicture
                viewModel.posterNameTextView = view.textView_forumPost_posterName
                viewModel.instructorTextView = view.textView_forumPost_instructor
                viewModel.timestampTextView = view.textView_forumPost_timestamp
                viewModel.deleteButton = view.button_forumPost_delete
                viewModel.nameTextView= view.textView_forumPost_name
                viewModel.descriptionTextView = view.textView_forumPost_details
                viewModel.downloadAttachmentButton = view.button_forumPost_download_attachment
                viewModel.popupicon = view.popupicon1
                viewModel.textview_forumPost_count = view.textview_forumPost_count
                viewModel.button_activityForum_download_attachment = view.button_activityForum_download_attachment
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

            viewModel.instructorTextView.visibility = if (post.poster.path == teacherPath) View.VISIBLE else View.GONE
            val date = android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val time = android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$time $date"
            viewModel.timestampTextView.setText(timestamp)
            viewModel.nameTextView.setText(post.name)
            viewModel.descriptionTextView.setText(post.description)
       //     viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE


            viewModel.deleteButton.setOnClickListener() { this.deleteForumPost(post) }
          //  viewModel.downloadAttachmentButton.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.button_activityForum_download_attachment.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.downloadAttachmentButton.setOnClickListener { post.attachmentPath?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } }
            viewModel.button_activityForum_download_attachment.setOnClickListener { post.attachmentPath?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } }


            post.reference!!.collection("Comments").get()
                .addOnSuccessListener()
                {
                    viewModel.textview_forumPost_count.text =  it.size().toString()
                }
                .addOnCompleteListener()
                {
                    //    Log.d("completed","completed")
                }
            viewModel.popupicon.setOnClickListener(View.OnClickListener {

                val popup = PopupMenu(context, it)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.actions, popup.menu)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                    val builder = AlertDialog.Builder(this.context)
                    builder.setTitle("Delete Forum Post")
                    builder.setMessage("Are you sure you wish to delete this post?")
                    builder.setPositiveButton("Delete")
                    {
                            _, _ ->
                        post.reference?.delete()
                            ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                            ?.addOnSuccessListener()
                            {
                                FirebaseStorage.getInstance().getReference("Forum Attachments").child(post.reference!!.id).delete()
                                    .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                            }
                    }
                    builder.setNegativeButton("Cancel", null)
                    builder.create().show()

                    true
                })
                popup.show()
            })
            view.setOnClickListener()
            {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra(CommentsActivity.IDENTIFIER_IS_ADMIN, isAdmin)
                intent.putExtra(CommentsActivity.IDENTIFIER_POST_PATH, post.reference!!.path)
                context.startActivity(intent)
            }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        root = inflater.inflate(R.layout.fragment_course_forum, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    private fun initialize()
    {
        val isAdmin = arguments?.getBoolean(IDENTIFIER_IS_ADMIN) ?: false


        card_view_createforum.setOnClickListener {
            var view = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_forum,null)

            view.button_forum_selectAttachment.setOnClickListener { this.selectFile { view.button_forum_selectAttachment.text = this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment) } }
            val forumPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Forum Posts")

            view.button_post_forum.setOnClickListener()
            {
                view.editText_post_forum_name.error = null
                view.editText_post_forum_description.error = null

                val name = view.editText_post_forum_name.text.toString().trim()
                val description = view.editText_post_forum_description.text.toString().trim()
                if (name.isBlank())
                {
                    view.editText_post_forum_name.error = "Forum Post name cannot be empty"
                    return@setOnClickListener
                }
                else if (description.isBlank())
                {
                    view.editText_post_forum_description.error = "Forum Post description cannot be empty"
                    return@setOnClickListener
                }
                else
                {
                    FirebaseAuth.getInstance().currentUser?.let()
                    {
                        val appActivity = this.activity as AppActivity
                        appActivity.showLoadingAlert()
                        forumPostsReference.add(ForumPost(name, description, it.documentReference()))
                            .addOnFailureListener { appActivity.showError (it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                view.editText_post_forum_name.setText("")
                                view.editText_post_forum_description.setText("")
                                this.updateAttachmentPath(it, FirebaseStorage.getInstance().getReference("Forum Attachments").child(it.id), "attachmentPath")
                            }
                    }
                }
                dialog.dismiss()
            }
            dialog = AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(true)
                .create()



            dialog.show()
        }

        val forumPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Forum Posts")

        forumPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(ForumPost::class.java) }?.let()
            { root!!.listView_courseFeed_forum.adapter = ForumPostAdapter(context!!, isAdmin, arguments?.getString(
                IDENTIFIER_COURSE_TEACHER_PATH) ?: "", it) }
        }
    }


}