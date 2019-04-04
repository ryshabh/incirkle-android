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
import com.clockworks.incirkle.Activities.CommentsActivity
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.ForumPost
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
import kotlinx.android.synthetic.main.fragment_course_forum.*
import kotlinx.android.synthetic.main.fragment_course_forum.view.*
import kotlinx.android.synthetic.main.list_item_post_forum.view.*
import kotlinx.android.synthetic.main.popup_add_forum.view.*


class CourseForumFragment() : Fragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
    }

    private var root: View? = null
    private var formList = ArrayList<ForumPost>()

    private lateinit var listenerRegistration: ListenerRegistration


    //    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
//    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var listView_comments: ListView

    lateinit var dialog: AlertDialog
    lateinit var adapter: ForumPostAdapter

    private var attachmentResult: KotResult? = null

    private lateinit var popupRootView: View
    private lateinit var appActivity: AppActivity
    private lateinit var forumPostsReference: CollectionReference

    class ForumPostAdapter(
        private val context: Context,
        private val courseForumFragment: CourseForumFragment,
        private val isTeacher: Boolean,
        private val isTeachingAssistant: Boolean,
        private var teacherPath: String,
        private var dataSource: List<ForumPost>
    ) : BaseAdapter()
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
            lateinit var downloadAttachmentImage: ImageView

        }

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteForumPost(post: ForumPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Forum Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete")
            { _, _ ->
                post.reference?.delete()
                    ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?.addOnSuccessListener()
                    {
                        FirebaseStorage.getInstance().getReference("Forum Attachments").child(post.reference!!.id)
                            .delete()
                            .addOnFailureListener {
                                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
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

            //   if (convertView == null)
            //{
            view = inflater.inflate(R.layout.list_item_post_forum, parent, false)
            viewModel = ViewModel()
            viewModel.posterPictureImageView = view.imageView_forumPost_posterPicture
            viewModel.posterNameTextView = view.textView_forumPost_posterName
            viewModel.instructorTextView = view.textView_forumPost_instructor
            viewModel.timestampTextView = view.textView_forumPost_timestamp
            viewModel.deleteButton = view.button_forumPost_delete
            viewModel.nameTextView = view.textView_forumPost_name
            viewModel.descriptionTextView = view.textView_forumPost_details
            viewModel.downloadAttachmentButton = view.button_forumPost_download_attachment
            viewModel.downloadAttachmentImage = view.button_forumPost_download_images
            viewModel.popupicon = view.popupicon1
            viewModel.textview_forumPost_count = view.textview_forumPost_count
            viewModel.button_activityForum_download_attachment = view.button_activityForum_download_attachment
            view.tag = viewModel
            //  }
            /*  else
              {
                  view = convertView
                  viewModel = convertView.tag as ViewModel
              }*/
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
                        viewModel.posterNameTextView.setText(it.fullName() + if (it.type == User.Type.TEACHER) " (Intructor) " else "")
                        viewModel.popupicon.visibility = when
                        {
                            isTeacher -> View.VISIBLE
                            it.phoneNumber == FirebaseAuth.getInstance().currentUser?.phoneNumber -> View.VISIBLE
                            else -> View.GONE
                        }
                    }
            }

            viewModel.instructorTextView.visibility = if (post.poster.path == teacherPath) View.VISIBLE else View.GONE
            val date =
                android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val time =
                android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$time $date"
            viewModel.timestampTextView.setText(timestamp)


            viewModel.nameTextView.setText(post.name)


            viewModel.descriptionTextView.setText(post.description)
            //     viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
//            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE
//            viewModel.popupicon.visibility = if (isTeacher) View.VISIBLE else View.GONE


            viewModel.deleteButton.setOnClickListener() { this.deleteForumPost(post) }

            if (post.attachmentDetail != null)
            {
                val fileTypeCondition: Boolean = post.attachmentDetail?.type?.startsWith(
                    "image",
                    true
                )!! || (post.attachmentDetail?.type?.startsWith("video", true)!!)
                if (fileTypeCondition)
                {
                    viewModel.downloadAttachmentImage.visibility = View.VISIBLE
                    viewModel.button_activityForum_download_attachment.visibility = View.GONE

                    Glide
                        .with(context)
                        .load(post.attachmentPath)
                        .into(viewModel.downloadAttachmentImage)

                }
                else
                {
                    viewModel.downloadAttachmentImage.visibility = View.GONE
                    viewModel.button_activityForum_download_attachment.visibility = View.VISIBLE
                    viewModel.button_activityForum_download_attachment.text = post.attachmentDetail?.name
                    viewModel.button_activityForum_download_attachment.paintFlags =
                        viewModel.button_activityForum_download_attachment.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
            }
            else
            {
                viewModel.downloadAttachmentImage.visibility = View.GONE
                viewModel.button_activityForum_download_attachment.visibility = View.GONE
            }


            /*if (post.attachmentPath != null)
            {
                viewModel.button_activityForum_download_attachment.visibility = View.VISIBLE
                viewModel.downloadAttachmentImage.visibility = View.VISIBLE
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
                                    viewModel.button_activityForum_download_attachment.visibility = View.VISIBLE
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
                                    viewModel.button_activityForum_download_attachment.visibility = View.GONE
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


            }
            else
            {
                viewModel.button_activityForum_download_attachment.visibility = View.GONE
                viewModel.downloadAttachmentImage.visibility = View.GONE
            }*/
            viewModel.button_activityForum_download_attachment.setOnClickListener {
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

            viewModel.textview_forumPost_count.text = post.commentCount.toString()

            post.reference!!.collection("Comments").get()
                .addOnSuccessListener()
                {
                    post.commentCount = it.size()
                    viewModel.textview_forumPost_count.text = it.size().toString()
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
                                FirebaseStorage.getInstance().getReference("Forum Attachments")
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
            view.llCommentcount.setOnClickListener()
            {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra(CommentsActivity.IDENTIFIER_IS_TEACHER, isTeacher)
                intent.putExtra(CommentsActivity.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)
                intent.putExtra(CommentsActivity.IDENTIFIER_POST_PATH, post.reference!!.path)
                intent.putExtra(CommentsActivity.SELECTED_ITEM_NO, position)
                courseForumFragment.activity?.startActivityForResult(intent, 25)
                courseForumFragment.activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
//                courseForumFragment.showBottomSheetDialog(isTeacher, isTeachingAssistant, post.reference!!.path)
                /*courseForumFragment.bottomSheetLayout.visibility = View.VISIBLE

                // get data from firebase
                var commentsReference =
                    FirebaseFirestore.getInstance().document(post.reference!!.path)
                        .collection("Comments")
                commentsReference.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener()
                { result, e ->
                    e?.let { (courseForumFragment.activity as AppActivity).showError(it) }
                        ?: result?.map { it.serialize(Comment::class.java) }?.let()
                        {
                            courseForumFragment.listView_comments.adapter =
                                CommentsAdapter(courseForumFragment, isTeacher!!, isTeachingAssistant!!, it)
                        }
                }*/

            }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        root = inflater.inflate(R.layout.fragment_course_forum, container, false)
//        bottomSheetLayout = root!!.findViewById(R.id.card_bottom_sheet)
        listView_comments = root!!.findViewById(R.id.listView_comments);

//        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheetLayout)
//
//        /**
//         * bottom sheet state change listener
//         * we are changing button text when sheet changed state
//         * */
//        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback()
//        {
//            override fun onStateChanged(bottomSheet: View, newState: Int)
//            {
//                when (newState)
//                {
//                    BottomSheetBehavior.STATE_HIDDEN ->
//                    {
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED ->
//                    {
//                        Log.d("CourseForum", "state expanded called")
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED ->
//                    {
//                        Log.d("CourseForum", "state close called")
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING ->
//                    {
//                    }
//                    BottomSheetBehavior.STATE_SETTLING ->
//                    {
//                    }
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float)
//            {
//
//            }
//        })

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    private fun initialize()
    {

        appActivity = this.activity as AppActivity
        forumPostsReference =
            FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                .collection("Forum Posts")

        val isTeacher = arguments?.getBoolean(IDENTIFIER_IS_TEACHER) ?: false
        val isTeacherAssistant = arguments?.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT) ?: false

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
        card_view_createforum.setOnClickListener {
            popupRootView = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_forum, null)

            popupRootView.button_forum_selectAttachment.setOnClickListener {
                //                val appActivity = this.activity as AppActivity
//                appActivity.selectFile {
//                    view.button_forum_selectAttachment.text =
//                        appActivity.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment)
                attachmentResult = null
                KotRequest.File(this, KotConstants.REQUEST_FILE)
                    .isMultiple(false)
                    .setMimeType(KotConstants.FILE_TYPE_FILE_ALL)
                    .pick()
//                }
            }

            popupRootView.button_post_forum.setOnClickListener()
            {
                popupRootView.editText_post_forum_name.error = null
                popupRootView.editText_post_forum_description.error = null

                val name = popupRootView.editText_post_forum_name.text.toString().trim()
                val description = popupRootView.editText_post_forum_description.text.toString().trim()
                if (name.isBlank())
                {
                    popupRootView.editText_post_forum_name.error = "Forum Post name cannot be empty"
                    return@setOnClickListener
                }
                else if (description.isBlank())
                {
                    popupRootView.editText_post_forum_description.error = "Forum Post description cannot be empty"
                    return@setOnClickListener
                }
                else if (attachmentResult == null)
                {
                    // only text
                    appActivity.showLoadingAlert()
                    val creatorRef = FirebaseAuth.getInstance().currentUser!!.documentReference()
                    val forumPost = ForumPost(name, description, creatorRef, null, null)
                    forumPostsReference.add(forumPost).addOnCompleteListener {
                        appActivity.dismissLoadingAlert()
                        if (!it.isSuccessful)
                        {
                            Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }

                    }
                }
                else if (attachmentResult != null)
                {
                    // text and attachment both
                    appActivity.showLoadingAlert()
                    val fileNameInFirebase = System.currentTimeMillis().toString()
                    attachmentResult?.nameInFirebase = fileNameInFirebase
                    AppConstantsValue.forumStorageRef.child(fileNameInFirebase).putFile(attachmentResult?.uri!!)
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                AppConstantsValue.forumStorageRef.child(fileNameInFirebase)
                                    .downloadUrl.addOnSuccessListener {
                                    // file uploaded successfully
                                    val fileUrl = it.toString()
                                    val creatorRef = FirebaseAuth.getInstance().currentUser!!.documentReference()
                                    val forumPost = ForumPost(name, description, creatorRef, fileUrl, attachmentResult)

                                    forumPostsReference.add(forumPost).addOnCompleteListener {
                                        appActivity.dismissLoadingAlert()
                                        attachmentResult = null
                                        if (!it.isSuccessful)
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


                    /*FirebaseAuth.getInstance().currentUser?.let()
                    {
                        val appActivity = this.activity as AppActivity
                        appActivity.showLoadingAlert()
                        forumPostsReference.add(ForumPost(name, description, it.documentReference()))
                            .addOnFailureListener { appActivity.showError(it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                popupRootView.editText_post_forum_name.setText("")
                                popupRootView.editText_post_forum_description.setText("")
                                (activity as CourseFeedActivity).updateAttachmentPath(
                                    it,
                                    FirebaseStorage.getInstance().getReference("Forum Attachments").child(it.id),
                                    "attachmentPath"
                                )
                            }
                    }*/
                }
                dialog.dismiss()
            }
            dialog = AlertDialog.Builder(activity)
                .setView(popupRootView)
                .setCancelable(true)
                .create()



            dialog.show()
        }

        adapter = ForumPostAdapter(
            context!!, this, isTeacher, isTeacherAssistant, arguments?.getString(
                IDENTIFIER_COURSE_TEACHER_PATH
            ) ?: "", formList
        )
        root!!.listView_courseFeed_forum.adapter = adapter

        val forumPostsReference =
            FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                .collection("Forum Posts")

        listenerRegistration = forumPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        { result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
                ?: result?.map { it.serialize(ForumPost::class.java) }?.let()
                {
                    for (item in it)
                    {

                        try
                        {
                            FirebaseStorage.getInstance().getReference("UserProfiles")
                                .child(item.poster.path.replace("Users/", "")).downloadUrl.addOnSuccessListener {
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

                    formList.clear()
                    formList.addAll(it)
                    adapter.notifyDataSetChanged()

                }
        }
    }

    /*
    fun showBottomSheetDialog(isTeacher: Boolean, isTeachingAssistant: Boolean, path: String)
    {
        val bundle = Bundle();
        bundle.putBoolean(CommentsBottomSheetFragment.IDENTIFIER_IS_TEACHER, isTeacher)
        bundle.putBoolean(CommentsBottomSheetFragment.IDENTIFIER_IS_TEACHING_ASSISTANT, isTeachingAssistant)
        bundle.putString(CommentsBottomSheetFragment.IDENTIFIER_POST_PATH, path)
        val commentsBottomSheetFragment = CommentsBottomSheetFragment()
        commentsBottomSheetFragment.arguments = bundle
        commentsBottomSheetFragment.show(activity?.supportFragmentManager, commentsBottomSheetFragment.tag)
    }


      class CommentsAdapter(
          private val cBSFragment: CourseForumFragment,
          private val isTeacher: Boolean,
          private val isTeachingAssistant: Boolean,
          private var dataSource: List<Comment>
      ) : BaseAdapter()
      {
          private class ViewModel
          {
              lateinit var posterPictureImageView: ImageView
              lateinit var posterNameTextView: TextView
              lateinit var timestampTextView: TextView
              lateinit var deleteButton: ImageButton
              lateinit var contentTextView: TextView
          }

          private val inflater: LayoutInflater =
              cBSFragment.activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

          private fun deleteComment(comment: Comment)
          {
              val builder = AlertDialog.Builder(cBSFragment.activity)
              builder.setTitle("Delete Comment")
              builder.setMessage("Are you sure you wish to delete this comment?")
              builder.setPositiveButton("Delete")
              { _, _ ->
                  comment.reference?.delete()
                      ?.addOnFailureListener {
                          Toast.makeText(
                              cBSFragment.activity,
                              it.localizedMessage,
                              Toast.LENGTH_LONG
                          ).show()
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

              if (convertView == null)
              {
                  view = inflater.inflate(R.layout.list_item_comment, parent, false)
                  viewModel = ViewModel()
                  viewModel.posterPictureImageView = view.imageView_comment_posterPicture
                  viewModel.posterNameTextView = view.textView_comment_posterName
                  viewModel.timestampTextView = view.textView_comment_timestamp
                  viewModel.deleteButton = view.button_comment_delete
                  viewModel.contentTextView = view.textView_comment_content
                  view.tag = viewModel
              }
              else
              {
                  view = convertView
                  viewModel = convertView.tag as ViewModel
              }

              val comment = this.dataSource[position]
              comment.poster.get().addOnCompleteListener()
              { task ->
                  task.exception?.let {
                      Toast.makeText(cBSFragment.activity, it.localizedMessage, Toast.LENGTH_LONG).show()
                  }
                      ?: task.result?.serialize(User::class.java)?.let()
                      {
                          viewModel.posterNameTextView.setText(it.fullName())
                          // TODO: Set Display Picture
                      }
              }

              val date = android.text.format.DateFormat.getDateFormat(cBSFragment.activity)
                  .format(comment.timestamp.toDate())
              val time = android.text.format.DateFormat.getTimeFormat(cBSFragment.activity)
                  .format(comment.timestamp.toDate())
              val timestamp = "$time $date"
              viewModel.timestampTextView.setText(timestamp)
              viewModel.contentTextView.setText(comment.content)
              val isCommentAdmin = FirebaseAuth.getInstance().currentUser?.documentReference() == comment.reference
              viewModel.deleteButton.visibility = if (isTeacher || isCommentAdmin) View.VISIBLE else View.GONE
              viewModel.deleteButton.setOnClickListener() { this.deleteComment(comment) }

              return view
          }
      }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && resultCode == Activity.RESULT_OK && data != null)
        {
            val selectedItemNo = data.getIntExtra(CommentsActivity.SELECTED_ITEM_NO, -1)
            val selectedItemCount = data.getIntExtra(CommentsActivity.SELECTED_ITEM_COUNT, -1)
            val commentRow = formList.get(selectedItemCount)
            commentRow.commentCount = selectedItemCount
            formList.set(selectedItemNo, commentRow)
            adapter.notifyDataSetChanged()
        }
        if (KotConstants.REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK)
        {
            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            attachmentResult = result!!.get(0)
            if (attachmentResult != null)
            {
                popupRootView.button_forum_selectAttachment.text = attachmentResult!!.name
            }
        }

    }

    override fun onDestroy()
    {
        super.onDestroy()
        listenerRegistration.remove()
    }


}