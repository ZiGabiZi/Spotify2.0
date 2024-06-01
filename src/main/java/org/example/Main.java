package org.example;

import Audit.AuditService;
import DBConnector.DBConnector;
import Playlists.Playlist;
import Playlists.PlaylistManagerImpl;
import Songs.SongsManagerImpl;
import Users.User;
import Users.UserManagerImpl;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import org.example.Exceptions.ErrorException;
import org.example.Exceptions.ExistsException;
import org.example.Exceptions.NonExistantException;

public class Main {
    public static void main(String[] args) throws SQLException {

        DBConnector db = DBConnector.getInstance();
        try (Connection c =
                db.connectToDatabase("jdbc:mysql://localhost:3306/users", "toor", "2004")) {
            UserManagerImpl userManager = UserManagerImpl.getInstance();
            AuditService auditService = new AuditService();
            System.out.println(userManager.getUsers());
            SongsManagerImpl songsManager = SongsManagerImpl.getInstance();
            PlaylistManagerImpl playlistManager = PlaylistManagerImpl.getInstance();
            Scanner scanner = new Scanner(System.in);
            User currentUser = null;
            while (true) {
                if (auditService.isAuditMode()) {
                    System.out.println("Options: nextpage, close_auditlog");
                } else {
                    if (currentUser == null) {
                        System.out.println("Options: login, register, exit");
                    } else if (currentUser.isAdmin()) {
                        System.out.println(
                                "Options: logout, promote, exit, auditlog, create song, list songs,"
                                        + " create playlist, list playlists, add song to playlist,"
                                        + " savejson, loadjson");
                    } else {
                        System.out.println(
                                "Options: logout, exit, list songs, create playlist, list"
                                        + " playlists, add song to playlist");
                    }
                }

                System.out.print("Enter command: ");
                String command = scanner.nextLine();
                boolean commandSuccess = false;

                switch (command.toLowerCase()) {
                    case "login":
                        if (currentUser != null) {
                            System.out.println("Already logged in as " + currentUser.getUsername());
                        } else {
                            System.out.print("Enter username: ");
                            String username = scanner.nextLine();
                            System.out.print("Enter password: ");
                            String password = scanner.nextLine();
                            currentUser = userManager.loginUser(username, password);
                            commandSuccess = (currentUser != null);
                        }
                        break;

                    case "logout":
                        if (currentUser == null) {
                            System.out.println("No user is currently logged in.");
                        } else {
                            currentUser = null;
                            System.out.println("Successfully logged out.");
                            commandSuccess = true;
                        }
                        break;

                    case "register":
                        if (currentUser != null) {
                            System.out.println("Already logged in. Please logout first.");
                        } else {
                            System.out.print("Enter username: ");
                            String newUsername = scanner.nextLine();
                            System.out.print("Enter password: ");
                            String newPassword = scanner.nextLine();
                            try {
                                userManager.registerUser(newUsername, newPassword);
                                commandSuccess = true;
                            } catch (ExistsException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                        break;

                    case "promote":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter username to promote: ");
                            String promoteUsername = scanner.nextLine();
                            userManager.promoteToAdmin(currentUser, promoteUsername);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "loadcsv":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter file path: ");
                            String loadCsvPath = scanner.nextLine();
                            userManager.loadUsersFromCSV(loadCsvPath);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "savecsv":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter file path: ");
                            String saveCsvPath = scanner.nextLine();
                            userManager.saveUsersToCSV(saveCsvPath);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "loadjson":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter file path: ");
                            String loadJsonPath = scanner.nextLine();
                            userManager.loadUsersFromJSON(loadJsonPath);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "savejson":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter file path: ");
                            String saveJsonPath = scanner.nextLine();
                            userManager.saveUsersToJSON(saveJsonPath);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "auditlog":
                        if (currentUser != null && currentUser.isAdmin()) {
                            commandSuccess = true;
                            System.out.print("How many items per page: ");
                            int itemsPerPage = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            auditService.startAuditLogPagination(itemsPerPage);

                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "nextpage":
                        if (currentUser != null && currentUser.isAdmin()) {
                            auditService.showNextPage();
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "close_auditlog":
                        if (currentUser != null && currentUser.isAdmin()) {
                            auditService.closeAuditLog();
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "create song":
                        if (currentUser != null && currentUser.isAdmin()) {
                            System.out.print("Enter song name: ");
                            String songName = scanner.nextLine();
                            System.out.print("Enter artist name: ");
                            String artistName = scanner.nextLine();
                            System.out.print("Enter release year: ");
                            int releaseYear = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            songsManager.createSong(songName, artistName, releaseYear);
                            commandSuccess = true;
                        } else {
                            System.out.println("You do not have the permission to use this!");
                        }
                        break;

                    case "list songs":
                        if (currentUser != null) {
                            songsManager.listSongs();
                            commandSuccess = true;
                        } else {
                            System.out.println("You need to be logged in to use this!");
                        }
                        break;

                    case "create playlist":
                        if (currentUser != null) {
                            try {
                                System.out.print("Enter playlist name: ");
                                String playlistName = scanner.nextLine();
                                playlistManager.createPlaylist(playlistName, currentUser);
                                commandSuccess = true;
                            } catch (ExistsException e) {
                                System.err.println(e.getMessage());
                            } catch (ErrorException e) {
                                System.err.println("Eroare SQL" + e.getMessage());
                            }

                        } else {
                            System.out.println("You need to be logged in to use this!");
                        }
                        break;

                    case "list playlists":
                        if (currentUser != null) {
                            currentUser.listPlaylists();
                            System.out.println("Options: view_songs, back");
                            System.out.print("Enter command: ");
                            String playlistCommand = scanner.nextLine();
                            switch (playlistCommand.toLowerCase()) {
                                case "view_songs":
                                    try {
                                        System.out.print(
                                                "Give playlist name (1) or playlist id (2): ");
                                        String playlistSpecifier = scanner.nextLine();
                                        if (playlistSpecifier.equals("1")) {
                                            System.out.print("Give playlist name: ");
                                            String playlistName = scanner.nextLine();
                                            // Afiseaza melodiile din playlist dupa nume
                                            playlistManager.viewSongsInPlaylistByName(playlistName);
                                        } else if (playlistSpecifier.equals("2")) {
                                            System.out.print("Give playlist id: ");
                                            String playlistId = scanner.nextLine();
                                            // Afiseaza melodiile din playlist dupa id
                                            playlistManager.viewSongsInPlaylistById(
                                                    Integer.parseInt(playlistId));
                                        } else {
                                            System.out.println("Invalid command.");
                                        }
                                        commandSuccess = true;
                                    } catch (NonExistantException e) {
                                        System.err.println(e.getMessage());
                                    }
                                    break;
                                case "back":
                                    break; // Nu facem nimic pentru că întoarcerea la meniul
                                    // principal este implicită
                                default:
                                    System.out.println("Invalid command.");
                            }
                        } else {
                            System.out.println("You need to be logged in to use this!");
                        }
                        break;

                    case "add song to playlist":
                        System.out.println("Choose option: 1) byId, 2) byName");
                        int option = Integer.parseInt(scanner.nextLine());

                        if (option == 1) {
                            System.out.print("Enter playlist ID: ");
                            int playlistId = Integer.parseInt(scanner.nextLine());
                            System.out.print("Enter song IDs (space-separated): ");
                            String songIdsInput = scanner.nextLine();
                            String[] songIds = songIdsInput.split("\\s+");

                            for (String songId : songIds) {
                                playlistManager.addSongToPlaylist(
                                        playlistId, Integer.parseInt(songId));
                            }
                        } else if (option == 2) {
                            System.out.print("Enter playlist name: ");
                            String playlistName = scanner.nextLine();
                            System.out.print("Enter song IDs (space-separated): ");
                            String songIdsInput = scanner.nextLine();
                            String[] songIds = songIdsInput.split("\\s+");

                            Playlist playlist = playlistManager.findPlaylistByName(playlistName);
                            if (playlist != null) {
                                try {
                                    for (String songId : songIds) {
                                        playlistManager.addSongToPlaylist(
                                                playlist.getId(), Integer.parseInt(songId));
                                    }
                                    commandSuccess = true;
                                } catch (NonExistantException e) {
                                    System.err.println(e.getMessage());
                                }
                            } else {
                                System.out.println("The desired playlist does not exist.");
                            }
                        } else {
                            System.out.println("Invalid option.");
                        }
                        break;

                    case "exit":
                        System.out.println("Exiting the application.");
                        scanner.close();
                        System.exit(0);

                    default:
                        if (!auditService.isAuditMode()) {
                            System.out.println("Invalid command. Please try again.");
                        }
                }

                if (currentUser != null && !auditService.isAuditMode()) {
                    auditService.logCommand(currentUser.getUsername(), command, commandSuccess);
                }
            }
        }
    }
}
