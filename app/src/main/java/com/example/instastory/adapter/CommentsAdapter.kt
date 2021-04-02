package com.example.instastory.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instastory.R
import com.example.instastory.data.CommentsModel
import com.example.instastory.databinding.ItemStoryCommentBinding
import java.util.ArrayList

/**
 *<h1></h1>

 *<p></p>

 * @author : Grishma
 * @since : 26 Sep, 2020
 * @version : 1.0
 * @company : Saeculum Solutions Pvt. Ltd.
 */
class CommentsAdapter(
        private val commentsList: MutableList<CommentsModel>?,
        private val context: Context
) : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

    private lateinit var binding: ItemStoryCommentBinding
    var onSelectionChangeListener: ((commentsModel : CommentsModel, position :Int) -> Unit)? = null
    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CommentsViewHolder {
        val binding =
            ItemStoryCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comments = commentsList?.get(position)


        Glide.with(context)
            .load(comments?.userProfileUrl)
            .apply(RequestOptions().circleCrop())
            .centerCrop()
            .placeholder(R.drawable.user_image)
            .into(holder.binding.ivCommentUserProfile)

        holder.binding.tvUserComment.text = comments?.userComment.toString()

        holder.binding.root.setOnClickListener {
            onSelectionChangeListener?.invoke(comments!!,position)
        }
    }


    override fun getItemCount(): Int = commentsList?.size ?: 0


    fun updateList(commentsList1: MutableList<CommentsModel>?) {

        commentsList?.clear()
        commentsList?.addAll(commentsList1!!)
        //storyUsersList == storyUsersList1
        notifyDataSetChanged()
    }

    class CommentsViewHolder(val binding: ItemStoryCommentBinding) :
        RecyclerView.ViewHolder(binding.root)
}