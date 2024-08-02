package com.enofir.testgps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.newland.sdk.ModuleManage;
import com.newland.sdk.module.devicebasic.DeviceBasicModule;
import com.newland.sdk.module.devicebasic.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {
    private static final int dukptIndex = 1;    // Slot 33
    private ModuleManage moduleManage;
    private DeviceBasicModule deviceBasicModule;
    private DeviceInfo deviceInfo;

    private Button butGetLocation;
    private EditText edLocation;
    private TextView textSupported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moduleManage = ModuleManage.getInstance();
        moduleManage.setDebugMode(true);
        moduleManage.init(this);

        deviceBasicModule = ModuleManage.getInstance().getDeviceBasicModule();
        deviceInfo = deviceBasicModule.getDeviceInfo();

        textSupported = findViewById(R.id.textGpsSupported);
        butGetLocation = findViewById(R.id.butGetLocation);
        edLocation = findViewById(R.id.edLocation);

        String supported = deviceInfo.isSupportGPS() ? "GPS Supported" : "GPS not Supported";
        textSupported.setText(supported);

        butGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] loc = getLocation();
                String location;
                if (loc == null) {
                    location = "unknow";
                } else {
                    location = String.format(Locale.US, "%.3f %.3f", loc[0], loc[1]);
                }
                edLocation.setText(location);
            }
        });
    }

    public double[] getLocation()
    {
        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager == null)
        {
            Log.e("LogApp", "locationManager null");
            return null;
        }
        List<String> providers = locationManager.getAllProviders();
        if((providers == null) || providers.isEmpty())
        {
            Log.e("LogApp", "not providers");
            return null;
        }
        for(String curProvider : providers)
        {
            Log.e("LogApp", "Provider: " + curProvider);
            Location location;
            try
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.e("LogApp", "Not permission");
                    return null;
                }
                location = locationManager.getLastKnownLocation(curProvider);
                if(location == null)
                {
                    Log.e("LogApp", "Fail get location");
                }
                else
                {
                    double[] loc = new double[2];
                    loc[0] = location.getLatitude();
                    loc[1] = location.getLongitude();
                    return loc;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        moduleManage.destroy();
    }
}
