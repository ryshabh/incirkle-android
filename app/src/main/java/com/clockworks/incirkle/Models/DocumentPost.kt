package com.clockworks.incirkle.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class DocumentPost()
{
    var reference: DocumentReference? = null

    lateinit var name: String
    lateinit var details: String
    // TODO: - Attachment
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp

    constructor(name: String, details: String, poster: DocumentReference) : this()
    {
        this.name = name
        this.details = details
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }
}