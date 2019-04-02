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
import com.clockworks.incirkle.Fragments.AssignmentFragment
import com.clockworks.incirkle.Models.SolutionModel
import com.clockworks.incirkle.R
import com.clockworks.incirkle.utils.AppConstantsValue
import kotlinx.android.synthetic.main.item_post_assignment.view.*
import kotlinx.android.synthetic.main.item_solution_list.view.*
import android.support.v4.content.ContextCompat.startActivity




/**
 * RecyclerView adapter for a assignment fragment
 */
class SolutionAdapter(
    aList: ArrayList<SolutionModel>
) : RecyclerView.Adapter<SolutionAdapter.ViewHolder>()
{
    var solutionList = aList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_solution_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(solutionList.get(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        fun bind(solution: SolutionModel)
        {
            itemView.tvName.text = solution.user?.fullName()
            val timestampDate =
                android.text.format.DateFormat.getDateFormat(itemView.context)
                    .format(solution.timestamp?.toDate())
            val timestampTime =
                android.text.format.DateFormat.getTimeFormat(itemView.context)
                    .format(solution.timestamp?.toDate())
            val timestamp = "$timestampTime, $timestampDate"

            itemView.tvTimeStamp.text = timestamp

            itemView.cardView.setOnClickListener{
                itemView.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(solution.solutionAttachmentUrl))
                )
            }
        }


    }

    override fun getItemCount(): Int
    {
        return solutionList.size
    }


}
