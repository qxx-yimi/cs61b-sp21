package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
        join(STAGING_REMOVE_DIR, filename).delete();
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
        File branchFile = join(HEADS_DIR, branch);
        if (!branchFile.exists()) {
            throw error("No such branch exists.");
        }
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (branch.equals(currentBranch)) {
            throw error("No need to checkout the current branch.");
        }

        // read current commit
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);

        // read branch commit
        String branchCommitHash = readContentsAsString(branchFile);
        Commit branchCommit = readObject(join(COMMITS_DIR, branchCommitHash), Commit.class);

        for (String fileInCWD : plainFilenamesIn(CWD)) {
            boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots().containsKey(fileInCWD);
            boolean isOverWrittenByBranch = branchCommit.getFileSnapshots().containsKey(fileInCWD);
            if (!isTrackedInCurrentCommit && isOverWrittenByBranch) {
                throw error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        // remove all files in working dirctory
        for (String fileInCWD : plainFilenamesIn(CWD)) {
            join(CWD, fileInCWD).delete();
        }

        // write all files from branch commit to working directory
        for (String fileInBranchCommit : branchCommit.getFileSnapshots().keySet()) {
            String blobHash = branchCommit.getFileSnapshots().get(fileInBranchCommit);
            String fileContent = readContentsAsString(join(OBJECTS_DIR, blobHash));
            writeContents(join(CWD, fileInBranchCommit), fileContent);
        }

        // clear staging area
        for (String stagedFile : plainFilenamesIn(STAGING_ADD_DIR)) {
            join(STAGING_ADD_DIR, stagedFile).delete();
        }
        for (String stagedFile : plainFilenamesIn(STAGING_REMOVE_DIR)) {
            join(STAGING_REMOVE_DIR, stagedFile).delete();
        }

        // HEAD points to the checked out branch
        writeContents(HEAD_FILE, branch);
    }

    private static void printCommitInfo(String commitHash, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (commit.getSecondParent() != null) {
            System.out.println("Merge: " + commit.getParent().substring(0, 7) + " " + commit.getSecondParent().substring(0, 7));
        }
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void log() {
        checkInit();
        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        while (currentCommit != null) {
            printCommitInfo(currentCommitHash, currentCommit);
            if (currentCommit.getParent() == null) {
                break;
            }
            currentCommitHash = currentCommit.getParent();
            currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        }
    }

    public static void rm(String filename) {
        checkInit();
        File stagedForAdditionFile = join(STAGING_ADD_DIR, filename);
        boolean isStagedForAddition = stagedForAdditionFile.exists();

        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots().containsKey(filename);

        if (!isStagedForAddition && !isTrackedInCurrentCommit) {
            throw error("No reason to remove the file.");
        }

        if (isStagedForAddition) {
            stagedForAdditionFile.delete();
        }

        if (isTrackedInCurrentCommit) {
            // stage file for removal
            writeContents(join(STAGING_REMOVE_DIR, filename), "");
            // remove file from working directory
            join(CWD, filename).delete();
        }
    }

    public static void globalLog() {
        checkInit();
        // Iterate through all commits in COMMITS_DIR
        for (String commitHash : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = readObject(join(COMMITS_DIR, commitHash), Commit.class);
            printCommitInfo(commitHash, commit);
        }
    }

    public static void find(String message) {
        checkInit();
        boolean found = false;
        for (String commitHash : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = readObject(join(COMMITS_DIR, commitHash), Commit.class);
            if (commit.getMessage().equals(message)) {
                found = true;
                System.out.println(commitHash);
            }
        }
        if (!found) {
            throw error("Found no commit with that message.");
        }
    }

    public static void status() {
        checkInit();
        // print branches
        String currentBranch = readContentsAsString(HEAD_FILE);
        System.out.println("=== Branches ===");
        for (String branch : plainFilenamesIn(HEADS_DIR)) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        // print staged files for addition
        System.out.println("=== Staged Files ===");
        for (String stagedFile : plainFilenamesIn(STAGING_ADD_DIR)) {
            System.out.println(stagedFile);
        }
        System.out.println();

        // print staged files for removal
        System.out.println("=== Removed Files ===");
        for (String stagedFile : plainFilenamesIn(STAGING_REMOVE_DIR)) {
            System.out.println(stagedFile);
        }
        System.out.println();

        // read current commit
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);

        // print modifications not staged for commit
        System.out.println("=== Modifications Not Staged For Commit ===");
        TreeMap<String, String> modifications = new TreeMap<>();
        // check tracked files in current commit
        for (String trackedFile : currentCommit.getFileSnapshots().keySet()) {
            File fileInCWD = join(CWD, trackedFile);
            if (fileInCWD.exists()) {
                boolean isStagedForAddition = join(STAGING_ADD_DIR, trackedFile).exists();
                boolean unChanged = currentCommit.hasFile(trackedFile, sha1(readContentsAsString(fileInCWD)));
                if (!isStagedForAddition && !unChanged) {
                    modifications.put(trackedFile, "modified");
                }
            } else {
                boolean isStagedForRemoval = join(STAGING_REMOVE_DIR, trackedFile).exists();
                if (!isStagedForRemoval) {
                    modifications.put(trackedFile, "deleted");
                }
            }
        }

        // check files staged for addition
        for (String stagedFile : plainFilenamesIn(STAGING_ADD_DIR)) {
            File fileInCWD = join(CWD, stagedFile);
            if (fileInCWD.exists()) {
                String fileInCWDHash = sha1(readContentsAsString(fileInCWD));
                String stagedFileHash = readContentsAsString(join(STAGING_ADD_DIR, stagedFile));
                if (!fileInCWDHash.equals(stagedFileHash)) {
                    modifications.put(stagedFile, "modified");
                }
            } else {
                modifications.put(stagedFile, "deleted");
            }
        }
        for (String file : modifications.keySet()) {
            System.out.println(file + " (" + modifications.get(file) + ")");
        }
        System.out.println();

        // print untracked files
        System.out.println("=== Untracked Files ===");
        for (String fileInCWD : plainFilenamesIn(CWD)) {
            boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots().containsKey(fileInCWD);
            boolean isStagedForAddition = join(STAGING_ADD_DIR, fileInCWD).exists();
            if (!isTrackedInCurrentCommit && !isStagedForAddition) {
                System.out.println(fileInCWD);
            }
        }
        System.out.println();
    }

    public static void branch(String branchName) {
        checkInit();
        File branchFile = join(HEADS_DIR, branchName);
        if (branchFile.exists()) {
            throw error("A branch with that name already exists.");
        }
        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        // create new branch pointing to current commit
        writeContents(branchFile, currentCommitHash);
    }
}
