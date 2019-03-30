package com.clockworks.incirkle.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Comment
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.list_item_comment.view.*
import kotlinx.android.synthetic.main.popup_add_activity.view.*


class CommentsActivity : AppActivity()
{
    companion object
    {
        const val IDENTIFIER_POST_PATH = "Post Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
    }

    private lateinit var commentsReference: CollectionReference
    private var commentList = ArrayList<Comment>()
    private lateinit var commentsAdapter: CommentsAdapter

    private lateinit var postPath: String

    private var dialog: AlertDialog? = null

    class CommentsAdapter(
        private val context: Context,
        private val isTeacher: Boolean,
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
            lateinit var tv_attachment: TextView
            lateinit var iv_images: ImageView
        }

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteComment(comment: Comment)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Comment")
            builder.setMessage("Are you sure you wish to delete this comment?")
            builder.setPositiveButton("Delete")
            { _, _ ->
                comment.reference?.delete()
                    ?.addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
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
                viewModel.tv_attachment = view.tv_attachment
                viewModel.iv_images = view.iv_images
                view.tag = viewModel
            }
            else
            {
                view = convertView
                viewModel = convertView.tag as ViewModel
            }
            try
            {
                val comment = this.dataSource[position]

                comment.poster.get().addOnCompleteListener()
                { task ->
                    task.exception?.let { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                        ?: task.result?.serialize(User::class.java)?.let()
                        {
                            try
                            {
                                viewModel.posterNameTextView.setText(it.fullName())
                                // TODO: Set Display Picture
                                var request: RequestOptions =
                                    RequestOptions().error(R.drawable.ic_user).override(100, 100)
                                        .placeholder(R.drawable.ic_user)
                                Glide.with(context.applicationContext)
                                    .load(it.profilepic)
                                    .apply(request)
                                    .into(viewModel.posterPictureImageView)
                                // comment user is loggged user then show delete button
                                val isCommentAdmin =
                                    it.phoneNumber == FirebaseAuth.getInstance().currentUser?.phoneNumber
                                viewModel.deleteButton.visibility =
                                    if (isTeacher || isCommentAdmin) View.VISIBLE else View.GONE
                            } catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                }

                val date = android.text.format.DateFormat.getDateFormat(context.applicationContext)
                    .format(comment.timestamp.toDate())
                val time = android.text.format.DateFormat.getTimeFormat(context.applicationContext)
                    .format(comment.timestamp.toDate())
                val timestamp = "$time $date"
                viewModel.timestampTextView.setText(timestamp)
                viewModel.contentTextView.setText(comment.content)
                viewModel.deleteButton.setOnClickListener() { this.deleteComment(comment) }

                viewModel.tv_attachment.setOnClickListener {
                    comment.attachmentPath?.let {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        )
                    }
                }

                viewModel.iv_images.setOnClickListener {
                    comment.attachmentPath?.let {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        )
                    }
                }

                if (comment.attachmentPath != null)
                {
                    viewModel.iv_images.visibility = View.VISIBLE
                    viewModel.tv_attachment.visibility = View.VISIBLE
                    comment.attachmentPath?.let {
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
                                    try
                                    {
                                        viewModel.tv_attachment.visibility = View.VISIBLE
                                        viewModel.iv_images.visibility = View.GONE
                                    } catch (e: Exception)
                                    {
                                        e.printStackTrace()
                                    }
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
                                    try
                                    {
                                        viewModel.tv_attachment.visibility = View.GONE
                                        viewModel.iv_images.visibility = View.VISIBLE
                                    } catch (e: Exception)
                                    {
                                        e.printStackTrace()
                                    }
                                    return false
                                }

                            })
                            .into(viewModel.iv_images);

                    }
                }
                else
                {
                    viewModel.iv_images.visibility = View.GONE
                    viewModel.tv_attachment.visibility = View.GONE
                }
            } catch (e: Exception)
            {
                e.printStackTrace()
            }

            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setStatusBarDim(true);
        setContentView(R.layout.activity_comments)
        val isTeacher = intent.getBooleanExtra(IDENTIFIER_IS_TEACHER, false)

        commentsAdapter = CommentsAdapter(this, isTeacher, commentList)
        listView_comments.adapter = commentsAdapter

        postPath = intent.getStringExtra(IDENTIFIER_POST_PATH)

        this.commentsReference =
            FirebaseFirestore.getInstance().document(postPath).collection("Comments")
        this.commentsReference.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener()
        { result, e ->
            e?.let { this.showError(it) }
                ?: result?.map { it.serialize(Comment::class.java) }?.let()
                {
                    Log.d("CommentsActivityData", it.size.toString());
                    commentList.clear()
                    commentList.addAll(it)
                    commentsAdapter.notifyDataSetChanged()
                    listView_comments.smoothScrollToPosition(commentList.size - 1)
                }
        }

