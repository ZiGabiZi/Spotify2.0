// SongsManager.java

package Songs;

public interface SongsManager {
    /**
     * Găsește o melodie după numele și artistul său.
     *
     * @param name   Numele melodiei
     * @param artist Artistul melodiei
     */
    Song findSong(String name, String artist);

    /**
     * Găsește o melodie după ID-ul său.
     *
     * @param songId ID-ul melodiei
     */
    Song findSongById(int songId);

    /**
     * Creează o nouă melodie și o adaugă în bibliotecă.
     *
     * @param name        Numele melodiei
     * @param artist      Artistul melodiei
     * @param releaseYear Anul de lansare al melodiei
     */
    void createSong(String name, String artist, int releaseYear);

    /**
     * Găsește o melodie după ID-ul său.
     *
     * @param id ID-ul melodiei
     */
    Song getSongById(int id);
}
