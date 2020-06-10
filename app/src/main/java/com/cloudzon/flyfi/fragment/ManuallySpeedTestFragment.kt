package com.cloudzon.flyfi.fragment

import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.*
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cloudzon.flyfi.R
import com.cloudzon.flyfi.activity.DashBoardActivity
import org.json.JSONObject
import java.text.DecimalFormat

class ManuallySpeedTestFragment : Fragment() {

    lateinit var activity: DashBoardActivity

    lateinit var iv_close_fragment: ImageView

    private val TAG: String = "ManuallySpeedTest"

    private lateinit var ll_fragment: LinearLayout

    lateinit var cv_download_speed_fragment: CardView

    lateinit var rl_download_speed_fragment: RelativeLayout

    lateinit var cv_upload_speed_fragment: CardView

    lateinit var rl_upload_speed_fragment: RelativeLayout

    lateinit var txt_service_provider_name: TextView

    lateinit var btn_start_fragment: Button

    lateinit var btn_stop_fragment: Button

    lateinit var ll_meter_view_fragment: LinearLayout

    lateinit var txt_ping_speed_fragment: TextView

    lateinit var txt_download_speed_fragment: TextView

    lateinit var txt_upload_speed_fragment: TextView

    lateinit var sharedPreferences: SharedPreferences

    var lastPingValue: Double = 0.0

    var download_speed: Double = 0.0

    var upload_speed: Double = 0.0

    var download_bytes: Double = 0.0

    var upload_bytes: Double = 0.0

    var service_provider_name: String = ""

    private lateinit var mHandler_final: Handler
    private lateinit var mRunnable_final: Runnable

    private lateinit var mHandler_testing: Handler
    private lateinit var mRunnable_testing: Runnable

    private var isStop: Boolean = false

    var netwotk_type: Int = 0

    var netwotk_name: String = "(Cellular)"

    var isRecordInserted: Boolean = false

    lateinit var network_animation_view: LottieAnimationView

    var myTask: AsyncTask<Void, Void, Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manually_speed_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        activity = this.requireActivity() as DashBoardActivity

        // Initialize the handler instance
        mHandler_testing = Handler()

        mHandler_final = Handler()

        ll_fragment = view.findViewById(R.id.ll_fragment)

        ll_fragment.setOnClickListener(View.OnClickListener {

        })

        iv_close_fragment = view.findViewById(R.id.iv_close_fragment)

        iv_close_fragment.setOnClickListener(View.OnClickListener {

            cancelAllTask() //stopManualSpeedCheck()

            passData("")

        })

        cv_download_speed_fragment = view.findViewById(R.id.cv_download_speed_fragment)
        //cv_download_speed_fragment.setOnClickListener(clickListener)

        rl_download_speed_fragment = view.findViewById(R.id.rl_download_speed_fragment)
        //rl_download_speed_fragment.setOnClickListener(clickListener)

        cv_upload_speed_fragment = view.findViewById(R.id.cv_upload_speed_fragment)
        //cv_upload_speed_fragment.setOnClickListener(clickListener)

        rl_upload_speed_fragment = view.findViewById(R.id.rl_upload_speed_fragment)
        //rl_upload_speed_fragment.setOnClickListener(clickListener)

        txt_ping_speed_fragment = view.findViewById(R.id.txt_ping_speed_fragment)
        txt_download_speed_fragment = view.findViewById(R.id.txt_download_speed_fragment)
        txt_upload_speed_fragment = view.findViewById(R.id.txt_upload_speed_fragment)

        txt_service_provider_name = view.findViewById(R.id.txt_service_provider_name)

        ll_meter_view_fragment = view.findViewById(R.id.ll_meter_view_fragment)

//        speedMeter_fragment = view.findViewById(R.id.speedMeter)

        network_animation_view = view.findViewById(R.id.network_animation_view)

        btn_start_fragment = view.findViewById(R.id.btn_start_fragment)
        btn_start_fragment.setOnClickListener(clickListener)

        btn_stop_fragment = view.findViewById(R.id.btn_stop_fragment)
        btn_stop_fragment.setOnClickListener(clickListener)

