package com.example.locationdetection.Utillity;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by user on 3/14/2018.
 */

public class Utils {
    public static void Message(Context context,String Message)
    {
        Toast.makeText(context,Message,Toast.LENGTH_LONG).show();
    }
    public static boolean isNetworkavailable(Activity activity)
    {
        ConnectivityManager  connectivityManager= (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if (networkInfo!=null && networkInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
}
/* StringBuilder stringBuilder=new StringBuilder();
                    stringBuilder.append(addresses.get(0).getFeatureName()).append("\n")
                            .append(addresses.get(0).getLocality()).append("\n").
                            append(addresses.get(0).getAdminArea()).append("\n")
                            .append(addresses.get(0).getCountryCode()).append("\n")
                            .append(addresses.get(0).getPostalCode()).append("\n").append(addresses.get(0).getCountryName()).append("\n");
                   *//* Log.d("Featurename",addresses.get(0).getFeatureName());
        Log.d("Locality",addresses.get(0).getLocality());
        Log.d("Address",addresses.get(0).getAdminArea());
        Log.d("Country",addresses.get(0).getCountryName());
        Log.d("CountryCode",addresses.get(0).getCountryCode());
        Log.d("Postal Code",addresses.get(0).getPostalCode())*/;