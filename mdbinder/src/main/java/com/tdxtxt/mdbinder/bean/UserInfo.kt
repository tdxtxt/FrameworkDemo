package com.tdxtxt.mdbinder.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-27
 *     desc   :
 * </pre>
 */
class UserInfo() : Parcelable {
    var id: String? = null
    var name: String? = null

    constructor(id: String?, name: String?): this(){
        this.id = id
        this.name = name
    }

    /**
     * 必不可少
     */
    fun readFromParcel(parcel: Parcel){
        id = parcel.readString()
        name = parcel.readString()
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "id = $id; name = $name"
    }
}