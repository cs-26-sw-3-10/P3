package cs_26_sw_3_10.P3Project.platform.sql.data;

import java.security.Timestamp;
import java.util.UUID;

/**
 * JournalEntries
 */
public class JournalEntries extends TableData {
    public UUID id;
    public UUID clientId;
    public String content;
    public Timestamp created;
    public Timestamp modified;

    public JournalEntries(UUID id, UUID clientId, String content, Timestamp created, Timestamp modified) {
        this.id = id;
        this.clientId = clientId;
        this.content = content;
        this.created = created;
        this.modified = modified;
    }
    public JournalEntries(String id, String clientId, String content, Timestamp created, Timestamp modified) {
        this.id = UUID.fromString(id);
        this.clientId = UUID.fromString(clientId);
        this.content = content;
        this.created = created;
        this.modified = modified;
    }public JournalEntries(String id, UUID clientId, String content, Timestamp created, Timestamp modified) {
        this.id = UUID.fromString(id);
        this.clientId = clientId;
        this.content = content;
        this.created = created;
        this.modified = modified;
    }public JournalEntries(UUID id, String clientId, String content, Timestamp created, Timestamp modified) {
        this.id = id;
        this.clientId = UUID.fromString(clientId);
        this.content = content;
        this.created = created;
        this.modified = modified;
    }
}