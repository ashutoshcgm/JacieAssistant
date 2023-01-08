package com.example.jacie.assistant


import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jacie.MainActivity
import com.example.jacie.R
import com.example.jacie.location.GPS_Tracker
import com.example.jacie.utils.utils
import com.example.jacie.utils.utils.commands
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject
import java.util.*


class ExploreActivity : AppCompatActivity() {


    var latitudeEx = 0.0
    var longitudeEx = 0.0

    var weather_url="https://api.weatherbit.io/v2.0/current?lat=23.2836915&lon=77.4617912&key=6920521469c242709bf208680dae0fcc"
    lateinit var newWeatherURL:String
    val api_id_my="6920521469c242709bf208680dae0fcc"

    private lateinit var temperature:TextView
    private lateinit var description:TextView
    private lateinit var greetings:TextView
    private lateinit var today:TextView
    private lateinit var cardViewWeather:CardView
    private val MY_PERMISSION_REQUEST_LOCATION=100
    private val REQUEST_GPS=101
    private var locationrequest: LocationRequest? =null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)
        utils.setCustomActionBar(supportActionBar, this)

        temperature=findViewById(R.id.temperature)
        description=findViewById(R.id.description)
        greetings=findViewById(R.id.greetings)
        today=findViewById(R.id.today)
        cardViewWeather=findViewById(R.id.weatherCardView)

        val chipGroup:ChipGroup=findViewById(R.id.chipCommands)
        for (command in commands){
            val chip = Chip(this)
            chip.text = command.toString()
            chip.setTextAppearance(R.style.chips)
            chip.setButtonDrawable(R.drawable.shape)
            chip.setPadding(20, 10, 20, 10)
            chipGroup.addView(chip)
        }

        if (ContextCompat.checkSelfPermission(this@ExploreActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ExploreActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }

        val locationManager=getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            val builder=AlertDialog.Builder(this)
            builder.setTitle("Turn ON Location")
            builder.setMessage("Location is OFF, Please Try to ON  ")
            builder.setPositiveButton("OK") {
                dialog,which->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            builder.setNegativeButton("Cancel") {
                    dialog,which->
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
            }
            val dialog:AlertDialog=builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.darker_gray)
            dialog.show()
        }

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        obtainLocation()

        cardViewWeather.setOnClickListener{
            obtainLocation()
        }

        val c:Calendar=Calendar.getInstance()
        when(c.get(Calendar.HOUR_OF_DAY)){
            in 4..11->{
                greetings.text= "Good Morning !!"
            }
            in 12..15->{
                greetings.text="Good Afternoon !!"
            }
            in 16..21->{
                greetings.text="Good Evening !!"
            }
            in 22..23->{
                greetings.text="Good Night !!"
            }
            in 0..3->{
                greetings.text="Good Night !!"
            }
        }

    }

    private fun obtainLocation() {

        val gpsTracker = GPS_Tracker(this)
        latitudeEx=gpsTracker.getLatitude()
        longitudeEx=gpsTracker.getLongitude()

        latitudeEx=Math.round(latitudeEx * 1000000.0) / 1000000.0
        longitudeEx=Math.round(longitudeEx * 1000000.0) / 1000000.0

        newWeatherURL="https://api.weatherbit.io/v2.0/current?lat=$latitudeEx&lon=$longitudeEx&key=6920521469c242709bf208680dae0fcc"
        // To fetch Weather API URL Data
        val queue=Volley.newRequestQueue(this)
        val stringReq = StringRequest(Request.Method.GET, newWeatherURL,
            { response ->
                val obj = JSONObject(response)
                val arr = obj.getJSONArray("data")
                val obj2 = arr.getJSONObject(0)
                temperature.text = obj2.getString("temp") + "°C " + obj2.getString("city_name")
                today.text = "TODAY | " + obj2.getString("datetime")
                description.text = obj2.getJSONObject("weather").getString("description")
            },
            { Log.e("error", "not worked") })
        queue.add(stringReq)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1->{
                if (grantResults.isNotEmpty() && grantResults[0]== PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this@ExploreActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PERMISSION_GRANTED){
                        Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}