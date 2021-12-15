package be.casperverswijvelt.unifiedinternetqs.privileged

import android.content.Context
import android.os.IBinder
import be.casperverswijvelt.unifiedinternetqs.IShizukuService
import rikka.shizuku.SystemServiceHelper
import kotlin.system.exitProcess

class ShizukuService : IShizukuService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun getWifiManger(): IBinder {
        return SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)
    }

    override fun getConnectivityManager(): IBinder {
        return SystemServiceHelper.getSystemService(Context.CONNECTIVITY_SERVICE)
    }

    override fun getTelephonyManager(): IBinder {
        return SystemServiceHelper.getSystemService(Context.CONNECTIVITY_SERVICE)
    }
}