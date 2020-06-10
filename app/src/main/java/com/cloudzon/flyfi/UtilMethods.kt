package com.cloudzon.flyfi

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

/**
 * Created by imran on 27/Dec/2017.
 */
object UtilMethods {

    private val TAG: String = "---UtilMethods"

    /**
     * @param context
     * @action show Long toast message
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    /**
     * @param context
     * @return true or false mentioning the device is connected or not
     * @brief checking the internet connection on run time
     */
    fun isConnectedToInternet(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val allNetworks = manager?.allNetworks?.let { it } ?: return false
            allNetworks.forEach { network ->
                val info = manager.getNetworkInfo(network)
                if (info.state == NetworkInfo.State.CONNECTED) return true
            }
        } else {
            val allNetworkInfo = manager?.allNetworkInfo?.let { it } ?: return false
            allNetworkInfo.forEach { info ->
                if (info.state == NetworkInfo.State.CONNECTED) return true
            }
        }
        return false
    }

    fun NoInternetConnectionDialog(context: Context) {
        val dialog = Dialog(context, R.style.DialogTheme)
        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.getWindow()?.getAttributes()?.windowAnimations = R.style.MyAlertDialogStyle;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_no_internet)


        val btn_try_again = dialog.findViewById(R.id.btn_try_again) as Button

        btn_try_again.setOnClickListener {
            dialog.dismiss()
        }
        dialog.create()
        dialog.show()
    }
}