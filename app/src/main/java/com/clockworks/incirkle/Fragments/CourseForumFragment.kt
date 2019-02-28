package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ForumPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_course_forum.*
import kotlinx.android.synthetic.main.list_item_post_forum.view.*

class CourseForumFragment(): Fragment()
{
    companion object
    {
        val IDENTIFIER_COURSE_PATH = "Course Path"
        val IDENTIFIER_IS_ADMIN = "Is Admin"
    }

    class ForumPostAdapter(private val context: Context, private val isAdmin: Boolean, private var dataSource: List<ForumPost>): BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var nameTextView: TextView
            lateinit var descriptionTextView: TextView
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
                post.reference?.delete()?.addOnFailureListener() { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
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
                viewModel.timestampTextView = view.textView_forumPost_timestamp
                viewModel.deleteButton = view.button_forumPost_delete
                viewModel.nameTextView= view.textView_forumPost_name
                viewModel.descriptionTextView = view.textView_forumPost_details
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
            viewModel.nameTextView.setText(post.name)
            viewModel.descriptionTextView.setText(post.description)
            viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteForumPost(post) }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val rootView = inflater.inflate(R.layout.fragment_course_forum, container, false)
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
        val forumPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Forum Posts")

        button_post_forum.setOnClickListener()
        {
            editText_post_forum_name.error = null
            editText_post_forum_description.error = null

            val name = editText_post_forum_name.text.toString().trim()
            val description = editText_post_forum_description.text.toString().trim()
            if (name.isBlank())
            {
                editText_post_forum_name.error = "Forum Post name cannot be empty"
                return@setOnClickListener
            }
            else if (description.isBlank())
            {
                editText_post_forum_description.error = "Forum Post description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    (this.activity as AppActivity).showLoadingAlert()
                    forumPostsReference.add(ForumPost(name, description, it.documentReference()))
                        .addOnSuccessListener { this.resetPostLayout() }
                        .addOnFailureListener { (this.activity as AppActivity).showError(it) }
                        .addOnCompleteListener { (this.activity as AppActivity).dismissLoadingAlert() }
                }
            }
        }
        forumPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(ForumPost::class.java) }?.let()
            { listView_courseFeed_forum.adapter = ForumPostAdapter(context!!, isAdmin, it) }
        }
        listView_courseFeed_forum.setOnItemClickListener()
        {
            _, _, position, _ ->

            // TODO: - Show comments page
            val post = listView_courseFeed_forum.adapter.getItem(position) as ForumPost
        }
    }

    private fun resetPostLayout()
    {
        editText_post_forum_name.setText("")
        editText_post_forum_description.setText("")
    }
}