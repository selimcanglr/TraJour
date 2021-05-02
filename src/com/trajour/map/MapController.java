package com.trajour.map;

import com.trajour.journey.Journey;
import com.trajour.main.Main;
import com.trajour.main.MainController;
import com.trajour.user.User;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.WorldMapView;
import com.trajour.profile.ProfileController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.Scanner;

import static com.trajour.db.DatabaseQuery.findJourneyByJourneyTitle;
import static com.trajour.main.MainController.buildNotification;

public class MapController implements Initializable {
    @FXML
    private Button homePageButton;

    @FXML
    private Button mapPageButton;

    @FXML
    private Button profilePageButton;

    @FXML
    private Slider zoomSlider;

    @FXML
    private WorldMapView worldMapView;

    @FXML
    private Button addJourneyButton;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextArea journeyDescriptionTextArea;

    @FXML
    private TextField selectedLocationField;

    @FXML
    private Button pickRandomCountryButton;

    @FXML
    private Button showDistanceButton;

    @FXML
    private TextField journeyTitleTextField;

    @FXML
    private RadioButton showWorldCapitalsRadioBox;

    @FXML
    private Label distanceResultLabel;

    private ObservableList<WorldMapView.Location> capitals;
    private User currentUser;
    private Tooltip tooltip;
    private SimpleBooleanProperty showLocationsView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initButtons();

        initWorldMapView();

