package app.morphe.patches.dialer

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.dialer.DialerConstants.COMPATIBILITY_GOOGLE_DIALER
import org.w3c.dom.Element
import org.w3c.dom.NodeList

/**
 * AMOLED Dark Theme & Silhouette Avatar Fix for Google Phone (Google Dialer).
 * Replicates v7 UltraSmooth AMOLED palette and contrast fixes 1:1.
 */
@Suppress("unused")
val googleDialerThemePatch = resourcePatch(
    name = "AMOLED Dark Theme",
    description = "Converts Google Phone dark theme from grey into pure pitch-black (#000000), " +
                  "enhances dialpad readability with pure white text, and fixes unsaved contact avatar outlines.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GOOGLE_DIALER)

    execute {
        println("Applying Google Dialer AMOLED modifications (v7 1:1)...")

        val nightColorReplacements = mapOf(
            "settingslib_materialColorSurfaceContainer" to "#000000",
            "settingslib_materialColorSurfaceContainerHigh" to "#0a0a0a",
            "settingslib_materialColorSurfaceContainerHighest" to "#141414",
            "settingslib_materialColorSurfaceVariant" to "#1f1f1f",
            "bam_center_fragment_background" to "#000000",
            "survey_surface_color" to "#000000",
            "survey_surface_color_elevation_2" to "#050505",
            "survey_surface_color_elevation_3" to "#0a0a0a",
            "survey_alert_dialog_bg_color" to "#000000",
            "google_marterial_dialog_background_color" to "#000000",
            "settingslib_dialog_background" to "#000000",
            "dialpad_icon_text_enabled" to "#ffffffff",
            "dialpad_icon_text_disabled" to "#80ffffff",
            "empty_content_view_message" to "#ffffffff",
            "settingslib_materialColorOnSurface" to "#ffffffff",
            "settingslib_materialColorOnSurfaceVariant" to "#ffffffff"
        )

        val outlineReplacements = mapOf(
            "settingslib_materialColorOutline" to "@android:color/system_outline_dark",
            "gm3_sys_color_dynamic_dark_outline" to "@android:color/system_outline_dark",
            "m3_sys_color_dynamic_dark_outline" to "@android:color/system_outline_dark",
            "sud_system_outline_dark" to "@android:color/system_outline_dark",
            "mc3_outline" to "@android:color/system_outline_dark"
        )

        // 1. Decode & patch night colors
        decode("res/values-night*/colors.xml").forEach { path ->
            document(path).use { doc ->
                doc.getElementsByTagName("color").forEach { elem ->
                    val name = elem.getAttribute("name")
                    val replacement = nightColorReplacements[name]
                    if (replacement != null) {
                        elem.textContent = replacement
                    }
                }
            }
        }

        // 2. Decode & patch v34 outline colors
        decode("res/values*34/colors.xml").forEach { path ->
            document(path).use { doc ->
                doc.getElementsByTagName("color").forEach { elem ->
                    val name = elem.getAttribute("name")
                    val replacement = outlineReplacements[name]
                    if (replacement != null) {
                        elem.textContent = replacement
                    }
                }
            }
        }

        // 3. Decode & patch v34 outline styles
        decode("res/values*34/styles.xml").forEach { path ->
            document(path).use { doc ->
                doc.getElementsByTagName("item").forEach { elem ->
                    val name = elem.getAttribute("name")
                    val replacement = outlineReplacements[name]
                    if (replacement != null) {
                        elem.textContent = replacement
                    }
                }
            }
        }

        println("Google Dialer AMOLED theme successfully applied (v7 1:1)!")
    }
}

private fun NodeList.forEach(action: (Element) -> Unit) {
    for (i in 0 until length) {
        val node = item(i)
        if (node is Element) action(node)
    }
}
