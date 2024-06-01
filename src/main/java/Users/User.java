package Users;

import Playlists.Playlist;
import Songs.Song;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
public class User {
    @Getter private String username;
    @Getter private String password;

    @JsonProperty("isAdmin")
    private boolean isadmin;

    @Getter private List<Playlist> playlists;

    @JsonCreator
    public User(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("isadmin") boolean admin) {
        this.username = username;
        this.password = password;
        this.isadmin = admin;
        this.playlists = new ArrayList<>();
    }

    public void listPlaylists() {
        if (playlists.isEmpty()) {
            System.out.println("You have no playlists.");
            return;
        }
        for (Playlist playlist : playlists) {
            System.out.println(playlist);
        }
    }

    public void addSongToPlaylist(String playlistName, Song song) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equalsIgnoreCase(playlistName)) {
                playlist.addSong(song);
                System.out.println(
                        "Added "
                                + song.getName()
                                + " by "
                                + song.getArtist()
                                + " to "
                                + playlistName);
                return;
            }
        }
        System.out.println("The desired playlist does not exist.");
    }

    public void addPlaylist(Playlist p) {
        playlists.add(p);
    }

    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isadmin;
    }

    public void promoteToAdmin() {
        this.isadmin = true;
    }
}
