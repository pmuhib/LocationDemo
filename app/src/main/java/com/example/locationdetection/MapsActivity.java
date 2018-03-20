package com.example.locationdetection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.locationdetection.Utillity.DirectionData;
import com.example.locationdetection.Utillity.PlaceAutocompleteAdapter;
import com.example.locationdetection.Utillity.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    boolean checkstatuspermission;
    public static int GpsRequesst = 101;
    public static int PERMISSION = 102;
    GoogleApiClient client;
    LatLng currentlatLng, Destlatlong;
    private FusedLocationProviderClient mFusedLocationClient;
    AutoCompleteTextView enterPlace;
    TextView Search;
    MarkerOptions currentMarker,destnationMarker;
    List<Address> currentAddresses,destinationAddress;
    double radius=0.50;
    PlaceAutocompleteAdapter  placeAutocompleteAdapter;
    String currentloaction,destlocation,distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        enterPlace=findViewById(R.id.input_search);
        Search=findViewById(R.id.text_serch);
        Search.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //Check Gps Status
        //Check internet Available
        if (Utils.isNetworkavailable(this)) {
            checkgpsstatus();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions();
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                  /*  if(checkstatuspermission==true) {
                      *//*  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(this);*//*
                    }
                 else {
                    Utils.Message(this, "Your App Needs Permisions");
                }*/
            } else {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Check Network Connection", Snackbar.LENGTH_LONG).show();
        }

    }


    private void checkgpsstatus() {
        /*Here we will chechk availability of Gps by using location manager*/
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        /*To Check if Gps is Enabled*/
        if (!isGpsenabled) {
            gotoGpsSettings();
        } else {
            Utils.Message(this, "Gps is Enabled");
        }
    }

    private void checkPermissions() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

                checkstatuspermission = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
                Utils.Message(MapsActivity.this,"Allowed");

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Utils.Message(MapsActivity.this,"Need Permissions to run this App");
                if (response.isPermanentlyDenied()) {
                    checkstatuspermission = false;
                    Utils.Message(MapsActivity.this,"Need Permissions to run this App");
                    gotoPermissionSettings();

                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
                checkstatuspermission = false;

            }
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {


            }
        }).check();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            currentlatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            currentAddresses = codeaddress(location);
                            if (!currentAddresses.isEmpty() && currentAddresses.size() > 0) {
                                currentMarker= new MarkerOptions();
                                currentMarker.position(currentlatLng).title("My Location").snippet(currentAddresses.get(0).getAddressLine(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                             //   currentMarker.position(currentlatLng).title("My Location=" + currentAddresses.get(0).getFeatureName() + "," + currentAddresses.get(0).getSubLocality() + "," + currentAddresses.get(0).getSubAdminArea()).snippet(currentAddresses.get(0).getLocality() + "," + currentAddresses.get(0).getAdminArea() + "," + currentAddresses.get(0).getCountryName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                mMap.addMarker(currentMarker);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlatLng));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatLng, 17));
                                setupAutocomplete();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.Message(MapsActivity.this,"Error trying to get last GPS location");
                }
            });
         //   buildgoogleclient();
        }
    }


    private  void setupAutocomplete() {
        client=new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this,this).build();
        LatLng latLng1=new LatLng(currentlatLng.latitude+radius,currentlatLng.longitude+radius);
        LatLng latLng2=new LatLng(currentlatLng.latitude-radius,currentlatLng.longitude-radius);
        LatLngBounds bounds=LatLngBounds.builder().include(latLng2).include(latLng1).build();
        placeAutocompleteAdapter=new PlaceAutocompleteAdapter(this,client,bounds,null);
        enterPlace.setAdapter(placeAutocompleteAdapter);
        enterPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geolocate();
                }
                return false;
            }
        });

    }

    private void gotoPermissionSettings() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, PERMISSION);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();

    }

    private void gotoGpsSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings").setMessage("GPS is not enabled. Do you want to go to settings menu?");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, GpsRequesst);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GpsRequesst) {
            checkgpsstatus();
        }
        if (requestCode == PERMISSION) {
            if (checkstatuspermission == false) {
                checkPermissions();
            }
        }

    }
    private List<Address> codeaddress(Location currentlatLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList=geocoder.getFromLocation(currentlatLng.getLatitude(),currentlatLng.getLongitude(),1);
            if(addressList.isEmpty())
            {
                Utils.Message(this,"Waiting");
            }
            else
            {
                if (addressList.size()>0)
                {
                    return addressList;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.text_serch:
                geolocate();
        }
    }

    private void geolocate() {
        String location=enterPlace.getText().toString().trim();
        if(!location.isEmpty())
        {
            mMap.clear();
            currentMarker= new MarkerOptions();
            currentMarker.position(currentlatLng).title("My Location=" + currentAddresses.get(0).getFeatureName() + "," + currentAddresses.get(0).getSubLocality() + "," + currentAddresses.get(0).getSubAdminArea()).snippet(currentAddresses.get(0).getLocality() + "," + currentAddresses.get(0).getAdminArea() + "," + currentAddresses.get(0).getCountryName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(currentMarker);
            Address address=null;
            destnationMarker=new MarkerOptions();
            Geocoder geocoder=new Geocoder(this);
            try {
                destinationAddress=geocoder.getFromLocationName(location,5);
                for (int i=0;i<destinationAddress.size();i++)
                {
                    address=   destinationAddress.get(0);
                    Destlatlong=new LatLng(address.getLatitude(),address.getLongitude());
                    destnationMarker.position(Destlatlong).title("Result Location=" + destinationAddress.get(0).getFeatureName() + "," + destinationAddress.get(0).getSubLocality() + "," + destinationAddress.get(0).getSubAdminArea()).snippet(destinationAddress.get(0).getLocality() + "," + destinationAddress.get(0).getAdminArea() + "," + destinationAddress.get(0).getCountryName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.addMarker(destnationMarker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(Destlatlong));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Destlatlong, 16));
                    setupline();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void setupline() {
        final PolylineOptions polylineOptions=new PolylineOptions();
        polylineOptions.geodesic(true).color(Color.BLUE).width(10);
        currentloaction="origin=" + currentlatLng.latitude + "," + currentlatLng.longitude;
        destlocation="destination=" + Destlatlong.latitude + "," + Destlatlong.longitude;
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = currentloaction + "&" + destlocation + "&" + sensor;

        // Output format
        String output = "json";
        String key="AIzaSyCfRT9eEPlnSi8bGEEAWs6MZ7DbMLcTs7s";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters+"&"+key;
        final List<LatLng> directionList = new ArrayList<LatLng>();
        RequestQueue volley= Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson=new Gson();
                DirectionData directionData=null;

                try {
                    directionData=new DirectionData();
                    directionData=gson.fromJson(response.toString(),DirectionData.class);
                    String status=directionData.getStatus();
                    if(status.equals("OK"))
                    {
                        for(int i=0;i<directionData.getRoutes().size();i++)
                        {
                            String duration=directionData.getRoutes().get(i).getLegs().get(i).getDuration().getText();
                            distance=directionData.getRoutes().get(i).getLegs().get(i).getDistance().getText();
                            Toast.makeText(getApplicationContext(),"Distance= "+distance+"\n"+"Duration= "+duration,Toast.LENGTH_LONG).show();

                            String encodedString=directionData.getRoutes().get(i).getOverview_polyline().getPoints();
                            List<LatLng> list=decodePoly(encodedString);
                            for (LatLng dir:list)
                            {
                                directionList.add(dir);
                            }
                        }
                        polylineOptions.addAll(directionList);
                        Polyline polyline=mMap.addPolyline(polylineOptions);
                    }
                    Log.d("response",""+response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error",""+error);

            }
        });
        volley.add(jsonObjectRequest);
    }
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
    public void hospitals(View view)
    {
        nearbyHospitals();
    }
    private void nearbyHospitals()
    {
        int PROXIMITY_RADIUS;
        if (currentlatLng!=null) {
            final MarkerOptions markerOptions = new MarkerOptions();
            PROXIMITY_RADIUS = 500;

         /*   if(distance!=null )
            {
                PROXIMITY_RADIUS= Integer.parseInt(distance)*1000;
            }
            else {
                PROXIMITY_RADIUS = 500;

            }*/
            //Defines the distance (in meters)

            String key="AIzaSyDCDw0K3aui_cCWMvFQVYQfkxw5l9DHJaw";

            StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlaceUrl.append("location="+currentlatLng.latitude+","+currentlatLng.longitude);
            googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
            googlePlaceUrl.append("&type="+"hospital");
            googlePlaceUrl.append("&key="+key);
            String url=googlePlaceUrl.toString();
            RequestQueue requestQueue=Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Gson gson=new Gson();
                    GetNearByPlaces nearByPlaces=null;

                    try {
                        nearByPlaces=new GetNearByPlaces();
                        nearByPlaces=gson.fromJson(response,GetNearByPlaces.class);
                        String status=nearByPlaces.getStatus();
                        List<GetNearByPlaces.ResultsBean> list=new ArrayList<>();
                        list=nearByPlaces.getResults();
                        for (int i=0;i<list.size();i++)
                        {
                            double lat=list.get(i).getGeometry().getLocation().getLat();
                            double longt=list.get(i).getGeometry().getLocation().getLng();
                            String name=list.get(i).getName();
                            String vicnity=list.get(i).getVicinity();
                            LatLng latLng=new LatLng(lat,longt);
                            markerOptions.title(name).position(latLng).snippet(vicnity).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            mMap.addMarker(markerOptions);
                            Log.d("hos",response);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error",""+error);

                }
            });
            requestQueue.add(stringRequest);
        } else {
        }

    }
}
