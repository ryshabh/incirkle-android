package com.clockworks.incirkle.Models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

class Course()
{
    lateinit var id: String
    lateinit var name: String
    lateinit var code: String
    lateinit var teacher: DocumentReference
    var teachingAssistants = ArrayList<DocumentReference>()
    var invitedStudents = HashMap<String, String>()

    @Exclude
    var reference: DocumentReference? = null
}