        // Zoom in and out
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> worldMapView.setZoomFactor(zoomSlider.getValue()));
    }

    public void initData(User user) {
        currentUser = user;

        // From the JavaFX tutorial in Oracle's website, disables the cells that corresponds to the date
        // selected in the startDate and all the cells corresponding to the preceding dates
        // https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm#CCHHJBEA
        // Inits date picker
        startDatePicker.setValue(LocalDate.now());
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item.isBefore(
                                        startDatePicker.getValue().plusDays(1))
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                                long p = ChronoUnit.DAYS.between(
                                        startDatePicker.getValue(), item
                                );

                                Tooltip tt = (new Tooltip("You're about to stay for " + p + " days"));
                                tt.setFont(new Font(14));

                                setTooltip(tt);
                            }
                        };
                    }
                };
        endDatePicker.setDayCellFactory(dayCellFactory);
        endDatePicker.setValue(startDatePicker.getValue().plusDays(1));

        // Inits the zoom handler and location view
        worldMapView.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> {
            double movement = (scrollEvent.getDeltaY()) / ((double) 40);
            zoomSlider.setValue(zoomSlider.getValue() + movement);
        });
    }
    @FXML
    void handleShowLocations(ActionEvent event) {
        showLocationsView.set(!showLocationsView.get());

        worldMapView.setShowLocations(showLocationsView.get());
    }

    @FXML
    void handleShowDistance(ActionEvent event) {
        if (worldMapView.getSelectedLocations().size() == 2) {
            WorldMapView.Location l1 = worldMapView.getSelectedLocations().get(0);
            WorldMapView.Location l2 = worldMapView.getSelectedLocations().get(1);
            double distance = calculateDistanceBetweenTwoLocations(l1, l2);

            // Format the distance, show only 3 decimals
            DecimalFormat df = new DecimalFormat("#,###.##");
            String formattedDistance = df.format(distance);

            distanceResultLabel.setText("Distance between " + l1.getName() + " and " + l2.getName() + " is " + formattedDistance + " kilometers.");
        }
        else {
            Notifications notification = buildNotification("Unable to Calculate Distance", "In order to " +
                    "calculate the distance you must choose  2 locations. To select locations change map view from top left.", 8, Pos.TOP_CENTER);
            notification.showWarning();
        }
    }

    @FXML
    void handleAddJourney(ActionEvent event)  {
        ObservableList<WorldMapView.Country> selectedCountry = worldMapView.getSelectedCountries();

        if (journeyTitleTextField.getText().isBlank() || journeyDescriptionTextArea.getText().isBlank()) {
            Notifications notification = buildNotification("Journey Addition Error", "Please fill in all the " +
                    "fields", 6, Pos.BASELINE_CENTER);
            notification.showError();
        }
        // Check whether the user chose only 1 country
        else if (selectedCountry.size() > 1) {
            Notifications notification = buildNotification("Selection Error", "Please choose only 1 country.", 8, Pos.BASELINE_CENTER);
            notification.showError();
        }
        else if (selectedCountry.size() == 0) {
            Notifications notification = buildNotification("Selection Error", "Please choose at least" +
                    " 1 country by left clicking on a country on the map.", 6, Pos.BASELINE_CENTER);
            notification.showError();
        }
        else {

            // Add the journey to the database
            String journeyDesc = journeyDescriptionTextArea.getText();
            String title = journeyTitleTextField.getText();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            String location = selectedCountry.get(0).getLocale().getDisplayCountry();

            Journey j = new Journey(location, title, journeyDesc, start, end);
            if (findJourneyByJourneyTitle(title, currentUser)) {
                Notifications notification = buildNotification("Journey Already Exists", "A journey with the " +
                        "exact same title already exists in your journeys list. I'm afraid we cannot allow this for your pleasure.", 6, Pos.BASELINE_CENTER);
                notification.showError();
            }
            else if (!j.addNewJourney(currentUser)) {
                Notifications notification = buildNotification("Journey Already Exists", "A journey with the " +
                        "exact same specifications already exists in your journeys list. ", 6, Pos.BASELINE_CENTER);
                notification.showError();
            }
            else {
                // Build notification
                Notifications notificationBuilder = buildNotification("Added Journey!", "Journey is successfully " +
                        "added. Journey details: \nCountry: " + j.getLocation() + ", Description: " + j.getDescription() +
                        ", Dates: " + j.getStartDate() + " - " + j.getEndDate(), 6, Pos.BASELINE_CENTER);
                notificationBuilder.showConfirm();
            }
        }
    }

    @FXML
    void openHomePage(ActionEvent event) {
        try {
            // Get the parent and create the scene
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/trajour/main/main.fxml"));
            Parent mainPageParent = loader.load();
            Scene mainPageScene = new Scene(mainPageParent, Main.APPLICATION_WIDTH, Main.APPLICATION_HEIGHT);

            // Get access to the main window controller
            MainController mainWindowController = loader.getController();
            mainWindowController.initData(currentUser);

            // Get the stage and change the scene
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(mainPageScene);
            window.show();
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    @FXML
    void openMapPage(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/trajour/map/mapxz.fxml"));

        try {
            Parent mapPageParent = loader.load();
            Scene mapPageScene = new Scene(mapPageParent, Main.APPLICATION_WIDTH, Main.APPLICATION_HEIGHT);

            // Get access to the map window controller
            MapController mapController = loader.getController();
            mapController.initData(currentUser);

            // Get the stage and change the scene
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(mapPageScene);
            window.show();
        }
        catch (IOException e) {
            e.getCause();
            e.printStackTrace();
        }
    }

    @FXML
    void openProfilePage(ActionEvent event) {
        try {
            // Get the parent and create the scene
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/trajour/profile/profile.fxml"));
            Parent profilePageParent = loader.load();
            Scene profilePageScene = new Scene(profilePageParent, Main.APPLICATION_WIDTH, Main.APPLICATION_HEIGHT);

            // Get access to the profile window controller
            ProfileController profileWindowController = loader.getController();
            profileWindowController.initData(currentUser);

            // Get the stage and change the scene
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(profilePageScene);
            window.show();
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

//

    private ObservableList<WorldMapView.Location> readCountryCapitals() {
        ObservableList<WorldMapView.Location> result = FXCollections.observableArrayList();

        try {
            Scanner in = new Scanner(new File("src/resources/country-capitals.csv"));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] pieces = line.split(",");

                String name = pieces[1];
                double latitude = Double.parseDouble(pieces[2]);
                double altitude = Double.parseDouble(pieces[3]);

                result.add(new WorldMapView.Location(name, latitude, altitude));
            }

        }
        catch (FileNotFoundException e) {
            e.getCause();
            e.printStackTrace();
        }

        return result;
    }
    private double calculateDistanceBetweenTwoLocations(WorldMapView.Location fromLocation, WorldMapView.Location toLocation) {
        double fromLatitude;
        double toLatitude;
        double fromLongitude;
        double toLongitude;
        double deltaLongitude;
        double deltaLatitude;
        double radiusOfEarth;
        double h;
        double distance;

        fromLatitude = Math.toRadians(fromLocation.getLatitude());
        toLatitude =  Math.toRadians(toLocation.getLatitude());
        fromLongitude =  Math.toRadians(fromLocation.getLongitude());
        toLongitude =  Math.toRadians(toLocation.getLongitude());

        // The formula for finding the distance between two locations. For further info check:
        // https://en.wikipedia.org/wiki/Haversine_formula#:~:text=The%20haversine%20formula%20determines%20the,and%20angles%20of%20spherical%20triangles.
        deltaLongitude = toLongitude - fromLongitude;
        deltaLatitude = toLatitude - fromLatitude;
        h = Math.pow(Math.sin(deltaLatitude / 2), 2) + Math.cos(fromLatitude) * Math.cos(toLatitude) * Math.pow(Math.sin(deltaLongitude / 2), 2);
        radiusOfEarth = 6371;

        distance = 2 * radiusOfEarth * Math.asin(Math.sqrt(h));

        return distance;
    }

    private void initButtons() {
        DropShadow shadow = new DropShadow(7, Color.WHITE);
        homePageButton.setOnMouseEntered(mouseEvent -> homePageButton.setEffect(shadow));
        homePageButton.setOnMouseExited(mouseEvent -> homePageButton.setEffect(null));

        mapPageButton.setOnMouseEntered(mouseEvent -> mapPageButton.setEffect(shadow));
        mapPageButton.setOnMouseExited(mouseEvent -> mapPageButton.setEffect(null));

        profilePageButton.setOnMouseEntered(mouseEvent -> profilePageButton.setEffect(shadow));
        profilePageButton.setOnMouseExited(mouseEvent -> profilePageButton.setEffect(null));

        DropShadow blackShadow = new DropShadow();
        addJourneyButton.setOnMouseEntered(mouseEvent -> addJourneyButton.setEffect(blackShadow));
        addJourneyButton.setOnMouseExited(mouseEvent -> addJourneyButton.setEffect(null));

        pickRandomCountryButton.setOnMouseEntered(mouseEvent -> pickRandomCountryButton.setEffect(blackShadow));
        pickRandomCountryButton.setOnMouseExited(mouseEvent -> pickRandomCountryButton.setEffect(null));

        showDistanceButton.setOnMouseEntered(mouseEvent -> showDistanceButton.setEffect(blackShadow));
        showDistanceButton.setOnMouseExited(mouseEvent -> showDistanceButton.setEffect(null));
    }

    private void initWorldMapView() {
        // Set locations to display
        capitals = readCountryCapitals();
        worldMapView.setLocations(capitals);
        showLocationsView = new SimpleBooleanProperty(false);
        worldMapView.setShowLocations(showLocationsView.get());


//        worldMapView.setLocations(capitals);
//
//        showLocationsView.set(false);
//        worldMapView.setShowLocations(showLocationsView.get());

        // Set what happens when clicked on a location or country. The latest chosen location or country is assumed as
        // the travel destination and the location text field is changed accordingly
        worldMapView.setOnMouseClicked(mouseEvent -> {
            if (worldMapView.getSelectedLocations().size() >= 1) {
                if (mouseEvent.getTarget() instanceof SVGPath) {
                    selectedLocationField.setText(worldMapView.getSelectedCountries().get(worldMapView.getSelectedCountries().size() - 1).getLocale().getDisplayCountry());
                    worldMapView.getSelectedLocations().removeAll(worldMapView.getSelectedLocations());
                }
                else {
                    selectedLocationField.setText(worldMapView.getSelectedLocations().get(worldMapView.getSelectedLocations().size() - 1).getName());
                    worldMapView.getSelectedCountries().removeAll(worldMapView.getSelectedCountries());
                }
            }
            if (worldMapView.getSelectedCountries().size() >= 1) {
                selectedLocationField.setText(worldMapView.getSelectedCountries().get(worldMapView.getSelectedCountries().size() - 1).getLocale().getDisplayCountry());
                worldMapView.getSelectedLocations().removeAll(worldMapView.getSelectedLocations());
            }
        });

        // Set the view of locations
        tooltip = new Tooltip();
        worldMapView.setLocationViewFactory(location -> {
            Circle circle = new Circle();
            circle.setRadius(3);
            circle.setTranslateX(-4);
            circle.setTranslateY(-4);
            circle.setOnMouseEntered(mouseEvent -> tooltip.setText(location.getName()));
            Tooltip.install(circle, tooltip);
            return circle;
        });

    }

//   private ObservableList<WorldMapView.Location> readCities(int numberOfCities) {
//        ObservableList<WorldMapView.Location> result = FXCollections.observableArrayList();
//
//        try {
//            Scanner in = new Scanner(new File("/resources/worldcities.csv"));
//
//            int count = 0;
//            while (in.hasNextLine() && count < numberOfCities) {
//                String line = in.nextLine();
//                String[] pieces = line.split(",");
//
//                String cityName = pieces[1].substring(1, pieces[1].length() - 1);
//                double latitude = Double.parseDouble(pieces[2].substring(1, pieces[2].length() - 1));
//                double longitude = Double.parseDouble((pieces[3].substring(1, pieces[3].length() - 1)));
//                String country = pieces[4].substring(1, pieces[4].length() - 1);
//                int population = Integer.parseInt(pieces[pieces.length - 2].substring(1, pieces[pieces.length - 2].length() - 1));
//
//                DetailedLocation detailedLocation = new DetailedLocation(cityName, latitude, longitude, country, population);
//                result.add(detailedLocation);
//
//                count++;
//            }
//        }
//        catch (FileNotFoundException e) {
//            System.out.println("Something wrong with 'worldcities.csv' file");
//            e.printStackTrace();
//            e.getCause();
//        }
//
//        return result;
//    }
}