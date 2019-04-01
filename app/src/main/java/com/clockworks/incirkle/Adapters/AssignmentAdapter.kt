package com.clockworks.incirkle.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clockworks.incirkle.Models.AssignmentModel
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.item_post_assignment.view.*


/**
 * RecyclerView adapter for a assignment fragment
 */
class AssignmentAdapter(
    aList: ArrayList<AssignmentModel>,
    teacher: Boolean,
    teachingAssistant: Boolean
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>()
{
    var isTeacher = teacher
    var isTeachingAssistant = teachingAssistant
    var assignmentList = aList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_post_assignment, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(assignmentList.get(position), isTeacher, isTeachingAssistant)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {

        fun bind(
            assignmentModel: AssignmentModel,
            teacher: Boolean,
            teachingAssistant: Boolean
        )
        {

            val resources = itemView.resources


            // user detail
//            itemView.imageView_assignmentPost_posterPicture
//           itemView.textView_assignmentPost_posterName.text = assignmentModel.name
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


//            itemView.button_assignmentPost_download_attachment =
//            itemView.button_assignmentPost_download_images


            if (teacher || teachingAssistant)
            {
                itemView.llTeacher.visibility = View.VISIBLE
                itemView.llStudent.visibility = View.GONE
                itemView.popupicon.visibility = View.VISIBLE
//            itemView.tvViewSubmission
//            itemView.tvSubmissionCount
            }
            else
            {
                itemView.llTeacher.visibility = View.GONE
                itemView.llStudent.visibility = View.VISIBLE
                itemView.popupicon.visibility = View.GONE
//            itemView.tvSubmit
//            itemView.tvLastSubmittedTime
            }
        }
    }

    override fun getItemCount(): Int
    {
        return assignmentList.size
    }


}
