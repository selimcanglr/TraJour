package com.trajour.profile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import com.trajour.user.User;
import javafx.scene.effect.DropShadow;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangePasswordController implements Initializable {


    @FXML
    private PasswordField oldPasswordTextField;

    @FXML
    private PasswordField newPasswordTextField;

    @FXML
    private PasswordField reEnteredNewPasswordTextField;

    @FXML
    private Button changeButton;

    @FXML
    private Label feedbackLabel;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Init buttons
        DropShadow shadow = new DropShadow();
        changeButton.setOnMouseEntered(mouseEvent -> changeButton.setEffect(shadow));
        changeButton.setOnMouseExited(mouseEvent -> changeButton.setEffect(null));
    }

    public void initData(User user) {
        currentUser = user;
    }

    @FXML
    void changePassword(ActionEvent event) {
        if ( ! oldPasswordTextField.getText().isBlank() && ! newPasswordTextField.getText().isBlank() && ! reEnteredNewPasswordTextField.getText().isBlank()) {
            if (! newPasswordTextField.getText().equals(reEnteredNewPasswordTextField.getText())) {
                feedbackLabel.setText("Your new passwords do not match.");
            }
            else {
                if (currentUser.updatePassword(oldPasswordTextField.getText(), newPasswordTextField.getText())) {
                    feedbackLabel.setText("Password successfully changed.");
                }
                else {
                    feedbackLabel.setText("Your old password does not match.");
                }
            }
        }
        else {
            feedbackLabel.setText("Please fill in all the text fields");
        }
    }
}