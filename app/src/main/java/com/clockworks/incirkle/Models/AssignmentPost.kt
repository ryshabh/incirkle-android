package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class AssignmentPost(): FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var name: String
    lateinit var details: String
    lateinit var dueDate: Timestamp
    var attachmentPath: String? = null
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp

    constructor(name: String, details: String, dueDate: Date, poster: DocumentReference) : this()
    {
        this.name = name
        this.details = details
        this.dueDate = Timestamp(dueDate)
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }
}