import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Scanner;

public class RoomBookingSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/floor_plans";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    /**
     * Main method to run the room booking system.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            updateRoomStatus(connection);
            showAvailableRooms(connection);
            bookRoom(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the room status by setting isBooked to false for rooms that have been freed up to the current time.
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    private static void updateRoomStatus(Connection connection) throws SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Timestamp currentTimestamp = Timestamp.valueOf(currentDateTime);
        //Before showing available rooms, update the rooms that have been freed till now
        String updateQuery = "UPDATE rooms SET isBooked = false WHERE room_id IN (SELECT room_id FROM bookings WHERE end_datetime <= ?)";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setTimestamp(1, currentTimestamp);
            updateStatement.executeUpdate();
        }
    }

    /**
     * Shows available rooms based on minimum capacity required.
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    private static void showAvailableRooms(Connection connection) throws SQLException {
        System.out.println("Enter minimum capacity required:");
        Scanner scanner = new Scanner(System.in);
        int minCapacity = scanner.nextInt();

        String query = "SELECT room_id, room_name, capacity, cost_per_hour, projector_available, floor FROM rooms WHERE capacity >= ? AND isBooked = false ORDER BY capacity";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, minCapacity);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("Available Rooms:");
            System.out.println("Room ID | Room Name | Capacity | Cost Per Hour | Floor | Projector Available");
            while (resultSet.next()) {
                int roomId = resultSet.getInt("room_id");
                String roomName = resultSet.getString("room_name");
                int capacity = resultSet.getInt("capacity");
                double costPerHour = resultSet.getDouble("cost_per_hour");
                int floor = resultSet.getInt("floor");
                boolean projectorAvailable = resultSet.getBoolean("projector_available");
                System.out.println(roomId + "   |   " + roomName + "    |   " + capacity + "    |   " + costPerHour + "    |   " + floor + "   |   " + projectorAvailable);
            }
        }
    }

    /**
     * Books a room based on user input for room ID, start date and time, and end date and time.
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    private static void bookRoom(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Room ID to book:");
        int selectedRoomId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter start date and time (yyyy-MM-dd HH:mm:ss):");
        String startDateTimeStr = scanner.nextLine();
        System.out.println(startDateTimeStr);
        Timestamp startDateTime = Timestamp.valueOf(startDateTimeStr);

        System.out.println("Enter end date and time (yyyy-MM-dd HH:mm:ss):");
        String endDateTimeStr = scanner.nextLine();
        Timestamp endDateTime = Timestamp.valueOf(endDateTimeStr);

        String bookQuery = "INSERT INTO bookings (room_id, admin_id, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement bookStatement = connection.prepareStatement(bookQuery)) {
            bookStatement.setInt(1, selectedRoomId);
            bookStatement.setInt(2, 1); // Assuming admin ID is 1 for simplicity
            bookStatement.setTimestamp(3, startDateTime);
            bookStatement.setTimestamp(4, endDateTime);
            bookStatement.executeUpdate();
            System.out.println("Room booked successfully.");
        }
    }
}
