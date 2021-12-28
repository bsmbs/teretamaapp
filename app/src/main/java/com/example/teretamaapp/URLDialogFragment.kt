package com.example.teretamaapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class URLDialogFragment : DialogFragment() {
    internal lateinit var listener: URLDialogListener
    lateinit var url: String

    interface URLDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, url: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_url, null)
            val editText = view.findViewById<EditText>(R.id.url)

            builder.setView(view)
                .setTitle(R.string.add_url)
                .setPositiveButton(R.string.add_button) { _, _ ->
                    val url = editText.text.toString()
                    listener.onDialogPositiveClick(this, url)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dialog?.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as URLDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement URLDialogListener"))
        }
    }
}