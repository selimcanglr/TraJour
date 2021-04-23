package trajour.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import trajour.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.Statement;

public class Main extends Application {
    protected final static int APPLICATION_WIDTH = 1400;
    protected final static int APPLICATION_HEIGHT = 1000;
    private final String dbName = "trajour";

    @Override
    public void init() {
        // Create the database if it does not exists
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection conn = dbConnection.getConnection();
        try {
            Statement statement = conn.createStatement();
            statement.executeQuery("CREATE DATABASE IF NOT EXISTS '" + dbName + "'");
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        // TODO Create tables: users, journeys, friends, posts.
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/trajour/view/fxml/loginPage.fxml"));
        primaryStage.setTitle("TraJour");
        primaryStage.setScene(new Scene(root, APPLICATION_WIDTH, APPLICATION_HEIGHT));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
