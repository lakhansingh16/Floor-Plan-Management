import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class FloorPlan {
    private static final String DB_HOST = "localhost";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";
    private static final String DB_NAME = "floor_plans";

    /**
     * Connects to the MySQL database.
     *
     * @return Connection object if successful, null otherwise
     * @throws SQLException if a database access error occurs
     */
    private static Connection connectToDatabase() throws SQLException {
        String dbURL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME;
        return DriverManager.getConnection(dbURL, DB_USER, DB_PASSWORD);
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(password.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticates an admin based on the provided username and password.
     *
     * @param conn     the database connection
     * @param username the admin's username
     * @param password the admin's password
     * @return true if authentication is successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private static boolean authenticate(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT password FROM admins WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (password.equals(storedPassword)) {
                    System.out.println("User '" + username + "' authenticated successfully");
                    return true;
                } else {
                    System.out.println("Incorrect password for user '" + username + "'");
                    return false;
                }
            } else {
                System.out.println("User '" + username + "' not found");
                return false;
            }
        }
    }
    public static void uploadRoom() {
        Scanner scanner = new Scanner(System.in);
        String dbURL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME;
        try (Connection conn = DriverManager.getConnection(dbURL, DB_USER, DB_PASSWORD)) {
            //Take room data input if connection to database was successful
            System.out.println("Welcome to the room data upload. Kindly provide all the details\n");
            System.out.println("Room name/number: ");
            String roomName = scanner.next();
            System.out.println("Room capacity: ");
            int capacity = scanner.nextInt();
            System.out.println("Cost of the room booking per hour: ");
            int costPerHour = scanner.nextInt();
            System.out.println("Is projector available?(Y?N) ");
            String projector = scanner.next();
            boolean projectorAvailable=false;
            if(projector.toLowerCase().equals("y")){
                projectorAvailable=true;
            }
            System.out.println("Which floor is the room located on? ");
            int floor=scanner.nextInt();
            //create MySQL query from the given data and insert the data in the database
            String query = "INSERT INTO rooms (room_name, capacity, cost_per_hour, projector_available, floor) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, roomName);
                stmt.setInt(2, capacity);
                stmt.setDouble(3, costPerHour);
                stmt.setBoolean(4, projectorAvailable);
                stmt.setInt(5, floor);
                stmt.executeUpdate();
                System.out.println("Room details uploaded successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error uploading room details: " + e.getMessage());
        }
    }
    /**
     * Main method to run the floor plan management system.
     *
     * @param args the command-line arguments
     * */
    public static void main(String[] args) {
        try (Connection conn = connectToDatabase()) {
            if (conn != null) {
                System.out.println("Connected to the database.");

                Scanner scanner = new Scanner(System.in);
                System.out.println("Welcome to the floor management app!");
                System.out.println("Enter Username:");
                String username = scanner.nextLine();
                System.out.println("Enter Password:");
                String password = scanner.nextLine();
                // Hash the password
                String hashedPassword = hashPassword(password);

                // Authenticate the admin
                if (!authenticate(conn, username, hashedPassword)){
                    System.out.println("Authentication failed. Exiting application.");
                }

                //After successful authentication, admins can either upload rooms data, or book meeting rooms for a party
                int choice = 0;
                do{
                    System.out.println("Please select an option:\n1.Upload rooms data\n2.Book Meeting room\n");
                    choice = scanner.nextInt();
                }while(choice<1 || choice>2);

                if(choice==1){
                    //call uploadRoom function to insert new room data into database
                    uploadRoom();
                }else if(choice==2){
                    RoomBookingSystem R = new RoomBookingSystem();
                    R.main(args);
                }
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
