// PlaylistManager.java

package Playlists;

import Users.User;
import java.util.ArrayList;

public interface PlaylistManager {

    /**
     * Returnează instanța managerului de playlist-uri.
     *
     * @return Instanța managerului de playlist-uri
     */
    PlaylistManagerImpl getInstance();

    /**
     * Returnează lista de playlist-uri ale unui utilizator dat.
     *
     * @param u Utilizatorul al cărui playlist-uri sunt căutate
     * @return Lista de playlist-uri ale utilizatorului dat
     */
    ArrayList<Playlist> getUserPlaylists(User u);
}
