package com.example.androidapp;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Core of the application
 */
public class MainActivity extends AppCompatActivity {

    /**
     * enum to represent ways of sorting
     */
    protected enum Sorting {
        ALPHABETICALLY,
        BY_DISTANCE,
        BY_STARS
    }

    /**
     * enum to hold scene which is set
     */
    protected enum Scene {
        BASE_SCENE,
        SEARCH_SCENE
    }

    /**
     * sorting enum {@link Sorting}
     */
    Sorting sorting;

    /**
     * holds already set scene {@link Scene}
     */
    Scene scene;


    /**
     * imageview to move back on previous scene ({@link #setBaseScene()})
     */
    ImageView arrowBackIcon;

    /**
     * icon which offers sorting possibilities when pressed, when is application in the search scene the canteens matching
     * searching values {@link #suitableCanteens} are sorted by {@link #sortSuitableCanteens()}, {@link #displayedCanteenNumber}
     * is set to 0 and scene is updated {@link #updateSearchScene()}
     */
    ImageView sortIcon;

    /**
     * funny icon which returns toast: GOOD FOOD = GOOD MOOD
     */
    ImageView goodMoodIcon;

    /**
     * icon which enables moving on the previous canteen during the browsing of canteens, when pressed reduce
     * {@link #displayedCanteenNumber} by one and calls {@link #updateSearchScene()}
     */
    ImageView moveBackIcon;

    /**
     * icon which enables moving on the next canteen during the browsing of canteens, when pressed increase
     * {@link #displayedCanteenNumber} by one and calls {@link #updateSearchScene()}
     */
    ImageView moveForwardIcon;

    /**
     * offers distance sorting values from {@link #distanceValuesArray}
     */
    Spinner distanceSpinner;

    /**
     * offers rating sorting values from {@link #ratingValuesArray}
     */
    Spinner ratingSpinner;

    /**
     * offers capacity sorting values from {@link #capacityValuesArray}
     */
    Spinner capacitySpinner;

    /**
     * array to hold spinners {@link #distanceSpinner}, {@link #ratingSpinner}, {@link #capacitySpinner}
     */
    Spinner[] spinners;

    /**
     * distance sorting values (location: res/values/strings)
     */
    ArrayAdapter<CharSequence> distanceValuesArray;

    /**
     * rating sorting values (location: res/values/strings)
     */
    ArrayAdapter<CharSequence> ratingValuesArray;

    /**
     * capacity sorting values (location: res/values/strings)
     */
    ArrayAdapter<CharSequence> capacityValuesArray;


    /**
     * button to save user searching preferences into internal storage
     */
    Button savePreferencesButton;

    /**
     * button to reset all searching values to unimportant, does not effect sorting value
     */
    Button resetButton;

    /**
     * search for canteens matching searching parameters, when pressed causes scene change from {@link #baseContent}
     * to {@link #searchContent} if location permissions granted and {@link #isGPSEnabled()}, {@link #isInternetAvailable()}
     * return true, in other case the dialog asking for permission, GPS enabling or Internet connection will be shown
     */
    Button searchButton;

    /**
     * method to call web browser to display canteen menu (using {@link Canteen#getUrl})
     */
    Button getCanteenMenuButton;

    /**
     * method to call google maps to display route to canteen
     */
    Button getRouteButton;

    /**
     * base layout which offer searching options - baseScene
     */
    LinearLayout baseContent;

    /**
     * layout to represent individual results of searching - searchScene, (visibility.GONE on start of application)
     */
    LinearLayout searchContent;

    /**
     * array to hold canteens {@link Canteen}
     */
    Canteen[] canteens = new Canteen[6];

    /**
     * array to hold canteens which satisfy searching parameters
     */
    List<Canteen> suitableCanteens;

    /**
     * to hold information which canteen from {@link #canteens} is displayed
     */
    int displayedCanteenNumber;

    /**
     * service to get internet availability
     */
    ConnectivityManager connectivityManager;

    /**
     * service to get location of the user
     */
    FusedLocationProviderClient locationProvider;

    /**
     * location manager to check whether GPS location is enabled
     */
    LocationManager locationManager;


    /**
     * user location gained by using {@link #locationManager}
     */
    Location userLocation;

    /**
     * latitude of the user gained by using {@link #userLocation}
     */
    double userLatitude;

