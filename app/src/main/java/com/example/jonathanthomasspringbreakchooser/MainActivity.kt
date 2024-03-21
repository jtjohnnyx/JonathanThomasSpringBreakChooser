package com.example.jonathanthomasspringbreakchooser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.widget.TextView
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner


class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val button = findViewById<Button>(R.id.button)
        val radioGroup = findViewById<RadioGroup>(R.id.options_radio_group)
        val text = findViewById<EditText>(R.id.editText)

        var lock = false

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            // Speech recognition service NOT available
            Log.d("ERRS", "what")
            return
        } else
            Log.d("ERRS", "okay")

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Set recognition listener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                text.text.clear()
                text.hint = "Speak in language"
                Log.d("ERRS", "onready")
            }

            override fun onBeginningOfSpeech() {
                Log.d("ERRS", "onbeg")
            }

            override fun onRmsChanged(p0: Float) {
                Log.d("ERRS", "onrms")
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.d("ERRS", "onbuf")
            }

            override fun onEndOfSpeech() {
                Log.d("ERRS", "onend")
            }

            override fun onError(e: Int) {
                if (e == 7) {
                    text.text.clear()
                    text.hint = "Choose a language"
                    lock = true
                    radioGroup.clearCheck()
                    lock = false
                    speechRecognizer.cancel()
                }
                Log.d("ERRS", "error $e")
            }

            override fun onResults(results: Bundle) {
                val data: ArrayList<String>? = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null) {
                    text.setText(data.joinToString(separator = " "))
                }
                speechRecognizer.cancel()
                lock = true
                radioGroup.clearCheck()
                lock = false
                Log.d("ERRS", "$data")
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.d("ERRS", "onpart")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.d("ERRS", "event $p0")
            }

        })

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!lock) {
                val selectedRadioButton = findViewById<RadioButton>(checkedId)
                val lang = selectedRadioButton.tag.toString()
                startListening(lang)
                //Log.d("ERRS", "changed")
            }
        }


//        button.setOnClickListener() {
//            lock = true
//            radioGroup.clearCheck()
//            lock = false
//        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        speechRecognizer.destroy()
//    }

    private fun startListening(lang : String) {
        Log.d("ERRS", "$lang")
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, lang
            )
        }

        speechRecognizer.startListening(recognizerIntent)
        Log.d("ERRS", "gogo")
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
    }
}
