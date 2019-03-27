package com.clockworks.incirkle.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.clockworks.incirkle.Models.AssignmentPost
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_course_assignments.*

import kotlinx.android.synthetic.main.list_item_post_assignment.view.*
import kotlinx.android.synthetic.main.popup_add_assigment.view.*
import java.util.*

class CourseAssignmentsFragment() : FileUploaderFragment()
{
    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"
    }

    private val calendar = Calendar.getInstance()

    lateinit var dialog: AlertDialog
    lateinit var adapter: AssignmentPostAdapter

    class AssignmentPostAdapter(
        private val context: Context,
        val uploaderFragment: FileUploaderFragment,
        private val isAdmin: Boolean,
        private var teacherPath: String,
        private var dataSource: List<AssignmentPost>
    ) : BaseAdapter()
    {
        private class ViewModel
        {
            lateinit var posterPictureImageView: ImageView
            lateinit var posterNameTextView: TextView
            lateinit var timestampTextView: TextView
            lateinit var nameTextView: TextView
            lateinit var detailsTextView: TextView
            lateinit var dueDateTextView: TextView
            lateinit var downloadAttachmentButton: TextView
            lateinit var button_assignmentPost_submissioncount: TextView
            lateinit var button_assignmentPost_download_images: ImageView
            lateinit var postSolutionButton: Button
            lateinit var linearLayout_submit_solution: LinearLayout
            lateinit var linearLayout_view_solution: LinearLayout
            lateinit var viewSolutionButton: Button
            lateinit var submitSolutionButton: Button
            lateinit var viewSubmissionButton: TextView
            lateinit var resubmitSolutionButton: Button

            lateinit var popupicon: ImageView
        }

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        private fun deleteAssignmentPost(post: AssignmentPost)
        {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("Delete Assignment Post")
            builder.setMessage("Are you sure you wish to delete this post?")
            builder.setPositiveButton("Delete",
                { _, _ ->
                    post.reference?.delete()
                        ?.addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                        ?.addOnSuccessListener()
                        {
                            FirebaseStorage.getInstance().getReference("Assignment Attachments")
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
                view = inflater.inflate(R.layout.list_item_post_assignment, parent, false)
                viewModel = ViewModel()
                viewModel.posterPictureImageView = view.imageView_assignmentPost_posterPicture
                viewModel.posterNameTextView = view.textView_assignmentPost_posterName
                viewModel.timestampTextView = view.textView_assignmentPost_timestamp
                viewModel.nameTextView = view.textView_assignmentPost_name
                viewModel.detailsTextView = view.textView_assignmentPost_details
                viewModel.dueDateTextView = view.textView_assignmentPost_dueDate
                viewModel.downloadAttachmentButton = view.button_assignmentPost_download_attachment
                viewModel.button_assignmentPost_submissioncount = view.button_assignmentPost_submissioncount
                viewModel.button_assignmentPost_download_images = view.button_assignmentPost_download_images
                viewModel.postSolutionButton = view.button_assignmentPost_post_solution
                viewModel.linearLayout_submit_solution = view.linearLayout_submit_solution
                viewModel.linearLayout_view_solution = view.linearLayout_view_solution
                viewModel.viewSolutionButton = view.button_assignmentPost_view_solution
                viewModel.submitSolutionButton = view.button_assignmentPost_submit_solution
//                viewModel.viewSubmissionButton = view.lastsubmission
                viewModel.resubmitSolutionButton = view.button_assignmentPost_resubmit_solution
                viewModel.popupicon = view.popupicon
                view.tag = viewModel
            }
            else
            {
                view = convertView
                viewModel = convertView.tag as ViewModel
            }
            var request: RequestOptions =
                RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
            Glide.with(context)
                .load(post.imagepath)
                .apply(request)
                .into(viewModel.posterPictureImageView);
            post.poster.get().addOnCompleteListener()
            { task ->

                task.exception?.let { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    ?: task.result?.serialize(User::class.java)?.let()
                    {
                        viewModel.posterNameTextView.setText(it.fullName())
                        // TODO: Set Display Picture
                    }
            }

            val dueDateDate =
                android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.dueDate.toDate())
            val dueDateTime =
                android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.dueDate.toDate())
            val dueDate = "$dueDateTime, $dueDateDate"

            val timestampDate =
                android.text.format.DateFormat.getDateFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestampTime =
                android.text.format.DateFormat.getTimeFormat(context.applicationContext).format(post.timestamp.toDate())
            val timestamp = "$timestampTime, $timestampDate"

            viewModel.timestampTextView.text = timestamp
            viewModel.nameTextView.text = post.name
            viewModel.detailsTextView.text = post.details
            viewModel.dueDateTextView.text = dueDate
            //   viewModel.deleteButton.visibility = if (isAdmin) View.VISIBLE else View.GONE


            viewModel.downloadAttachmentButton.visibility = if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.button_assignmentPost_download_images.visibility =
                if (post.attachmentPath != null) View.VISIBLE else View.GONE
            viewModel.downloadAttachmentButton.setOnClickListener {
                post.attachmentPath?.let {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    )
                }
            }
            viewModel.popupicon.visibility = if (isAdmin) View.VISIBLE else View.GONE
            //  viewModel.viewSolutionButton.visibility = if (post.solutionPath != null) View.VISIBLE else View.GONE
            viewModel.linearLayout_view_solution.visibility = if (post.solutionPath != null) View.VISIBLE else View.GONE
            viewModel.viewSolutionButton.setOnClickListener {
                post.solutionPath?.let {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(it)
                        )
                    )
                }
            }
            viewModel.linearLayout_view_solution.setOnClickListener {
                post.solutionPath?.let {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    )
                }
            }
            FirebaseAuth.getInstance().currentUser?.let()
            { user ->

                viewModel.popupicon.visibility =
                    if (isAdmin || post.poster == user.documentReference()) View.VISIBLE else View.GONE

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
                        { _, _ ->
                            post.reference?.delete()
                                ?.addOnFailureListener {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                                ?.addOnSuccessListener()
                                {
                                    FirebaseStorage.getInstance().getReference("Assignment Attachments")
                                        .child(post.reference!!.id).delete()
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
            val postAssignmentsStorage =
                FirebaseStorage.getInstance().getReference("Assignments").child(post.reference!!.id)
            /*  viewModel.postSolutionButton.setOnClickListener()
              {
                  uploaderFragment.selectFile()
                  {
                      uploaderFragment.updateAttachmentPath(post.reference!!, postAssignmentsStorage.child("Solution"), "solutionPath")
                      {
                          viewModel.postSolutionButton.visibility = View.GONE

                          viewModel.linearLayout_submit_solution.visibility = View.GONE
                          viewModel.viewSolutionButton.visibility = View.VISIBLE
                      }
                  }
              } */
            if (post.attachmentPath != null)
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
                                viewModel.button_assignmentPost_download_images.visibility = View.GONE
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
                                viewModel.button_assignmentPost_download_images.visibility = View.VISIBLE
                                return false
                            }

                        })
                        .into(viewModel.button_assignmentPost_download_images);

                }
            }
            viewModel.linearLayout_submit_solution.setOnClickListener()
            {
                uploaderFragment.selectFile()
                {
                    uploaderFragment.updateAttachmentPath(
                        post.reference!!,
                        postAssignmentsStorage.child("Solution"),
                        "solutionPath"
                    )
                    {

                        viewModel.linearLayout_submit_solution.visibility = View.GONE
                        //  viewModel.viewSolutionButton.visibility = View.VISIBLE
                        viewModel.linearLayout_view_solution.visibility = View.VISIBLE
                    }
                }
            }

            FirebaseAuth.getInstance().currentUser?.documentReference()?.let()
            {
                val isTeacher = it.path == teacherPath
                val submissionReference = post.submissionsReferece().document(it.id)

                //   viewModel.postSolutionButton.visibility = if (isTeacher && post.solutionPath == null) View.VISIBLE else View.GONE
                viewModel.linearLayout_submit_solution.visibility =
                    if (isTeacher && post.solutionPath == null) View.VISIBLE else View.GONE

                submissionReference.get()
                    .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                    .addOnSuccessListener()
                    {
                        val isSubmitted = it.exists()
                        viewModel.submitSolutionButton.visibility =
                            if (isTeacher || isSubmitted) View.GONE else View.VISIBLE
                        view.linearLayout_submitted_solution.visibility =
                            if (isTeacher || !isSubmitted) View.GONE else View.VISIBLE
                    }

                val submissionStorageReference = postAssignmentsStorage.child("Submissions").child(it.id)



                post.reference!!.collection("Submissions").get()
                    .addOnSuccessListener()
                    {
                        viewModel.button_assignmentPost_submissioncount.text = it.size().toString() + " Submissions"
                    }
                    .addOnCompleteListener()
                    {
                        //    Log.d("completed","completed")
                    }
                viewModel.submitSolutionButton.setOnClickListener()
                {
                    uploaderFragment.selectFile()
                    {
                        submissionReference.set(AssignmentPost.Submission())
                            .addOnFailureListener {
                                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                            .addOnSuccessListener()
                            {
                                uploaderFragment.updateAttachmentPath(
                                    submissionReference,
                                    submissionStorageReference,
                                    "submissionPath"
                                )
                                {
                                    viewModel.submitSolutionButton.visibility = View.GONE
                                    view.linearLayout_submitted_solution.visibility = View.VISIBLE
                                }
                            }
                    }
                }
                viewModel.viewSubmissionButton.setOnClickListener()
                {
                    submissionReference.get()
                        .addOnFailureListener { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() }
                        .addOnSuccessListener {
                            it.serialize(AssignmentPost.Submission::class.java).let {
                                it.submissionPath.let {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(it)
                                        )
                                    )
                                }
                            }
                        }
                }
