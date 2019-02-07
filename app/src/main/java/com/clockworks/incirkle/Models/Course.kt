package com.clockworks.incirkle.Models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

class Course()
{
    lateinit var password: String
    lateinit var name: String
    lateinit var code: String
    lateinit var teacher: DocumentReference
    var teachingAssistants = ArrayList<DocumentReference>()
    var invitedStudents = HashMap<String, String>()

    @Exclude
    var reference: DocumentReference? = null

    constructor(password: String, teacher: DocumentReference): this()
    {
        this.password = password
        this.name = ""
        this.code = ""
        this.teacher = teacher
    }
}