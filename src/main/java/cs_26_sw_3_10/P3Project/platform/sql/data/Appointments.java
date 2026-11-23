package cs_26_sw_3_10.P3Project.platform.sql.data;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Appointments
 */
public class Appointments extends TableData {
    public UUID id;
    public UUID clientId;
    public Timestamp start;
    public Timestamp end;
    public Boolean notify;

    public Appointments(UUID id, UUID clientId, Timestamp start, Timestamp end, Boolean notify) {
        this.id = id;
        this.clientId = clientId;
        this.start = start;
        this.end = end;
        this.notify = notify;
    }
    public Appointments(String id, String clientId, Timestamp start, Timestamp end, Boolean notify) {
        this.id = UUID.fromString(id);
        this.clientId = UUID.fromString(clientId);
        this.start = start;
        this.end = end;
        this.notify = notify;
    }
    public Appointments(String id, UUID clientId, Timestamp start, Timestamp end, Boolean notify) {
        this.id = UUID.fromString(id);
        this.clientId = clientId;
        this.start = start;
        this.end = end;
        this.notify = notify;
    }
    public Appointments(UUID id, String clientId, Timestamp start, Timestamp end, Boolean notify) {
        this.id = id;
        this.clientId = UUID.fromString(clientId);
        this.start = start;
        this.end = end;
        this.notify = notify;
    }

}