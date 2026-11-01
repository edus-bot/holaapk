package com.example.holaapk

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var estado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val titulo = TextView(this)
        titulo.text = "Hola mundo"
        titulo.textSize = 32f
        titulo.gravity = Gravity.CENTER

        estado = TextView(this)
        estado.text = "Versión " + BuildConfig.VERSION_CODE
        estado.textSize = 16f
        estado.gravity = Gravity.CENTER

        val caja = LinearLayout(this)
        caja.orientation = LinearLayout.VERTICAL
        caja.gravity = Gravity.CENTER
        caja.addView(titulo)
        caja.addView(estado)
        setContentView(caja)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        buscarActualizacion()
    }

    private fun mostrar(texto: String) {
        runOnUiThread { estado.text = texto }
    }

    private fun buscarActualizacion() {
        Thread {
            try {
                val json = JSONObject(
                    descargarTexto("https://api.github.com/repos/edus-bot/holaapk/releases/latest")
                )
                val nueva = json.getString("tag_name").removePrefix("v").toInt()
                if (nueva > BuildConfig.VERSION_CODE) {
                    mostrar("Bajando versión $nueva...")
                    val direccion = json.getJSONArray("assets")
                        .getJSONObject(0)
                        .getString("browser_download_url")
                    val archivo = File(getExternalFilesDir(null), "actualizacion.apk")
                    descargarArchivo(direccion, archivo)
                    mostrar("Versión $nueva lista para instalar")
                    avisar(archivo)
                } else {
                    mostrar("Versión " + BuildConfig.VERSION_CODE + " (al día)")
                }
            } catch (e: Exception) {
                mostrar("Versión " + BuildConfig.VERSION_CODE + " (no pude buscar: " + e.message + ")")
            }
        }.start()
    }

    private fun descargarTexto(direccion: String): String {
        val conexion = URL(direccion).openConnection() as HttpURLConnection
        conexion.setRequestProperty("Accept", "application/vnd.github+json")
        conexion.connectTimeout = 15000
        conexion.readTimeout = 15000
        return conexion.inputStream.bufferedReader().use { it.readText() }
    }

    private fun descargarArchivo(direccion: String, destino: File) {
        val conexion = URL(direccion).openConnection() as HttpURLConnection
        conexion.connectTimeout = 15000
        conexion.readTimeout = 60000
        conexion.inputStream.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
    }

    private fun avisar(archivo: File) {
        val uri = FileProvider.getUriForFile(this, packageName + ".fileprovider", archivo)
        val instalar = Intent(Intent.ACTION_VIEW)
        instalar.setDataAndType(uri, "application/vnd.android.package-archive")
        instalar.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendiente = PendingIntent.getActivity(
            this, 0, instalar,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val gestor = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canal = "actualizaciones"
        val constructor: Notification.Builder
        if (Build.VERSION.SDK_INT >= 26) {
            gestor.createNotificationChannel(
                NotificationChannel(canal, "Actualizaciones", NotificationManager.IMPORTANCE_HIGH)
            )
            constructor = Notification.Builder(this, canal)
        } else {
            constructor = Notification.Builder(this)
        }

        val notificacion = constructor
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Hay una versión nueva")
            .setContentText("Tocá para instalar")
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()
        gestor.notify(1, notificacion)
    }
}