    /**
     * longitude of the user gained by using {@link #userLocation}
     */
    double userLongitude;


    /**
     * textview to hold name of displayed canteen, {@link Canteen}
     */
    TextView canteenName;

    /**
     * textview to hold distance to displayed canteen, {@link Canteen}
     */
    TextView distance;

    /**
     * textview to hold rating of displayed canteen, {@link Canteen}
     */
    TextView rating;

    /**
     * textview to hold capacity of displayed canteen, {@link Canteen}
     */
    TextView capacity;

    /**
     * textview to hold opening time of displayed canteen (Monday - Thursday), {@link Canteen}
     */
    TextView openingTime;

    /**
     * textview to hold friday opening time of displayed canteen, {@link Canteen}
     */
    TextView fridayOpeningTime;

    /**
     * array to hold integer values of {@link #distanceSpinner}
     */
    int[] distanceTimeValues = {500,1000,2000,3000,5000,10_000,Integer.MAX_VALUE};

    /**
     * array to hold integer values of {@link #ratingSpinner}
     */
    int[] minimalRatingValues = {20,30,35,40,45,Integer.MIN_VALUE};

    /**
     * array to hold integer values of {@link #capacitySpinner}
     */
    int[] minimalCapacityValues = {50,100,150,200,Integer.MIN_VALUE};

    /**
     * alert dialog builder for {@link #requestGPSEnabling()}, {@link #requestInternetEnabling()},
     * {@link #showDialogLocalizationInProgress()}, {@link #showDialogLocalizationFailure()},
     * {@link #showDialogNoCanteenFound()}
     */
    AlertDialog.Builder alertDialogBuilder;

    /**
     * alert dialog for {@link #requestGPSEnabling()}, {@link #requestInternetEnabling()},
     * {@link #showDialogLocalizationInProgress()}, {@link #showDialogLocalizationFailure()},
     * {@link #showDialogNoCanteenFound()}
     */
    AlertDialog alert;

