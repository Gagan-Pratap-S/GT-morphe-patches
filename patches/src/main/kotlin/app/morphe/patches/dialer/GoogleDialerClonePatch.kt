package app.morphe.patches.dialer

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.dialer.DialerConstants.COMPATIBILITY_GOOGLE_DIALER
import org.w3c.dom.Element
import org.w3c.dom.NodeList

/**
 * Clones package to com.google.android.dialer.amoled for non-rooted users.
 * Replicates v7 UltraSmooth manifest cloning 1:1.
 */
@Suppress("unused")
val googleDialerClonePatch = resourcePatch(
    name = "Clone Dialer Package",
    description = "Changes package to com.google.android.dialer.amoled for non-rooted install. " +
                  "Leave OFF on rooted devices to mount over stock dialer without crashing.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GOOGLE_DIALER)

    execute {
        val packageName = "com.google.android.dialer"
        val newPackageName = "com.google.android.dialer.amoled"

        document("AndroidManifest.xml").use { doc ->
            // 1. Root package attribute
            doc.documentElement.setAttribute("package", newPackageName)

            // 2. Custom permission declarations
            doc.getElementsByTagName("permission").forEach { perm ->
                val name = perm.getAttribute("android:name")
                if (name.startsWith("$packageName.")) {
                    perm.setAttribute("android:name", name.replaceFirst(packageName, newPackageName))
                }
            }

            // 3. Permission usages
            doc.getElementsByTagName("uses-permission").forEach { perm ->
                val name = perm.getAttribute("android:name")
                if (name.startsWith("$packageName.")) {
                    perm.setAttribute("android:name", name.replaceFirst(packageName, newPackageName))
                }
            }

            // 4. All provider authorities (v7 1:1: isolation of all authorities)
            doc.getElementsByTagName("provider").forEach { provider ->
                val authorities = provider.getAttribute("android:authorities")
                if (authorities.isNotEmpty()) {
                    val newAuthorities = authorities.split(';').map { auth ->
                        if (auth.startsWith("$packageName.")) {
                            auth.replaceFirst(packageName, newPackageName)
                        } else if (!auth.endsWith(".amoled")) {
                            "$auth.amoled"
                        } else {
                            auth
                        }
                    }
                    provider.setAttribute("android:authorities", newAuthorities.joinToString(";"))
                }
            }

            // 5. Proprietary Google Dialer Support Library (make optional to prevent crash on non-Pixel devices)
            doc.getElementsByTagName("uses-library").forEach { lib ->
                if (lib.getAttribute("android:name") == "com.google.android.dialer.support") {
                    lib.setAttribute("android:required", "false")
                }
            }

            // 6. Preferred SIM fallback metadata
            doc.getElementsByTagName("meta-data").forEach { meta ->
                if (meta.getAttribute("android:value") == "com.google.android.dialer.preferredsimfallback") {
                    meta.setAttribute("android:value", "com.google.android.dialer.preferredsimfallback.amoled")
                }
            }
        }

        println("AndroidManifest.xml successfully cloned to $newPackageName (v7 1:1)")
    }
}

private fun NodeList.forEach(action: (Element) -> Unit) {
    for (i in 0 until length) {
        val node = item(i)
        if (node is Element) action(node)
    }
}
