package bigdata.cdc.model;

import java.io.Serializable;

public enum EventType implements Serializable {
    // sql 语句类型
    BEGIN,
    COMMIT,
    INSERT,
    UPDATE,
    DELETE;

    private static final long serialVersionUID = 1L;

    public static EventType getEventType(String message) {
        if (EventType.INSERT.name().equalsIgnoreCase(message)) {
            return EventType.INSERT;
        } else if (EventType.DELETE.name().equalsIgnoreCase(message)) {
            return EventType.DELETE;
        } else if (EventType.UPDATE.name().equalsIgnoreCase(message)) {
            return EventType.UPDATE;
        } else if (EventType.BEGIN.name().equalsIgnoreCase(message)) {
            return EventType.BEGIN;
        } else if (EventType.COMMIT.name().equalsIgnoreCase(message)) {
            return EventType.COMMIT;
        } else {
            throw new IllegalArgumentException("unsupported event:" + message);
        }
    }
}
