package com.example.jacie.functions

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.net.Credentials
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Telephony
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jacie.assistant.AssistantViewModel
import com.example.jacie.utils.utils.logKeeper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.ml.quaterion.text2summary.Text2Summary
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class AssistantFunctions {
    companion object {
        var REQUEST_CALL = 1
        var SEND_SMS = 2
        var READ_SMS = 3
        var SHARE_FILE = 4
        var SHARE_TEXT_FILE = 5
        var READ_CONTACTS = 6
        var CAPTURE_PHOTO = 7
        var imageIndex: Int = 0
        var REQUEST_CODE_SELECT_DOC: Int = 100
        var REQUEST_ENABLE_BT = 1000
        var questions: List<String> = listOf<String>(
            "What would you name your boat if you had one? ",
            "What's the closest thing to real magic?",
            "Who is the messiest person you know?",
            "What will finally break the internet? ",
            "What's the most useless talent you have?",
            "Where is the worst smelling place you've been?"
        )


        @Suppress("DEPRECATION")
        val imageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/assistant"
        val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        fun speak(
            message: String,
            textToSpeech: TextToSpeech,
            assistantViewModel: AssistantViewModel,
            keeper: String
        ) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
            assistantViewModel.sendMessageToDatabase(keeper, message)
        }

        fun getDate(textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            val calendar = Calendar.getInstance()
            val formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
            val splitDate = formattedDate.split(",").toTypedArray()
            val date = splitDate[1].trim { it <= ' ' }
            speak("The date is $date", textToSpeech, assistantViewModel, keeper)
        }

        fun getTime(
            textToSpeech: TextToSpeech,
            assistantViewModel: AssistantViewModel,
            keeper: String
        ) {
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("HH:mm:ss")
            val time = format.format(calendar.time)
            speak("The Time is $time", textToSpeech, assistantViewModel, keeper)
        }
        fun openFacebook(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.facebook.katana")
            intent.let { activity.startActivity(it) }
        }
        fun openMaps(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
            intent.let { activity.startActivity(it) }
        }
        fun openGoogle(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.google.android.googlequicksearchbox")
            intent.let { activity.startActivity(it) }
        }
        fun openYoutube(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            intent.let { activity.startActivity(it) }
        }
        fun openWhatsapp(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.whatsapp")
            intent.let { activity.startActivity(it) }
        }
        fun openMessages(activity: Activity, context: Context) {
            val intent = activity.packageManager.getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(context))
            intent.let { activity.startActivity(it) }
        }
        fun openGmail(activity:Activity){
            val intent = activity.packageManager.getLaunchIntentForPackage("com.google.android.gm")
            intent.let { activity.startActivity(it) }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun readSMS(activity: Activity, context: Context, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_SMS
                ) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_SMS),
                    READ_SMS
                )
                Log.d(logKeeper, "reading sms")
            } else {
                val cursor = activity.contentResolver.query(
                    Uri.parse("content://sms"), null,
                    null, null)
                cursor!!.moveToFirst()
                speak("Your last message was" + cursor.getString(12), textToSpeech, assistantViewModel, keeper)

            }
        }
        fun shareAFile(activity:Activity,context: Context){
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    SHARE_FILE
                )
            }
            else{
                val buider=StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(buider.build())
                val myFileIntent=Intent(Intent.ACTION_GET_CONTENT)
                myFileIntent.type="application/pdf"
                activity.startActivityForResult(myFileIntent, REQUEST_CODE_SELECT_DOC)
            }
        }
        fun shareATextMessage(activity:Activity,context: Context,textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    SHARE_TEXT_FILE
                )
            }
            else{
                try {
                    val buider = StrictMode.VmPolicy.Builder()
                    StrictMode.setVmPolicy(buider.build())
                    val message = keeper.split("that").toTypedArray()[1]
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message)
                    activity.startActivity(Intent.createChooser(shareIntent, "Sharing Text"))
                }
                catch (e:Exception){
                    e.printStackTrace()
                    speak("Something Went Wrong", textToSpeech, assistantViewModel, keeper)
                }
            }
        }

        fun sendSMS(activity:Activity,context: Context,textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.SEND_SMS
                ) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.SEND_SMS),
                    SEND_SMS
                )
            }
            else{
                try {
                    val keeperReplaced=keeper.replace(" ".toRegex(),"")
                    val number=keeperReplaced.split("o").toTypedArray()[1].split("t").toTypedArray()[0]
                    val message=keeper.split("that").toTypedArray()[1]
                    val mySMSManager=SmsManager.getDefault()
                    mySMSManager.sendTextMessage(
                        number.trim{it <= ' '},
                        null,
                        message.trim{it <= ' '},
                        null,
                        null
                    )
                    speak("Message has been send to $number and the message was $message", textToSpeech, assistantViewModel, keeper)
                }catch (e:Exception){
                    e.printStackTrace()
                    speak("Something Went Wrong", textToSpeech, assistantViewModel, keeper)
                }

            }
        }

        fun makeAPhoneCall(activity:Activity,context: Context,textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            val keeperSplit=keeper.replace(" ".toRegex(),"").split("dial")
            val number=keeperSplit[1]
            if (number.trim{it<=' '}.isNotEmpty()){
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CALL_PHONE
                    ) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        REQUEST_CALL
                    )
                }
                else{
                    try {
                        val dial = "tel:$number"
                        speak("Calling  $number", textToSpeech, assistantViewModel, keeper)
                        activity.startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                        speak("Something Went Wrong", textToSpeech, assistantViewModel, keeper)
                    }
                }
            }
            else {
                speak("Dial Correct Phone Number", textToSpeech, assistantViewModel, keeper)
            }
        }
        fun search(activity: Activity, keeper: String)
        {
            val uri = Uri.parse("https://www.google.com/search?q=$keeper")
            val gSearchIntent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(gSearchIntent)
        }

        fun callContact(activity: Activity, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            var number= ""
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.READ_CONTACTS
                ) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS),
                    READ_CONTACTS
                )
            } else {
                val name = keeper.split("call").toTypedArray()[1].trim {
                    it <= ' '
                }
                Log.d("check", name)
                try {
                    val phones: Cursor = activity.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)!!
                    while (phones.moveToNext()) {
                        var contactName: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        contactName=contactName.toLowerCase()
                        if (contactName.contains(name.toLowerCase())) {
                            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            Log.d("number", number)
                        }
                    }
                    phones.close()
                    if (number.trim {
                            it <= ' '
                        }.isNotEmpty()) {
                        if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.CALL_PHONE) != PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL)
                        } else {
                            val dial = "tel:$number"
                            speak("Calling $name", textToSpeech, assistantViewModel, keeper)
                            activity.startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))

                        }
                    } else {
                        speak("Wrong Contact Name", textToSpeech, assistantViewModel, keeper)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("error in call", e.message.toString())
                    speak("Something went wrong", textToSpeech, assistantViewModel, keeper)
                }
            }
        }

        fun turnOnBluetooth(activity: Activity, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            if(!bluetoothAdapter.isEnabled){
                speak("Turning On Bluetooth",textToSpeech,assistantViewModel,keeper)
                val intent=Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(intent, REQUEST_ENABLE_BT)
            }
            else{
                speak("Bluetooth Already On",textToSpeech,assistantViewModel,keeper)
            }
        }

        fun turnOffBluetooth( textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            if(bluetoothAdapter.isEnabled){
                speak("Turning Off Bluetooth",textToSpeech,assistantViewModel,keeper)
                bluetoothAdapter.disable()
            }
            else{
                speak("Bluetooth Already Off",textToSpeech,assistantViewModel,keeper)
            }
        }

        fun getAllPairedDevices( textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String){
            if(bluetoothAdapter.isEnabled){
                speak("Paired Devices are",textToSpeech,assistantViewModel,keeper)
                var text=""
                var count=1
                val devices : Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
                for (device in devices){
                    text+="\nDevices : $count ${device.name}"
                    count+=1
                }
                speak(text,textToSpeech,assistantViewModel,keeper)
            }
            else{
                speak("To get paired devices turn On bluetooth",textToSpeech,assistantViewModel,keeper)
            }
        }

        fun turnOnFlash(cameraManager: CameraManager,cameraID:String,textToSpeech: TextToSpeech,assistantViewModel: AssistantViewModel,keeper: String){
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    cameraManager.setTorchMode(cameraID,true)
                    speak("Flash On",textToSpeech,assistantViewModel,keeper)
                }
            }catch (e:Exception){
                e.printStackTrace()
                speak("Error Occurred",textToSpeech,assistantViewModel,keeper)
            }
        }

        fun turnOffFlash(cameraManager: CameraManager,cameraID:String,textToSpeech: TextToSpeech,assistantViewModel: AssistantViewModel,keeper: String){
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    cameraManager.setTorchMode(cameraID,false)
                    speak("Flash Off",textToSpeech,assistantViewModel,keeper)
                }
            }catch (e:Exception){
                e.printStackTrace()
                speak("Error Occurred",textToSpeech,assistantViewModel,keeper)
            }
        }

        fun clipBoardCopy(clipboardManager: ClipboardManager, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            val data = keeper.split("that").toTypedArray()[0].trim {
                it <= ' '
            }
            if (data.isNotEmpty()) {
                val clipData = ClipData.newPlainText("text", data)
                clipboardManager.setPrimaryClip(clipData)
                speak("data copied to clipboard that is $data", textToSpeech, assistantViewModel, keeper)
            }
            else {
                speak("Clipboard is Empty", textToSpeech, assistantViewModel, keeper)
            }
        }

        fun clipBoardSpeak(clipboardManager: ClipboardManager, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            val item = clipboardManager.primaryClip!!.getItemAt(0)
            val pasteData = item.text.toString()
            if (pasteData != "") {
                speak("Data stored in last clipboard is $pasteData", textToSpeech, assistantViewModel, keeper)
            } else {
                speak("Clipboard is Empty", textToSpeech, assistantViewModel, keeper)
            }
        }

        fun capturePhoto(activity: Activity, context: Context, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CAPTURE_PHOTO

                )
            } else {
                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
                imageIndex++
                val file = "$imageDirectory$imageIndex.jpg"
                val newFile = File(file)
                try {
                    newFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val outputFileUri = Uri.fromFile(newFile)
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                activity.startActivity(cameraIntent)
                speak("Photo will be saved to $file", textToSpeech, assistantViewModel, keeper)

            }
        }

        fun playRingtone(ringtone: Ringtone,textToSpeech: TextToSpeech,assistantViewModel: AssistantViewModel,keeper: String){
            speak("Playing Ringtone", textToSpeech, assistantViewModel, keeper)
            ringtone.play()
        }

        fun stopRingtone(ringtone: Ringtone,textToSpeech: TextToSpeech,assistantViewModel: AssistantViewModel,keeper: String){
            speak("Ringtone stopped", textToSpeech, assistantViewModel, keeper)
            ringtone.stop()
        }
        fun readMe(activity: Activity) {
            CropImage.startPickImageActivity(activity)
        }
        fun question(textToSpeech: TextToSpeech,assistantViewModel: AssistantViewModel,keeper: String){
            val indexes:List<Int> = listOf(0,1,2,3,4,5)
            val index=indexes.random()
            speak(questions[index],textToSpeech,assistantViewModel,keeper)
        }

        fun motivationalThoughts(textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {

            val thoughts: List<String> = listOf<String>("If you want to achieve greatness stop asking for permission.",
                "Things work out best for those who make the best of how things work out.",
                "To live a creative life, we must lose our fear of being wrong.",
                "If you are not willing to risk the usual you will have to settle for the ordinary.",
                "Trust because you are willing to accept the risk, not because it's safe or certain.",
                "Take up one idea. Make that one idea your life--think of it, dream of it, live on that idea. Let the brain, muscles, " +
                        "nerves, every part of your body, be full of that idea, and just leave" +
                        " every other idea alone. This is the way to success.",
                "All our dreams can come true if we have the courage to pursue them.",
                "Good things come to people who wait, but better things come to those who go out and get them.",
                "If you do what you always did, you will get what you always got.",
                "Success is walking from failure to failure with no loss of enthusiasm.",
                "Just when the caterpillar thought the world was ending, he turned into a butterfly.")
            val indexes: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            val index = indexes.random()
            speak(thoughts[index], textToSpeech, assistantViewModel, keeper)
        }

        fun joke(textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            val jokes: List<String> = listOf<String>("That bizarre moment when you pick up your car from the garage and you realize that the breaks are still not working, but they made your horn louder.\n",
                " I had a dream where an evil queen forced me to eat a gigantic marshmallow. When I woke up, my pillow was gone.\n",
                "Yesterday I learnt that 20 piranhas can strip all flesh off a man within 15 minutes. - Unfortunately, I also lost my job at the local swimming pool.\n"
            )
            val indexes: List<Int> = listOf(0, 1, 2)
            val index = indexes.random()
            speak(jokes[index], textToSpeech, assistantViewModel, keeper)
        }

        fun getTextFromBitmap(activity: Activity, bitmap: Bitmap, textToSpeech: TextToSpeech, assistantViewModel: AssistantViewModel, keeper: String) {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient()
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val resultText = visionText.text
                    if (keeper.contains("summarise")) {
                        speak("Reading Image and Summarising it :\n" +
                                summariseText(resultText), textToSpeech, assistantViewModel, keeper)
                    } else {
                        speak("Reading Image and Summarising it $resultText", textToSpeech, assistantViewModel, keeper)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity.applicationContext, "Error ${e.message}", Toast.LENGTH_SHORT).show()

                }
        }

        private fun summariseText(keeper: String): String? {
            val summary: kotlin.jvm.internal.Ref.ObjectRef<*> = kotlin.jvm.internal.Ref.ObjectRef<Any?>()
            summary.element = Text2Summary.Companion.summarize(keeper, 0.04f)
            return summary.element as String
        }

    }
}