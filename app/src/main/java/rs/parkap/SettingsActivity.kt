package rs.parkap

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private val ink = Color.parseColor("#141413")
    private val muted = Color.parseColor("#6B6A63")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Config.prefs(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FAFAF7"))
            setPadding(dp(24), dp(56), dp(24), dp(32))
        }

        root.addView(TextView(this).apply {
            text = "Podešavanja"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ink)
        })

        val plateField = addField(root, "Registarski broj vozila", Config.plate(this))

        val numberFields = Config.zones.associate { (key, label) ->
            key to addField(root, "$label – broj za SMS", Config.number(this, key))
        }

        root.addView(TextView(this).apply {
            text = "Tekst SMS-a: registarski broj bez razmaka i crtica"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(muted)
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(16) })

        root.addView(Button(this).apply {
            text = "Sačuvaj"
            setOnClickListener {
                val editor = prefs.edit()
                editor.putString("plate", plateField.text.toString().trim().uppercase())
                for ((key, field) in numberFields) {
                    editor.putString("num_$key", field.text.toString().trim())
                }
                editor.apply()
                Toast.makeText(this@SettingsActivity, "Sačuvano", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
        ).apply { topMargin = dp(24) })

        setContentView(root)
    }

    private fun addField(parent: LinearLayout, label: String, value: String): EditText {
        parent.addView(TextView(this).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(muted)
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(16) })

        val field = EditText(this).apply {
            setText(value)
            inputType = InputType.TYPE_CLASS_TEXT
            setTextColor(ink)
            setBackgroundColor(Color.WHITE)
        }
        parent.addView(field, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return field
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
