package com.example.aura_project;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.aura_project.Model.Driver;
import com.example.aura_project.Model.User;
//import com.example.aura_project.POJO.Example;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import com.example.aura_project.POJO.Example;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SearchView searchView;
    SearchView searchView1;
    ImageView ImgExpandable;
    ImageView Hospital_search;
    BottomSheetDriverFragment mbottomSheet;

    double latitude;
    double longitude;
    private int PROXIMITY_RADIUS = 10000;

    public View Mapview;
    private final float DEFAULT_ZOOM = 18;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    Marker mUserMaker;

    Button btnPickUpRequeset;

    private FusedLocationProviderClient mfusedLocationProviderClient;

    boolean isDriverAvailable=false;
    LatLng pickUpLocation;
    String DriverId="";
    int radius =1; // 1 km
    int distance=1;//3km
    private static final int Limit =3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.search_location);

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverHome.this);


        //InitView
        ImgExpandable = (ImageView) findViewById(R.id.ImgExpandable);
        Hospital_search=(ImageView) findViewById(R.id.hospital_search);
        mbottomSheet = BottomSheetDriverFragment.newInstance("userBottomSheet");
        ImgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mbottomSheet.show(getSupportFragmentManager(), mbottomSheet.getTag());
            }
        });

        Hospital_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                build_retrofit_and_get_response("hospital");
            }
        });



        btnPickUpRequeset = (Button) findViewById(R.id.pickuprequest);
        btnPickUpRequeset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPickUphere(FirebaseAuth.getInstance().getCurrentUser().getUid());

            }
        });


       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null ||location.equals("")) {
                    Geocoder geocoder = new Geocoder(DriverHome.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Log.d("mytag", "unable to find Location");

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });







    }

    private void build_retrofit_and_get_response(String hospital) {
        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);

        Call<Example> call = service.getNearbyPlaces(hospital, latitude + "," + longitude, PROXIMITY_RADIUS);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                try {
                    mMap.clear();
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        // Position of Marker on Map
                        markerOptions.position(latLng);
                        // Adding Title to the Marker
                        markerOptions.title(placeName + " : " + vicinity);
                        // Adding Marker to the Camera.
                        Marker m = mMap.addMarker(markerOptions);
                        // Adding colour to the marker
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // move map camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d("onFailure", t.toString());

            }


        });
    }

    private void requestPickUphere(String uid) {
        DatabaseReference dbrequest = FirebaseDatabase.getInstance().getReference("PickUpLocation");
        GeoFire mgeofire = new GeoFire(dbrequest);
        mgeofire.setLocation(uid, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Adding Marker

                pickUpLocation=new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                Log.d("mytag","GeofireCompleted");

                if (mUserMaker!=null)
                    mUserMaker.remove();
                mUserMaker = mMap.addMarker(new MarkerOptions()
                        .title("PickUpHere")
                        .snippet("")
                        .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mUserMaker.showInfoWindow();
                btnPickUpRequeset.setText("Getting Your Driver....");
                findDriver();

            }
        });

    }

    private void findDriver() {
        DatabaseReference Riders= FirebaseDatabase.getInstance().getReference( "DriverAvaliable");
       // Log.d("riders","bye1");
        GeoFire gfRider=new GeoFire(Riders);
        //Log.d("gfrider","bye1");
        GeoQuery geoQuery= gfRider.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude),radius);
        Log.d("geoquery",geoQuery.toString());
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                Log.d("hi2","bye2");

                //if Found
                if (!isDriverAvailable)
                {
                    Log.d("hi1","bye1");
                    Toast.makeText(DriverHome.this, "Finding Driver", Toast.LENGTH_SHORT).show();
                    isDriverAvailable=true;
                    DriverId=key;
                    btnPickUpRequeset.setText("Call Driver");
                    Toast.makeText(DriverHome.this, " "+key, Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                ///if Still not Found Driver Increase Distance
                if (!isDriverAvailable)
                {
                    radius++;
                    findDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyAYik4Zj_pXGGREwPsnMMZzW7SSC8-Li-k");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);



        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (Mapview != null && Mapview.findViewById(Integer.parseInt("1")) != null) {

            View locationButton = ((View) Mapview.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }
        //check if user gps is unable or not then apply changes
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(DriverHome.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(DriverHome.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                getDeviceLocation();

            }
        });


        task.addOnFailureListener(DriverHome.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(DriverHome.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void getDeviceLocation() {
        mfusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()){
                            mLastKnownLocation=task.getResult();
                            if (mLastKnownLocation!=null){
                                if (mUserMaker!=null)
                                    mUserMaker.remove();
                                //Add new Marker
                                mUserMaker=mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                                        .title(String.format("YOU")));
////////////////
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                                LoadAllUsers();

                            }else {
                                final LocationRequest locationRequest=LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback=new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult==null){
                                            return;
                                        }
                                        mLastKnownLocation= locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));

                                        mfusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mfusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
                            }

                        }else {
                            Toast.makeText(DriverHome.this, "Unable to get LastLocation", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void LoadAllUsers() {

        DatabaseReference driverLocation= FirebaseDatabase.getInstance().getReference("DriverAvaliable");
        GeoFire gf=new GeoFire(driverLocation);

        GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //load the drivers
                //setting database path
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Driver and user class are same
                                User user= dataSnapshot.getValue(User.class);

                                //add driver to Map

                                try {
                                    if (mUserMaker!=null)
                                        mUserMaker.remove();
                                    mUserMaker=mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                            .flat(true)
                                          // .title(user.getName())
                                           //.snippet("Phone : " + driver.getPhone())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(DriverHome.this, ""+e, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance<=Limit){
                    distance++;
                    LoadAllUsers();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


}

