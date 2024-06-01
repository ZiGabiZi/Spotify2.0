package Songs;

import DBConnector.DBConnector;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.example.Exceptions.ErrorException;
import org.example.Exceptions.ExistsException;
import org.example.Exceptions.NonExistantException;

public class SongsManagerImpl {
    @Setter @Getter private ArrayList<Song> songs;
    private final DBConnector connector = DBConnector.getInstance();
    private static SongsManagerImpl instance = null;

    private SongsManagerImpl() {
        createSongsTable();
        this.songs = new ArrayList<>();
        loadSongsFromDatabase();
    }

    public Song getSongById(int id) {
        for (Song s : songs) if (s.getId() == id) return s;
        return null;
    }

    public static SongsManagerImpl getInstance() {
        if (instance == null) {
            instance = new SongsManagerImpl();
        }
        return instance;
    }

    public Song findSong(String name, String artist) {
        return songs.stream()
                .filter(
                        s ->
                                s.getName().equalsIgnoreCase(name)
                                        && s.getArtist().equalsIgnoreCase(artist))
                .findFirst()
                .orElse(null);
    }

    public Song findSongById(int songId) {
        return songs.stream().filter(song -> song.getId() == songId).findFirst().orElse(null);
    }

    public void createSong(String name, String artist, int releaseYear) {
        try {
            checkIfSongExists(name, artist, releaseYear);
        } catch (ExistsException | ErrorException e) {
            System.err.println(e.getMessage());
        }

        Song newSong = new Song(name, artist, releaseYear);
        saveSongToDatabase(newSong);
        newSong.setId(getSongId(name, artist, releaseYear));
        songs.add(newSong);
        System.out.println("Added " + name + " by " + artist + " to the library.");
    }

    public void listSongs() {
        for (Song s : songs)
            System.out.println(
                    s.getArtist()
                            + "  - "
                            + s.getName()
                            + " ("
                            + s.getReleaseYear()
                            + ")"
                            + " ["
                            + s.getId()
                            + "]");
    }

    private int getSongId(String name, String artist, int releaseYear) {
        String selectSql =
                "SELECT * from songs where upper(name) like (?) and upper(artist) like (?) and"
                        + " releaseYear = (?)";
        try (PreparedStatement s = connector.getConnection().prepareStatement(selectSql)) {
            s.setString(1, name.toUpperCase());
            s.setString(2, artist.toUpperCase());
            s.setInt(3, releaseYear);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                return r.getInt("id");
            }
            throw new NonExistantException("Nu exista melodia");
        } catch (SQLException e) {
            throw new ErrorException(e.getMessage());
        }
    }

    private void loadSongsFromDatabase() {
        songs.clear();
        try (Statement stmt = connector.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM songs");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String artist = rs.getString("artist");
                int releaseYear = rs.getInt("releaseYear");
                songs.add(new Song(id, name, artist, releaseYear));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveSongToDatabase(Song song) {
        try (PreparedStatement pstmt =
                connector
                        .getConnection()
                        .prepareStatement(
                                "INSERT INTO songs (name, artist, releaseYear) VALUES (?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, song.getName());
            pstmt.setString(2, song.getArtist());
            pstmt.setInt(3, song.getReleaseYear());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createSongsTable() {
        try (Statement stmt = connector.getConnection().createStatement()) {
            String sql =
                    "CREATE TABLE IF NOT EXISTS songs "
                            + "(id INTEGER PRIMARY KEY AUTO_INCREMENT, "
                            + "name TEXT NOT NULL, "
                            + "artist TEXT NOT NULL, "
                            + "releaseYear INTEGER NOT NULL)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkIfSongExists(String name, String artist, int releaseYear) {
        String selectSql =
                "SELECT * from songs where upper(name) like (?) and upper(artist) like (?) and"
                        + " releaseYear = (?)";
        try (PreparedStatement s = connector.getConnection().prepareStatement(selectSql)) {
            s.setString(1, name.toUpperCase());
            s.setString(2, artist.toUpperCase());
            s.setInt(3, releaseYear);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                throw new ExistsException("Exista deja aceasta melodie");
            }
        } catch (SQLException e) {
            throw new ErrorException(e.getMessage());
        }
    }
}
