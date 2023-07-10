package com.example.chatty.modals

class MessageData() {
    private var messageId: String = "0"
    private lateinit var message: String
    private lateinit var contentType: String
    private lateinit var senderId: String
    private lateinit var caption: String
    private var timeStamp: Long = 0

    constructor(message: String, senderId: String, timeStamp: Long, contentType: String, caption: String) : this() {
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
        this.contentType = contentType
        this.caption = caption
    }


    fun getMessageId(): String{
        return messageId
    }
    fun setMessageId(MessageId: String){
        this.messageId = MessageId
    }
    fun getMessage(): String{
        return message
    }
    fun setMessage(message: String){
        this.message = message
    }

    fun getCaption(): String{
        return caption
    }
    fun setCaption(caption: String){
        this.caption = caption
    }

    fun getContentType(): String{
        return contentType
    }
    fun setContentType(contentType: String){
        this.contentType = contentType
    }

    fun getSenderId(): String{
        return senderId
    }
    fun setSenderId(senderId: String){
        this.senderId = senderId
    }
    fun getTimeStamp(): Long{
        return timeStamp
    }
    fun setTimeStamp(timeStamp: Long){
        this.timeStamp = timeStamp
    }
}