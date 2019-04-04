package com.clockworks.incirkle.filePicker

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

/**
 * Created by AndroidBuffer on 13/4/18.
 */
@IgnoreExtraProperties
data class KotResult(
    @get:Exclude
    val uri: Uri? = null
    , val name: String? = null
    , val size: String? = null
    , val location: String? = null
    , val type: String? = null
    , val modified: String? = null
    , var nameInFirebase:String?=null
):Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )
    {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(name)
        parcel.writeString(size)
        parcel.writeString(location)
        parcel.writeString(type)
        parcel.writeString(modified)
        parcel.writeString(nameInFirebase)
    }

    override fun describeContents(): Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KotResult>
    {
        override fun createFromParcel(parcel: Parcel): KotResult
        {
            return KotResult(parcel)
        }

        override fun newArray(size: Int): Array<KotResult?>
        {
            return arrayOfNulls(size)
        }
    }

}