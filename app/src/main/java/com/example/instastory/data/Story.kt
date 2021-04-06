package com.example.instastory.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Story(var url: String? = "",
                 var storyDate: Long ? = 0L,
                 var commentsList : MutableList<CommentsModel>? = mutableListOf(),
                 var ctaText : String? = "",
                 var ctaUrl : String? = ""
) : Parcelable {

    fun isVideo() =  url?.contains(".mp4")
}