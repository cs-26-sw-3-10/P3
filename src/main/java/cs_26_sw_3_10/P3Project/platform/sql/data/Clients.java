package cs_26_sw_3_10.P3Project.platform.sql.data;

import java.util.UUID;

/**
 * Clients
 */
public class Clients extends TableData {
    public UUID id;
    public UUID userId;
    public String name;
    public String cpr;
    public String phone;
    public String email;
    public Integer houseNum;
    public Integer roomNum;
    public Integer postalCode;
    public String street;
    public String city;
    public Boolean notify;

    public Clients(UUID id, UUID userId, String name, String cpr, String phone, String email, Integer houseNum,
            Integer roomNum, Integer postalCode, String street, String city, Boolean notify) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.cpr = cpr;
        this.phone = phone;
        this.email = email;
        this.houseNum = houseNum;
        this.roomNum = roomNum;
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
        this.notify = notify;
    }
    public Clients(String id, String userId, String name, String cpr, String phone, String email, Integer houseNum,
            Integer roomNum, Integer postalCode, String street, String city, Boolean notify) {
        this.id = UUID.fromString(id);
        this.userId = UUID.fromString(userId);
        this.name = name;
        this.cpr = cpr;
        this.phone = phone;
        this.email = email;
        this.houseNum = houseNum;
        this.roomNum = roomNum;
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
        this.notify = notify;
    }
    public Clients(UUID id, String userId, String name, String cpr, String phone, String email, Integer houseNum,
            Integer roomNum, Integer postalCode, String street, String city, Boolean notify) {
        this.id = id;
        this.userId = UUID.fromString(userId);
        this.name = name;
        this.cpr = cpr;
        this.phone = phone;
        this.email = email;
        this.houseNum = houseNum;
        this.roomNum = roomNum;
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
        this.notify = notify;
    }
    public Clients(String id, UUID userId, String name, String cpr, String phone, String email, Integer houseNum,
            Integer roomNum, Integer postalCode, String street, String city, Boolean notify) {
        this.id = UUID.fromString(id);
        this.userId = userId;
        this.name = name;
        this.cpr = cpr;
        this.phone = phone;
        this.email = email;
        this.houseNum = houseNum;
        this.roomNum = roomNum;
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
        this.notify = notify;
    }
}