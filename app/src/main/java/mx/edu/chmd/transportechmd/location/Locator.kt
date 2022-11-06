package mx.edu.chmd.transportechmd.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*

class Locator(activity: Activity, locationCallBack: ILocationCallBack) {

    private val TAG = "location"
    private var mTrackingLocation: Boolean = true
    private var mLocationCallback: LocationCallback? = null


    private var mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(
            activity
        )


    interface ILocationCallBack {

        fun permissionDenied()

        fun locationSettingFailed()

        fun getLocation(location: Location)

    }


    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private val locationRequest: LocationRequest
        get() {
            val locationRequest = LocationRequest()
            locationRequest.interval = 20000
            locationRequest.fastestInterval = 15000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            return locationRequest
        }

    init {

        // Initialize the location callbacks.
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
             locationResult?.lastLocation?.let { locationCallBack.getLocation(it) }
            }
        }


        var permissionHelper = (activity as FragmentActivity).supportFragmentManager
            .findFragmentByTag(TAG) as PermissionHelper?
        if (permissionHelper == null) {
            permissionHelper =
                PermissionHelper.newInstance(object : PermissionHelper.PermissionListener {


                    override fun fetchLocation() {
                        mTrackingLocation = true
                        if (ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        mFusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            mLocationCallback!!, null
                        )


                    }

                    override fun permissionStatus(permissionValue: Boolean) {

                        if (permissionValue)
                            fetchLocation()
                        else
                            locationCallBack.permissionDenied()


                    }

                    override fun settingStatus(settingValue: Boolean) {


                        if (settingValue)
                            permissionHelper?.getPermissionStatus()
                        else
                            locationCallBack.locationSettingFailed()


                    }

                    override fun stopLocationUpdates() {

                        mFusedLocationClient.removeLocationUpdates(mLocationCallback as LocationCallback)

                    }


                })

            activity.supportFragmentManager.beginTransaction().add(permissionHelper, TAG)
                .commit()

            permissionHelper.setLocationRequest(locationRequest)


        }
    }


}