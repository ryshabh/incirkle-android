package com.clockworks.incirkle.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.DocumentPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.clockworks.incirkle.filePicker.KotConstants
import com.clockworks.incirkle.filePicker.KotRequest
import com.clockworks.incirkle.filePicker.KotResult
import com.clockworks.incirkle.utils.AppConstantsValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_course_documents.*
import kotlinx.android.synthetic.main.list_item_post_document.view.*
import kotlinx.android.synthetic.main.popup_add_document.view.*

class CourseDocumentsFragment() : Fragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
    }

    lateinit var dialog: AlertDialog
    lateinit var adapter: DocumentPostAdapter
    private var documentPostList = ArrayList<DocumentPost>()

    private var attachmentResult: KotResult? = null


    private lateinit var popupRootView: View
    private lateinit var appActivity: AppActivity
    private lateinit var documentPostsReference: CollectionReference


    class DocumentPostAdapter(
        private val context: Context,
        private val isAdmin: Boolean,
        private var dataSource: List<DocumentPost>
    ) : BaseAdapter()
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

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteDocumentPost(post: DocumentPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Document Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
                { _, _ ->
                    post.reference?.delete()
                        ?.addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                        ?.addOnSuccessListener()
                        {
                            FirebaseStorage.getInstance().getReference("Document Attachments")
                                .child(post.reference!!.id).delete()
                                .addOnFailureListener {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
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
                viewModel.nameTextView = view.textView_documentPost_name
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
            try
            {
                var request: RequestOptions =
                    RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
                Glide.with(context)
                    .load(post.imagepath)
                    .apply(request)
                    .into(viewModel.posterPictureImageView)
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
            post.poster.get().addOnCompleteListener()
            { task ->

                task.exception?.let { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?: task.result?.serialize(User::class.java)?.let()
                    {
                        viewModel.posterNameTextView.setText(it.fullName())
                        // TODO: Set Display Picture
                    }
            }

            val date =
                android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val time =
                android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$time $date"
            viewModel.timestampTextView.setText(timestamp)
            viewModel.nameTextView.setText(post.name)
            viewModel.detailsTextView.setText(post.details)
            //  viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteDocumentPost(post) }


            viewModel.downloadAttachmentButton.setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(post.attachmentPath)
                    )
                )
            }

            viewModel.downloadAttachmentImage.setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(post.attachmentPath)
                    )
                )
            }

            if (post.attachmentDetail != null)
            {
                val fileTypeCondition: Boolean = post.attachmentDetail?.type?.startsWith(
                    "image",
                    true
                )!! || (post.attachmentDetail?.type?.startsWith("video", true)!!)
                if (fileTypeCondition)
                {
                    viewModel.downloadAttachmentImage.visibility = View.VISIBLE
                    viewModel.downloadAttachmentButton.visibility = View.GONE

                    Glide
                        .with(context)
                        .load(post.attachmentPath)
                        .into(viewModel.downloadAttachmentImage)

                }
                else
                {
                    viewModel.downloadAttachmentImage.visibility = View.GONE
                    viewModel.downloadAttachmentButton.visibility = View.VISIBLE
                    viewModel.downloadAttachmentButton.text = post.attachmentDetail?.name
                    viewModel.downloadAttachmentButton.paintFlags =
                        viewModel.downloadAttachmentButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
            }
            else
            {
                viewModel.downloadAttachmentImage.visibility = View.GONE
                viewModel.downloadAttachmentButton.visibility = View.GONE
            }


            /*if (post.attachmentPath != null)
            {
                viewModel.downloadAttachmentImage.visibility = View.VISIBLE
                viewModel.downloadAttachmentButton.visibility = View.VISIBLE
                post.attachmentPath?.let {

                    try
                    {
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
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            else
            {
                viewModel.downloadAttachmentImage.visibility = View.GONE
                viewModel.downloadAttachmentButton.visibility = View.GONE
            }*/
            viewModel.popupicon.setOnClickListener(View.OnClickListener {

                val popup = PopupMenu(context, it)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.actions, popup.menu)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                    val builder = AlertDialog.Builder(this.context)
                    builder.setTitle("Delete Forum Post")
                    builder.setMessage("Are you sure you wish to delete this post?")
                    builder.setPositiveButton("Delete")
                    { _, _ ->
                        post.reference?.delete()
                            ?.addOnFailureListener {
                                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                            ?.addOnSuccessListener()
                            {
                                if (post.attachmentPath.isNullOrEmpty())
                                {
                                    return@addOnSuccessListener
                                }
                                FirebaseStorage.getInstance().getReference("Document Attachments")
                                    .child(post.attachmentDetail?.nameInFirebase!!).delete()
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            it.localizedMessage,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
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

        appActivity = this.activity as AppActivity
        documentPostsReference =
            FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                .collection("Document Posts")

        val isTeacher = arguments?.getBoolean(IDENTIFIER_IS_TEACHER) ?: false
        val isTeachingAssistant = arguments?.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT) ?: false
        FirebaseAuth.getInstance().currentUser?.let()
        {

            FirebaseStorage.getInstance().getReference("UserProfiles").child(it.uid).downloadUrl.addOnSuccessListener {
                try
                {
                    var request: RequestOptions =
                        RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
                    Glide.with(context)
                        .load(it.toString())
                        .apply(request)
                        .into(imageview_profileimage);
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            };
        }
        //  layout_post_document_new.visibility = if(isAdmin) View.VISIBLE else View.GONE
        card_view_createdocument.visibility = if (isTeacher || isTeachingAssistant) View.VISIBLE else View.GONE
        card_view_createdocument.setOnClickListener {
            popupRootView = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_document, null)

            popupRootView.button_document_selectAttachment_add.setOnClickListener {
                //                val appActivity = this.activity as AppActivity
//                appActivity.selectFile {
//                    view.button_document_selectAttachment_add.error = null
//                    view.button_document_selectAttachment_add.text =
//                        appActivity.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment)
//                }

                attachmentResult = null
                KotRequest.File(this, KotConstants.REQUEST_FILE)
                    .isMultiple(false)
                    .setMimeType(KotConstants.FILE_TYPE_FILE_ALL)
                    .pick()
            }

            popupRootView.button_document_forum_add.setOnClickListener()
            {

                popupRootView.editText_post_document_name_add.error = null
                popupRootView.editText_post_document_description_add.error = null
                popupRootView.button_document_selectAttachment_add.error = null

                val name = popupRootView.editText_post_document_name_add.text.toString().trim()
                val details = popupRootView.editText_post_document_description_add.text.toString().trim()
                if (name.isBlank())
                {
                    popupRootView.editText_post_document_name_add.error = "Document name cannot be empty"
                    return@setOnClickListener
                }
                else if (details.isBlank())
                {
                    popupRootView.editText_post_document_description_add.error = "Document description cannot be empty"
                    return@setOnClickListener
                }
                else if (attachmentResult == null)
                {
                    popupRootView.button_document_selectAttachment_add.error = "Attachment is missing"
                    return@setOnClickListener
                }
                else if (attachmentResult != null)
                {
                        // text and attachment both

                        appActivity.showLoadingAlert()
                        val fileNameInFirebase = System.currentTimeMillis().toString()
                        attachmentResult?.nameInFirebase = fileNameInFirebase
                        AppConstantsValue.documentStorgagRef.child(fileNameInFirebase).putFile(attachmentResult?.uri!!)
                            .addOnCompleteListener {
                                if (it.isSuccessful)
                                {
                                    AppConstantsValue.documentStorgagRef.child(fileNameInFirebase)
                                        .downloadUrl.addOnSuccessListener {
                                        // file uploaded successfully
                                        val fileUrl = it.toString()
                                        val creatorRef = FirebaseAuth.getInstance().currentUser!!.documentReference()
                                        val activityPost = DocumentPost(name,details, creatorRef, fileUrl, attachmentResult)

                                        documentPostsReference.add(activityPost).addOnCompleteListener {
                                            appActivity.dismissLoadingAlert()
                                            attachmentResult = null
                                            if (it.isSuccessful)
                                            {
//                                           Toast.makeText(activity, it.result?.path, Toast.LENGTH_LONG).show()
                                            }
                                            else
                                            {
                                                Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }.addOnFailureListener {
                                        attachmentResult = null
                                        appActivity.dismissLoadingAlert()
                                        Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                                else
                                {
                                    attachmentResult = null
                                    appActivity.dismissLoadingAlert()
                                    Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                                }
                            }








//                    FirebaseAuth.getInstance().currentUser?.let()
//                    {
//                        val appActivity = this.activity as AppActivity
//                        appActivity.showLoadingAlert()
//                        documentPostsReference.add(DocumentPost(name, details, it.documentReference()))
//                            .addOnFailureListener { appActivity.showError(it) }
//                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
//                            .addOnSuccessListener()
//                            {
//                                this.resetPostLayout()
//                                (this.activity as AppActivity).updateAttachmentPath(
//                                    it,
//                                    FirebaseStorage.getInstance().getReference("Document Attachments").child(it.id),
//                                    "attachmentPath"
//                                )
//                            }
//                    }
                }

                dialog.dismiss()
            }
            dialog = AlertDialog.Builder(activity)
                .setView(popupRootView)
                .setCancelable(true)
                .create()



            dialog.show()
        }
        button_document_selectAttachment.setOnClickListener {
            (this.activity as AppActivity).selectFile {
                button_document_selectAttachment.text =
                    (this.activity as AppActivity).selectedFileUri?.getName(context!!)
                        ?: getString(R.string.text_select_attachment)
            }
        }


        /*
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
                else if ((this.activity as AppActivity).selectedFileUri == null)
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
                            .addOnFailureListener { appActivity.showError(it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                this.resetPostLayout()
                                (this.activity as AppActivity).updateAttachmentPath(
                                    it,
                                    FirebaseStorage.getInstance().getReference("Document Attachments").child(it.id),
                                    "attachmentPath"
                                )
                            }
                    }
                }
            }*/

        // set adapter
        adapter = DocumentPostAdapter(context!!, isTeacher || isTeachingAssistant, documentPostList)
        listView_courseFeed_documents.adapter = adapter

        try
        {
            val documentPostsReference =
                FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                    .collection("Document Posts")
            documentPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
            { result, e ->
                e?.let {
                    try
                    {
                        (this.activity as AppActivity).showError(it)
                    } catch (e: Exception)
                    {
                        e.printStackTrace()
                    }

                }
                    ?: result?.map {
                        it.serialize(DocumentPost::class.java)
                    }?.let()
                    {
                        for (item in it)
                        {
                            try
                            {
                                FirebaseStorage.getInstance().getReference("UserProfiles")
                                    .child(item.poster.path.replace("Users/", ""))
                                    .downloadUrl.addOnSuccessListener {
                                    item.imagepath = it.toString();
                                    adapter.notifyDataSetChanged()
                                }.addOnFailureListener {
                                    it.printStackTrace()
                                };
                            } catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        documentPostList.clear()
                        documentPostList.addAll(it)
                        adapter.notifyDataSetChanged()
//                    adapter = DocumentPostAdapter(context!!, isTeacher || isTeachingAssistant, it)
//                    listView_courseFeed_documents.adapter = adapter

                    }
            }
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun resetPostLayout()
    {
        editText_post_document_name.setText("")
        editText_post_document_details.setText("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (KotConstants.REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK)
        {
            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            attachmentResult = result!!.get(0)
            if (attachmentResult != null)
            {
                popupRootView.button_document_selectAttachment_add.error = null
                popupRootView.button_document_selectAttachment_add.text = attachmentResult!!.name
            }
        }

    }


}