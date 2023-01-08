package com.example.jacie.assistant

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.jacie.R
import com.example.jacie.data.AssistantDatabase
import com.example.jacie.databinding.ActivityAssistantBinding
import com.example.jacie.functions.AssistantFunctions.Companion.CAPTURE_PHOTO
import com.example.jacie.functions.AssistantFunctions.Companion.READ_CONTACTS
import com.example.jacie.functions.AssistantFunctions.Companion.READ_SMS
import com.example.jacie.functions.AssistantFunctions.Companion.REQUEST_CALL
import com.example.jacie.functions.AssistantFunctions.Companion.REQUEST_CODE_SELECT_DOC
import com.example.jacie.functions.AssistantFunctions.Companion.REQUEST_ENABLE_BT
import com.example.jacie.functions.AssistantFunctions.Companion.SHARE_FILE
import com.example.jacie.functions.AssistantFunctions.Companion.SHARE_TEXT_FILE
import com.example.jacie.functions.AssistantFunctions.Companion.callContact
import com.example.jacie.functions.AssistantFunctions.Companion.capturePhoto
import com.example.jacie.functions.AssistantFunctions.Companion.clipBoardCopy
import com.example.jacie.functions.AssistantFunctions.Companion.clipBoardSpeak
import com.example.jacie.functions.AssistantFunctions.Companion.getAllPairedDevices
import com.example.jacie.functions.AssistantFunctions.Companion.getDate
import com.example.jacie.functions.AssistantFunctions.Companion.getTextFromBitmap
import com.example.jacie.functions.AssistantFunctions.Companion.getTime
import com.example.jacie.functions.AssistantFunctions.Companion.joke
import com.example.jacie.functions.AssistantFunctions.Companion.makeAPhoneCall
import com.example.jacie.functions.AssistantFunctions.Companion.motivationalThoughts
import com.example.jacie.functions.AssistantFunctions.Companion.openFacebook
import com.example.jacie.functions.AssistantFunctions.Companion.openGmail
import com.example.jacie.functions.AssistantFunctions.Companion.openGoogle
import com.example.jacie.functions.AssistantFunctions.Companion.openMaps
import com.example.jacie.functions.AssistantFunctions.Companion.openMessages
import com.example.jacie.functions.AssistantFunctions.Companion.openWhatsapp
import com.example.jacie.functions.AssistantFunctions.Companion.openYoutube
import com.example.jacie.functions.AssistantFunctions.Companion.playRingtone
import com.example.jacie.functions.AssistantFunctions.Companion.question
import com.example.jacie.functions.AssistantFunctions.Companion.readMe
import com.example.jacie.functions.AssistantFunctions.Companion.readSMS
import com.example.jacie.functions.AssistantFunctions.Companion.search
import com.example.jacie.functions.AssistantFunctions.Companion.sendSMS
import com.example.jacie.functions.AssistantFunctions.Companion.shareAFile
import com.example.jacie.functions.AssistantFunctions.Companion.shareATextMessage
import com.example.jacie.functions.AssistantFunctions.Companion.speak
import com.example.jacie.functions.AssistantFunctions.Companion.stopRingtone
import com.example.jacie.functions.AssistantFunctions.Companion.turnOffBluetooth
import com.example.jacie.functions.AssistantFunctions.Companion.turnOffFlash
import com.example.jacie.functions.AssistantFunctions.Companion.turnOnBluetooth
import com.example.jacie.functions.AssistantFunctions.Companion.turnOnFlash
import com.example.jacie.functions.GoogleLensActivity
import com.example.jacie.utils.utils
import com.example.jacie.utils.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class AssistantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssistantBinding
    private lateinit var assistantViewModel: AssistantViewModel
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var recognizerIntent: Intent
    private lateinit var keeper:String
    private lateinit var cameraManager:CameraManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var cameraID:String
    private lateinit var ringtone: Ringtone
   private lateinit var imageUri: Uri


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCustomActionBar(supportActionBar, this)
        overridePendingTransition(R.anim.non_movable, R.anim.non_movable)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_assistant)

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.System.canWrite(this)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        )
        {
            ringtone=RingtoneManager.getRingtone(applicationContext,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        }
        else
        {
            val intent=Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data= Uri.parse("package:"+this.packageName)
            startActivity(intent)
        }
        //Initialize our viewModel to listen live data
        val application= requireNotNull(this).application
        val dataSource=AssistantDatabase.getInstance(this).assistantDao
        val viewModelFactory=AssistantViewModelFactory(dataSource,application)
        assistantViewModel=ViewModelProvider(this,viewModelFactory).get(AssistantViewModel::class.java)
        val adapter=AssistantAdapter()
        binding.recyclerView.adapter=adapter
        // To update live data and msg in interface
        assistantViewModel.messages.observe(this,
            {
                it?.let{
                    adapter.data=it
                }
            })
        binding.lifecycleOwner=this
        if (savedInstanceState==null){
            binding.assistantLinearLayout.visibility= View.INVISIBLE
            //Tree to save the conversation between human and assistant
            //chat like app
            val viewTreeObserver:ViewTreeObserver=binding.assistantLinearLayout.viewTreeObserver
            if (viewTreeObserver.isAlive){
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
                    override fun onGlobalLayout() {
                        circularRevealActivivty()
                        binding.assistantLinearLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }


        cameraManager=getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraID=cameraManager.cameraIdList[0]
            //0 Back Camera
            //1 Front Camera
        }
        catch (e:java.lang.Exception){
            e.printStackTrace()
        }


        clipboardManager=getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        textToSpeech= TextToSpeech(this)
        {
            status->
            if (status==TextToSpeech.SUCCESS){
                val result:Int=textToSpeech.setLanguage(Locale.ENGLISH)
                if (result==TextToSpeech.LANG_NOT_SUPPORTED || result==TextToSpeech.LANG_MISSING_DATA){
                    Log.i(logTTS,"Language Not Supported")
                }
                else{
                    Log.i(logTTS, "Language Supported")
                }
            }
            else{
                Log.i(logTTS,"Initialization Of Text To Speech Failed")
            }
        }
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        speechRecognizer.setRecognitionListener(object:RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
                Log.i(logSR,"Started")
            }

            override fun onRmsChanged(p0: Float) {
            }

            override fun onBufferReceived(p0: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                Log.i(logSR,"Ended")
            }

            override fun onError(error: Int) {
                Log.i(logSR, error.toString())
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResults(bundles: Bundle?) {
                val data=bundles!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data!=null){
                    keeper=data[0]
                    Log.d(logKeeper,keeper)
                    when
                    {
                        keeper.contains("thanks") -> speak("Its my job , let me know if there is something else", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("search") -> search(this@AssistantActivity,keeper)
                        keeper.contains("welcome") -> speak("Its my pleasure to help you out", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("go out with me") ||  keeper.contains("club") ||  keeper.contains("coffee") ||  keeper.contains("dance") ||  keeper.contains("love") -> speak("Yes , Ofcourse", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("clear") ||  keeper.contains("delete")-> assistantViewModel.onClear()
                        keeper.contains("date") -> getDate(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("time") -> getTime(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("dial") -> makeAPhoneCall(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("send sms") || keeper.contains("send SMS") -> sendSMS(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("read my last sms") || keeper.contains("read my last SMS") || keeper.contains("read my SMS") -> readSMS(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("open Gmail") || keeper.contains("Gmail") ||keeper.contains("gmail") ||keeper.contains("mail") -> openGmail(this@AssistantActivity)
                        keeper.contains("open Maps") || keeper.contains("open maps")  || keeper.contains("maps")-> openMaps(this@AssistantActivity)
                        keeper.contains("open Google") || keeper.contains("open Google") || keeper.contains("open Chrome") -> openGoogle(this@AssistantActivity)
                        keeper.contains("open Whatsapp") || keeper.contains("open WhatsApp") -> openWhatsapp(this@AssistantActivity)
                        keeper.contains("open facebook") || keeper.contains("open Facebook") || keeper.contains("open Face") || keeper.contains("open Facebook") -> openFacebook(this@AssistantActivity)
                        keeper.contains("open messages")|| keeper.contains("messages") -> openMessages(this@AssistantActivity, applicationContext)
                        keeper.contains("how to use jacie") || keeper.contains("jc") || keeper.contains("how to use") || keeper.contains("can I do") || keeper.contains("what can I do") || keeper.contains("Jacie") || keeper.contains("can")-> speak("Try some Commands : open whatsapp , open facebook , tell me a joke , hi , hello , explore , google lens", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("open youtube") || keeper.contains("open YouTube") -> openYoutube(this@AssistantActivity)
                        keeper.contains("share file") -> shareAFile(this@AssistantActivity, applicationContext)
                        keeper.contains("share a text message") -> shareATextMessage(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("call") -> callContact(this@AssistantActivity, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("turn on bluetooth") || keeper.contains("turn on Bluetooth") -> turnOnBluetooth(this@AssistantActivity, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("turn off bluetooth") || keeper.contains("turn off Bluetooth") -> turnOffBluetooth(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("get bluetooth devices") -> getAllPairedDevices(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("turn on flash") -> turnOnFlash(cameraManager, cameraID, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("turn off flash") -> turnOffFlash(cameraManager, cameraID, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("copy to clipboard") ||   keeper.contains("copy") -> clipBoardCopy(clipboardManager, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("read last clipboard")|| keeper.contains("read my clipboard")|| keeper.contains("read clipboard")-> clipBoardSpeak(clipboardManager, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("capture photo") -> capturePhoto(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("play ringtone") ||  keeper.contains("play something") ||  keeper.contains("play")||  keeper.contains("song")-> playRingtone(ringtone, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("stop ringtone") || keeper.contains("stop playing") || keeper.contains("stop music") || keeper.contains("stop") || keeper.contains("sto") -> stopRingtone(ringtone, textToSpeech, assistantViewModel, keeper)
                        keeper.contains("read me") -> readMe(this@AssistantActivity)
                        keeper.contains("weather") ||
                                keeper.contains("explore")||
                                keeper.contains("Explore")||
                                keeper.contains("Commands")||
                                keeper.contains("commands")-> startActivity(Intent(this@AssistantActivity, ExploreActivity::class.java))
                        keeper.contains("lens")||keeper.contains("Lens")||keeper.contains("len")-> startActivity(Intent(this@AssistantActivity, GoogleLensActivity::class.java))
                        keeper.contains("motivate") || keeper.contains("any thoughts") || keeper.contains("motivational thoughts") || keeper.contains("motivational") -> motivationalThoughts(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("joke") ||  keeper.contains("tell me joke")||  keeper.contains("say joke") -> joke(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("question") -> question(textToSpeech, assistantViewModel, keeper)
                        keeper.contains("haha") || keeper.contains("hehe") -> speak("I know , I am funny", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("are you married") ||   keeper.contains("married") ||   keeper.contains("marry") -> speak("Yes to my work !", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("boat") || keeper.contains("real magic")
                                || keeper.contains("magic") || keeper.contains("useless talent")
                                || keeper.contains("smelling place") || keeper.contains("smelling ") ->
                            speak("You are funny ", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("what is your name") || keeper.contains("your name")
                                || keeper.contains("what do you call your self") ->
                            speak("I am Jacie at  your service", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("hello") || keeper.contains("hi") || keeper.contains("hey") || keeper.contains("hay")
                        -> speak("Hello , how can I help you ?", textToSpeech, assistantViewModel, keeper)
                        keeper.contains("who are friends")|| keeper.contains("friends")|| keeper.contains("friend")|| keeper.contains("dost")-> speak("Bhosdiwale hote hai", textToSpeech, assistantViewModel, keeper)
                        else -> speak("Please try another comment like  what is your name , call someone , read my sms , open lens , explore", textToSpeech, assistantViewModel, keeper)


                    }
                }
            }

            override fun onPartialResults(p0: Bundle?) {
            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }

        })

        binding.assistantMic.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                }

                MotionEvent.ACTION_DOWN -> {
                    textToSpeech.stop()
                    speechRecognizer.startListening(recognizerIntent)
                }
            }
            false

        }
        checkSpeechRecognizerAvailable()

    }
    private fun checkSpeechRecognizerAvailable() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.d(logSR, "yes")
        } else {
            Log.d(logSR, "false")
        }
    }

    private fun circularRevealActivivty() {
        val cx:Int=binding.assistantLinearLayout.right-getDips(44)
        val cy:Int=binding.assistantLinearLayout.bottom-getDips(44)
        val finalRadius:Int=Math.max(
            binding.assistantLinearLayout.width,
            binding.assistantLinearLayout.height
        )
        val circularReveal=ViewAnimationUtils.createCircularReveal(
            binding.assistantLinearLayout,
            cx,
            cy,
            0f,
            finalRadius.toFloat()
        )
        val AnimationTime=100
        circularReveal.duration=AnimationTime.toLong()
        binding.assistantLinearLayout.visibility=View.VISIBLE

    }
    private fun getDips(i: Int): Int {
        val resources:Resources=resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            i.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer.cancel()
        speechRecognizer.destroy()
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            val cx:Int=binding.assistantLinearLayout.right-getDips(44)
            val cy:Int=binding.assistantLinearLayout.bottom-getDips(44)
            val finalRadius:Int=Math.max(
                binding.assistantLinearLayout.width,
                binding.assistantLinearLayout.height
            )
            val circularReveal=ViewAnimationUtils.createCircularReveal(
                binding.assistantLinearLayout,
                cx,
                cy,
                0f,
                finalRadius.toFloat()
            )
            circularReveal.addListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    binding.assistantLinearLayout.visibility=View.GONE
                    finish()
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }

            })
            val AnimationTime=500
            circularReveal.duration=AnimationTime.toLong()
            circularReveal.start()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==REQUEST_CODE_SELECT_DOC && resultCode== RESULT_OK){
            val filePath= data!!.data!!.path
            val file=File(filePath)
            val intentShare=Intent(Intent.ACTION_SEND)
            intentShare.type="application/pdf"
            intentShare.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://$file"))
            startActivity(Intent.createChooser(intentShare,"Share File"))
        }
        if (requestCode== REQUEST_ENABLE_BT){
            if (resultCode== RESULT_OK){
                speak("Bluetooh is On",textToSpeech,assistantViewModel,keeper)
            }
            else{
                speak("Unable to turn On Bluetoooth",textToSpeech,assistantViewModel,keeper)
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageuri = CropImage.getPickImageResultUri(this, data)
            imageUri = imageuri
            startCrop(imageUri)
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result:CropImage.ActivityResult=CropImage.getActivityResult(data)
            if (resultCode== RESULT_OK){
                imageUri=result.uri
                try{
                    val inputStream=contentResolver.openInputStream(imageUri)
                    val bitmap=BitmapFactory.decodeStream(inputStream)
                    getTextFromBitmap(this,bitmap,textToSpeech,assistantViewModel,keeper)
                }
                catch (e:FileNotFoundException){
                    e.printStackTrace()
                }
            }
            Toast.makeText(this, "Image Captured", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCrop(imageUri: Uri?) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
            .start(this)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                makeAPhoneCall(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == READ_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    readSMS(this, applicationContext, textToSpeech, assistantViewModel, keeper)
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SHARE_FILE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                shareAFile(this, applicationContext)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SHARE_TEXT_FILE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                shareATextMessage(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                callContact(this@AssistantActivity, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == CAPTURE_PHOTO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                capturePhoto(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}