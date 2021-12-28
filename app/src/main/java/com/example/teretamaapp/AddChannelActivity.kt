package com.example.teretamaapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.io.*

val supportedMIME = listOf("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/heic", "image/heif")

class AddChannelActivity : AppCompatActivity(), URLDialogFragment.URLDialogListener {
    private var startForResult: ActivityResultLauncher<Intent>? = null
    private var uri: Uri? = null
    private var editId: Int? = null

    private lateinit var preview: ImageView
    private lateinit var hint: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_channel)

        // Configure toolbar
        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Handle results
        val addText = findViewById<TextInputEditText>(R.id.add_text)
        val button = findViewById<Button>(R.id.add_button)
        val container = findViewById<MaterialCardView>(R.id.add_imageContainer)
        val urlSelect = findViewById<Button>(R.id.add_select)

        preview = findViewById(R.id.add_imagePreview)
        hint = findViewById(R.id.add_imageHint)

        // Check if the activity was invoked in add or edit mode
        val editData = intent.getBundleExtra("edit")
        if (editData != null) { // edit mode
            val editUri = Uri.parse(editData.getString("imageUri"))
            addText.setText(editData.getString("name"))

            toolbar.setTitle(R.string.edit_title)
            button.setText(R.string.edit_button)

            hint.visibility = View.INVISIBLE
            preview.load(editUri)
            uri = editUri

            editId = editData.getInt("id")
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val res = result.data?.data

                applicationContext.contentResolver.openInputStream(res!!).use { input ->
                    val filePath = applicationContext.applicationInfo.dataDir + File.separator + System.currentTimeMillis() + ".png"

                    val file: File = saveStream(filePath, input!!)

                    hint.visibility = View.INVISIBLE
                    preview.load(file.toUri())
                    uri = file.toUri()
                }
            }
        }

        container.setOnClickListener {
            imageChooser()
        }

        urlSelect.setOnClickListener {
            val dialogFragment = URLDialogFragment()
            dialogFragment.show(supportFragmentManager, "urldialog")
        }

        button.setOnClickListener {
            if (TextUtils.isEmpty(addText.text) || uri == null) {
                Toast.makeText(this, R.string.add_error, Toast.LENGTH_SHORT).show()
            } else {
                val replyIntent = Intent()
                val name = addText.text.toString()

                if (editId != null) {
                    replyIntent.putExtra(EXTRA_ID, editId)
                }
                replyIntent.putExtra(EXTRA_REPLY, name)
                replyIntent.putExtra(EXTRA_IMAGEURI, uri.toString())

                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, url: String) {
        // Verify if the URL is valid
        if (Patterns.WEB_URL.matcher(url).matches()) {
                Thread {
                    try {
                        val con = java.net.URL(url).openConnection()
                        val contentType = con.contentType

                        val input = con.getInputStream()
                        val file: File?

                        if (supportedMIME.contains(contentType)) {
                            val filePath = applicationContext.applicationInfo.dataDir + File.separator + System.currentTimeMillis() + ".png"
                            file = saveStream(filePath, input)
                        } else if (contentType.contains("svg")) {
                            val filePath = applicationContext.applicationInfo.dataDir + File.separator + System.currentTimeMillis() + ".svg"
                            file = saveStream(filePath, input)
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, R.string.add_error_format, Toast.LENGTH_SHORT).show()
                            }
                            return@Thread
                        }
                        runOnUiThread {
                            hint.visibility = View.INVISIBLE
                            preview.load(file.toUri())
                            uri = file.toUri()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
        } else {
            Toast.makeText(this, R.string.add_url_invalid, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStream(path: String, input: InputStream) : File {
        val file = File(path)

        val outputStream = FileOutputStream(file)
        outputStream.use { output ->
            val buffer = ByteArray(4 * 1024)
            while (true) {
                val byteCount = input.read(buffer)
                if (byteCount < 0) break
                output.write(buffer, 0, byteCount)
            }
            output.flush()
        }

        return file
    }

    private fun imageChooser() {
        intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startForResult?.launch(intent)
    }

    companion object {
        const val EXTRA_REPLY = "com.example.teretamaapp.ADD_REPLY"
        const val EXTRA_IMAGEURI = "com.example.teretamaapp.ADD_IMAGEURI"
        const val EXTRA_ID = "com.example.teretamaapp.EDIT_ID"
    }
}