package Users;

import java.sql.SQLException;

public interface UserManager {

    /**
     * Înregistrează un nou utilizator în sistem.
     *
     * @param username Numele de utilizator al utilizatorului nou
     * @param password Parola utilizatorului nou
     */
    void registerUser(String username, String password) throws SQLException;

    /**
     * Autentifică un utilizator în sistem.
     *
     * @param username Numele de utilizator al utilizatorului care încearcă să se autentifice
     * @param password Parola utilizatorului care încearcă să se autentifice
     */

    User loginUser(String username, String password);

    /**
     * Atribuie rolul de admin unui user
     * @param admin Administratorul ce atribuie rolul
     * @param username Username-ul ce devine admin
     */
    void promoteToAdmin(User admin, String username);
}
