package Playlists;

import Songs.Song;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Playlist {
    private int id;
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        } else {
            System.out.println(
                    "The song "
                            + song.getName()
                            + " by "
                            + song.getArtist()
                            + " is already part of "
                            + name);
        }
    }

    @Override
    public String toString() {
        return name + " [ID: " + id + "]";
    }
}
