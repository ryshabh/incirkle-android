package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.DocumentPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_course_documents.*
import kotlinx.android.synthetic.main.list_item_post_document.view.*
import kotlinx.android.synthetic.main.popup_add_document.view.*

class CourseDocumentsFragment(): FileUploaderFragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }

    lateinit var dialog: AlertDialog
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
            lateinit var downloadAttachmentButton: TextView
            lateinit var popupicon: ImageView
            lateinit var downloadAttachmentImage: ImageView
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
                post.reference?.delete()
                    ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?.addOnSuccessListener()
                    {
                        FirebaseStorage.getInstance().getReference("Document Attachments").child(post.reference!!.id).delete()
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
                view = inflater.inflate(R.layout.list_item_post_document, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_documentPost_posterPicture
                viewModel.posterNameTextView = view.textView_documentPost_posterName
                viewModel.timestampTextView = view.textView_documentPost_timestamp
                viewModel.deleteButton = view.button_documentPost_delete
                viewModel.nameTextView= view.textView_documentPost_name
                viewModel.detailsTextView = view.textView_documentPost_details
                viewModel.downloadAttachmentButton = view.button_documentPost_download_attachment
                viewModel.downloadAttachmentImage = view.button_documentPost_download_images
                viewModel.popupicon = view.popupicon2

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
            viewModel.detailsTextView.setText(post.details)
          //  viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteDocumentPost(post) }
            viewModel.downloadAttachmentImage.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.downloadAttachmentButton.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE

            viewModel.downloadAttachmentButton.setOnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.attachmentPath))) }


                if(post.attachmentPath != null)
                {
                    post.attachmentPath?.let {


                        Glide
                            .with(context)
                            .load(it)
                            .listener(object : RequestListener<Drawable>
                            {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean
                                {
                                    viewModel.downloadAttachmentButton.visibility = View.VISIBLE
                                    viewModel.downloadAttachmentImage.visibility = View.GONE
                                    return false

                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean
                                {
                                    viewModel.downloadAttachmentButton.visibility = View.GONE
                                    viewModel.downloadAttachmentImage.visibility = View.VISIBLE
                                    return false
                                }

                            })
                            .into(viewModel.downloadAttachmentImage);

                    }
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
                                FirebaseStorage.getInstance().getReference("Document Attachments").child(post.reference!!.id).delete()
                                    .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                            }
                    }
                    builder.setNegativeButton("Cancel", null)
                    builder.create().show()

                    true
                })
                popup.show()
            })
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

      //  layout_post_document_new.visibility = if(isAdmin) View.VISIBLE else View.GONE
        card_view_createdocument.visibility = if(isAdmin) View.VISIBLE else View.GONE
        card_view_createdocument.setOnClickListener {
            var view = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_document,null)

            view.button_document_selectAttachment_add.setOnClickListener { this.selectFile { view.button_document_selectAttachment_add.text = this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment) } }
            val forumPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(CourseForumFragment.IDENTIFIER_COURSE_PATH)).collection("Forum Posts")

            view.button_document_forum_add.setOnClickListener()
            {

                view.editText_post_document_name_add.error = null
                view.editText_post_document_description_add.error = null

                val name = view.editText_post_document_name_add.text.toString().trim()
                val details = view.editText_post_document_description_add.text.toString().trim()
                if (name.isBlank())
                {
                    view.editText_post_document_name_add.error = "Document name cannot be empty"
                    return@setOnClickListener
                }
                else if (details.isBlank())
                {
                    view.editText_post_document_description_add.error = "Document description cannot be empty"
                    return@setOnClickListener
                }
                else if (this.selectedFileUri == null)
                {
                    view.button_document_selectAttachment_add.error = "Attachment is missing"
                }
                else
                {
                    val documentPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Document Posts")

                    FirebaseAuth.getInstance().currentUser?.let()
                    {
                        val appActivity = this.activity as AppActivity
                        appActivity.showLoadingAlert()
                        documentPostsReference.add(DocumentPost(name, details, it.documentReference()))
                            .addOnFailureListener { appActivity.showError (it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                this.resetPostLayout()
                                this.updateAttachmentPath(it, FirebaseStorage.getInstance().getReference("Document Attachments").child(it.id), "attachmentPath")
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
        button_document_selectAttachment.setOnClickListener { this.selectFile { button_document_selectAttachment.text = this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment) } }



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
            else if (this.selectedFileUri == null)
            {
                button_document_selectAttachment.error = "Attachment is missing"
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    val appActivity = this.activity as AppActivity
                    appActivity.showLoadingAlert()
                    documentPostsReference.add(DocumentPost(name, details, it.documentReference()))
                        .addOnFailureListener { appActivity.showError (it) }
                        .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                        .addOnSuccessListener()
                        {
                            this.resetPostLayout()
                            this.updateAttachmentPath(it, FirebaseStorage.getInstance().getReference("Document Attachments").child(it.id), "attachmentPath")
                        }
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