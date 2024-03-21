package com.example.jonathanthomasspringbreakchooser

import android.content.Intent
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*
import android.speech.RecognitionListener

import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlin.math.sqrt

import kotlin.random.Random

// spring break locations as coordinates
private val locations = listOf(
    Location(R.string.location1, 40.748817, -73.985428, "en-US"),
    Location(R.string.location2, 34.008360, -118.498759, "en-US"),
    Location(R.string.location3, 41.378337, 2.191797, "es-ES"),
    Location(R.string.location4, 36.689354, -4.442282, "es-ES"),
    Location(R.string.location5, 43.766209, 11.258710, "it-IT"),
    Location(R.string.location6, 40.829973, 14.246307, "it-IT"),
 )

class MainActivity : AppCompatActivity() {

    // for listening to user's voice
    private lateinit var speechRecognizer: SpeechRecognizer

    // for holding the current language
    private var lang = ""
    private var temp = ""

    // for detecting vigorous shaking
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    // for greeting the user
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialise views
        //val button = findViewById<Button>(R.id.button) //for testing purposes
        val radioGroup = findViewById<RadioGroup>(R.id.options_radio_group)
        val text = findViewById<EditText>(R.id.editText)

        // even though you disable the list options, it may still be called; ultimately locks it
        var lock = false

        // for testing speech
//        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
//            // Speech recognition service NOT available
//            Log.d("ERRS", "what")
//            return
//        } else
//            Log.d("ERRS", "okay")

        // initialise text to speech
        textToSpeech = TextToSpeech(this) {status ->
            if (status == TextToSpeech.SUCCESS){
                Log.d("ERRS", "Initialization Success")
            }else{
                Log.d("ERRS", "Initialization Failed")
            }
        }

        // initialises the accelerometer tracking
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Set recognition listener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                text.text.clear()
                text.hint = "Speak"
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
                // if network error or no speech detected; not exhaustive :(
                if (e== 2 || e == 7) {
                    Toast.makeText(applicationContext, "Timed out. No speech detected", Toast.LENGTH_SHORT).show()
                    text.text.clear()
                    text.hint = "Choose a language"
                    lang = ""
                    temp = ""
                    lock = true
                    radioGroup.clearCheck()
                    lock = false
                    speechRecognizer.cancel()
                    enableRadioGroup(radioGroup)
                }
                Log.d("ERRS", "error $e")
            }

            override fun onResults(results: Bundle) {
                val data: ArrayList<String>? = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null) {
                    val words = data.joinToString(separator = " ")
                    text.setText(words)
                }
                lang = temp
                speechRecognizer.cancel()
                enableRadioGroup(radioGroup)

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
                lang = ""
                temp = selectedRadioButton.tag.toString()
                disableRadioGroup(radioGroup)
                startListening(temp)
                Log.d("ERRS", "changed")
            }
        }


        // button for testing
//        button.setOnClickListener() {
//            lock = true
//            radioGroup.clearCheck()
//            lock = false

//            val packageManager = this.packageManager
//            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
//            val googleMapsPackageName = intent?.component?.packageName
//            if (googleMapsPackageName != null) {
//                Log.d("ERRS", googleMapsPackageName)
//            }

            // Define the location (latitude and longitude)
//            val latitude = locations[0].lat
//            val longitude = locations[0].lon
//            // Create a Uri for the location
//            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?z=15")
//            // Create an Intent with the Uri
//            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//
//            mapIntent.setPackage("com.google.android.apps.maps")
//
//            startActivity(mapIntent)
//            openMap(lang)
//            speak(lang)
//        }


    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        super.onDestroy()
    }

    private fun openMap(lang: String) {
        var i = -1
        when (lang) {
            "en-US" -> i = Random.nextInt(0, 2)
            "es-ES" -> i = Random.nextInt(2, 4)
            "it-IT" -> i = Random.nextInt(4, 6)
            "" -> return
        }
        val gmmIntentUri = Uri.parse("google.streetview:cbll=${locations[i].lat},${locations[i].lon}&cbp=0,bearing,0,zoom,tilt")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        startActivity(mapIntent)
    }

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

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.7f + delta

            if (acceleration > 7) {
                if (lang != "")
                    Toast.makeText(applicationContext, "Taking you to your spring break!", Toast.LENGTH_SHORT).show()
                speak(lang)
                openMap(lang)

            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun speak(lang: String) {
        when (lang) {
            "en-US" -> {
                textToSpeech.language = Locale("en", "US")
                textToSpeech.speak("Hello!", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            "es-ES" -> {
                textToSpeech.language = Locale("es", "ES")
                textToSpeech.speak("Hola!", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            "it-IT" -> {
                textToSpeech.language = Locale("it", "IT")
                textToSpeech.speak("Ciao!", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            "" -> return
        }
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    private fun disableRadioGroup(group: RadioGroup) {
        for (i in 0 until group.childCount) {
            val radioButton = group.getChildAt(i) as? RadioButton
            radioButton?.isEnabled = false
        }
    }

    private fun enableRadioGroup(group: RadioGroup) {
        for (i in 0 until group.childCount) {
            val radioButton = group.getChildAt(i) as? RadioButton
            radioButton?.isEnabled = true
        }
    }

}