//        listView_comments.setOnTouchListener(object : View.OnTouchListener
//        {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean
//            {
//                var action = event?.getAction();
//                when (action)
//                {
//                    MotionEvent.ACTION_DOWN -> v?.getParent()?.requestDisallowInterceptTouchEvent(true);
//                    MotionEvent.ACTION_UP -> v?.getParent()?.requestDisallowInterceptTouchEvent(false);
//                }
//
//                // Handle ListView touch events.
//                v?.onTouchEvent(event);
//                return true;
//            }
//        })


//        listView.setOnTouchListener(new ListView.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        // Disallow NestedScrollView to intercept touch events.
//                        v.getParent().requestDisallowInterceptTouchEvent(true);
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        // Allow NestedScrollView to intercept touch events.
//                        v.getParent().requestDisallowInterceptTouchEvent(false);
//                        break;
//                }
//
//                // Handle ListView touch events.
//                v.onTouchEvent(event);
//                return true;
//            }
//        });


        btnPost.setOnClickListener()
        {
            etComment.error = null

            val content = etComment.text.trim().toString()
            if (content.isBlank())
            {
                etComment.error = "Comment is empty"
                return@setOnClickListener
            }
            else
            {
                etComment.setText("")
                FirebaseAuth.getInstance().currentUser?.documentReference()?.let()
                {
                    val newComment = Comment(content, it)
                    this.showLoadingAlert()
                    this.commentsReference.add(newComment)
                        .addOnFailureListener(::showError)
                        .addOnSuccessListener { etComment.setText("") }
                        .addOnCompleteListener { this.dismissLoadingAlert() }
                }
            }
        }

        ib_attachment.setOnClickListener {

            selectFile {
                val fileName = selectedFileUri?.getName(this)
                if (fileName!=null)
                {
                    val intent = Intent(this, CommentWithAttachment::class.java)
                    intent.putExtra("fileName", fileName)
                    intent.putExtra("fileUri", selectedFileUri)
                    intent.putExtra(IDENTIFIER_POST_PATH, postPath)
                    startActivityForResult(intent, 510)
                }
            }

        }


        touch_outside.setOnClickListener() {
            finish()
        }

        val behavior = BottomSheetBehavior.from(card_bottom_sheet)
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback()
        {
            override fun onSlide(bottomSheet: View, slideOffset: Float)
            {
                behavior.peekHeight = 500
                Log.d("CommentsActivity", "onSlide");
            }

            override fun onStateChanged(bottomSheet: View, newState: Int)
            {
                Log.d("CommentsActivity", newState.toString());
                when (newState)
                {
                    BottomSheetBehavior.STATE_HIDDEN -> finish()
                    BottomSheetBehavior.STATE_EXPANDED -> setStatusBarDim(false)
                    else -> setStatusBarDim(true)
                }
            }
        });

        behavior.peekHeight = 50000