//                viewModel.resubmitSolutionButton
//                    .setOnClickListener { uploaderFragment
//                        .selectFile { uploaderFragment
//                            .updateAttachmentPath(submissionReference, submissionStorageReference, "submissionPath") } }
            }

            return view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val rootView = inflater.inflate(R.layout.fragment_course_assignments, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        this.initialize()
    }

    private fun initialize()
    {
        val isTeacher = arguments?.getBoolean(IDENTIFIER_IS_TEACHER) ?: false
        val isTeachingAssistant = arguments?.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT) ?: false
        //    layout_post_assignment_new.visibility = if(isAdmin) View.VISIBLE else View.GONE

        FirebaseAuth.getInstance().currentUser?.let()
        {

            FirebaseStorage.getInstance().getReference("UserProfiles").child(it.uid).downloadUrl.addOnSuccessListener {
                var request: RequestOptions =
                    RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
                Glide.with(context)
                    .load(it.toString())
                    .apply(request)
                    .into(imageview_profileimage);
            }.addOnFailureListener {
                it.printStackTrace()
            };
        }
        card_view_createforum_assignment.visibility = if (isTeacher || isTeachingAssistant) View.VISIBLE else View.GONE
        card_view_createforum_assignment.setOnClickListener {
            var view = layoutInflater.inflate(com.clockworks.incirkle.R.layout.popup_add_assigment, null)


            view.button_post_assignment_dueDate.text =
                android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
            view.button_post_assignment_dueDate.setOnClickListener()
            {
                val listener = DatePickerDialog.OnDateSetListener()
                { _, year, month, day ->
                    this.calendar.set(Calendar.YEAR, year)
                    this.calendar.set(Calendar.MONTH, month)
                    this.calendar.set(Calendar.DAY_OF_MONTH, day)
                    view.button_post_assignment_dueDate.text =
                        android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
                }
                val year = this.calendar.get(Calendar.YEAR)
                val month = this.calendar.get(Calendar.MONTH)
                val day = this.calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(context, listener, year, month, day).show()
            }
            view.button_post_assignment_dueTime.text =
                android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
            view.button_post_assignment_dueTime.setOnClickListener()
            {
                val listener = TimePickerDialog.OnTimeSetListener()
                { _, hour, minute ->
                    this.calendar.set(Calendar.HOUR_OF_DAY, hour)
                    this.calendar.set(Calendar.MINUTE, minute)
                    view.button_post_assignment_dueTime.text =
                        android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
                }
                val hour = this.calendar.get(Calendar.HOUR_OF_DAY)
                val minute = this.calendar.get(Calendar.MINUTE)
                TimePickerDialog(context, listener, hour, minute, false).show()
            }
            view.button_assignment_selectAttachment.setOnClickListener {
                this.selectFile {
                    view.button_assignment_selectAttachment.text =
                        this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment)
                }
            }

            val assignmentPostsReference =
                FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                    .collection("Assignment Posts")

            view.button_post_assignment.setOnClickListener()
            {
                view.editText_post_assignment_name.error = null
                view.editText_post_assignment_details.error = null

                val name = view.editText_post_assignment_name.text.toString().trim()
                val details = view.editText_post_assignment_details.text.toString().trim()

                if (name.isBlank())
                {
                    view.editText_post_assignment_name.error = "Assignment name cannot be empty"
                    return@setOnClickListener
                }
                else if (details.isBlank())
                {
                    view.editText_post_assignment_details.error = "Assignment description cannot be empty"
                    return@setOnClickListener
                }
                else
                {
                    FirebaseAuth.getInstance().currentUser?.let()
                    {
                        val appActivity = this.activity as AppActivity
                        appActivity.showLoadingAlert()
                        assignmentPostsReference.add(
                            AssignmentPost(
                                name,
                                details,
                                this.calendar.time,
                                it.documentReference()
                            )
                        )
                            .addOnFailureListener { appActivity.showError(it) }
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnSuccessListener()
                            {
                                this.resetPostLayout()
                                this.updateAttachmentPath(
                                    it,
                                    FirebaseStorage.getInstance().getReference("Activity Attachments").child(it.id),
                                    "attachmentPath"
                                )
                            }
                    }
                }
                assignmentPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
                { result, e ->
                    e?.let { (this.activity as AppActivity).showError(it) }
                        ?: result?.map { it.serialize(AssignmentPost::class.java) }?.let()

                        {
                            adapter = AssignmentPostAdapter(
                                context!!, this, isTeacher || isTeachingAssistant, arguments?.getString(
                                    IDENTIFIER_COURSE_TEACHER_PATH
                                ) ?: "", it
                            )
                            listView_courseFeed_assignments.adapter = adapter
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
        this.button_post_assignment_dueDate.text =
            android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueDate.setOnClickListener()
        {
            val listener = DatePickerDialog.OnDateSetListener()
            { _, year, month, day ->
                this.calendar.set(Calendar.YEAR, year)
                this.calendar.set(Calendar.MONTH, month)
                this.calendar.set(Calendar.DAY_OF_MONTH, day)
                this.button_post_assignment_dueDate.text =
                    android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
            }
            val year = this.calendar.get(Calendar.YEAR)
            val month = this.calendar.get(Calendar.MONTH)
            val day = this.calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, listener, year, month, day).show()
        }

        button_post_assignment_dueTime.text =
            android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueTime.setOnClickListener()
        {
            val listener = TimePickerDialog.OnTimeSetListener()
            { _, hour, minute ->
                this.calendar.set(Calendar.HOUR_OF_DAY, hour)
                this.calendar.set(Calendar.MINUTE, minute)
                button_post_assignment_dueTime.text =
                    android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
            }
            val hour = this.calendar.get(Calendar.HOUR_OF_DAY)
            val minute = this.calendar.get(Calendar.MINUTE)
            TimePickerDialog(context, listener, hour, minute, false).show()
        }

        button_assignment_selectAttachment.setOnClickListener {
            this.selectFile {
                button_assignment_selectAttachment.text =
                    this.selectedFileUri?.getName(context!!) ?: getString(R.string.text_select_attachment)
            }
        }

        val assignmentPostsReference =
            FirebaseFirestore.getInstance().document(arguments!!.getString(IDENTIFIER_COURSE_PATH))
                .collection("Assignment Posts")
        button_post_assignment.setOnClickListener()
        {
            editText_post_assignment_name.error = null
            editText_post_assignment_details.error = null

            val name = editText_post_assignment_name.text.toString().trim()
            val details = editText_post_assignment_details.text.toString().trim()

            if (name.isBlank())
            {
                editText_post_assignment_name.error = "Assignment name cannot be empty"
                return@setOnClickListener
            }
            else if (details.isBlank())
            {
                editText_post_assignment_details.error = "Assignment description cannot be empty"
                return@setOnClickListener
            }
            else
            {
                FirebaseAuth.getInstance().currentUser?.let()
                {
                    val appActivity = this.activity as AppActivity
                    appActivity.showLoadingAlert()
                    assignmentPostsReference.add(
                        AssignmentPost(
                            name,
                            details,
                            this.calendar.time,
                            it.documentReference()
                        )
                    )
                        .addOnFailureListener { appActivity.showError(it) }
                        .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                        .addOnSuccessListener()
                        {
                            this.resetPostLayout()
                            this.updateAttachmentPath(
                                it,
                                FirebaseStorage.getInstance().getReference("Activity Attachments").child(it.id),
                                "attachmentPath"
                            )
                        }
                }
            }
        }
        assignmentPostsReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener()
        { result, e ->
            e?.let { (this.activity as AppActivity).showError(it) }
                ?: result?.map { it.serialize(AssignmentPost::class.java) }?.let()
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
                    adapter = AssignmentPostAdapter(
                        context!!, this, isTeacher || isTeachingAssistant, arguments?.getString(
                            IDENTIFIER_COURSE_TEACHER_PATH
                        ) ?: "", it
                    )
                    listView_courseFeed_assignments.adapter = adapter
                }

        }
    }

    private fun resetPostLayout()
    {
        editText_post_assignment_name.setText("")
        editText_post_assignment_details.setText("")
        this.button_post_assignment_dueDate.text =
            android.text.format.DateFormat.getDateFormat(this.context).format(this.calendar.time)
        button_post_assignment_dueTime.text =
            android.text.format.DateFormat.getTimeFormat(this.context).format(this.calendar.time)
    }
}