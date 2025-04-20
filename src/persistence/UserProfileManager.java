package persistence;

import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file operations for user profile persistence.
 * This class provides methods to save and load user profiles.
 */
public class UserProfileManager {
    private static final String PROFILES_DIRECTORY = "data/profiles";

    /**
     * Constructor that ensures the profiles directory exists
     */
    public UserProfileManager() {
        // Create profiles directory if it doesn't exist
        File profilesDir = new File(PROFILES_DIRECTORY);
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
    }

    /**
     * Saves a user profile to a file
     *
     * @param user the user to save
     * @throws IOException if an I/O error occurs
     */
    public void saveUserProfile(User user) throws IOException {
        String filename = user.getUsername().replaceAll("[^a-zA-Z0-9]", "_") + ".profile";
        File profileFile = new File(PROFILES_DIRECTORY, filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(profileFile))) {
            writer.write("username=" + user.getUsername());
            writer.newLine();
            writer.write("ipAddress=" + user.getIpAddress());
            writer.newLine();
            writer.write("isOnline=" + user.isOnline());
            writer.newLine();
            writer.write("avatar=" + user.getAvatar());
            writer.newLine();
        }
    }

    /**
     * Loads a user profile from a file
     *
     * @param username the username of the profile to load
     * @return the loaded user, or null if not found
     * @throws IOException if an I/O error occurs
     */
    public User loadUserProfile(String username) throws IOException {
        String filename = username.replaceAll("[^a-zA-Z0-9]", "_") + ".profile";
        File profileFile = new File(PROFILES_DIRECTORY, filename);

        if (!profileFile.exists()) {
            return null;
        }

        String loadedUsername = username;
        String ipAddress = "127.0.0.1";
        boolean isOnline = false;
        String avatar = "default_avatar.png";

        try (BufferedReader reader = new BufferedReader(new FileReader(profileFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;

                String key = parts[0];
                String value = parts[1];

                switch (key) {
                    case "username":
                        loadedUsername = value;
                        break;
                    case "ipAddress":
                        ipAddress = value;
                        break;
                    case "isOnline":
                        isOnline = Boolean.parseBoolean(value);
                        break;
                    case "avatar":
                        avatar = value;
                        break;
                }
            }
        }

        return new User(loadedUsername, ipAddress, isOnline, avatar);
    }

    /**
     * Gets a list of all saved user profiles
     *
     * @return a list of usernames
     */
    public List<String> getSavedUsernames() {
        List<String> usernames = new ArrayList<>();
        File profilesDir = new File(PROFILES_DIRECTORY);

        if (profilesDir.exists() && profilesDir.isDirectory()) {
            File[] profileFiles = profilesDir.listFiles((dir, name) -> name.endsWith(".profile"));

            if (profileFiles != null) {
                for (File file : profileFiles) {
                    String filename = file.getName();
                    // Remove .profile extension
                    String username = filename.substring(0, filename.length() - 8);
                    usernames.add(username);
                }
            }
        }

        return usernames;
    }

    /**
     * Deletes a user profile
     *
     * @param username the username of the profile to delete
     * @return true if the profile was deleted, false otherwise
     */
    public boolean deleteUserProfile(String username) {
        String filename = username.replaceAll("[^a-zA-Z0-9]", "_") + ".profile";
        File profileFile = new File(PROFILES_DIRECTORY, filename);

        return profileFile.exists() && profileFile.delete();
    }
}
