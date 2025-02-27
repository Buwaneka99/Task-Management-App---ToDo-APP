package com.example.todoapp

import android.os.Parcel
import android.os.Parcelable

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val remindertime: String,
    var isCompleted: Boolean = false,
    var category: String = "Others",
    var reminderTime: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "Others",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(remindertime)
        parcel.writeByte(if (isCompleted) 1 else 0)
        parcel.writeString(category)
        parcel.writeString(reminderTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}