        netwotk_name = checkNetworkTypeAvailable(activity)

    }

    var clickListener = View.OnClickListener { v ->

        when (v.id) {

            R.id.btn_start_fragment -> checkManualReadPer() //checkReadPer() //callStartService()

            R.id.btn_stop_fragment -> cancelAllTask() //stopManualSpeedCheck() //callStopService()

        }

    }

    private fun checkManualReadPer() {
        val checkSelfPermission = ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                2
            )
        } else {
            testUpAndDownSpeed()
        }
    }

    private fun testUpAndDownSpeed() {

        isRecordInserted = false

        lastPingValue = 0.0

        download_speed = 0.0

        upload_speed = 0.0

        download_bytes = 0.0

        upload_bytes = 0.0

        if (isConnectedToInternet(activity)) {

            sharedPreferences
                .edit()
                .putBoolean("IsManualStart", true)
                .apply()

            var isLaunchFirstTime: Boolean = sharedPreferences.getBoolean("IsManualStart", false)

            if (isLaunchFirstTime) {

                txt_service_provider_name.visibility = View.INVISIBLE

                btn_start_fragment.isClickable = false
                btn_start_fragment.isEnabled = false
                btn_start_fragment.visibility = View.GONE

                btn_stop_fragment.isClickable = true
                btn_stop_fragment.isEnabled = true
                ll_meter_view_fragment.visibility = View.VISIBLE

                activity.runOnUiThread(java.lang.Runnable {
                    network_animation_view.playAnimation()
                })

                ll_meter_view_fragment.animate()
                    .translationY(0.0f)
                    .alpha(1.0f)
                    .setDuration(300)

                btn_start_fragment.animate()
                    .translationY(btn_start_fragment.height.toFloat())
                    .alpha(0.0f)
                    .setDuration(300)

            }

            isStop = false


            // lastPingValue = pingg("www.google.com")!!.toLong()

            //txt_ping_speed_fragment.setText("0." + lastPingValue.toString() + "ms")

            mRunnable_testing = Runnable {
                // Do something here
                //callSpeedTestPython() //downloadSpeedFireBase() //downloadSpeedCheckByOkHttp()

                class doAsync() : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void?): Void? {
                        if (isStop)
                            return null

                        if (!Python.isStarted())
                            Python.start(AndroidPlatform(activity))

                        val py = Python.getInstance()

                        val pyf = py.getModule("speedtest")

                        try {
                            val obj = pyf.callAttr("shell")
                            println(obj.toString())
                            //textView.setText(obj.toString())

                            val json_contact: JSONObject = JSONObject(obj.toString())

                            val download: Double = json_contact.getDouble("download")
                            val upload: Double = json_contact.getDouble("upload")
                            val ping: Double = json_contact.getDouble("ping")

                            val bytes_sent: Long = json_contact.getLong("bytes_sent")

                            val bytes_received: Long = json_contact.getLong("bytes_received")

                            Log.e("download", download.toString())
                            Log.e("upload", upload.toString())
                            Log.e("ping", ping.toString())
                            Log.e("bytes_sent", bytes_sent.toString())
                            Log.e("bytes_received", bytes_received.toString())

                            lastPingValue = ping
                            download_speed = (download / 1000.0 / 1000.0)
                            upload_speed = (upload / 1000.0 / 1000.0)
                            download_bytes = bytes_received.toDouble()
                            upload_bytes = bytes_sent.toDouble()

                            // UtilMethods.showLongToast(activity,obj.toString())
                        } catch (e: Exception) {
                            // textView.setText("No address associated with hostname")
                            // UtilMethods.showLongToast(activity,"No address associated with hostname")

                        }
                        return null
                    }

                    override fun onPreExecute() {
                        super.onPreExecute()

                        txt_ping_speed_fragment.setText("Loading")
                        txt_download_speed_fragment.setText("Loading")
                        txt_upload_speed_fragment.setText("Loading")

                    }

                    override fun onPostExecute(result: Void?) {
                        super.onPostExecute(result)

                        saveLastSpeed()
                    }
                }

                myTask = doAsync()

                if (myTask != null) {
                    doAsync().execute()
                }

                mHandler_testing.removeCallbacks(mRunnable_testing)
            }

            // Schedule the task to repeat after 1 second
            mHandler_testing.postDelayed(
                mRunnable_testing, // Runnable
                100 // Delay in milliseconds
            )

        } else {
            saveLastSpeed()
        }

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

    private fun saveLastSpeed() {

        var delay_time: Long = 500

        if (!isStop) {
            delay_time = 500
        } else {
            delay_time = 500
        }


        if (!isRecordInserted) {

            isRecordInserted = true

        }

        txt_service_provider_name.visibility = View.VISIBLE

        txt_ping_speed_fragment.setText(lastPingValue.toString() + "ms")
        txt_download_speed_fragment.setText(
            download_speed?.toDouble()?.let { checkSpeedKbOrMb(it) })
        txt_upload_speed_fragment.setText(upload_speed?.toDouble()?.let { checkSpeedKbOrMb(it) })

        mRunnable_final = Runnable {
            // Do something here

            // try to touch View of UI thread
            activity.runOnUiThread(java.lang.Runnable {

                sharedPreferences
                    .edit()
                    .putBoolean("IsManualStart", false)
                    .apply()

                var isLaunchFirstTime: Boolean =
                    sharedPreferences.getBoolean("IsManualStart", false)

                if (!isLaunchFirstTime) {
                    btn_start_fragment.isClickable = true
                    btn_start_fragment.isEnabled = true
                    btn_start_fragment.visibility = View.VISIBLE

                    btn_stop_fragment.isClickable = false
                    btn_stop_fragment.isEnabled = false
                    ll_meter_view_fragment.visibility = View.GONE

                    activity.runOnUiThread(java.lang.Runnable {
                        network_animation_view.pauseAnimation()
                    })

                    ll_meter_view_fragment.animate()
                        .translationY(ll_meter_view_fragment.height.toFloat())
                        .alpha(0.0f)
                        .setDuration(300)

                    btn_start_fragment.animate()
                        .translationY(0.0f)
                        .alpha(1.0f)
                        .setDuration(300)

                }

                isStop = false
            })

        }


        // Schedule the task to repeat after 1 second
        mHandler_final.postDelayed(
            mRunnable_final, // Runnable
            delay_time // Delay in milliseconds
        )


    }

    private fun checkSpeedKbOrMb(mTheSpeedValue: Double): String {

        val df2 = DecimalFormat("#.##")

//        var speedMbps: Double = ((mTheSpeedValue / 1024).toDouble())
//
//        var mTheSpeed: String = speedMbps.toString()
//
//        val firstChar: Char = mTheSpeed.get(0)
        var speedFinal: String = ""
        // if (firstChar == '0') {
        //speedFinal = df2.format(mTheSpeedValue.toFloat()) + " KB/s"
//        } else {
        // speedFinal = df2.format(speedMbps) + " MB/s"

        speedFinal = df2.format(mTheSpeedValue) + " Mbps"
//        }

        return speedFinal
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onPause() {
        super.onPause()


    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {

        } else {
            cancelAllTask() //stopManualSpeedCheck()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun passData(data: String) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2 ->
                if (grantResults.isNotEmpty() && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                    testUpAndDownSpeed() // saveUploadVideotoSDCard()
                } else {
                    Toast.makeText(activity, "You deied the permission", Toast.LENGTH_SHORT).show()
                }

        }
    }

    fun checkNetworkTypeAvailable(context: Context?): String {

        var netwotk_nameTemp:String = ""

        if (context == null) return netwotk_nameTemp
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        netwotk_type = 0

                        try {
                            val manager =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            val carrierName = manager.networkOperatorName

                            netwotk_nameTemp = carrierName //+ " (Cellular)"

                            netwotk_nameTemp = netwotk_nameTemp.replace('"', ' ').trim()

                        } catch (e: java.lang.Exception) {
                            netwotk_nameTemp = "(Cellular)"
                        }

                        // netwotk_nameTemp = netwotk_name

                        return netwotk_nameTemp
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        netwotk_type = 1

                        try {
                            val wifiManager =
                                context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            val info = wifiManager.connectionInfo
                            val ssid: String = info.ssid

                            netwotk_nameTemp = ssid

                            netwotk_nameTemp = netwotk_nameTemp.replace('"', ' ').trim()

                        } catch (e: java.lang.Exception) {
                            netwotk_nameTemp = "(WIFI)"
                        }

                        //netwotk_nameTemp = netwotk_name

                        return netwotk_nameTemp
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        netwotk_type = 2
                        netwotk_nameTemp = "(ETHERNET)"

                        netwotk_nameTemp = netwotk_nameTemp.replace('"', ' ').trim()

                        // netwotk_nameTemp = netwotk_name

                        return netwotk_nameTemp
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {

                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi

                    netwotk_type = 1

                    try {
                        val wifiManager =
                            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val info = wifiManager.connectionInfo
                        val ssid: String = info.ssid

                        netwotk_nameTemp = ssid

                        netwotk_nameTemp = netwotk_nameTemp.replace('"', ' ').trim()


                    } catch (e: java.lang.Exception) {
                        netwotk_nameTemp = "(WIFI)"
                    }

                    // netwotk_nameTemp = netwotk_name

                    return netwotk_nameTemp
                } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan

                    netwotk_type = 0

                    try {
                        val manager =
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        val carrierName = manager.networkOperatorName

                        netwotk_nameTemp = carrierName //+ " (Cellular)"

                        netwotk_nameTemp = netwotk_nameTemp.replace('"', ' ').trim()

                    } catch (e: java.lang.Exception) {
                        netwotk_nameTemp = "(Cellular)"
                    }

                    // netwotk_nameTemp = netwotk_name

                    return netwotk_nameTemp
                }

                //return true
            }
        }
        return netwotk_nameTemp
    }

    private fun cancelAllTask() {

        isStop = true

        if (myTask != null) {
            myTask!!.cancel(true)
        }

        saveLastSpeed()
    }

}