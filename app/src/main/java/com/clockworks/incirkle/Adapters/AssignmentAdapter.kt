package com.clockworks.incirkle.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.clockworks.incirkle.Models.AssignmentModel
import com.clockworks.incirkle.Models.AssignmentPost
import com.clockworks.incirkle.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query


/**
 * RecyclerView adapter for a assignment fragment
 */
open class AssignmentAdapter(query: Query) :
        FirestoreAdapter<AssignmentAdapter.ViewHolder>(query) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_post_assignment, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot
        ) {

            val assignmentModel = snapshot.toObject(AssignmentModel::class.java)
            if (assignmentModel == null) {
                return
            }

            val resources = itemView.resources

            // Load image
//            Glide.with(itemView.restaurantItemImage.context)
//                    .load(restaurant.photo)
//                    .into(itemView.restaurantItemImage)
//
//            val numRatings: Int = restaurant.numRatings
//
//            itemView.restaurantItemName.text = restaurant.name
//            itemView.restaurantItemRating.rating = restaurant.avgRating.toFloat()
//            itemView.restaurantItemCity.text = restaurant.city
//            itemView.restaurantItemCategory.text = restaurant.category
//            itemView.restaurantItemNumRatings.text = resources.getString(
//                    R.string.fmt_num_ratings,
//                    numRatings)
//            itemView.restaurantItemPrice.text = RestaurantUtil.getPriceString(restaurant)


        }
    }
}
