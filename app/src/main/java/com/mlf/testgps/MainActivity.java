package com.mlf.testgps;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private static final String LOG_TAG = "AppLog" ;

    private AppCompatButton butGetLocation;
    private AppCompatEditText edLocation;

    private ActivityResultLauncher<String> mPermissionResult;

    private volatile boolean gpsPermissionChecked = false;
    private volatile boolean gpsPermission = false;
    private volatile double[] gpsResult = null;
    private volatile String gpsError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butGetLocation = findViewById(R.id.butGetLocation);
        edLocation = findViewById(R.id.edLocation);

        mPermissionResult = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result)
                    {
                        Log.e(LOG_TAG, "onActivityResult() " + result);
                        gpsPermissionChecked = true;
                        gpsPermission = result;
                    }
                });

        butGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.e(LOG_TAG, "call getLocation() and wait result");
                getLocation();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while(gpsError == null)
                        {

                        }
                        Log.e(LOG_TAG, "Location getted");
                        String location;
                        if(gpsError.isEmpty())
                        {
                            location = String.format(Locale.US, "%.3f %.3f", gpsResult[0], gpsResult[1]);
                        }
                        else
                        {
                            location = gpsError;
                        }
                        edLocation.setText(location);
                    }
                }).start();
            }
        });
    }

    private void checkGpsPermission()
    {
        Log.e(LOG_TAG, "checkGpsPermission()");

        gpsPermissionChecked = false;
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(result == PackageManager.PERMISSION_GRANTED)
        {
            gpsPermission = true;
            gpsPermissionChecked = true;
        }
        else
        {
            mPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public void getLocation()
    {
        gpsError = null;

        Log.e(LOG_TAG, "checkGpsPermission() and wait");

        checkGpsPermission();

        // Wait for result
        new Thread(new Runnable()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void run()
            {
                while(!gpsPermissionChecked)
                {
                }
                Log.e(LOG_TAG, "Permission checked");
                if(!gpsPermission)
                {
                    gpsError = "Not permission";
                    return;
                }
                // Get the location manager
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if(locationManager == null)
                {
                    gpsError = "locationManager error";
                    return;
                }
                List<String> providers = locationManager.getAllProviders();
                if(providers.isEmpty())
                {
                    gpsError = "not providers";
                    return;
                }
                for(String curProvider : providers)
                {
                    Log.e("LogApp", "Provider: " + curProvider);
                    Location location;
                    try
                    {
                        location = locationManager.getLastKnownLocation(curProvider);
                        if(location != null)
                        {
                            gpsError = "";
                            gpsResult = new double[2];
                            gpsResult[0] = location.getLatitude();
                            gpsResult[1] = location.getLongitude();
                            return;
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                gpsError = "Fail get location";
            }
        }).start();
    }
}