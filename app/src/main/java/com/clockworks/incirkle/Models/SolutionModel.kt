package com.clockworks.incirkle.Models

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
    var solutionAttachmentFileType: String? = null,
    var studentSubmitter: DocumentReference? = null,
    var timestamp: Timestamp? = null,
    @get:Exclude
    var user:User?=null

)
{

    constructor(
        assignmentDocumentId: String?,
        solutionAttachmentUrl: String?,
        solutionAttachmentFileType: String?,
        studentSubmitter: DocumentReference?
    ) : this()
    {
        this.assignmentDocumentId = assignmentDocumentId
        this.solutionAttachmentUrl = solutionAttachmentUrl
        this.solutionAttachmentFileType = solutionAttachmentFileType
        this.studentSubmitter = studentSubmitter
        this.timestamp = Timestamp.now()
    }

}
