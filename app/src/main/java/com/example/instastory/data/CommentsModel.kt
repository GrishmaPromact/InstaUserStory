package com.example.instastory.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentsModel(
        var userComment : String? = "",
        var userProfileUrl : String? = "") :Parcelable
{

}