package cs_26_sw_3_10.P3Project.platform.sql.data;

import java.util.UUID;

/**
 * Java container for the "Users" table elements in the SQL database
 */
public class Users extends TableData {
    /**
     * The unique user id
     */
    public UUID id;
    /**
     * The name of the user
     */
    public String name;
    /**
     * The sha256 encrypted password (Is required to be exactly 66 characters long)
     */
    public String password;
    /**
     * Authenticator application token
     */
    public String totpsecret;

    public Users(UUID id, String name, String password, String totpsecret) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.totpsecret = totpsecret;
    }

    public Users(String id, String name, String password, String totpsecret) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.password = password;
        this.totpsecret = totpsecret;
    }

}