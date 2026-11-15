
package com.edith.assistant

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var output: TextView
    private var tts: TextToSpeech? = null

    private val speechRequest = 42
    private val microphoneRequest = 43

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(3, 12, 27),
                Color.rgb(2, 38, 62)
            )
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 40, 32, 40)
            this.background = background
        }

        fun label(
            value: String,
            size: Float,
            color: Int
        ): TextView {
            return TextView(this).apply {
                text = value
                textSize = size
                setTextColor(color)
                gravity = Gravity.CENTER
                typeface = Typeface.create(
                    "sans-serif-light",
                    Typeface.NORMAL
                )
                setPadding(8, 18, 8, 18)
            }
        }

        val cyan = Color.rgb(55, 222, 255)

        root.addView(label("E.D.I.T.H.", 42f, cyan))

        root.addView(
            label(
                "SISTEMA DE ASISTENCIA PERSONAL • V1.0",
                13f,
                Color.LTGRAY
            )
        )

        root.addView(label("◉", 115f, cyan))

        status = label("SISTEMA EN LÍNEA", 17f, cyan)
        root.addView(status)

        output = label(
            "Pulsa el botón para hablar conmigo.",
            18f,
            Color.WHITE
        )
        root.addView(output)

        val button = Button(this).apply {
            text = "HABLAR CON E.D.I.T.H."
            textSize = 15f
            setTextColor(Color.BLACK)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(cyan)

            setOnClickListener {
                startListening()
            }
        }

        root.addView(button)

        root.addView(
            label(
                "PRIMER PROTOTIPO • CONTROL POR VOZ",
                11f,
                Color.GRAY
            )
        )

        setContentView(root)
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "DO")
        }
    }

    private fun startListening() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                microphoneRequest
            )
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "es-DO"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Habla con E.D.I.T.H."
            )
        }

        try {
            status.text = "ESCUCHANDO..."
            startActivityForResult(intent, speechRequest)
        } catch (e: Exception) {
            status.text = "RECONOCIMIENTO NO DISPONIBLE"
            output.text =
                "Activa el reconocimiento de voz de Android."
        }
    }

    @Deprecated("Platform speech result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == speechRequest) {
            status.text = "SISTEMA EN LÍNEA"

            if (resultCode == RESULT_OK) {
                val heard = data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    ?.firstOrNull()
                    .orEmpty()

                val response = reply(heard)

                output.text =
                    "Tú: $heard\n\nE.D.I.T.H.: $response"

                tts?.speak(
                    response,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "edith-response"
                )
            }
        }
    }

    private fun reply(heard: String): String {
        val question = heard.lowercase(Locale.ROOT)

        return when {
            "hola" in question ->
                "Hola. Soy Edith, tu asistente personal."

            "nombre" in question ->
                "Mi nombre es Edith."

            "hora" in question ->
                "Son las " +
                java.text.SimpleDateFormat(
                    "h:mm a",
                    Locale("es", "DO")
                ).format(java.util.Date())

            "fecha" in question ->
                "Hoy es " +
                java.text.SimpleDateFormat(
                    "EEEE d 'de' MMMM",
                    Locale("es", "DO")
                ).format(java.util.Date())

            "gracias" in question ->
                "Siempre a tu servicio."

            else ->
                "Te escuché. Esta es mi primera versión. " +
                "Pronto tendré más capacidades."
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == microphoneRequest &&
            grantResults.firstOrNull() ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
