package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

/**
 * Represents a gitlet commit object.
 *
 * @author onemeter
 */
public class Commit implements Serializable {

    /** The commit message. */
    private String message;

    /** The commit timestamp. */
    private String timestamp;

    /** The parent commit SHA-1 hash. */
    private String parent;

    /** The second parent commit SHA-1 hash (for merges). */
    private String secondParent;

    /** File snapshots in this commit: filename -> blob SHA-1 hash. */
    private HashMap<String, String> fileSnapshots;

    public Commit(String message, Date timestamp, String parent,
                  String secondParent, HashMap<String, String> fileSnapshots) {
        this.message = message;
        Formatter fmt = new Formatter(Locale.US);
        fmt.format("%ta %tb %td %tT %tY %Tz", timestamp, timestamp, timestamp,
                timestamp, timestamp, timestamp);
        this.timestamp = fmt.toString();
        fmt.close();
        this.parent = parent;
        this.secondParent = secondParent;
        this.fileSnapshots = fileSnapshots;
    }

    public boolean hasFile(String fileName, String hashValue) {
        return fileSnapshots.containsKey(fileName) && fileSnapshots.get(fileName).equals(hashValue);
    }

    public HashMap<String, String> getFileSnapshots() {
        return fileSnapshots;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getParent() {
        return parent;
    }

    public String getSecondParent() {
        return secondParent;
    }
}
