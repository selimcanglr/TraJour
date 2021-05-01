package com.trajour.journey;

import com.trajour.db.DatabaseQuery;
import com.trajour.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.File;

import static com.trajour.db.DatabaseQuery.insertPost;
import static com.trajour.db.DatabaseQuery.updateImageOfPost;


/**
 * A simple Java class!
*/
public class Post extends GridPane implements Shareable {
    // Properties
    private String text;
    private Journey theJourney;
    private Image journeyPhoto;

    // Components
    private ImageView journeyPhotoView;
    private ImageView userPhotoView;
    private Label journeyLocationLabel;
    private Label usernameLabel;
    private Label dateLabel;
    private Label commentLabel;
    private Label journeyNameLabel;
    private VBox userVBox;
    private ColumnConstraints cc;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Journey getTheJourney() {
        return theJourney;
    }

    // Constructor
    public Post( Journey theJourney, String text, Image journeyPhoto ) {
        this.theJourney = theJourney;
        this.text = text;
        this.journeyPhoto = journeyPhoto;
        journeyPhotoView = new ImageView();
        userPhotoView = new ImageView();
        userVBox = new VBox();
        cc = new ColumnConstraints();
        cc.setFillWidth(true);
        cc.setHgrow(Priority.ALWAYS);
        this.setGridLinesVisible(true);
        this.setPadding(new Insets(20));
        this.setMinWidth(USE_COMPUTED_SIZE);
        this.setMaxWidth(USE_COMPUTED_SIZE);
        this.setPrefWidth(USE_COMPUTED_SIZE);
        for (int i = 0; i < 4; i++) {
            this.getColumnConstraints().add(cc);
        }

    }

    @Override
    public boolean share(User user, VBox mainFeed) {
        journeyLocationLabel = new Label("Location: " + theJourney.getLocation());
        usernameLabel = new Label(user.getUsername());
        commentLabel = new Label("Comment: " + text);
        dateLabel = new Label("Date: " + theJourney.getStartDate() + " / " + theJourney.getEndDate());
        journeyNameLabel = new Label("Name: " + theJourney.getTitle());
        journeyPhotoView.setImage(journeyPhoto);
        userPhotoView.setImage(new Image(DatabaseQuery.getProfilePhotoFile(user).toURI().toString(), 40, 40, false, false));
        userVBox.getChildren().add(userPhotoView);
        userVBox.getChildren().add(usernameLabel);

        journeyLocationLabel.setFont(new Font("Arial Bold", 12));
        journeyNameLabel.setFont(new Font("Arial Bold", 12));
        usernameLabel.setFont(new Font("Arial Bold", 12));
        commentLabel.setFont(new Font("Arial Bold", 12));
        dateLabel.setFont(new Font("Arial Bold", 12));

        add(userVBox, 0, 0);
        add(journeyNameLabel, 0, 1);
        add(journeyLocationLabel, 1, 1);
        add(dateLabel, 2, 1);
        add(journeyPhotoView, 3, 1);
        add(commentLabel, 4, 1);

        mainFeed.getChildren().add(this);

        insertPost(user, this);
        return true;
    }

    public boolean updatePostImage(File file, User user, String journeyTitle) {
        if (!updateImageOfPost(file, user, journeyTitle)) {
            return false;
        }
        else {
            updateImageOfPost(file, user, journeyTitle);
            return true;
        }
    }

}