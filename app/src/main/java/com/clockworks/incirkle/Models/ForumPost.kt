package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.clockworks.incirkle.filePicker.KotResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class ForumPost() : FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var name: String
    lateinit var description: String
    var attachmentPath: String? = null
    var attachmentDetail: KotResult? = null
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp
    var imagepath: String = ""

    var commentCount:Int = 0


    constructor(
        name: String,
        description: String,
        poster: DocumentReference,
        attachmentPath: String?,
        attachmentDetail: KotResult?
    ) : this()
    {
        this.name = name
        this.description = description
        this.poster = poster
        this.attachmentPath = attachmentPath
        this.attachmentDetail = attachmentDetail
        this.timestamp = Timestamp(Date())
    }

}