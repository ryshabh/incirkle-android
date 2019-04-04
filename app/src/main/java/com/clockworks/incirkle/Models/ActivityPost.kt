package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.clockworks.incirkle.filePicker.KotResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class ActivityPost(): FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var description: String
    lateinit var poster: DocumentReference
    var attachmentPath: String? = null
    var attachmentDetail:KotResult?=null
    lateinit var timestamp: Timestamp
    var imagepath : String = ""

    constructor(description: String, poster: DocumentReference) : this()
    {
        this.description = description
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }

    constructor(description: String, poster: DocumentReference,attachmentPath:String?,attachmentDetail:KotResult?) : this()
    {
        this.description = description
        this.poster = poster
        this.attachmentPath = attachmentPath
        this.attachmentDetail = attachmentDetail
        this.timestamp = Timestamp(Date())
    }
}