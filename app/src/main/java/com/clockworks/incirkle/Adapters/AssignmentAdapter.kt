package com.clockworks.incirkle.Adapters

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.clockworks.incirkle.Activities.SubmissionActivity
import com.clockworks.incirkle.Fragments.AssignmentFragment
import com.clockworks.incirkle.Models.AssignmentModel
import com.clockworks.incirkle.Models.SolutionModel
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.clockworks.incirkle.utils.AppConstantsValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_post_assignment.view.*


/**
 * RecyclerView adapter for a assignment fragment
 */
class AssignmentAdapter(
    aList: ArrayList<AssignmentModel>,
    teacher: Boolean,
    teachingAssistant: Boolean,
    assignmentFragment: AssignmentFragment
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>()
{
    var isTeacher = teacher
    var isTeachingAssistant = teachingAssistant
    var assignmentList = aList
    var mAssignmentFragment = assignmentFragment


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_post_assignment, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(assignmentList.get(position), isTeacher, isTeachingAssistant, mAssignmentFragment)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {

        fun bind(
            assignmentModel: AssignmentModel,
            teacher: Boolean,
            teachingAssistant: Boolean,
            mAssignmentFragment: AssignmentFragment
        )
        {

            val resources = itemView.resources


            // user detail
            Glide
                .with(itemView.context)
                .load(assignmentModel.user?.profilepic)
                .into(itemView.imageView_assignmentPost_posterPicture)
            itemView.textView_assignmentPost_posterName.text = assignmentModel.user?.fullName()
//
            itemView.textView_assignmentPost_name.text = assignmentModel.name
            itemView.textView_assignmentPost_details.text = assignmentModel.detail

            val dueDateDate =
                android.text.format.DateFormat.getDateFormat(itemView.context).format(assignmentModel.dueDate?.toDate())
            val dueDateTime =
                android.text.format.DateFormat.getTimeFormat(itemView.context).format(assignmentModel.dueDate?.toDate())
            val dueDate = "$dueDateTime, $dueDateDate"

            val timestampDate =
                android.text.format.DateFormat.getDateFormat(itemView.context)
                    .format(assignmentModel.timestamp.toDate())
            val timestampTime =
                android.text.format.DateFormat.getTimeFormat(itemView.context)
                    .format(assignmentModel.timestamp.toDate())
            val timestamp = "$timestampTime, $timestampDate"

            itemView.textView_assignmentPost_timestamp.text = timestamp
            itemView.textView_assignmentPost_dueDate.text = dueDate

            val fileTypeCondition: Boolean = assignmentModel.assignmentAttachmentFileType?.startsWith(
                "image",
                true
            )!! || (assignmentModel.assignmentAttachmentFileType?.startsWith("video", true)!!)
            if (fileTypeCondition)
            {
                itemView.button_assignmentPost_download_images.visibility = View.VISIBLE
                itemView.button_assignmentPost_download_attachment.visibility = View.GONE

                Glide
                    .with(itemView.context)
                    .load(assignmentModel.assignmentAttachmentUrl)
                    .into(itemView.button_assignmentPost_download_images);

            }
            else
            {
                itemView.button_assignmentPost_download_images.visibility = View.GONE
                itemView.button_assignmentPost_download_attachment.visibility = View.VISIBLE
            }

            itemView.button_assignmentPost_download_images.setOnClickListener{
                itemView.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(assignmentModel.assignmentAttachmentUrl))
                )
            }

            itemView.button_assignmentPost_download_images.setOnClickListener{
                itemView.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(assignmentModel.assignmentAttachmentUrl))
                )
            }

            itemView.popupicon.setOnClickListener {
                if (assignmentModel.documentId != null)
                {
                    val popup = PopupMenu(itemView.context, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.actions, popup.menu)
                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                        val builder = AlertDialog.Builder(itemView.context)
                        builder.setTitle("Delete Assignment Post")
                        builder.setMessage("Are you sure you wish to delete this post?")
                        builder.setPositiveButton(
                            "Delete"
                        ) { _, _ ->
                            mAssignmentFragment.deleteItem(assignmentModel.documentId!!)
                        }
                        builder.setNegativeButton("Cancel", null)
                        builder.create().show()

                        true
                    })
                    popup.show()
                }
            }

            if (teacher || teachingAssistant)
            {
                itemView.llTeacher.visibility = View.VISIBLE
                itemView.llStudent.visibility = View.GONE
                itemView.popupicon.visibility = View.VISIBLE
                AppConstantsValue.solutionCollectionRef
                    .whereEqualTo("assignmentDocumentId", assignmentModel.documentId).get().addOnCompleteListener {
                        itemView.tvSubmissionCount.text = "Submission count: ${it.result?.size()}"
                    }
            }
            else
            {
                itemView.llTeacher.visibility = View.GONE
                itemView.llStudent.visibility = View.VISIBLE
                itemView.popupicon.visibility = View.GONE
                AppConstantsValue.solutionCollectionRef
                    .whereEqualTo("assignmentDocumentId", assignmentModel.documentId).whereEqualTo("studentSubmitter",
                        FirebaseAuth.getInstance().currentUser!!.documentReference()).limit(1).get().addOnCompleteListener {
                        if (it.isSuccessful && !it.result?.isEmpty!!)
                        {
                            val solutionModel = it.result?.toObjects(SolutionModel::class.java)

                            val timestampDate =
                                android.text.format.DateFormat.getDateFormat(itemView.context)
                                    .format(solutionModel?.get(0)?.timestamp?.toDate())
                            val timestampTime =
                                android.text.format.DateFormat.getTimeFormat(itemView.context)
                                    .format(solutionModel?.get(0)?.timestamp?.toDate())
                            val timestamp = "Last submitted: $timestampTime, $timestampDate"
                            itemView.tvLastSubmittedTime.visibility = View.VISIBLE
                            itemView.tvLastSubmittedTime.text = timestamp
                        }
                        else
                        {
                            itemView.tvLastSubmittedTime.text = itemView.context.getString(R.string.not_submitted_yet)
                        }
                    }
            }


            itemView.tvSubmit.setOnClickListener {
                if (assignmentModel.documentId != null)
                {
                    mAssignmentFragment.openFileLocation(assignmentModel.documentId!!)
                }
            }

            itemView.tvViewSubmission.setOnClickListener {
                // open assisgnment solution list activity
                var intent = Intent(itemView.context,SubmissionActivity::class.java)
                intent.putExtra(SubmissionActivity.ASSIGNMENT_ID,assignmentModel.documentId)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int
    {
        return assignmentList.size
    }


}
