package cs_26_sw_3_10.P3Project.platform.sql.data;

import java.util.UUID;

/**
 * Users
 */
public class Users extends TableData {
    public UUID id;
    public String name;
    public String password;
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