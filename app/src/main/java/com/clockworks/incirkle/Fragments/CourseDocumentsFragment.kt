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
import com.clockworks.incirkle.Models.DocumentPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_course_documents.*
import kotlinx.android.synthetic.main.list_item_post_document.view.*

class CourseDocumentsFragment(): Fragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }

    class DocumentPostAdapter(private val context: Context, private val isAdmin: Boolean, private var dataSource: List<DocumentPost>): BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var nameTextView: TextView
            lateinit var detailsTextView: TextView
        }

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteDocumentPost(post: DocumentPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Document Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
            {
                _, _ ->
                post.reference?.delete()?.addOnFailureListener() { Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show() }
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
                view = inflater.inflate(R.layout.list_item_post_document, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_documentPost_posterPicture
                viewModel.posterNameTextView = view.textView_documentPost_posterName
                viewModel.timestampTextView = view.textView_documentPost_timestamp
                viewModel.deleteButton = view.button_documentPost_delete
                viewModel.nameTextView= view.textView_documentPost_name
                viewModel.detailsTextView = view.textView_documentPost_details
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

                task.exception?.let { Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show() }
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
            viewModel.detailsTextView.setText(post.details)
            viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteDocumentPost(post) }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val rootView = inflater.inflate(R.layout.fragment_course_documents, container, false)
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
        layout_post_document_new.visibility = if(isAdmin) View.VISIBLE else View.GONE

        val documentPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Document Posts")

        button_post_document.setOnClickListener()
        {
            editText_post_document_name.error = null
            editText_post_document_details.error = null

            val name = editText_post_document_name.text.toString().trim()
            val details = editText_post_document_details.text.toString().trim()
            if (name.isBlank())
            {
                editText_post_document_name.error = "Document name cannot be empty"
                return@setOnClickListener
            }
            else if (details.isBlank())
            {
                editText_post_document_details.error = "Document description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    (this.activity as AppActivity).showLoadingAlert()
                    documentPostsReference.add(DocumentPost(name, details, it.documentReference()))
                        .addOnSuccessListener { this.resetPostLayout() }
                        .addOnFailureListener { (this.activity as AppActivity).showError(it) }
                        .addOnCompleteListener { (this.activity as AppActivity).dismissLoadingAlert() }
                }
            }
        }
        documentPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(DocumentPost::class.java) }?.let()
            { listView_courseFeed_documents.adapter = DocumentPostAdapter(context!!, isAdmin, it) }
        }
    }

    private fun resetPostLayout()
    {
        editText_post_document_name.setText("")
        editText_post_document_details.setText("")
    }
}