//        this.configurePostCommentView()
    }

    fun configurePostCommentView()
    {
        val contentEditTextLayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        contentEditTextLayoutParams.gravity = Gravity.CENTER_VERTICAL
        contentEditTextLayoutParams.weight = 1.0f

        val contentEditText = EditText(this)
        contentEditText.layoutParams = contentEditTextLayoutParams
        contentEditText.hint = "Write Comment here"
        contentEditText.inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT

        val postButtonLayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        postButtonLayoutParams.gravity = Gravity.CENTER_VERTICAL

        val postButton = Button(this)
        postButton.layoutParams = postButtonLayoutParams
        postButton.text = "Post"
        postButton.setOnClickListener()
        {
            contentEditText.error = null

            val content = contentEditText.text.trim().toString()
            if (content.isBlank())
            {
                contentEditText.error = "Comment is empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.documentReference()?.let()
                {
                    val newComment = Comment(content, it)
                    this.showLoadingAlert()
                    this.commentsReference.add(newComment)
                        .addOnFailureListener(::showError)
                        .addOnSuccessListener { contentEditText.setText("") }
                        .addOnCompleteListener { this.dismissLoadingAlert() }
                }
            }
        }

        val postCommentView = LinearLayout(this)
        postCommentView.orientation = LinearLayout.HORIZONTAL
        postCommentView.addView(contentEditText)
        postCommentView.addView(postButton)

        listView_comments.addFooterView(postCommentView)
    }

    private fun setStatusBarDim(dim: Boolean)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            window.statusBarColor = if (dim)
                Color.TRANSPARENT
            else
                ContextCompat.getColor(this, getThemedResId(R.attr.colorPrimaryDark))
        }
    }

    private fun getThemedResId(@AttrRes attr: Int): Int
    {
        val a = theme.obtainStyledAttributes(intArrayOf(attr))
        val resId = a.getResourceId(0, 0)
        a.recycle()
        return resId
    }

    fun showAttachmentDialog(fileName: String?)
    {
        var view = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_activity, null)
        view.tvHeader.text = "Add Attachment"
        view.button_activity_selectAttachment.text = fileName
        view.editText_post_activity_description.error = null

        if (dialog == null)
        {
            dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
        }
        dialog?.show()

        view.button_activity_selectAttachment.setOnClickListener {

            selectFile {
                view.button_activity_selectAttachment.text =
                    selectedFileUri?.getName(this) ?: getString(
                        com.clockworks.incirkle.R.string.text_select_attachment
                    )

            }

        }

        view.ib_cross.setOnClickListener({
            dialog?.cancel()

        })
        view.button_post_activity.setOnClickListener()
        {
            view.editText_post_activity_description.error = null

            val description = view.editText_post_activity_description.text.toString().trim()
            if (description.isBlank())
            {
                view.editText_post_activity_description.error = " description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                dialog?.cancel()
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    this.showLoadingAlert()
                    commentsReference.add(Comment(description, it.documentReference()))
                        .addOnFailureListener { this.showError(it) }
                        .addOnCompleteListener { this.dismissLoadingAlert() }
                        .addOnSuccessListener()
                        {
                            dialog?.cancel()
                            view.editText_post_activity_description.setText("")
                            this.updateAttachmentPath(
                                it,
                                FirebaseStorage.getInstance().getReference("Comments Attachments").child(it.id),
                                "attachmentPath"
                            )
                        }
                }
            }

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 510 && resultCode == Activity.RESULT_OK && data != null)
        {
            // add data in list
            val des = data.getStringExtra("description")
            postCommentWithAttachment(des)
        }

    }

    private fun postCommentWithAttachment(description: String)
    {
        FirebaseAuth.getInstance().currentUser?.let()
        {
            this.showLoadingAlert()
            commentsReference.add(Comment(description, it.documentReference()))
                .addOnFailureListener { this.showError(it) }
                .addOnCompleteListener { this.dismissLoadingAlert() }
                .addOnSuccessListener()
                {
                    this.updateAttachmentPath(
                        it,
                        FirebaseStorage.getInstance().getReference("Comments Attachments").child(it.id),
                        "attachmentPath"
                    )
                }
        }
    }
}
