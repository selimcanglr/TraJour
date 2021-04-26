package trajour.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import trajour.db.DatabaseConnection;
import trajour.model.User;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import static trajour.db.DatabaseQuery.*;

/**
 * Controller for the login process
 * @author Selim Can Güler
 * @version 16 April 2021
 */
public class LoginController implements Initializable {
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink learnMoreHyperlink;

    @FXML
    private Label loginFeedbackLabel;

    private DatabaseConnection dbConnection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dbConnection = new DatabaseConnection();
        // Button effects
        DropShadow shadow = new DropShadow();

        // Event listeners for text fields and buttons
        emailTextField.requestFocus();

        loginButton.setOnMouseEntered(mouseEvent -> loginButton.setEffect(shadow));
        loginButton.setOnMouseExited(mouseEvent -> loginButton.setEffect(null));

        registerButton.setOnMouseEntered(mouseEvent -> registerButton.setEffect(shadow));
        registerButton.setOnMouseExited(mouseEvent -> registerButton.setEffect(null));

        passwordField.setOnAction((ActionEvent e) -> {
            if (!emailTextField.getText().isBlank() && !passwordField.getText().isBlank()) {
                handleLogin(e);
            }
            else {
                loginFeedbackLabel.setText("Please enter your email and password.");
            }
        });

        emailTextField.setOnAction((ActionEvent e) -> {
            if (!emailTextField.getText().isBlank() && !passwordField.getText().isBlank()) {
                handleLogin(e);
            }
            else {
                loginFeedbackLabel.setText("Please enter your email and password.");
            }
        });
    }

    /**
     * Handles and validates login switches to the main page if the login is successful
     * @param event Event
     */
    public void handleLogin(ActionEvent event)  {
        String email = emailTextField.getText();
        String password = passwordField.getText();
        // Check if the text fields are empty or not
        if ( ! email.isBlank() && !  password.isBlank()) {
            if (validateLogin(email, password)) {
                // TODO Wait for a few seconds so that the user can understand login is successful, then redirect to the the main page
                String username = getUsernameByEmail(email);
                System.out.println(username);
                openMainPage(event, new User(getUserIdByUsername(username), username, email));
            }
            else {
                loginFeedbackLabel.setText("Incorrect email or password.");
            }
        }
        else {
            loginFeedbackLabel.setText("Please enter your email and password.");
        }
    }

    /**
     * Creates a new register stage
     * @param event Event
     */
    @FXML
    private void openRegisterPage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load((getClass().getResource("/trajour/view/fxml/register.fxml")));

            Stage registerStage = new Stage();
            registerStage.setTitle("Register");

            registerStage.setScene(new Scene(root, 800, 600));
            registerStage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the main page
     * @param event Event
     */
    private void openMainPage(ActionEvent event, User user)  {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/trajour/view/fxml/main.fxml"));
            Parent mainPageParent = loader.load();
            Scene mainPageScene = new Scene(mainPageParent, Main.APPLICATION_WIDTH, Main.APPLICATION_HEIGHT);

            MainController mainWindowController = loader.getController();
            mainWindowController.initData(user);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(mainPageScene);
            window.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the app website
     * @param event Event
     */
    @FXML
    private void openTheWebsite(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("http://a.yilmazyildiz.ug.bilkent.edu.tr/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
