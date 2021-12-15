/**
 * Based on zacharee's FabricateOverlay project
 *  https://github.com/zacharee/FabricateOverlay/blob/master/fabricateoverlay/src/main/java/tk/zwander/fabricateoverlay/OverlayAPI.kt
 */


package be.casperverswijvelt.unifiedinternetqs.privileged

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.IShizukuService
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper

@SuppressLint("PrivateApi")
class PrivilegedAPI(
    private val wifiManager: IBinder,
    private val connectivityManager: IBinder,
    private val telephonyManager: IBinder
) {

    companion object {

        private const val TAG = "PrivilegedAPI"

        private val connection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                synchronized(instanceLock) {
                    binding = false

                    val service = IShizukuService.Stub.asInterface(binder)

                    log("telephony ${service.telephonyManager}")

                    instance = PrivilegedAPI(
                        ShizukuBinderWrapper(
                            service.wifiManger
                        ),
                        ShizukuBinderWrapper(
                            service.connectivityManager
                        ),
                        ShizukuBinderWrapper(
                            service.telephonyManager
                        )
                    )

                    callbacks.forEach { callback ->
                        callback(instance!!)
                    }
                    callbacks.clear()
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                log("disconnected")
                synchronized(instanceLock) {
                    binding = false

                    instance = null
                }
            }

            override fun onNullBinding(name: ComponentName?) {
                log("Null binding")
            }

            override fun onBindingDied(name: ComponentName?) {
                log("binding died")
            }
        }

        /**
         * Callbacks for getting the [PrivilegedAPI] instance.
         */
        private val callbacks = arrayListOf<(PrivilegedAPI) -> Unit>()

        /**
         * A thread lock.
         */
        private val instanceLock = Any()

        /**
         * The current [PrivilegedAPI] instance.
         */
        @Volatile
        private var instance: PrivilegedAPI? = null

        /**
         * Whether there's been a Shizuku Service bind request.
         */
        @Volatile
        private var binding = false

        /**
         * Get an instance of [PrivilegedAPI] using Shizuku.
         *
         * @param context used to get the app's package name.
         * @param callback invoked once the [PrivilegedAPI] instance is ready.
         */
        fun getInstance(context: Context, callback: (PrivilegedAPI) -> Unit) {
            synchronized(instanceLock) {

                instance?.let {

                    //If we already have an instance, immediately invoke the callback.
                    callback(instance!!)
                } ?: run {

                    //Otherwise, queue the callback.
                    callbacks.add(callback)

                    if (!binding) {

                        //If there's not already a bind request in progress, make one.
                        binding = true

                        val serviceArgs = Shizuku.UserServiceArgs(
                            ComponentName(
                                context.packageName,
                                ShizukuService::class.java.name
                            )
                        ).processNameSuffix("betterinternettiles")
                            .debuggable(BuildConfig.DEBUG)
                            .version(BuildConfig.VERSION_CODE + 1)
                            .daemon(false)

                        Shizuku.bindUserService(serviceArgs, connection)
                    }
                }
            }
        }

        /**
         * If you've already retrieved an instance before, you can use
         * this to get it without a callback.
         */
        fun peekInstance(): PrivilegedAPI? {
            return instance
        }

        fun log(text: String) {
            Log.d(TAG, text)
        }
    }

    private val iwmClass = Class.forName("android.net.wifi.IWifiManager")
    private val iwmsClass = Class.forName("android.net.wifi.IWifiManager\$Stub")
    private val iwmInstance = iwmsClass.getMethod(
        "asInterface",
        IBinder::class.java
    ).invoke(
        null,
        wifiManager
    )

    private val icmClass = Class.forName("android.net.IConnectivityManager")
    private val icmsClass = Class.forName("android.net.IConnectivityManager\$Stub")
    private val icmInstance = iwmsClass.getMethod(
        "asInterface",
        IBinder::class.java
    ).invoke(
        null,
        connectivityManager
    )

    private val itClass = Class.forName("com.android.internal.telephony.ITelephony")
    private val itsClass = Class.forName("com.android.internal.telephony.ITelephony\$Stub")

    @SuppressLint("PrivateApi")
    fun setWifiEnabled(enabled: Boolean) {

        try {
            iwmClass.getMethod(
                "setWifiEnabled",
                String::class.java,
                Boolean::class.java
            ).invoke(
                iwmInstance,
                "" ,
                enabled
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SoonBlockedPrivateApi")
    fun setMobileDataEnabled(enabled: Boolean) {

        //println(TelephonyManager::class.java.methods.map { it.toString() })
        //println(HiddenApiBypass.getDeclaredMethods(TelephonyManager::class.java).map { it.toString() })
        val method = TelephonyManager::class.java.getDeclaredMethod("getITelephony")

        try {
            Class.forName("com.android.internal.telephony.ITelephony")
            //method.invoke(telephonyManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //printMethodInfo(icmClass, "setMobileDataEnabled")
    }

    private fun printMethodInfo(javaClass: Class<*>, methodName: String) {
        println("${HiddenApiBypass.getDeclaredMethods(javaClass).size}")
        javaClass.methods.forEach {
            println(it.name)
            if (it.name == methodName) {
                log(it.parameters.map { param ->
                    "${param.name} ${param.type.name}"
                }.toString())
                return@forEach
            }
        }
    }
}