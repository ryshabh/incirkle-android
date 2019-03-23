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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ActivityPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_course_activities.*
import kotlinx.android.synthetic.main.list_item_post_activity.view.*
import kotlinx.android.synthetic.main.popup_add_activity.*
import kotlinx.android.synthetic.main.popup_add_activity.view.*


class CourseActivitiesFragment(): FileUploaderFragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_ADMIN = "Is Admin"
    }
    lateinit var dialog: AlertDialog
    lateinit var adapter :  ActivityPostAdapter

    class ActivityPostAdapter(private val context: Context, private val isAdmin: Boolean, private var dataSource: List<ActivityPost>): BaseAdapter()
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
            lateinit var popupicon : ImageView
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
            var request : RequestOptions = RequestOptions().error(R.drawable.ic_user).override(100,100).placeholder(R.drawable.ic_user)
            Glide.with(context)
                .load(post.imagepath)
                .apply(request)
                .into(viewModel.posterPictureImageView);
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
          //  viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE
            viewModel.deleteButton.setOnClickListener() { this.deleteActivityPost(post) }
            viewModel.downloadAttachmentButton.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.downloadAttachmentImage.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE

            viewModel.downloadAttachmentButton.setOnClickListener { post.attachmentPath?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } }



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
        val isAdmin = arguments?.getBoolean(IDENTIFIER_IS_ADMIN) ?: false
        //layout_post_activity_new.visibility = if(isAdmin) View.VISIBLE else View.GONE

        FirebaseAuth.getInstance().currentUser?.let()
        {

            FirebaseStorage.getInstance().getReference("UserProfiles").child(it.uid).downloadUrl.addOnSuccessListener {
                var request : RequestOptions = RequestOptions().error(R.drawable.ic_user).override(100,100).placeholder(R.drawable.ic_user)
                Glide.with(context)
                    .load(it.toString())
                    .apply(request)
                    .into(imageview_profileimage);
            }.addOnFailureListener {
                it.printStackTrace()
            };
        }
        card_view_createactivities.visibility = if(isAdmin) View.VISIBLE else View.GONE

        card_view_createactivities.setOnClickListener {

           var view = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_activity,null)

           view.button_activity_selectAttachment.setOnClickListener { this.selectFile { button_activity_selectAttachment.text = this.selectedFileUri?.getName(context!!) ?: getString(
               com.clockworks.incirkle.R.string.text_select_attachment) } }
            view.button_post_activity.setOnClickListener()
            {
                view.editText_post_activity_description.error = null

                val description = view.editText_post_activity_description.text.toString().trim()
                if (description.isBlank())
                {
                    view.editText_post_activity_description.error = "Activity description cannot be empty"
                    return@setOnClickListener
                }
                else
                {
                    val activityPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Activity Posts")

                    FirebaseAuth.getInstance().currentUser?.let()
                    {
                        val appActivity = this.activity as AppActivity
                        appActivity.showLoadingAlert()
                        activityPostsReference.add(ActivityPost(description, it.documentReference()))
                            .addOnFailureListener { appActivity.showError (it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                view.editText_post_activity_description.setText("")
                                this.updateAttachmentPath(it, FirebaseStorage.getInstance().getReference("Activity Attachments").child(it.id), "attachmentPath")
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

        val activityPostsReference = FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH)).collection("Activity Posts")

        activityPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        {
            result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(ActivityPost::class.java) }?.let()
            {
                for (item in it)
                {

                    try
                    {
                        FirebaseStorage.getInstance().getReference("UserProfiles").child(item.poster.path.replace("Users/","")).downloadUrl.addOnSuccessListener {
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
                adapter =  ActivityPostAdapter(context!!, isAdmin, it)
                listView_courseFeed_activities.adapter = adapter
            }
        }
    }


}
