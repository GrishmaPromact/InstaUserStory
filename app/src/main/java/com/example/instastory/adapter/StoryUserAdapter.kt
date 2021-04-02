package com.c2m.storyviewer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.MultiAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instastory.data.StoryUser
import com.example.instastory.R
import com.example.instastory.databinding.ItemStoryUserBinding
import java.util.ArrayList

/**
 *<h1></h1>

 *<p></p>

 * @author : Grishma
 * @since : 26 Sep, 2020
 * @version : 1.0
 * @company : Saeculum Solutions Pvt. Ltd.
 */
class StoryUserAdapter(
        private var storyUsersList: MutableList<StoryUser>?,
        private val context: Context
) : RecyclerView.Adapter<StoryUserAdapter.StoryUserViewHolder>() {

    private lateinit var binding: ItemStoryUserBinding
    var onSelectionChangeListener: ((storyUserModel: StoryUser, position: Int) -> Unit)? = null
    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): StoryUserViewHolder {
        val binding =
                ItemStoryUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryUserViewHolder, position: Int) {
        val storyUser = storyUsersList?.get(position)

        /* if(storyUser?.isStorySeen==true){
             holder.binding.ivUserProfile.borderColor = ContextCompat.getColor(context,R.color.visited_story_color)
         }*/

        if (storyUser?.isPinStory == false) {
            if (storyUser!!.viewInex!! >= (storyUser.stories!!.size)) {
                holder.binding.ivUserProfile.borderColor = ContextCompat.getColor(context, R.color.visited_story_color)
            }
        } else {
            holder.binding.ivUserProfile.borderColor = ContextCompat.getColor(context, R.color.pending_story_color)
        }

        Glide.with(context)
                .load(storyUser?.profilePicUrl)
                .apply(RequestOptions().circleCrop())
                .centerCrop()
                .placeholder(R.drawable.user_image)
                .into(holder.binding.ivUserProfile)

        holder.binding.tvUserName.text = storyUser?.username.toString()

        holder.binding.root.setOnClickListener {
            onSelectionChangeListener?.invoke(storyUser!!, position)
        }
    }


    override fun getItemCount(): Int = storyUsersList?.size ?: 0


    fun updateList(storyUsersList: MutableList<StoryUser>?) {

        //storyUsersList?.clear()
        //storyUsersList?.addAll(storyUsersList!!)
        this.storyUsersList = storyUsersList
        notifyDataSetChanged()
    }

    class StoryUserViewHolder(val binding: ItemStoryUserBinding) :
            RecyclerView.ViewHolder(binding.root)
}