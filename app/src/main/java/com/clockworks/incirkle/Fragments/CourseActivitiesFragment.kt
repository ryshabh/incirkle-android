package com.clockworks.incirkle.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ActivityPost
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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_course_activities.*
import kotlinx.android.synthetic.main.list_item_post_activity.view.*
import kotlinx.android.synthetic.main.popup_add_activity.view.*


class CourseActivitiesFragment() : Fragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
    }

    private var TAG = CourseActivitiesFragment.javaClass.simpleName

    private lateinit var listenerRegistration: ListenerRegistration

    lateinit var dialog: AlertDialog
    lateinit var adapter: ActivityPostAdapter
    private var activityPostList = ArrayList<ActivityPost>()
    private var attachmentResult: KotResult? = null

    private lateinit var popupRootView: View
    private lateinit var activityPostsReference: CollectionReference
    private lateinit var appActivity: AppActivity

    class ActivityPostAdapter(
        private val context: Context,
        private val isTeacher: Boolean,
        private val isTeachingAssistant: Boolean,
        private var dataSource: List<ActivityPost>
    ) : BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var deleteButton: ImageButton
            lateinit var descriptionTextView: TextView
            lateinit var downloadAttachmentButton: TextView
            lateinit var downloadAttachmentImage: ImageView
            lateinit var popupicon: ImageView
        }

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteActivityPost(post: ActivityPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Activity Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
                { _, _ ->
                    post.reference?.delete()
                        ?.addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                        ?.addOnSuccessListener()
                        {
                            if (post.attachmentDetail != null)
                            {
                                FirebaseStorage.getInstance().getReference("Activity Attachments")
                                    .child(post.attachmentDetail?.nameInFirebase!!).delete()
                                    .addOnFailureListener {
                                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                    }
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
                view = inflater.inflate(com.clockworks.incirkle.R.layout.list_item_post_activity, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_activityPost_posterPicture
                viewModel.posterNameTextView = view.textView_activityPost_posterName
                viewModel.timestampTextView = view.textView_activityPost_timestamp
                viewModel.deleteButton = view.button_activityPost_delete
                viewModel.descriptionTextView = view.textView_activityPost_description
                viewModel.downloadAttachmentButton = view.button_activityPost_download_attachment
                viewModel.downloadAttachmentImage = view.button_activityPost_download_images
                viewModel.popupicon = view.popupicon
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
                    .into(viewModel.posterPictureImageView);
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
            viewModel.descriptionTextView.setText(post.description)
            //  viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.popupicon.visibility = if (isTeacher || isTeachingAssistant) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteActivityPost(post) }

            viewModel.downloadAttachmentButton.setOnClickListener {
                post.attachmentPath?.let {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    )
                }
            }

            viewModel.downloadAttachmentImage.setOnClickListener {
                post.attachmentPath?.let {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    )
                }
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
                    builder.setTitle("Delete Activity Post")
                    builder.setMessage("Are you sure you wish to delete this post?")
                    builder.setPositiveButton("Delete",
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
                                    FirebaseStorage.getInstance().getReference("Activity Attachments")
                                        .child(post.attachmentDetail?.nameInFirebase!!).delete()
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                it.localizedMessage,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                        })
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
        val rootView = inflater.inflate(com.clockworks.incirkle.R.layout.fragment_course_activities, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    private fun initialize()
    {
        activityPostsReference =
            FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                .collection("Activity Posts")

        appActivity = this.activity as AppActivity


        val isTeacher = arguments?.getBoolean(IDENTIFIER_IS_TEACHER) ?: false
        val isTeachingAssistant = arguments?.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT) ?: false
        //layout_post_activity_new.visibility = if(isAdmin) View.VISIBLE else View.GONE

        FirebaseAuth.getInstance().currentUser?.let()
        {

            FirebaseStorage.getInstance().getReference("UserProfiles").child(it.uid).downloadUrl.addOnSuccessListener {
                try
                {
                    var request: RequestOptions =
                        RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
                    Glide.with(this)
                        .load(it.toString())
                        .apply(request)
                        .into(imageview_profileimage)
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            };
        }
        card_view_createactivities.visibility = if (isTeacher || isTeachingAssistant) View.VISIBLE else View.GONE

        card_view_createactivities.setOnClickListener {

            popupRootView = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_activity, null)


            popupRootView.button_activity_selectAttachment.setOnClickListener {
                //                val appActivity = this.activity as AppActivity
//                appActivity.selectFile {
//                    view.button_activity_selectAttachment.text =
//                        appActivity.selectedFileUri?.getName(context!!) ?: getString(
//                            com.clockworks.incirkle.R.string.text_select_attachment
//                        )
//                }
                attachmentResult = null
                KotRequest.File(this, KotConstants.REQUEST_FILE)
                    .isMultiple(false)
                    .setMimeType(KotConstants.FILE_TYPE_FILE_ALL)
                    .pick()

            }
            popupRootView.button_post_activity.setOnClickListener()
            {
                popupRootView.editText_post_activity_description.error = null

                val description = popupRootView.editText_post_activity_description.text.toString().trim()
                if (description.isBlank())
                {
                    popupRootView.editText_post_activity_description.error = "Activity description cannot be empty"
                    return@setOnClickListener
                }
                else if (attachmentResult == null)
                {
                    // only text
                    appActivity.showLoadingAlert()
                    popupRootView.editText_post_activity_description.setText("")
                    val creatorRef = FirebaseAuth.getInstance().currentUser!!.documentReference()
                    val activityPost = ActivityPost(description, creatorRef, null, null)

                    activityPostsReference.add(activityPost).addOnCompleteListener {
                        appActivity.dismissLoadingAlert()
                        attachmentResult = null
                        if (it.isSuccessful)
                        {
//                                    Toast.makeText(activity, it.result?.path, Toast.LENGTH_LONG).show()
                        }
                        else
                        {
                            Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else if (attachmentResult != null)
                {
                    // text and attachment both

                    appActivity.showLoadingAlert()
                    popupRootView.editText_post_activity_description.setText("")
                    val fileNameInFirebase = System.currentTimeMillis().toString()
                    attachmentResult?.nameInFirebase = fileNameInFirebase
                    AppConstantsValue.activityStorgageRef.child(fileNameInFirebase).putFile(attachmentResult?.uri!!)
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                AppConstantsValue.activityStorgageRef.child(fileNameInFirebase)
                                    .downloadUrl.addOnSuccessListener {
                                    // file uploaded successfully
                                    val fileUrl = it.toString()
                                    val creatorRef = FirebaseAuth.getInstance().currentUser!!.documentReference()
                                    val activityPost = ActivityPost(description, creatorRef, fileUrl, attachmentResult)

                                    activityPostsReference.add(activityPost).addOnCompleteListener {
                                        appActivity.dismissLoadingAlert()
                                        attachmentResult = null
                                        if (it.isSuccessful)
                                        {
//                                    Toast.makeText(activity, it.result?.path, Toast.LENGTH_LONG).show()
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


                }

                dialog.dismiss()
            }
            dialog = AlertDialog.Builder(activity)
                .setView(popupRootView)
                .setCancelable(true)
                .create()



            dialog.show()

        }

        // set adapter
        adapter = ActivityPostAdapter(context!!, isTeacher, isTeachingAssistant, activityPostList)
        listView_courseFeed_activities.adapter = adapter

        try
        {

            val activityPostsReference =
                FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                    .collection("Activity Posts")
            listenerRegistration = activityPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
            { result, e ->
                e?.let { (this.activity as AppActivity).showError(it) }
                    ?: result?.map { it.serialize(ActivityPost::class.java) }?.let()
                    {
                        try
                        {
                            for (item in it)
                            {


                                FirebaseStorage.getInstance().getReference("UserProfiles")
                                    .child(item.poster.path.replace("Users/", "")).downloadUrl.addOnSuccessListener {
                                    item.imagepath = it.toString();
                                    adapter.notifyDataSetChanged()

                                }.addOnFailureListener {
                                    it.printStackTrace()
                                };

                            }
                            activityPostList.clear()
                            activityPostList.addAll(it)
                            adapter.notifyDataSetChanged()
//                    adapter = ActivityPostAdapter(context!!, isAdmin, it)
//                    listView_courseFeed_activities.adapter = adapter
                        } catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
            }

        } catch (e: Exception)
        {
            e.printStackTrace()
        }
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
                popupRootView.button_activity_selectAttachment.text = attachmentResult!!.name
            }
        }

    }

    override fun onDestroy()
    {
        listenerRegistration.remove()
        super.onDestroy()
    }
}
