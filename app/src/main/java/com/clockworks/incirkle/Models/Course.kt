package com.clockworks.incirkle.Models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

class Course()
{
    lateinit var name: String
    lateinit var teacher: DocumentReference
    var teachingAssistants = ArrayList<DocumentReference>()
    lateinit var code: String
    var invitedStudents = HashMap<String, String>()

    @Exclude
    var reference: DocumentReference? = null
}