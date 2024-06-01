package Playlists;

import DBConnector.DBConnector;
import Songs.Song;
import Songs.SongsManagerImpl;
import Users.User;
import Users.UserManagerImpl;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.Exceptions.ErrorException;
import org.example.Exceptions.ExistsException;
import org.example.Exceptions.NonExistantException;

public class PlaylistManagerImpl {
    private final DBConnector connector = DBConnector.getInstance();
    private static PlaylistManagerImpl instance = null;
    private final SongsManagerImpl songsService = SongsManagerImpl.getInstance();
    private final List<User> users;
    private final List<Song> songs;

    private PlaylistManagerImpl() {
        createPlaylistsTable();
        createPlaylistSongsTable();
        UserManagerImpl userService = UserManagerImpl.getInstance();
        users = userService.getUsers();
        songs = songsService.getSongs();
        loadPlaylistsFromDatabase();
    }

    public static PlaylistManagerImpl getInstance() {
        if (instance == null) instance = new PlaylistManagerImpl();
        return instance;
    }

    private void createPlaylistsTable() {
        try (Statement stmt = connector.getConnection().createStatement()) {
            String sql =
                    "CREATE TABLE IF NOT EXISTS playlists "
                            + "(id INTEGER PRIMARY KEY AUTO_INCREMENT, "
                            + "name TEXT NOT NULL, "
                            + "username VARCHAR(255) NOT NULL, "
                            + "FOREIGN KEY (username) REFERENCES users(username))";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylistsFromDatabase() {
        try (Statement stmt = connector.getConnection().createStatement()) {
            Map<Integer, ArrayList<Song>> playlistSongs = new HashMap<>();
            ResultSet rs = stmt.executeQuery("SELECT * FROM playlists");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String username = rs.getString("username");
                try (PreparedStatement ps =
                        connector
                                .getConnection()
                                .prepareStatement(
                                        "SELECT * from playlist_songs where playlist_id=(?)")) {
                    ps.setInt(1, id);
                    ResultSet rs2 = ps.executeQuery();
                    while (rs2.next()) {
                        if (!playlistSongs.containsKey(id)) {
                            playlistSongs.put(id, new ArrayList<>());
                        }
                        playlistSongs.get(id).add(songsService.getSongById(rs2.getInt("song_id")));
                    }
                }
                for (User u : users)
                    if (u.getUsername().equalsIgnoreCase(username)) {
                        u.addPlaylist(new Playlist(rs.getInt("id"), name, playlistSongs.get(id)));
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createPlaylistSongsTable() {
        try (Statement stmt = connector.getConnection().createStatement()) {
            String sql =
                    "CREATE TABLE IF NOT EXISTS playlist_songs "
                            + "(playlist_id INTEGER, "
                            + "song_id INTEGER, "
                            + "FOREIGN KEY (playlist_id) REFERENCES playlists(id), "
                            + "FOREIGN KEY (song_id) REFERENCES songs(id), "
                            + "PRIMARY KEY (playlist_id, song_id))";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlaylist(String playlistName, User u) {
        Playlist p = savePlaylistToDatabase(playlistName, u);
        u.addPlaylist(p);

        System.out.println("Playlist " + playlistName + " was created successfully!");
    }

    private void checkIfPlaylistExists(String playlistName, User u) {
        String selectSql = "SELECT * from playlists where upper(name) like (?) and username=(?)";
        try (PreparedStatement s = connector.getConnection().prepareStatement(selectSql)) {
            s.setString(1, playlistName.toUpperCase());
            s.setString(2, u.getUsername());
            ResultSet r = s.executeQuery();
            if (r.next()) {
                throw new ExistsException("Ai deja un playlist creat cu acel nume");
            }
        } catch (SQLException e) {
            throw new ErrorException(e.getMessage());
        }
    }

    private Playlist savePlaylistToDatabase(String playlistName, User u) {
        checkIfPlaylistExists(playlistName, u);
        Playlist p = new Playlist(playlistName);
        try (PreparedStatement pstmt =
                connector
                        .getConnection()
                        .prepareStatement("INSERT INTO playlists (name, username) VALUES (?, ?)")) {
            pstmt.setString(1, playlistName);
            pstmt.setString(2, u.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    private void saveSongToPlaylist(int playlistId, int songId) {
        // Log the playlistId and songId
        System.out.println(
                "Attempting to insert playlist_id: " + playlistId + " and song_id: " + songId);

        // Check if the playlist_id exists in the playlists table
        try (PreparedStatement checkStmt =
                connector
                        .getConnection()
                        .prepareStatement("SELECT id FROM playlists WHERE id = ?")) {
            checkStmt.setInt(1, playlistId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.err.println(
                        "Playlist ID " + playlistId + " does not exist in the playlists table.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Insert the song into the playlist_songs table
        try (PreparedStatement pstmt =
                connector
                        .getConnection()
                        .prepareStatement(
                                "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?,"
                                        + " ?)")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Playlist> getUserPlaylists(User u) throws SQLException {
        ArrayList<Playlist> playlists = new ArrayList<>();
        try (PreparedStatement pstmt =
                connector
                        .getConnection()
                        .prepareStatement(
                                "Select * from playlists where upper(username) = upper((?))")) {
            pstmt.setString(1, u.getUsername());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                Playlist p = findPlaylistById(id);
                if (p != null) playlists.add(p);
            }
            return playlists;
        } catch (SQLException e) {
            throw new ErrorException("Eroare SQL" + e.getMessage());
        }
    }

    private void loadSongsForPlaylist(Playlist playlist, User u) {
        try (PreparedStatement pstmt =
                connector
                        .getConnection()
                        .prepareStatement("SELECT * FROM playlist_songs WHERE playlist_id = (?)")) {
            pstmt.setInt(1, playlist.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int songId = rs.getInt("song_id");
                Song song = songsService.findSongById(songId);
                if (song != null) {
                    playlist.addSong(song);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSongToPlaylist(int playlistId, int songId) {
        Playlist playlist = findPlaylistById(playlistId);
        Song song = songsService.getSongById(songId);

        if (playlist != null && song != null) {
            if (playlist.getSongs() == null) playlist.setSongs(new ArrayList<>());
            if (!playlist.getSongs().contains(song)) {
                playlist.addSong(song);
                saveSongToPlaylist(playlistId, songId);
                System.out.println(
                        "Added \""
                                + song.getName()
                                + "\" by "
                                + song.getArtist()
                                + " to "
                                + playlist.getName()
                                + ".");
            } else {
                System.out.println(
                        "The song \""
                                + song.getName()
                                + "\" by "
                                + song.getArtist()
                                + " is already part of \""
                                + playlist.getName()
                                + "\".");
            }
        } else {
            if (playlist == null) {
                throw new NonExistantException("Nu exista playlist-ul cu ID-ul" + playlistId);
            }
            throw new NonExistantException("Nu exista melodia cu ID-ul" + songId);
        }
    }

    public Playlist findPlaylistByName(String playlistName) {
        for (User user : users) {
            for (Playlist playlist : user.getPlaylists()) {
                if (playlist.getName().equalsIgnoreCase(playlistName)) {
                    return playlist;
                }
            }
        }
        return null; // Returnăm null dacă playlist-ul nu este găsit
    }

    public Playlist findPlaylistById(int playlistId) {
        for (User user : users) {
            for (Playlist playlist : user.getPlaylists()) {
                if (playlist.getId() == playlistId) {
                    return playlist;
                }
            }
        }
        return null; // Returnăm null dacă playlist-ul nu este găsit
    }

    public void viewSongsInPlaylistById(int playlistId) {
        Playlist p = findPlaylistById(playlistId);
        if (p == null) throw new NonExistantException("Nu exista playlist cu ID-ul dat");
        if (p.getSongs() == null) throw new NonExistantException("Playlistul nu are melodii");
        for (Song s : p.getSongs()) System.out.println(s);
    }

    public void viewSongsInPlaylistByName(String playlistName) {
        Playlist p = findPlaylistByName(playlistName);
        if (p == null) throw new NonExistantException("Nu exista playlist cu numele dat");
        if (p.getSongs() == null) throw new NonExistantException("Playlistul nu are melodii");
        for (Song s : p.getSongs()) System.out.println(s);
    }
}
