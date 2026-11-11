package rs.parkap

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.parseColor("#FAFAF7")
    private val ink = Color.parseColor("#141413")
    private val muted = Color.parseColor("#6B6A63")

    // Zone colors: blue, red, yellow, white for day ticket
    private val zoneStyle = mapOf(
        "z1" to Pair(Color.parseColor("#2563EB"), Color.WHITE),
        "z2" to Pair(Color.parseColor("#DC2626"), Color.WHITE),
        "z3" to Pair(Color.parseColor("#EAB308"), ink),
        "day" to Pair(Color.WHITE, ink)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(24), dp(56), dp(24), dp(32))
        }

        // Header: title + settings button
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "Parkap"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTextColor(ink)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(Button(this).apply {
            text = "⚙"
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        })
        root.addView(header)

        // Vehicle card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        card.addView(TextView(this).apply {
            text = "Vozilo"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(muted)
        })
        card.addView(TextView(this).apply {
            text = Config.plate(this@MainActivity)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(ink)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        root.addView(card, marginTop(dp(16)))

        root.addView(TextView(this).apply {
            text = "Izaberi zonu za parking"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(muted)
        }, marginTop(dp(16)))

        // Zone buttons
        for ((key, label) in Config.zones) {
            val (bgColor, textColor) = zoneStyle[key] ?: Pair(ink, bg)
            root.addView(Button(this).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTextColor(textColor)
                setBackgroundColor(bgColor)
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                setPadding(dp(20), 0, dp(20), 0)
                setOnClickListener { confirm(key, label) }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)).apply {
                topMargin = dp(12)
            })
        }

        root.addView(TextView(this).apply {
            text = "Klik otvara potvrdu pre slanja"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(muted)
            gravity = Gravity.CENTER_HORIZONTAL
        }, marginTop(dp(24)))

        setContentView(root)
    }

    private fun confirm(zoneKey: String, label: String) {
        val number = Config.number(this, zoneKey)
        val plate = Config.plate(this)
        if (number.isBlank()) {
            Toast.makeText(this, "Unesi broj u podešavanjima", Toast.LENGTH_LONG).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Poslati SMS za $label?")
            .setMessage("Vozilo: $plate\nNa broj: $number\nTekst: $plate")
            .setNegativeButton("Otkaži", null)
            .setPositiveButton("Pošalji") { _, _ -> sendSms(number, plate) }
            .show()
    }

    private fun sendSms(number: String, text: String) {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Dozvoli slanje SMS-a", Toast.LENGTH_LONG).show()
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 1)
            return
        }
        try {
            val sms = if (Build.VERSION.SDK_INT >= 31) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            sms.sendTextMessage(number, null, text, null, null)
            Toast.makeText(this, "SMS poslat", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    private fun marginTop(px: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = px }
}
