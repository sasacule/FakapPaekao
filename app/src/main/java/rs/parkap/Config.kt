package rs.parkap

import android.content.Context

object Config {
    const val DEFAULT_PLATE = "NS472ER"
    const val DEFAULT_NUMBER = "+381638433232"
    private const val PREFS = "parkap"

    // Zone key, label shown in the UI
    val zones = listOf(
        "z1" to "Zona 1",
        "z2" to "Zona 2",
        "z3" to "Zona 3",
        "day" to "Dnevna karta"
    )

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun plate(context: Context): String =
        prefs(context).getString("plate", DEFAULT_PLATE) ?: DEFAULT_PLATE

    fun number(context: Context, zoneKey: String): String =
        prefs(context).getString("num_$zoneKey", DEFAULT_NUMBER) ?: DEFAULT_NUMBER
}
