package com.ysy.switcherfiveg

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.AppUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

internal object ShizukuUtils {

    private const val SHIZUKU_PKG_NAME = "moe.shizuku.privileged.api"
    private const val SHIZUKU_REQ_CODE = 950803

    private var requestPermissionResultListener: Shizuku.OnRequestPermissionResultListener? = null

    fun onCreate(activity: Activity?): Boolean {
        if (!AppUtils.isAppInstalled(SHIZUKU_PKG_NAME)) {
            activity.runWithUIContext { ctx ->
                AlertDialog.Builder(ctx)
                    .setMessage(R.string.dialog_pls_check_shizuku_msg_1)
                    .setPositiveButton(R.string.dialog_pls_check_shizuku_install) { dialog, _ ->
                        installOrUpgrade()
                        dialog.dismiss()
                        activity?.finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            return false
        }

        requestPermissionResultListener =
            Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
                val granted = grantResult == PackageManager.PERMISSION_GRANTED
                if (granted && requestCode == SHIZUKU_REQ_CODE) {
                    // Do stuff based on the result and the request code
                    grantRequiredPermission()
                }
            }.also {
                Shizuku.addRequestPermissionResultListener(it)
            }

        return if (checkPermission(activity, SHIZUKU_REQ_CODE)) {
            grantRequiredPermission()
            true
        } else {
            false
        }
    }

    fun onDestroy() {
        requestPermissionResultListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
        requestPermissionResultListener = null
    }

    private fun checkPermission(activity: Activity?, code: Int): Boolean = runCatching {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            activity.runWithUIContext { ctx ->
                AlertDialog.Builder(ctx)
                    .setMessage(R.string.dialog_pls_check_shizuku_msg_2)
                    .setPositiveButton(R.string.dialog_pls_check_shizuku_upgrade) { dialog, _ ->
                        installOrUpgrade()
                        dialog.dismiss()
                        activity?.finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            return false
        }
        return if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            activity.runWithUIContext { ctx ->
                AlertDialog.Builder(ctx)
                    .setMessage(R.string.dialog_pls_check_shizuku_msg_3)
                    .setPositiveButton(R.string.dialog_pls_check_shizuku_allow) { dialog, _ ->
                        AppUtils.launchApp(SHIZUKU_PKG_NAME)
                        dialog.dismiss()
                        activity?.finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            false
        } else {
            // Request the permission
            Shizuku.requestPermission(code)
            false
        }
    }.onFailure {
        activity.runWithUIContext { ctx ->
            AlertDialog.Builder(ctx)
                .setMessage(R.string.dialog_pls_check_shizuku_msg_3)
                .setPositiveButton(R.string.dialog_pls_check_shizuku_allow) { dialog, _ ->
                    AppUtils.launchApp(SHIZUKU_PKG_NAME)
                    dialog.dismiss()
                    activity?.finish()
                }
                .setCancelable(false)
                .show()
        }
    }.getOrDefault(false)

    private fun asInterface(className: String, serviceName: String): Any? =
        Class.forName("$className\$Stub")
            .getMethod("asInterface", IBinder::class.java)
            .invoke(null, ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName)))

    @SuppressLint("PrivateApi")
    private fun grantRequiredPermission() {
        runCatching {
            // android.content.pm.IPackageManager
            val clzName = "YW5kcm9pZC5jb250ZW50LnBtLklQYWNrYWdlTWFuYWdlcg".convertRuntimeName()
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HiddenApiBypass.invoke(
                        Class.forName(clzName),
                        asInterface(
                            clzName,
                            "cGFja2FnZQ".convertRuntimeName()
                        ),
                        "Z3JhbnRSdW50aW1lUGVybWlzc2lvbg".convertRuntimeName(),
                        FSApp.getContext().packageName,
                        // android.permission.WRITE_SECURE_SETTINGS
                        "YW5kcm9pZC5wZXJtaXNzaW9uLldSSVRFX1NFQ1VSRV9TRVRUSU5HUw".convertRuntimeName(),
                        0
                    )
                } else {
                    throw RuntimeException()
                }
            }.getOrElse {
                Class.forName(clzName).getMethod(
                    // grantRuntimePermission
                    "Z3JhbnRSdW50aW1lUGVybWlzc2lvbg".convertRuntimeName(),
                    String::class.java /* package name */,
                    String::class.java /* permission name */,
                    Int::class.java /* user ID */
                ).invoke(
                    // package
                    asInterface(
                        clzName,
                        "cGFja2FnZQ".convertRuntimeName()
                    ),
                    FSApp.getContext().packageName,
                    // android.permission.WRITE_SECURE_SETTINGS
                    "YW5kcm9pZC5wZXJtaXNzaW9uLldSSVRFX1NFQ1VSRV9TRVRUSU5HUw".convertRuntimeName(),
                    0
                )
            }
        }
    }

    private fun installOrUpgrade() {
        FSApp.getContext().tryStartActivity(
            "aHR0cHM6Ly9wbGF5Lmdvb2dsZS5jb20vc3RvcmUvYXBwcy9kZXRhaWxzP2lkPW1vZS5zaGl6dWt1LnByaXZpbGVnZWQuYXBp".convertRuntimeName()
        )
    }
}