    /**
     * main method called when application starts
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseContent = findViewById(R.id.base_content);
        searchContent = findViewById(R.id.search_content);
        getLayoutInflater().inflate(R.layout.base_layout, baseContent);
        getLayoutInflater().inflate(R.layout.search_layout, searchContent);
        initialization();
        setBaseScene();


        goodMoodIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "GOOD FOOD = GOOD MOOD", Toast.LENGTH_SHORT).show();
            }
        });


        arrowBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBaseScene();
            }
        });


        sortIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, findViewById(R.id.toolbar_layout), Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
                popupMenu.getMenu().getItem(sorting.ordinal()).setEnabled(false);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            switch (menuItem.getItemId()) {
                                case R.id.alphabetically:
                                    sorting = Sorting.ALPHABETICALLY;
                                    return true;
                                case R.id.by_distance:
                                    sorting = Sorting.BY_DISTANCE;
                                    return true;
                                case R.id.by_stars:
                                    sorting = Sorting.BY_STARS;
                                    return true;
                                default:
                                    return false;
                            }
                        }
                        finally {
                            if (scene == Scene.SEARCH_SCENE) {
                                sortSuitableCanteens();
                                displayedCanteenNumber = 0;
                                updateSearchScene();
                            }
                        }

                    }
                });
                popupMenu.show();
            }
        });


        moveBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayedCanteenNumber--;
                updateSearchScene();
            }
        });

        moveForwardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayedCanteenNumber++;
                updateSearchScene();
            }
        });


        savePreferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileOutputStream writer = openFileOutput("user_preferences.txt", MODE_PRIVATE);
                    writer.write("Number of selected item in distanceSpinner, starsSpinner, capacitySpinner, sortingSpinner".getBytes());
                    writer.write("\n".getBytes());

                    for (Spinner spinner : spinners) {
                        writer.write(String.valueOf(spinner.getSelectedItemPosition()).getBytes());
                        writer.write("\n".getBytes());
                    }
                    writer.write(String.valueOf(sorting.ordinal()).getBytes());
                    writer.write("\n".getBytes());
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distanceSpinner.setSelection(distanceValuesArray.getCount() - 1);
                ratingSpinner.setSelection(ratingValuesArray.getCount() - 1);
                capacitySpinner.setSelection(capacityValuesArray.getCount() - 1);
            }
        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE}, 1001);
                }
                else if (!isGPSEnabled()){
                    requestGPSEnabling();
                }
                else if (!isInternetAvailable()){
                    requestInternetEnabling();
                }
                else {
                    getUserLocation();
                }
            }
        });


        getCanteenMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(suitableCanteens.get(displayedCanteenNumber).getUrl()));
                startActivity(browserIntent);
            }
        });


        getRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(suitableCanteens.get(displayedCanteenNumber).getAddress()));
                startActivity(mapIntent);
            }
        });
    }

    /**
     * method to get user location by using {@link #locationProvider}, if last location of user is reasonably up to date the
     * method {@link #useLastLocation()} will be called otherwise the method {@link #useCurrentLocation()} will be called
     */
    @SuppressLint("MissingPermission")
    private void getUserLocation(){
        locationProvider.getLocationAvailability()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationAvailability>() {
                    @Override
                    public void onSuccess(LocationAvailability locationAvailability) {
                        if (locationAvailability.isLocationAvailable()){
                            useLastLocation();
                        }
                        else {
                            useCurrentLocation();
                        }
                    }
                });
    }


    /**
     * method to get last known location of user provided by {@link #locationProvider}, called by {@link #getUserLocation()},
     * if it fails th method {@link #useCurrentLocation()} is called
     */
    @SuppressLint("MissingPermission")
    private void useLastLocation() {
        locationProvider.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Location location) {
                        // Get last known location. In some rare situations this can be null
                        if (location != null) {
                            userLocation = location;
                            measureDistances();
                            setSearchScene();
                        }
                        else {
                            useCurrentLocation();
                        }
                    }

                })
                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        useCurrentLocation();
                    }
                });
    }

    /**
     * method to get current location of user provided by {@link #locationProvider}, called by {@link #getUserLocation()},
     * if it fails alert dialog {@link #showDialogLocalizationFailure()} will be shown
     */
    @SuppressLint("MissingPermission")
    private void useCurrentLocation() {
        showDialogLocalizationInProgress();
        locationProvider.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Location location) {
                        // Get current location
                        if (location != null) {
                            alert.cancel();
                            userLocation = location;
                            measureDistances();
                            setSearchScene();
                        }
                        else {
                            showDialogLocalizationFailure();
                        }
                    }
                })
                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showDialogLocalizationFailure();
                    }
                });
    }


    /**
     * alert dialog showing that user localization is in progress (displayed during use of {@link #useCurrentLocation()})
     */
    private void showDialogLocalizationInProgress() {
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(R.string.localization_text);
        alert = alertDialogBuilder.create();
        alert.show();
    }


    /**
     * alert dialog informing that user localization failed
     */
    private void showDialogLocalizationFailure() {
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setMessage("Lokalizace uživatele se nezdařila")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }


    /**
     * check whether file already exist
     * @param context  context
     * @param filename name of file
     * @return boolean value whether file exists or not
     */
    private boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
    }


    /**
     * initialization of all icons
     */
    private void initializeImageViews() {
        arrowBackIcon = findViewById(R.id.arrow_back_icon);
        sortIcon = findViewById(R.id.sort_icon);
        goodMoodIcon = findViewById(R.id.good_mood_icon);
        moveBackIcon = findViewById(R.id.move_back_icon);
        moveForwardIcon = findViewById(R.id.move_forward_icon);
    }

    /**
     * method to initialize all buttons
     */
    private void initializeButtons() {
        savePreferencesButton = findViewById(R.id.save_preferences_button);
        resetButton = findViewById(R.id.reset_button);
        searchButton = findViewById(R.id.search_button);
        getCanteenMenuButton = findViewById(R.id.get_canteen_menu_button);
        getRouteButton = findViewById(R.id.get_route_button);
    }

    /**
     * Initialization of components which holds displayed canteen data
     */
    private void initializeCanteenInfoPanels() {
        canteenName = findViewById(R.id.name_value);
        distance = findViewById(R.id.distance_value);
        rating = findViewById(R.id.rating_value);
        capacity = findViewById(R.id.capacity_value);
        openingTime = findViewById(R.id.opening_time_value);
        fridayOpeningTime = findViewById(R.id.friday_opening_time_value);
    }

    /**
     * initialize array of canteens {@link Canteen} using assets/canteens.txt
     */
    private void initializeCanteens() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("canteens.txt")));
            reader.readLine();
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                String name = values[0];
                int rating = Integer.parseInt(values[1]);
                int capacity = Integer.parseInt(values[2]);
                double latitude = Double.parseDouble(values[3]);
                double longitude = Double.parseDouble(values[4]);
                String openingTime = values[5];
                String fridayOpeningTime = values[6];
                String address = values[7];
                String url = values[8];
                canteens[i] = new Canteen(name, rating, capacity, latitude, longitude, openingTime, fridayOpeningTime, address, url);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialization of internet network service
     */
    private void initializeConnectivityManager(){
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    /**
     * initialization of location service {@link #locationManager}, {@link #locationProvider}
     */
    private void initializeLocationManagerAndProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationServices.getFusedLocationProviderClient(MainActivity.this);
    }


    /**
     * method to initialize spinners {@link #spinners}
     */
    private void initializeSpinners() {
        distanceSpinner = findViewById(R.id.distance_spinner);
        ratingSpinner = findViewById(R.id.rating_spinner);
        capacitySpinner = findViewById(R.id.capacity_spinner);
        spinners = new Spinner[]{distanceSpinner, ratingSpinner, capacitySpinner};
    }

    /**
     * method to initialize arrays which holds spinner´s values, arrays may be found in res/values/strings.xml
     */
    private void initializeSpinnerItems() {
        distanceValuesArray = ArrayAdapter.createFromResource(this, R.array.distanceTime, R.layout.custom_spinner);
        ratingValuesArray = ArrayAdapter.createFromResource(this, R.array.minimalRating, R.layout.custom_spinner);
        capacityValuesArray = ArrayAdapter.createFromResource(this, R.array.minimalCapacity, R.layout.custom_spinner);
    }

    /**
     * method to add items to spinners {@link #spinners}
     */
    private void addItemsToSpinners() {
        distanceSpinner.setAdapter(distanceValuesArray);
        ratingSpinner.setAdapter(ratingValuesArray);
        capacitySpinner.setAdapter(capacityValuesArray);
    }

    /**
     * method to load user preferences for searching from internal storage, if file does not exist the file
     * default_preferences.txt located in res/assets/default_preferences.txt is used
     */
    private void loadUserPreferences() {
        try {
            BufferedReader reader = fileExists(this, "user_preferences.txt")
                    ? new BufferedReader(new InputStreamReader(openFileInput("user_preferences.txt")))
                    : new BufferedReader(new InputStreamReader(getAssets().open("default_preferences.txt")));
            reader.readLine();
            for (Spinner spinner : spinners) {
                spinner.setSelection(Integer.parseInt(reader.readLine()));
            }
            sorting = Sorting.values()[Integer.parseInt(reader.readLine())];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * initialization of all components:
     * {@link #initializeImageViews()},
     * {@link #initializeButtons()},
     * {@link #initializeCanteenInfoPanels()},
     * {@link #initializeCanteens()},
     * {@link #initializeConnectivityManager()},
     * {@link #initializeLocationManagerAndProvider()},
     * {@link #initializeSpinners()},
     * {@link #initializeSpinnerItems()},
     * {@link #addItemsToSpinners()},
     * {@link #loadUserPreferences()},
     */
    private void initialization() {
        initializeImageViews();
        initializeButtons();
        initializeCanteenInfoPanels();
        initializeCanteens();
        initializeConnectivityManager();
        initializeLocationManagerAndProvider();
        initializeSpinners();
        initializeSpinnerItems();
        addItemsToSpinners();
        loadUserPreferences();
    }

    /**
     * setting base scene {@link #baseContent}
     */
    private void setBaseScene() {
        scene = Scene.BASE_SCENE;
        baseContent.setVisibility(View.VISIBLE);
        goodMoodIcon.setVisibility(View.VISIBLE);

        searchContent.setVisibility(View.GONE);
        arrowBackIcon.setVisibility(View.INVISIBLE);
    }

    /**
     * setting search scene which displays searching result, if no canteen match searching parameters the dialog informing
     * about this fact will be shown
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSearchScene() {
        suitableCanteens = new ArrayList<>();
        int maximalDistance = distanceTimeValues[distanceSpinner.getSelectedItemPosition()];
        int minimalRating = minimalRatingValues[ratingSpinner.getSelectedItemPosition()];
        int minimalCapacity = minimalCapacityValues[capacitySpinner.getSelectedItemPosition()];
        for (Canteen canteen: canteens){
            if (canteen.getDistance() <=  maximalDistance && canteen.getRating() >= minimalRating && canteen.getCapacity() >= minimalCapacity){
                suitableCanteens.add(canteen);
            }
        }

        if (suitableCanteens.size() == 0){
            showDialogNoCanteenFound();
        }
        else {
            scene = Scene.SEARCH_SCENE;
            displayedCanteenNumber = 0;
            searchContent.setVisibility(View.VISIBLE);
            arrowBackIcon.setVisibility(View.VISIBLE);
            baseContent.setVisibility(View.GONE);
            goodMoodIcon.setVisibility(View.INVISIBLE);

            sortSuitableCanteens();
            updateSearchScene();
        }
    }

    /**
     * alert dialog to inform user that no {@link Canteen} from {@link #canteens} matches searching parameters
     */
    private void showDialogNoCanteenFound() {
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setMessage("Hledání neodpovídají žádné položky")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }


    /**
     * updating information about canteen when new canteen is set to be displayed (moving between canteens is done by
     * {@link #moveBackIcon}, {@link #moveForwardIcon})
     */
    private void updateSearchScene() {
        int i = displayedCanteenNumber;
        moveBackIcon.setVisibility(View.VISIBLE);
        moveBackIcon.setEnabled(true);
        moveForwardIcon.setVisibility(View.VISIBLE);
        moveForwardIcon.setEnabled(true);

        if (i == 0) {
            moveBackIcon.setVisibility(View.INVISIBLE);
            moveBackIcon.setEnabled(false);
        }

        if (i == suitableCanteens.size() - 1) {
            moveForwardIcon.setVisibility(View.INVISIBLE);
            moveForwardIcon.setEnabled(false);
        }

        canteenName.setText(suitableCanteens.get(i).getName());
        distance.setText(suitableCanteens.get(i).getDistanceString());
        rating.setText(suitableCanteens.get(i).getRatingString());
        capacity.setText(suitableCanteens.get(i).getCapacityString());
        openingTime.setText(suitableCanteens.get(i).getOpeningTime());
        fridayOpeningTime.setText(suitableCanteens.get(i).getFridayOpeningTime());
    }

    /**
     * sort canteens matching searching parameters by value given in {@link #sorting} which is set by using
     * {@link #sortIcon}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortSuitableCanteens() {
        switch (sorting) {
            case ALPHABETICALLY:
                Collections.sort(suitableCanteens, Comparator.comparing(Canteen::getName));
                break;

            case BY_DISTANCE:
                Collections.sort(suitableCanteens, Comparator.comparing(Canteen::getDistance));
                break;

            case BY_STARS:
                Collections.sort(suitableCanteens, Comparator.comparing(Canteen::getRating).reversed());
                break;
        }
    }


    /**
     * method to check whether GPS is enabled
     * @return boolean value
     */
    private boolean isGPSEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * method to check whether internet connection is enabled
     * @return boolean value
     */
    private boolean isInternetAvailable(){
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * alert dialog requesting internet connection
     */
    private void requestInternetEnabling() {
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setCancelable(false)
                .setMessage("Aplikace vyžaduje pro správné fungování internetové připojení")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }


    /**
     * alert dialog requesting GPS enabling
     */
    private void requestGPSEnabling() {
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setCancelable(false)
                .setMessage("Aplikace vyžaduje pro fungování přístup k poloze zařízení")
                .setPositiveButton("Umožnit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(callGPSSettingIntent);
                    }
                })
                .setNegativeButton("Zavřít", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GREEN);
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    /**
     * method to measure distance between user and all {@link #canteens}, inside loop calls
     * {@link Canteen#measureDistance(double, double)} providing {@link #userLatitude} and
     * {@link #userLongitude} gained by using {@link #userLocation}
     */
    private void measureDistances() {
        userLatitude = userLocation.getLatitude();
        userLongitude = userLocation.getLongitude();

        for (Canteen canteen: canteens){
            canteen.measureDistance(userLatitude, userLongitude);
        }
    }
}