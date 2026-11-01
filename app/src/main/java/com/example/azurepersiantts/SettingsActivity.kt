package com.example.azurepersiantts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("azure_tts", Context.MODE_PRIVATE)
        val keyInput = findViewById<EditText>(R.id.azure_key)
        val regionInput = findViewById<EditText>(R.id.azure_region)
        val saveButton = findViewById<Button>(R.id.save_button)

        keyInput.setText(prefs.getString("azure_key", ""))
        regionInput.setText(prefs.getString("azure_region", "eastus"))

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("azure_key", keyInput.text.toString().trim())
                .putString("azure_region", regionInput.text.toString().trim().ifEmpty { "eastus" })
                .apply()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
