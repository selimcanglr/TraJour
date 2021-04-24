package trajour.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseQuery {
    private static DatabaseConnection dbConnection;
    private static Connection conn;

    /**
     * Finds a user by checking a certain username in the database.
     * @param username Username to search
     * @return 1 if user exists 0 if the user does not exist
     */
    public static int findUserByUsername(String username) {
        try {
            dbConnection = new DatabaseConnection();
            conn = dbConnection.getConnection();

            Statement statement = conn.createStatement();
            String query = "SELECT COUNT(*) FROM users WHERE username = '" + username + "'";
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                if (rs.getInt(1) == 1) {
                    return 1;
                }
                else {
                    return 0;
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Finds a user by checking a certain email in the database.
     * @param email Email to search
     * @return 1 if the user is found, 0 if the user wasn't found
     */
    public static int findUserByEmail(String email) {
        try {
            dbConnection = new DatabaseConnection();
            conn = dbConnection.getConnection();

            Statement statement = conn.createStatement();
            String query = "SELECT COUNT(*) FROM users where email = '" + email + "'";
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                if (rs.getInt(1) == 1) {
                    System.out.println("Found user by email"); // Might delete later
                    return 1;
                }
                else {
                    System.out.println("Couldn't find user by email"); // Might delete later
                    return 0;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }


    public static int findPasswordByUsername(String username, String password) {
        try {
            dbConnection = new DatabaseConnection();
            conn = dbConnection.getConnection();

            Statement statement = conn.createStatement();
            String query = "SELECT password FROM users where username = '" + username + "'";
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                if (rs.getString(1).equals(password)) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return 0;
    }

    /**
     * Finds a user by checking a certain email in the database.
     * @param email Email to search
     * @return 1 if the user is found, 0 if the user wasn't found
     */
    public static String getUsernameByEmail(String email) {
        try {
            dbConnection = new DatabaseConnection();
            conn = dbConnection.getConnection();

            Statement statement = conn.createStatement();
            String query = "SELECT username FROM users WHERE email = '" + email + "'";
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                return rs.getString(1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void updatePassword(String username, String newPassword) {
        try {
            dbConnection = new DatabaseConnection();
            conn = dbConnection.getConnection();

            Statement statement = conn.createStatement();
            String query = "UPDATE users SET password = '" + newPassword + "' WHERE username = '" + username + "'";
            statement.executeUpdate(query);
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Validates the login attempt by checking the entered email and password in the database.
     * @param email Entered email
     * @param password Entered password
     * @return True if login is successful, false otherwise
     */
    public static boolean validateLogin(String email, String password) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        String verifyLoginQuery = "SELECT COUNT(*) FROM users WHERE email = '" + email + "' AND password = '" + password + "';";
        try {
            Statement statement = conn.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLoginQuery);

            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     *
     * @param username Entered username by the user
     * @param email Entered email by the user
     * @param password Entered password by the user
     * @return 1 if the process is successful, 0 if the process was unsuccessful for some reason (?), -1 if a user with
     * the entered username already exists, -2 if a user with the entered email already exists.
     */
    public static int validateRegistry(String username, String email, String password) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        if (findUserByUsername(username) == 1) {
            return -1;
        }
        if (findUserByEmail(email) == 1) {
            return -2;
        }

        String insertFieldsQuery = "INSERT INTO users(username, email, password) ";
        String insertValuesQuery = "VALUES ('" + username + "', '" + email + "', '" + password + "');";
        String registerQuery = insertFieldsQuery + insertValuesQuery;

        try {
            Statement query = conn.createStatement();
            int result = query.executeUpdate(registerQuery);
            if (result >= 1) {
                return 1;
            } else {
                return 0;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return 0;
    }

    /**
     *
     * @param friendName Name of the searched friend
     * @return 1 if a friend with the given username exists, 0 otherwise
     */
    public static int findFriendByUsername(String friendName) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        String query = "SELECT COUNT(*) friendName FROM users INNER JOIN friends ON users.userId = friends.userID WHERE "
                + "friends.friendName = '" + friendName + "'";
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                e.getCause();
            }
        }

        return 0;
    }

    public static int findFriendByEmail(String email) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        String query = "SELECT COUNT(*) friendName FROM users INNER JOIN friends ON users.userId = friends.userID WHERE "
                + "friends.friendEmail = '" + email + "'";
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                e.getCause();
            }
        }

        return 0;
    }

    public static void insertFriendByUsername(String friendName, String username) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        String query = "INSERT INTO friends(userId, friendName, friendEmail) VALUES(" + getUserIdByName(username) + ", "
                + "'" + friendName + "', " + "'" + getEmailByUsername(friendName) + "'";
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                e.getCause();
            }
        }
    }

    public static void insertFriendByEmail(String email, String username) {
        dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection();

        String query = "INSERT INTO friends(userId, friendName, friendEmail) VALUES(" + getUserIdByName(username) + ", "
                + "'" + friendName + "', " + "'" + email + "'";
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
        finally {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                e.getCause();
            }
        }
    }
}
