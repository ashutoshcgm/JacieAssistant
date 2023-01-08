package com.example.jacie.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.ActionBar;

import com.example.jacie.R;

public class utils {
//    Commands that can be used by user
    public static String[] commands={
            "check my mail",
        "What can I do?",
        "Can we meet?",
        "How to use assistant?",
        "Hi!!",
        "Hello",
        "Hey!",
        "search __apple__",
        "Thanks !!!",
        "WELCOME",
        "clear",
        "delete",
        "What's the time now?",
        "What's today date?",
        "Send SMS",
        "Tell me a joke",
        "Ask me a fun question",
        "Open Whatsapp",
        "Open Facebook",
        "Open Youtube",
        "Open Gmail",
        "Open Google",
        "Open GoogleMaps",
        "Turn ON Bluetooth",
        "Turn OFF Bluetooth",
        "Get Bluetooth Devices",
        "Turn ON Torch",
        "Turn OFF Torch",
        "Capture Photo",
        "Open Camera",
        "Call __Mom__",
        "Dial Phone Number",
        "Any Thoughts",
        "Play Music",
        "Stop Music",
        "HaHA!!",
        "Read me",
        "Read my SMS",
        "Share a file",
        "Send a SMS to contact",
        "Copy to Clipboard",
        "Read my last Clipboard",
        "Open GoogleLens",
        "Explore",
        "What's your Name?"
    };
    // used to log the messages from text to speech progress
    public static final String logTTS="Text To Speech";

    //See progress of speech recognition
    public static final String logSR="Speech Recognition";

    // To see the strings subtracted from speech recognition calling it as keeper
    public static final String logKeeper="Keeper";

    public static final String tablename="assistant_table_name";

    public static void setCustomActionBar(ActionBar supportActionBar, Context context){
        supportActionBar.setDisplayShowHomeEnabled(true);
        supportActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater=LayoutInflater.from(context);
        @SuppressLint("InflateParams") View mCustomView=mInflater.inflate(R.layout.custom_toolbar,null);
        supportActionBar.setCustomView(mCustomView);
        supportActionBar.setDisplayShowCustomEnabled(true);
    }
//    public static void setCustomActionBar(Fragment fragment, Context context) {
//        setCustomActionBar( ((AppCompatActivity) fragment.requireActivity()).getSupportActionBar(), context);
//    }

}
