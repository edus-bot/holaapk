package com.example.holaapk

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val texto = TextView(this)
        texto.text = "Hola mundo"
        texto.textSize = 32f
        texto.gravity = Gravity.CENTER
        setContentView(texto)
    }
}
