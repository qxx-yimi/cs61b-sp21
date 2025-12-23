package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a gitlet repository.
 *
 * @author onemeter
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** .gitlet subdirectories and files */
    /** The HEAD file pointing to current branch. */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** The local branches directory. */
    public static final File HEADS_DIR = join(GITLET_DIR, "refs", "heads");

    /** The objects directory for storing blobs. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** The commits directory for storing commit objects. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** The staging/add directory for files staged for addition. */
    public static final File STAGING_ADD_DIR = join(GITLET_DIR, "staging", "add");

    /** The staging/remove directory for files staged for removal. */
    public static final File STAGING_REMOVE_DIR = join(GITLET_DIR, "staging", "remove");

    private static void checkInit() {
        if (!GITLET_DIR.exists()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }

        GITLET_DIR.mkdir();
        HEADS_DIR.mkdirs();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGING_ADD_DIR.mkdirs();
        STAGING_REMOVE_DIR.mkdirs();

        // Create initial commit and persist it
        Commit initialCommit = new Commit("initial commit", new Date(0), null, null, new HashMap<>());
        String hashValue = sha1(serialize(initialCommit));
        writeObject(join(COMMITS_DIR, hashValue), initialCommit);

        // Initialize master branch
        writeContents(join(HEADS_DIR, "master"), hashValue);

        // HEAD points to master
        writeContents(HEAD_FILE, "master");
    }

    public static void add(String filename) {
        checkInit();
        File fileToAdd = join(CWD, filename);
        if (!fileToAdd.exists()) {
            throw error("File does not exist.");
        }

        // persist object blob
        String fileContent = readContentsAsString(fileToAdd);
        String hashValue = sha1(fileContent);
        writeContents(join(OBJECTS_DIR, hashValue), fileContent);

        // stage file for addition
        writeContents(join(STAGING_ADD_DIR, filename), hashValue);

        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);

        // if file is tracked in current commit and unchanged, unstage it
        if (currentCommit.hasFile(filename, hashValue)) {
            join(STAGING_ADD_DIR, filename).delete();
        }

        // if file is staged for removal, unstage it
        File stagedForRemoval = join(STAGING_REMOVE_DIR, filename);
        if (stagedForRemoval.exists()) {
            stagedForRemoval.delete();
        }
    }

    private static boolean isStagingEmpty() {
        List<String> adds = plainFilenamesIn(STAGING_ADD_DIR);
        List<String> removes = plainFilenamesIn(STAGING_REMOVE_DIR);
        return (adds == null || adds.isEmpty()) && (removes == null || removes.isEmpty());
    }

    public static void commit(String message) {
        checkInit();
        if (message.trim().isEmpty()) {
            throw error("Please enter a commit message.");
        }
        if (isStagingEmpty()) {
            throw error("No changes added to the commit.");
        }

        // copy file snapshots from current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        HashMap<String, String> newFileSnapshots = new HashMap<>(currentCommit.getFileSnapshots());

        // apply staged additions and delete staging files
        for (String stagedFile : plainFilenamesIn(STAGING_ADD_DIR)) {
            String blobHash = readContentsAsString(join(STAGING_ADD_DIR, stagedFile));
            newFileSnapshots.put(stagedFile, blobHash);
            join(STAGING_ADD_DIR, stagedFile).delete();
        }

        // apply staged removals and delete staging files
        for (String stagedFile : plainFilenamesIn(STAGING_REMOVE_DIR)) {
            newFileSnapshots.remove(stagedFile);
            join(STAGING_REMOVE_DIR, stagedFile).delete();
        }

        // create new commit
        Commit newCommit = new Commit(message, new Date(), currentCommitHash, null, newFileSnapshots);
        String newCommitHash = sha1(serialize(newCommit));
        writeObject(join(COMMITS_DIR, newCommitHash), newCommit);

        // update current branch to point to new commit
        writeContents(join(HEADS_DIR, currentBranch), newCommitHash);
    }

    private static void checkoutFileFromCommit(Commit commit, String filename) {
        if (!commit.getFileSnapshots().containsKey(filename)) {
            throw error("File does not exist in that commit.");
        } else {
            String blobHash = commit.getFileSnapshots().get(filename);
            String fileContent = readContentsAsString(join(OBJECTS_DIR, blobHash));
            writeContents(join(CWD, filename), fileContent);
        }
    }

    public static void checkoutHeadCommitFile(String filename) {
        checkInit();
        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        checkoutFileFromCommit(currentCommit, filename);
    }

    public static void checkoutCommitFile(String commitHash, String filename) {
        checkInit();
        File commitFile = join(COMMITS_DIR, commitHash);
        if (!commitFile.exists()) {
            throw error("No commit with that id exists.");
        }
        Commit commit = readObject(commitFile, Commit.class);
        checkoutFileFromCommit(commit, filename);
    }

    public static void checkoutBranch(String branch) {
        checkInit();
    }

    public static void log() {
        checkInit();
        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommitHash);
            if (currentCommit.getSecondParent() != null) {
                System.out.println("Merge: " + currentCommit.getParent().substring(0, 7) + " " + currentCommit.getSecondParent().substring(0, 7));
            }
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();

            if (currentCommit.getParent() == null) {
                break;
            }
            currentCommitHash = currentCommit.getParent();
            currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        }
    }
}
