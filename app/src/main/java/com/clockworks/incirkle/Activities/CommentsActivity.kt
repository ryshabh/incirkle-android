package com.clockworks.incirkle.Activities

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Comment
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.list_item_comment.view.*


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
                task.exception?.let { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?: task.result?.serialize(User::class.java)?.let()
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
                       val isCommentAdmin = it.phoneNumber == FirebaseAuth.getInstance().currentUser?.phoneNumber
                        viewModel.deleteButton.visibility = if (isTeacher || isCommentAdmin) View.VISIBLE else View.GONE
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
        this.commentsReference =
            FirebaseFirestore.getInstance().document(intent.getStringExtra(IDENTIFIER_POST_PATH)).collection("Comments")
        this.commentsReference.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener()
        { result, e ->
            e?.let { this.showError(it) }
                ?: result?.map { it.serialize(Comment::class.java) }?.let()
                {
                    commentList.clear()
                    commentList.addAll(it)
                    commentsAdapter.notifyDataSetChanged()
                    listView_comments.smoothScrollToPosition(commentList.size-1)
                }
        }



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

        touch_outside.setOnClickListener() {
            finish()
        }
        BottomSheetBehavior.from(card_bottom_sheet)
            .setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback()
            {
                override fun onSlide(bottomSheet: View, slideOffset: Float)
                {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int)
                {
                    when (newState)
                    {
                        BottomSheetBehavior.STATE_HIDDEN -> finish()
                        BottomSheetBehavior.STATE_EXPANDED -> setStatusBarDim(false)
                        else -> setStatusBarDim(true)
                    }
                }
            });

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
}
