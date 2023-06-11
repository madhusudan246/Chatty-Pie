package com.example.chatty.modals

class StoriesData() {
    private lateinit var statusImageUri: String
    private var lastUpdated: Long = 0

    constructor(statusImageUri: String, LastUpdated: Long) : this() {
        this.statusImageUri = statusImageUri
        this.lastUpdated = LastUpdated
    }

    fun getStatusImageUri(): String{
        return statusImageUri
    }
    fun setStatusImageUri(statusImageUri: String){
        this.statusImageUri = statusImageUri
    }
    fun getLastUpdated(): Long{
        return lastUpdated
    }
    fun setLastUpdated(timeStamp: Long){
        this.lastUpdated = timeStamp
    }

}