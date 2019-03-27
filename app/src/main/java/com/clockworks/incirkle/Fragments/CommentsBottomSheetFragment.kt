package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Activities.AppActivity
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
import kotlinx.android.synthetic.main.activity_comments.view.*
import kotlinx.android.synthetic.main.list_item_comment.view.*

class CommentsBottomSheetFragment : BottomSheetDialogFragment()
{

    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    companion object
    {
        const val IDENTIFIER_POST_PATH = "Post Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
    }

    private lateinit var commentsReference: CollectionReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(object : DialogInterface.OnShowListener
        {
            override fun onShow(dialog: DialogInterface?)
            {
                val bsd = dialog as BottomSheetDialog
                val bottomSheet = bsd.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

            }

        })

        // Do something with your dialog like setContentView() or whatever
        return dialog;
    }


override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View?
{
    // Inflate the layout for this fragment
    rootView = inflater.inflate(R.layout.activity_comments, container, false)

    val isTeacher = arguments?.getBoolean(IDENTIFIER_IS_TEACHER, false)
    val isTeachingAssistant = arguments?.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT, false)
    val path: String? = arguments?.getString(IDENTIFIER_POST_PATH, "");
    this.commentsReference =
        FirebaseFirestore.getInstance().document(path!!)
            .collection("Comments")
    this.commentsReference.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener()
    { result, e ->
        e?.let { (activity as AppActivity).showError(it) }
            ?: result?.map { it.serialize(Comment::class.java) }?.let()
            { rootView.listView_comments.adapter = CommentsAdapter(this, isTeacher!!, isTeachingAssistant!!, it) }
    }

    this.configurePostCommentView()

    return rootView;
}

fun configurePostCommentView()
{
    val contentEditTextLayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    contentEditTextLayoutParams.gravity = Gravity.CENTER_VERTICAL
    contentEditTextLayoutParams.weight = 1.0f

    val contentEditText = EditText(this.activity)
    contentEditText.layoutParams = contentEditTextLayoutParams
    contentEditText.hint = "Write Comment here"
    contentEditText.inputType = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT

    val postButtonLayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    postButtonLayoutParams.gravity = Gravity.CENTER_VERTICAL

    val postButton = Button(this.activity)
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
                (activity as AppActivity).showLoadingAlert()
                this.commentsReference.add(newComment)
//                        .addOnFailureListener(::showError)
                    .addOnSuccessListener { contentEditText.setText("") }
                    .addOnCompleteListener { (activity as AppActivity).dismissLoadingAlert() }
            }
        }
    }

    val postCommentView = LinearLayout(this.activity)
    postCommentView.orientation = LinearLayout.HORIZONTAL
    postCommentView.addView(contentEditText)
    postCommentView.addView(postButton)

    rootView.listView_comments.addFooterView(postCommentView)
}


class CommentsAdapter(
    private val cBSFragment: CommentsBottomSheetFragment,
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
}

}


