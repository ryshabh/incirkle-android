package com.clockworks.incirkle.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Organisation
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.activity_select_organisation.*
import kotlinx.android.synthetic.main.alert_new_organisation.*

class SelectOrganisationActivity : AppActivity()
{
    private var organisations = ArrayList<Organisation>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_organisation)
        setSupportActionBar(toolbar)

        Organisation.reference.addSnapshotListener()
        {
            task, exception ->

            this.organisations = ArrayList()
            task?.let()
            {
                it.documents.forEach()
                {
                    this.performThrowable { it.serialize(Organisation::class.java) }
                    ?.let { this.organisations.add(it) }
                }
            }
            ?: exception?.let { this.showError(it) }
            spinner_organisations.adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, this.organisations.map { "${it.name}, ${it.location}" })
            button_proceed.isEnabled = !this.organisations.isEmpty()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun createOrganisation(v: View)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Organisation")
        builder.setView(R.layout.alert_new_organisation)
        builder.setPositiveButton("Add", null)
        builder.setNeutralButton("Cancel", null)
        val alert = builder.create()
        alert.setOnShowListener()
        {
            dialogInterface ->
            (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener()
            {
                val organisation_name_textView = dialogInterface.textView_organisation_name
                val organisation_location_textView = dialogInterface.textView_organisation_location
                organisation_name_textView.error = null
                organisation_location_textView.error = null

                val name = organisation_name_textView.text.toString().trim()
                val location = organisation_location_textView.text.toString().trim()

                if (name.isBlank())
                {
                    organisation_name_textView.error = "Name cannot be blank"
                    return@setOnClickListener
                }
                else if (location.isBlank())
                {
                    organisation_location_textView.error = "Location cannot be blank"
                    return@setOnClickListener
                }

                val organisation = Organisation(name, location)
                Organisation.reference.add(organisation)
                    .addOnFailureListener(::showError)
                    .addOnSuccessListener()
                    {
                        Toast.makeText(this, "Successfully added $name", Toast.LENGTH_LONG).show()
                        dialogInterface.dismiss()
                    }
            }
        }
        alert.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun proceed(v: View)
    {
        this.organisations[spinner_organisations.selectedItemPosition].reference?.id?.let()
        {
            val intent = Intent(this, EnrolCourseActivity::class.java)
            intent.putExtra(EnrolCourseActivity.IDENTIFIER_SELECTED_ORGANISATION, it)
            startActivity(intent)
        }
    }
}
