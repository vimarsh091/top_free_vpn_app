package kittoku.osc.preference.accessor

import android.content.SharedPreferences
import kittoku.osc.preference.OscPreference


internal fun getStringPrefValue(key: OscPreference, prefs: SharedPreferences): String {
    val defaultValue = when (key) {
        OscPreference.HOME_HOSTNAME -> "219.100.37.96"
        OscPreference.HOME_USERNAME -> "vpn"
        OscPreference.HOME_PASSWORD -> "vpn"
        OscPreference.HOME_SELECTED_COUNTRY -> "Default"
        OscPreference.HOME_STATUS,
        OscPreference.SSL_CERT_DIR,
        OscPreference.LOG_DIR -> ""
        OscPreference.SSL_VERSION -> "DEFAULT"
        else -> throw NotImplementedError()
    }

    return prefs.getString(key.name, defaultValue)!!
}

internal fun setStringPrefValue(value: String, key: OscPreference, prefs: SharedPreferences) {
    prefs.edit().also {
        it.putString(key.name, value)
        it.apply()
    }
}
