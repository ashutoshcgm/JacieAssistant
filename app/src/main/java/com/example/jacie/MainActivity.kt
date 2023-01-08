package com.example.jacie

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.jacie.utils.utils.setCustomActionBar
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jacie.assistant.AssistantActivity
import com.example.jacie.assistant.ExploreActivity
import com.example.jacie.functions.GoogleLensActivity

class MainActivity : AppCompatActivity() {
    private lateinit var hijacie:ImageView
    private lateinit var jexplore:ImageView
    private lateinit var jlens:ImageView
    private lateinit var jassistant:ImageView
    private val REQUEST_PERMISSION_CODE:Int=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setCustomActionBar(supportActionBar,this)

        hijacie=findViewById(R.id.hijacie)
        jexplore=findViewById(R.id.jexplore)
        jlens=findViewById(R.id.jlens)
        jassistant=findViewById(R.id.jassistant)

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO)!= PERMISSION_GRANTED){
            checkPermission()
        }

        jexplore.setOnClickListener{
            startActivity(Intent(this,ExploreActivity::class.java))
        }
        jlens.setOnClickListener{
            startActivity(Intent(this,GoogleLensActivity::class.java))
        }
        jassistant.setOnClickListener{
            startActivity(Intent(this,AssistantActivity::class.java))
        }

    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==REQUEST_PERMISSION_CODE &&grantResults.isNotEmpty()){
            if (grantResults[0]== PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
            }
        }
    }
}