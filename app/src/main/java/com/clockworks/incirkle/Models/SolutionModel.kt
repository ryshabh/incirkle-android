package com.clockworks.incirkle.Models

import com.clockworks.incirkle.filePicker.KotResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class SolutionModel(
    var documentId: String? = null,
    var assignmentDocumentId: String? = null,
    var solutionAttachmentUrl: String? = null,
    var solutionAttachmentDetail: KotResult? = null,
    var studentSubmitter: DocumentReference? = null,
    var timestamp: Timestamp? = null,
    @get:Exclude
    var user:User?=null

)
{

    constructor(
        assignmentDocumentId: String?,
        solutionAttachmentUrl: String?,
        solutionAttachmentDetail: KotResult?,
        studentSubmitter: DocumentReference?
    ) : this()
    {
        this.assignmentDocumentId = assignmentDocumentId
        this.solutionAttachmentUrl = solutionAttachmentUrl
        this.solutionAttachmentDetail = solutionAttachmentDetail
        this.studentSubmitter = studentSubmitter
        this.timestamp = Timestamp.now()
    }

}
