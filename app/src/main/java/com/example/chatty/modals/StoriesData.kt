package com.example.chatty.modals

class StoriesData() {
    private lateinit var statusImageUri: String
    private var LastUpdated: Long = 0

    constructor(statusImageUri: String, LastUpdated: Long) : this() {
        this.statusImageUri = statusImageUri
        this.LastUpdated = LastUpdated
    }

    fun getStatusImageUri(): String{
        return statusImageUri
    }
    fun setStatusImageUri(statusImageUri: String){
        this.statusImageUri = statusImageUri
    }
    fun getLastUpdated(): Long{
        return LastUpdated
    }
    fun setLastUpdated(timeStamp: Long){
        this.LastUpdated = timeStamp
    }

}