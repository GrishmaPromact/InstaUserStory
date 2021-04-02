package com.example.instastory.data

import android.os.Parcelable
import com.example.instastory.data.Story
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryUser(var username: String? = "",
                     var profilePicUrl: String ? = "",
                     var stories: MutableList<Story> ? = mutableListOf(),
                     var viewInex: Int? = 0,
                     var isAllStorySeen : Boolean? = false,
                     var isPinStory : Boolean? = false
) : Parcelable {

                     }