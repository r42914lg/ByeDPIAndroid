package io.github.dovecoteescapee.byedpi.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxyUIPreferences
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxyUIPreferences.DesyncMethod.*
import io.github.dovecoteescapee.byedpi.utility.*

class ByeDpiUISettingsFragment : PreferenceFragmentCompat() {

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            updatePreferences()
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.byedpi_ui_settings, rootKey)

        setEditTextPreferenceListener("byedpi_proxy_ip") { checkIp(it) }
        setEditTestPreferenceListenerPort("byedpi_proxy_port")
        setEditTestPreferenceListenerInt(
            "byedpi_max_connections",
            1,
            Short.MAX_VALUE.toInt()
        )
        setEditTestPreferenceListenerInt(
            "byedpi_buffer_size",
            1,
            Int.MAX_VALUE / 4
        )
        setEditTestPreferenceListenerInt("byedpi_default_ttl", 0, 255)
        setEditTestPreferenceListenerInt(
            "byedpi_split_position",
            Int.MIN_VALUE,
            Int.MAX_VALUE
        )
        setEditTestPreferenceListenerInt("byedpi_fake_ttl", 1, 255)
        setEditTestPreferenceListenerInt(
            "byedpi_tlsrec_position",
            2 * Short.MIN_VALUE,
            2 * Short.MAX_VALUE,
        )

        findPreferenceNotNull<EditTextPreference>("byedpi_oob_data")
            .setOnBindEditTextListener {
                it.filters = arrayOf(android.text.InputFilter.LengthFilter(1))
            }

        updatePreferences()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    private fun updatePreferences() {
        val desyncMethod =
            findPreferenceNotNull<ListPreference>("byedpi_desync_method")
                .value.let { ByeDpiProxyUIPreferences.DesyncMethod.fromName(it) }

        val desyncHttp = findPreferenceNotNull<CheckBoxPreference>("byedpi_desync_http")
        val desyncHttps = findPreferenceNotNull<CheckBoxPreference>("byedpi_desync_https")
        val desyncUdp = findPreferenceNotNull<CheckBoxPreference>("byedpi_desync_udp")
        val splitPosition = findPreferenceNotNull<EditTextPreference>("byedpi_split_position")
        val splitAtHost = findPreferenceNotNull<CheckBoxPreference>("byedpi_split_at_host")
        val ttlFake = findPreferenceNotNull<EditTextPreference>("byedpi_fake_ttl")
        val fakeSni = findPreferenceNotNull<EditTextPreference>("byedpi_fake_sni")
        val oobData = findPreferenceNotNull<EditTextPreference>("byedpi_oob_data")
        val hostMixedCase = findPreferenceNotNull<CheckBoxPreference>("byedpi_host_mixed_case")
        val domainMixedCase = findPreferenceNotNull<CheckBoxPreference>("byedpi_domain_mixed_case")
        val hostRemoveSpaces =
            findPreferenceNotNull<CheckBoxPreference>("byedpi_host_remove_spaces")
        val splitTlsRec = findPreferenceNotNull<CheckBoxPreference>("byedpi_tlsrec_enabled")
        val splitTlsRecPosition =
            findPreferenceNotNull<EditTextPreference>("byedpi_tlsrec_position")
        val splitTlsRecAtSni = findPreferenceNotNull<CheckBoxPreference>("byedpi_tlsrec_at_sni")

        when (desyncMethod) {
            None -> {
                desyncHttp.isVisible = false
                desyncHttps.isVisible = false
                desyncUdp.isVisible = false
                splitPosition.isVisible = false
                splitAtHost.isVisible = false
                ttlFake.isVisible = false
                fakeSni.isVisible = false
                oobData.isVisible = false
                hostMixedCase.isVisible = false
                domainMixedCase.isVisible = false
                hostRemoveSpaces.isVisible = false
            }

            else -> {
                desyncHttp.isVisible = true
                desyncHttps.isVisible = true
                desyncUdp.isVisible = true
                splitPosition.isVisible = true
                splitAtHost.isVisible = true

                val desyncAllProtocols =
                    !desyncHttp.isChecked && !desyncHttps.isChecked && !desyncUdp.isChecked

                if (desyncAllProtocols || desyncHttp.isChecked) {
                    hostMixedCase.isVisible = true
                    domainMixedCase.isVisible = true
                    hostRemoveSpaces.isVisible = true
                } else {
                    hostMixedCase.isVisible = false
                    domainMixedCase.isVisible = false
                    hostRemoveSpaces.isVisible = false
                }

                when (desyncMethod) {
                    Fake -> {
                        ttlFake.isVisible = true
                        fakeSni.isVisible = true
                        oobData.isVisible = false
                    }

                    OOB -> {
                        ttlFake.isVisible = false
                        fakeSni.isVisible = false
                        oobData.isVisible = true
                    }

                    else -> {
                        ttlFake.isVisible = false
                        fakeSni.isVisible = false
                        oobData.isVisible = false
                    }
                }
            }
        }

        splitTlsRecPosition.isVisible = splitTlsRec.isChecked
        splitTlsRecAtSni.isVisible = splitTlsRec.isChecked
    }
}
