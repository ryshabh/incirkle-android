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

    constructor(id: String, teacher: DocumentReference): this()
    {
        this.id = id
        this.name = ""
        this.code = ""
        this.teacher = teacher
    }
}