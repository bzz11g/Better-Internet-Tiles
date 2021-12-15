// IShizukuService.aidl
package be.casperverswijvelt.unifiedinternetqs;

// Declare any non-default types here with import statements

interface IShizukuService {

    void destroy() = 16777114;

    IBinder getWifiManger() = 1;

    IBinder getConnectivityManager() = 2;

    IBinder getTelephonyManager() = 3;
}