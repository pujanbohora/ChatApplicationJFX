package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user in the chat application.
 * This class encapsulates user information such as username, IP address, online status, and avatar.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String ipAddress;
    private boolean isOnline;
    private String avatar;

    /**
     * Default constructor
     */
    public User() {
        this.username = "Anonymous";
        this.ipAddress = "127.0.0.1";
        this.isOnline = false;
        this.avatar = "default_avatar.png";
    }

    /**
     * Constructor with username and IP address
     *
     * @param username the user's name
     * @param ipAddress the user's IP address
     */
    public User(String username, String ipAddress) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.isOnline = false;
        this.avatar = "default_avatar.png";
    }

    /**
     * Constructor with all parameters
     *
     * @param username the user's name
     * @param ipAddress the user's IP address
     * @param isOnline the user's online status
     * @param avatar the user's avatar file path
     */
    public User(String username, String ipAddress, boolean isOnline, String avatar) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.isOnline = isOnline;
        this.avatar = avatar;
    }

    /**
     * Gets the username
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the IP address
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address
     *
     * @param ipAddress the IP address to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Checks if the user is online
     *
     * @return true if the user is online, false otherwise
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Sets the online status
     *
     * @param online the online status to set
     */
    public void setOnline(boolean online) {
        isOnline = online;
    }

    /**
     * Gets the avatar file path
     *
     * @return the avatar file path
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar file path
     *
     * @param avatar the avatar file path to set
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Compares this user to another object for equality
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(username, user.username) &&
                Objects.equals(ipAddress, user.ipAddress);
    }

    /**
     * Generates a hash code for this user
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, ipAddress);
    }

    /**
     * Returns a string representation of this user
     *
     * @return a string representation of this user
     */
    @Override
    public String toString() {
        return username + " (" + (isOnline ? "Online" : "Offline") + ")";
    }
}
