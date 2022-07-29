package com.example.geolocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
                   GoogleMap.OnPolylineClickListener,
                   GoogleMap.OnPolygonClickListener {

    /* Start stylePolyline */
    private static final int COLOR_BLACK_ARGB = 0x65000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;
    private static final int COLOR_RED_ARGB = 0x65FF0000;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    /* Start stylePolygon */
    // Crea un patrón de trazo de un espacio seguido de un punto.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED =
            Arrays.asList(GAP, DOT);

    // Crea un patrón de trazo de un espacio seguido de un guión.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA =
            Arrays.asList(GAP, DASH);

    // Crea un patrón de trazo de un punto seguido de un espacio, un guión y otro espacio.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    /* End stylePolygon */

    private GoogleMap myMap;
    private LocationManager locationManager;

    private TextView tvState,
            tvLatitude,
            tvLongitude,
            tvAccuracy,
            tvAltitude,
            tvSpeed,
            tvAddress;
    private String status,
            latitude,
            longitude,
            accuracy,
            altitude,
            speed,
            address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Initialization();
        /* Actualizar GPS */
        registrarLocalizacion();

        /* MAPS */
        // Obtenga SupportMapFragment y reciba una notificación cuando el mapa esté listo para ser utilizado.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mv_map_one);
        mapFragment.getMapAsync(this);
    }

    private void Initialization() {
        tvState         = findViewById(R.id.tv_main_status_title_value);

        tvLatitude      = findViewById(R.id.tv_main_latitude_value);
        tvLongitude     = findViewById(R.id.tv_main_longitude_value);

        tvAccuracy      = findViewById(R.id.tv_main_accuracy_value);

        tvAltitude      = findViewById(R.id.tv_main_altitude_value);
        tvSpeed         = findViewById(R.id.tv_main_speed_value);

        tvAddress       = findViewById(R.id.tv_main_address_value);

    } // End Init

    private void registrarLocalizacion() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                Log.d("Coordenadas", "Location: " + location);
                updateLocationInfo(location);
            }

            public void onProviderDisabled(String provider) {
                Log.d("Coordenadas", "Provider OFF");
                tvState.setText(R.string.tv_status_provider_disabled);
            }

            public void onProviderEnabled(String provider) {
                Log.d("Coordenadas", "Provider ON");
                tvState.setText(R.string.tv_status_provider_enable);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Coordenadas", "Provider Status: " + status);
                tvState.setText(getString(R.string.tv_status_provider_changed,provider, status));

                /* Este método quedó obsoleto en el nivel de API 29. Esta devolución de llamada nunca se invocará en Android Q y versiones posteriores. */
                Toast.makeText(MapActivity.this,"Cambios en proveedor " +provider+ ", Estado: " + status,Toast.LENGTH_LONG).show();
                /* Metodo 2 */
                switch (status){
                    case LocationProvider.OUT_OF_SERVICE: // 0:  proveedor fuera de servicio
                        Log.d("coordenadasChanged", "LocationProvider.OUT_OF_SERVICE");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE: // 1:  proveedor temporalmente deshabilitado
                        Log.d("coordenadasChanged", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                        break;
                    case LocationProvider.AVAILABLE: // 2 : proveedor habilitado
                        Log.d("coordenadasChanged", "LocationProvider.AVAILABLE");
                        break;
                } // End Switch
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener); // deberemos pasar 4 parámetros distintos:
        /* 1. Nombre del proveedor de localización al que nos queremos suscribir.
           2. Tiempo mínimo entre actualizaciones, en milisegundos: 3000 = 3 seg.
           3. Distancia mínima entre actualizaciones, en metros.
           4. Instancia de un objeto LocationListener, que tendremos que implementar previamente para definir las acciones a realizar al recibir cada nueva actualización de la posición.
        */

    } // End metodo

    private void updateLocationInfo(Location location) {

        latitude     = String.valueOf(location.getLatitude());
        longitude    = String.valueOf(location.getLongitude());

        accuracy     = String.valueOf(location.getAccuracy());

        altitude     = String.valueOf(location.getAltitude());
        speed        = String.valueOf(location.getSpeed());

        Log.d("Coordenadas", "La direccion ha cambiado");
        Log.d("coordenadas", "Latitud: " + latitude);
        Log.d("coordenadas", "Longitud: " + longitude);
        Log.d("coordenadas", "_________________________________________________");

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.CANADA.getDefault());
        try {
            List<Address> addressList;
            addressList = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            Log.d("coordenadas", "getAddresLine: " +addressList.get(0).getAddressLine(0));
            address = String.valueOf(addressList.get(0).getAddressLine(0));
            tvAddress.setText(address);

            Log.d("coordenadas", "getAdminArea: " +addressList.get(0).getAdminArea());              // Área de administración
            Log.d("coordenadas", "getCountryCode: " +addressList.get(0).getCountryCode());          // Código del país
            Log.d("coordenadas", "getCountryName: " +addressList.get(0).getCountryName());          // Nombre del país
            Log.d("coordenadas", "getFeatureName: " +addressList.get(0).getFeatureName());          // Nombre de la función
            Log.d("coordenadas", "getLocality: " +addressList.get(0).getLocality());                // Localidad
            Log.d("coordenadas", "getPostalCode: " +addressList.get(0).getPostalCode());            // Código postal                (Null)
            Log.d("coordenadas", "getPremises: " +addressList.get(0).getPremises());                // Locales                      (Null)
            Log.d("coordenadas", "getSubAdminArea: " +addressList.get(0).getSubAdminArea());        // Área de subadministración    (Null)
            Log.d("coordenadas", "getSubLocality: " +addressList.get(0).getSubLocality());          // Sublocalidad                 (Null)
            Log.d("coordenadas", "getSubThoroughfare: " +addressList.get(0).getSubThoroughfare());  // Sub vía
            Log.d("coordenadas", "getThoroughfare: " +addressList.get(0).getThoroughfare());        // Vía pública

            // La única diferencia entre USA y COP es que estos resultados no tienen PostalCode y SubAdminArea.
            // debemos identificar el sistema de cada país o región y desarrollar para cada uno.

            //https://www.flipandroid.com/qu-devuelve-el-mtodo-location-address-de-cada-android.html
            Log.d("coordenadas", "___________________________________________________________________________");

        } catch (IOException e) {
            e.printStackTrace();
        }
        tvLatitude.setText(latitude);
        tvLongitude.setText(longitude);
        tvAccuracy.setText(accuracy); // 68%

        if(location.hasAltitude()){ // Obtenga la altitud, si está disponible, en metros por encima del elipsoide de referencia WGS 84
            tvAltitude.setText(altitude);
        } else {
            tvAltitude.setText(R.string.tv_altitude_value);
        }

        if(location.hasSpeed()){ // en metros / segundo sobre el suelo.
            tvSpeed.setText(speed);
        } else {
            tvSpeed.setText(R.string.tv_speed_value);
        }

    } // End onLocationChanged

    public void OnBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        /* Coordenadas */
        LatLng UPMCampusSur = new LatLng(40.38812766305057, -3.629740063200698);
        LatLng Home = new LatLng(40.43962783144964, -3.612459195817192);

        LatLng ceroCero = new LatLng(0, 0);
        LatLng ceroDiez = new LatLng(0,10);

        LatLng diezCero = new LatLng(10,0);
        LatLng diezDiez = new LatLng(10,10);

        LatLng veinteCero = new LatLng(20,0);
        LatLng ceroVeinte = new LatLng(0,20);

        LatLng ochentaCero = new LatLng(80,0);
        LatLng MenosochentaCero = new LatLng(-80,0);
        LatLng ceroCientoOchenta = new LatLng(0,175);
        LatLng ceroMenosCientoOchenta = new LatLng(0,-175);

        /* Marcacion */
        myMap.addMarker(new MarkerOptions().position(ochentaCero).title(ochentaCero.latitude +", "+ ochentaCero.longitude));
        myMap.addMarker(new MarkerOptions().position(MenosochentaCero).title(MenosochentaCero.latitude +", "+ MenosochentaCero.longitude));

        myMap.addMarker(new MarkerOptions().position(ceroCientoOchenta).title(ceroCientoOchenta.latitude +", "+ ceroCientoOchenta.longitude));
        myMap.addMarker(new MarkerOptions().position(ceroMenosCientoOchenta).title(ceroMenosCientoOchenta.latitude +", "+ ceroMenosCientoOchenta.longitude));

        myMap.addMarker(new MarkerOptions().position(ceroCero).title(ceroCero.latitude +", "+ ceroCero.longitude));
        myMap.addMarker(new MarkerOptions().position(ceroDiez).title(ceroDiez.latitude+ ", " +ceroDiez.longitude));

        myMap.addMarker(new MarkerOptions().position(diezCero).title(diezCero.latitude+ ", " +diezCero.longitude));
        myMap.addMarker(new MarkerOptions().position(diezDiez).title(diezDiez.latitude+ ", " +diezDiez.longitude));

        myMap.addMarker(new MarkerOptions().position(veinteCero).title(veinteCero.latitude+ ", " +veinteCero.longitude));
        myMap.addMarker(new MarkerOptions().position(ceroVeinte).title(ceroVeinte.latitude+ ", " +ceroVeinte.longitude));

        myMap.addMarker(new MarkerOptions().position(UPMCampusSur).title("UPM"));
        myMap.addMarker(new MarkerOptions().position(Home).title("Home"));

        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        /* Metodos */
        /* Calcular distancias en KM, Metros */
        Location location = new Location("localizacion 1");
        location.setLatitude(Home.latitude);  //latitud
        location.setLongitude(Home.longitude); //longitud
        Location location2 = new Location("localizacion 2");
        location2.setLatitude(UPMCampusSur.latitude);  //latitud
        location2.setLongitude(UPMCampusSur.longitude); //longitud
        double distance = location.distanceTo(location2);
        float distanceF = location.distanceTo(location2);
        Log.d("MapaDistancia", "DistanciaDouble: " + distance);
        Log.d("MapaDistancia", "DistanciaFloat: " + distanceF);
        // El valor devuelto es en metros.
        //CalculationByDistance(Home, UPMCampusSur);
        CalculationByDistance(ceroCero, ceroDiez);
        CalculationByDistance(Home, UPMCampusSur);

        //getDistanceFromLatLonInKm(Home.latitude, Home.longitude, UPMCampusSur.latitude, UPMCampusSur.longitude);

        // Convertir latitude y longitude a grados, minutos y segundos
        getFormattedLocationInDegree(Home.latitude, Home.longitude);

        /* Trazar linea */
        // Agrega polilíneas al mapa.
        // Las polilíneas son útiles para mostrar una ruta o alguna otra conexión entre puntos.
        Polyline polyline0 = myMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(Home.latitude, Home.longitude),
                        new LatLng(UPMCampusSur.latitude, UPMCampusSur.longitude) // Punto final donde va la imagen
                ));
        // Almacene un objeto de datos con la polilínea, que se usa aquí para indicar un tipo arbitrario.
        polyline0.setTag("A");
        stylePolyline(polyline0);

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(10, 0),
                        new LatLng(10, 10),
                        new LatLng(0, 10),
                        new LatLng(0, 0),
                        new LatLng(10, 0)
                ));
        // Almacene un objeto de datos con la polilínea, que se usa aquí para indicar un tipo arbitrario.
        polyline1.setTag("A");

        Polygon polygon2 = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(27.485750478700233, -79.87455741876153),
                        new LatLng(32.321384,	-64.75737),
                        new LatLng(18.684642597792827, -65.46049499903513),
                        new LatLng(27.485750478700233, -79.87455741876153)));
        // Almacena un objeto de datos con el polígono, usado aquí para indicar un tipo arbitrario.
        polygon2.setTag("alpha");
        stylePolygon(polygon2);

        //myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Home, 12)); // 1... 30 12  mayor numero mas cerca
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ceroCientoOchenta, 1)); // 1... 30 12  mayor numero mas cerca

        // Set listeners for click events.
        myMap.setOnPolylineClickListener(MapActivity.this);
        myMap.setOnPolygonClickListener(MapActivity.this);
        /* End Trazar linea */

    } // End onMapReady


    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Obtiene el objeto de datos almacenado con la polilínea.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // Si no se proporciona ningún tipo, permita que la API utilice el predeterminado.
            case "A":
                // Utilice un mapa de bits personalizado como límite al comienzo de la línea.
                polyline.setEndCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Utilice una tapa redonda al comienzo de la línea.
                polyline.setStartCap(new RoundCap());
                break;
        }
        polyline.setStartCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }
    // End stylePolyline

    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Obtiene el objeto de datos almacenado con el polígono.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // Si no se proporciona ningún tipo, permita que la API utilice el predeterminado.
            case "alpha":
                // Aplicar un patrón de trazo para renderizar una línea discontinua y definir colores.
                pattern = PATTERN_POLYGON_ALPHA;
                //strokeColor = COLOR_GREEN_ARGB;
                strokeColor = COLOR_RED_ARGB;
                //fillColor = COLOR_PURPLE_ARGB;
                fillColor = COLOR_RED_ARGB;
                break;
            case "beta":
                // Aplicar un patrón de trazo para representar una línea de puntos y guiones, y definir colores.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }
        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }
    // End stylePolygon


    /* fórmula de Haversine Metodo 1 */
    public double CalculationByDistance(LatLng coordenadasA, LatLng coordenadasB) {
        int Radius = 6371;// radio de la tierra en  kilómetros
        double latitudA     = coordenadasA.latitude;
        double longitudA    = coordenadasA.longitude;
        double latitudB     = coordenadasB.latitude;
        double longitudB    = coordenadasB.longitude;

        double dLat = Math.toRadians(latitudB - latitudA);
        double dLon = Math.toRadians(longitudB - longitudA);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitudA))
                * Math.cos(Math.toRadians(latitudB)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        double valueResult = Radius * c;

        double km = valueResult / 1; //Convertir a KM y Metros
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.d("MapaDistancia", "" + valueResult
                + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return valueResult;

    } // End CalculationByDistance

    public static String getFormattedLocationInDegree(double latitude, double longitude) {
        try {
            int latSeconds = (int) Math.round(latitude * 3600);
            int latDegrees = latSeconds / 3600;
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = latSeconds / 60;
            latSeconds %= 60;

            int longSeconds = (int) Math.round(longitude * 3600);
            int longDegrees = longSeconds / 3600;
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = longSeconds / 60;
            longSeconds %= 60;
            String latDegree = latDegrees >= 0 ? "N" : "S";
            String lonDegrees = longDegrees >= 0 ? "E" : "W";

            Log.d("MapaConvertir", "Convertido a: "+Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                    + "\"" + latDegree + " " + Math.abs(longDegrees) + "°" + longMinutes
                    + "'" + longSeconds + "\"" + lonDegrees);

            return Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                    + "\"" + latDegree + " " + Math.abs(longDegrees) + "°" + longMinutes
                    + "'" + longSeconds + "\"" + lonDegrees;

        } catch (Exception e) {
            return "" + String.format("%8.5f", latitude) + "  "
                    + String.format("%8.5f", longitude);
        }
    } // End getFormattedLocationInDegree

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    } // End onPolylineClick
    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    } // End onPolygonClick

}