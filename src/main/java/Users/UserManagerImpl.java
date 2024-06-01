package Users;

import DBConnector.DBConnector;
import Playlists.PlaylistManagerImpl;
import Songs.Song;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.example.Exceptions.ExistsException;

public class UserManagerImpl implements UserManager {

    @Getter @Setter private List<User> users;
    private final DBConnector connector = DBConnector.getInstance();
    private static UserManagerImpl instance = null;
    private PlaylistManagerImpl playlistManager;

    private UserManagerImpl() {
        createUsersTable();
        users = new ArrayList<>();
        loadUsersFromDatabase();
    }

    public static UserManagerImpl getInstance() {
        if (instance == null) instance = new UserManagerImpl();
        return instance;
    }

    public void registerUser(String username, String password) throws SQLException {

        // Adăugăm utilizatorul nou
        User newUser = new User(username, password, false);
        saveUser(newUser);

        // Dacă este primul utilizator, îl facem administrator
        if (users.size() == 1) {
            newUser.promoteToAdmin();
            System.out.println("Registered account with user name " + username + " (Admin)");
        } else {
            System.out.println("Registered account with user name " + username);
        }
    }

    public User loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                System.out.println("You are now authenticated as " + username);
                return user;
            }
        }

        System.out.println("Username or password is invalid. Please try again!");
        return null;
    }

    public void promoteToAdmin(User admin, String username) {
        if (!admin.isAdmin()) {
            System.out.println("You do not have the permission to use this!");
            return;
        }

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.promoteToAdmin();
                System.out.println(username + " is now an administrator!");
                return;
            }
        }

        System.out.println("Specified user does not exist!");
    }

    private ArrayList<Song> get_all_songs() {
        String selectSql = "SELECT * FROM songs;";
        ArrayList<Song> songs = new ArrayList<>();

        try (Statement statement = connector.getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(selectSql)) {
                while (resultSet.next()) {
                    Song song = new Song();
                    song.setId(resultSet.getInt("id")); // Setează ID-ul melodiei
                    song.setName(resultSet.getString("name"));
                    song.setArtist(resultSet.getString("artist"));
                    song.setReleaseYear(resultSet.getInt("releaseYear"));
                    songs.add(song);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return songs;
    }

    private void createUsersTable() {
        try (Statement stmt = connector.getConnection().createStatement()) {
            String sql =
                    "CREATE TABLE IF NOT EXISTS users "
                            + "(username VARCHAR(255) PRIMARY KEY, "
                            + "password TEXT NOT NULL, "
                            + "isAdmin INTEGER NOT NULL)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(User u) throws SQLException {
        String selectSql = "SELECT * from users where username=(?)";
        try (PreparedStatement s = connector.getConnection().prepareStatement(selectSql)) {
            s.setString(1, u.getUsername());
            ResultSet r = s.executeQuery();
            if (r.next()) {
                throw new ExistsException("Userul " + u.getUsername() + " deja exista");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement statement =
                connector
                        .getConnection()
                        .prepareStatement(
                                "INSERT INTO users (username, password, isAdmin) VALUES (?, ?,"
                                        + " ?)")) {
            statement.setString(1, u.getUsername());
            statement.setString(2, u.getPassword());
            statement.setBoolean(3, u.isAdmin());
            statement.executeUpdate();
            users.add(u);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadUsersFromDatabase() {
        users.clear();
        try (Statement stmt = connector.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                boolean isAdmin = rs.getInt("isAdmin") == 1;
                User user = new User(username, password, false);
                if (isAdmin) {
                    user.promoteToAdmin();
                }
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadUsersFromCSV(String csvFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] nextLine;
            // Skip the header line
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                String username =
                        nextLine[0].trim(); // Elimină spațiile din ambele capete ale șirului
                String password =
                        nextLine[1].trim(); // Elimină spațiile din ambele capete ale șirului
                boolean isAdmin =
                        Boolean.parseBoolean(
                                nextLine[2]
                                        .trim()); // Elimină spațiile din ambele capete ale șirului
                User user = new User(username, password, isAdmin);
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveUsersToCSV(String csvFilePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            for (User user : users) {
                String[] userData = {
                    user.getUsername(), user.getPassword(), user.isAdmin() ? "true" : "false"
                };
                writer.writeNext(userData, false); // Setează applyQuotesToAll pe false
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUsersFromJSON(String jsonFilePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            User[] userArray = mapper.readValue(jsonContent, User[].class);
            for (User user : userArray) {
                try {
                    saveUser(user);
                    System.out.println("Userul " + user.getUsername() + " a fost adaugat din json");
                } catch (ExistsException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveUsersToJSON(String jsonFilePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            ObjectWriter writer = mapper.writer();
            loadUsersFromDatabase();
            writer.writeValue(new FileWriter(jsonFilePath), users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
