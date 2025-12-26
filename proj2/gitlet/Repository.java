package gitlet;

import java.io.File;

import static gitlet.Utils.*;

import java.util.*;

/**
 * Represents a gitlet repository.
 *
 * @author onemeter
 */
public class Repository {

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** .gitlet subdirectories and files */
    /**
     * The HEAD file pointing to current branch.
     */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /**
     * The local branches directory.
     */
    public static final File HEADS_DIR = join(GITLET_DIR, "refs", "heads");

    /**
     * The remote repository directory.
     */
    public static final File REMOTES_REPO_DIR = join(GITLET_DIR, "remotes");

    /**
     * The remote branches directory.
     */
    public static final File REMOTES_HEADS_DIR = join(GITLET_DIR, "refs", "remotes");

    /**
     * The objects directory for storing blobs.
     */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /**
     * The commits directory for storing commit objects.
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /**
     * The staging/add directory for files staged for addition.
     */
    public static final File STAGING_ADD_DIR = join(GITLET_DIR, "staging", "add");

    /**
     * The staging/remove directory for files staged for removal.
     */
    public static final File STAGING_REMOVE_DIR = join(GITLET_DIR, "staging", "remove");

    private static void checkInit() {
        if (!GITLET_DIR.exists()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already "
                    + "exists in the current directory.");
        }

        GITLET_DIR.mkdir();
        HEADS_DIR.mkdirs();
        REMOTES_REPO_DIR.mkdir();
        REMOTES_HEADS_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGING_ADD_DIR.mkdirs();
        STAGING_REMOVE_DIR.mkdirs();

        // Create initial commit and persist it
        Commit initialCommit = new Commit("initial commit", new Date(0),
                null, null, new HashMap<>());
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

    public static void commit(String message, String secondParentHash) {
        checkInit();
        if (isStagingEmpty()) {
            throw error("No changes added to the commit.");
        }
        if (message.trim().isEmpty()) {
            throw error("Please enter a commit message.");
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
        Commit newCommit = new Commit(message, new Date(), currentCommitHash,
                secondParentHash, newFileSnapshots);
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

    private static String getFullCommitHash(String partialHash) {
        String fullHash = partialHash;
        for (String commitFileName : plainFilenamesIn(COMMITS_DIR)) {
            if (commitFileName.startsWith(partialHash)) {
                fullHash = commitFileName;
                break;
            }
        }
        return fullHash;
    }

    public static void checkoutCommitFile(String commitHash, String filename) {
        checkInit();
        commitHash = getFullCommitHash(commitHash);
        File commitFile = join(COMMITS_DIR, commitHash);
        if (!commitFile.exists()) {
            throw error("No commit with that id exists.");
        }
        Commit commit = readObject(commitFile, Commit.class);
        checkoutFileFromCommit(commit, filename);
    }

    private static boolean hasUntrackedAndOverrideFiles(Commit currentCommit, Commit branchCommit) {
        for (String fileInCWD : plainFilenamesIn(CWD)) {
            boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots()
                    .containsKey(fileInCWD);
            boolean isOverWrittenByBranch = branchCommit.getFileSnapshots().containsKey(fileInCWD);
            if (!isTrackedInCurrentCommit && isOverWrittenByBranch) {
                return true;
            }
        }
        return false;
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

        if (hasUntrackedAndOverrideFiles(currentCommit, branchCommit)) {
            throw error("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
        }

        // remove files from working directory if file in current commit
        for (String fileInCurrentCommit : currentCommit.getFileSnapshots().keySet()) {
            join(CWD, fileInCurrentCommit).delete();
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
            System.out.println("Merge: " + commit.getParent().substring(0, 7) + " "
                    + commit.getSecondParent().substring(0, 7));
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

    private static void statusExtra() {
        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
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
                boolean unChanged = currentCommit.hasFile(trackedFile,
                        sha1(readContentsAsString(fileInCWD)));
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
            boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots().
                    containsKey(fileInCWD);
            boolean isStagedForAddition = join(STAGING_ADD_DIR, fileInCWD).exists();
            if (!isTrackedInCurrentCommit && !isStagedForAddition) {
                System.out.println(fileInCWD);
            }
        }
        System.out.println();
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

        statusExtra();
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

    public static void rmBranch(String branchName) {
        checkInit();
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            throw error("A branch with that name does not exist.");
        }
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (branchName.equals(currentBranch)) {
            throw error("Cannot remove the current branch.");
        }
        branchFile.delete();
    }

    public static void reset(String commitHash) {
        checkInit();
        commitHash = getFullCommitHash(commitHash);
        File commitFile = join(COMMITS_DIR, commitHash);
        if (!commitFile.exists()) {
            throw error("No commit with that id exists.");
        }
        String originalBranch = readContentsAsString(HEAD_FILE);
        // checkout commit
        File branchFile = join(HEADS_DIR, commitHash);
        writeContents(branchFile, commitHash);
        checkoutBranch(commitHash);
        branchFile.delete();
        // update current branch to point to commit
        writeContents(join(HEADS_DIR, originalBranch), commitHash);
        writeContents(HEAD_FILE, originalBranch);
    }

    private static HashSet<String> getAllAncestors(String commitHash, File gitletDir) {
        HashSet<String> ancestors = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.offer(commitHash);
        while (!queue.isEmpty()) {
            String currentHash = queue.poll();
            ancestors.add(currentHash);
            updateQueue(queue, currentHash, gitletDir);
        }
        return ancestors;
    }

    private static String findSplitPoint(String currentCommitHash, String branchCommitHash) {
        HashSet<String> branchCommitAncestors = getAllAncestors(branchCommitHash, GITLET_DIR);
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.offer(currentCommitHash);
        while (!queue.isEmpty()) {
            String commitHash = queue.poll();
            if (branchCommitAncestors.contains(commitHash)) {
                return commitHash;
            }
            updateQueue(queue, commitHash, GITLET_DIR);
        }
        return null;
    }

    private static void updateQueue(ArrayDeque<String> queue, String commitHash, File gitletDir) {
        Commit commit = readObject(join(gitletDir, "commits", commitHash), Commit.class);
        String parentHash = commit.getParent();
        String secondParentHash = commit.getSecondParent();
        if (parentHash != null) {
            queue.offer(parentHash);
        }
        if (secondParentHash != null) {
            queue.offer(secondParentHash);
        }
    }

    private static void handleConflict(String currentBlobHash, String branchBlobHash,
                                       String fileInSplitPoint) {
        String currentContent = currentBlobHash == null ? ""
                : readContentsAsString(join(OBJECTS_DIR, currentBlobHash));
        String branchContent = branchBlobHash == null ? ""
                : readContentsAsString(join(OBJECTS_DIR, branchBlobHash));
        String mergedContent = "<<<<<<< HEAD\n" + currentContent + "=======\n"
                + branchContent + ">>>>>>>\n";
        writeContents(join(CWD, fileInSplitPoint), mergedContent);
        String mergedBlobHash = sha1(mergedContent);
        writeContents(join(OBJECTS_DIR, mergedBlobHash), mergedContent);
        // stage merged file for addition
        writeContents(join(STAGING_ADD_DIR, fileInSplitPoint), mergedBlobHash);
    }

    private static boolean handleMerge(String currentCommitHash, String branchCommitHash,
                                       String splitPointCommitHash) {
        boolean isConflict = false;
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);
        Commit branchCommit = readObject(join(COMMITS_DIR, branchCommitHash), Commit.class);
        Commit splitPointCommit = readObject(join(COMMITS_DIR, splitPointCommitHash), Commit.class);
        // for each file in split point commit
        for (String fileInSplitPoint : splitPointCommit.getFileSnapshots().keySet()) {
            String splitPointBlobHash = splitPointCommit.getFileSnapshots().get(fileInSplitPoint);
            String currentBlobHash = currentCommit.getFileSnapshots().get(fileInSplitPoint);
            String branchBlobHash = branchCommit.getFileSnapshots().get(fileInSplitPoint);
            boolean unChangedInCurrent = currentBlobHash != null
                    && currentBlobHash.equals(splitPointBlobHash);
            boolean changedInCurrent = currentBlobHash != null
                    && !currentBlobHash.equals(splitPointBlobHash);
            boolean changedInBranch = branchBlobHash != null
                    && !branchBlobHash.equals(splitPointBlobHash);
            if (unChangedInCurrent && changedInBranch) {
                // checkout file from branch commit
                checkoutCommitFile(branchCommitHash, fileInSplitPoint);
                // stage file for addition
                writeContents(join(STAGING_ADD_DIR, fileInSplitPoint), branchBlobHash);
            }
            if (unChangedInCurrent && branchBlobHash == null) {
                rm(fileInSplitPoint);
            }
            if (changedInCurrent && branchBlobHash == null
                    || changedInBranch && currentBlobHash == null
                    || (changedInBranch && changedInCurrent
                    && !branchBlobHash.equals(currentBlobHash))) {
                handleConflict(currentBlobHash, branchBlobHash, fileInSplitPoint);
                isConflict = true;
            }
        }
        // for each file in branch commit
        for (String fileInBranch : branchCommit.getFileSnapshots().keySet()) {
            String splitPointBlobHash = splitPointCommit.getFileSnapshots().get(fileInBranch);
            String currentBlobHash = currentCommit.getFileSnapshots().get(fileInBranch);
            String branchBlobHash = branchCommit.getFileSnapshots().get(fileInBranch);
            // file only in branch commit
            if (splitPointBlobHash == null && currentBlobHash == null) {
                // checkout file from branch commit
                checkoutCommitFile(branchCommitHash, fileInBranch);
                // stage file for addition
                writeContents(join(STAGING_ADD_DIR, fileInBranch), branchBlobHash);
            }
            // file in branch diff in current and file not in split point
            boolean diffInCurrentAndBranch = currentBlobHash != null
                    && !currentBlobHash.equals(branchBlobHash);
            if (diffInCurrentAndBranch && splitPointBlobHash == null) {
                handleConflict(currentBlobHash, branchBlobHash, fileInBranch);
                isConflict = true;
            }
        }
        return isConflict;
    }

    public static void merge(String branchName) {
        checkInit();
        // if stageing area is not empty, throw error
        if (!isStagingEmpty()) {
            throw error("You have uncommitted changes.");
        }
        // read branch commit
        File branchFile = join(HEADS_DIR, branchName);
        // if branch does not exist, throw error
        if (!branchFile.exists()) {
            throw error("A branch with that name does not exist.");
        }
        String branchCommitHash = readContentsAsString(branchFile);
        Commit branchCommit = readObject(join(COMMITS_DIR, branchCommitHash), Commit.class);

        // read current commit
        String currentBranch = readContentsAsString(HEAD_FILE);
        // if branch is current branch, throw error
        if (branchName.equals(currentBranch)) {
            throw error("Cannot merge a branch with itself.");
        }
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        Commit currentCommit = readObject(join(COMMITS_DIR, currentCommitHash), Commit.class);

        if (hasUntrackedAndOverrideFiles(currentCommit, branchCommit)) {
            throw error("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
        }

        // read split point commit
        String splitPointHash = findSplitPoint(currentCommitHash, branchCommitHash);

        // if split point is the same as branch commit, do nothing
        if (splitPointHash.equals(branchCommitHash)) {
            throw error("Given branch is an ancestor of the current branch.");
        }

        // if split point is the same as current commit, checkout branch and update HEAD
        if (splitPointHash.equals(currentCommitHash)) {
            checkoutBranch(branchName);
            throw error("Current branch fast-forwarded.");
        }

        boolean isConflict = handleMerge(currentCommitHash, branchCommitHash, splitPointHash);
        commit("Merged " + branchName + " into " + currentBranch + ".", branchCommitHash);
        if (isConflict) {
            throw error("Encountered a merge conflict.");
        }
    }

    public static void addRemote(String remoteName, String remotePath) {
        checkInit();
        File remoteFile = join(REMOTES_REPO_DIR, remoteName);
        if (remoteFile.exists()) {
            throw error("A remote with that name already exists.");
        }
        writeContents(remoteFile, remotePath);
        join(REMOTES_HEADS_DIR, remoteName).mkdir();
    }

    public static void rmRemote(String remoteName) {
        checkInit();
        File remoteFile = join(REMOTES_REPO_DIR, remoteName);
        if (!remoteFile.exists()) {
            throw error("A remote with that name does not exist.");
        }
        remoteFile.delete();
        File remoteHeadsDir = join(REMOTES_HEADS_DIR, remoteName);
        for (String remoteBranch : plainFilenamesIn(remoteHeadsDir)) {
            join(remoteHeadsDir, remoteBranch).delete();
        }
        remoteHeadsDir.delete();
    }

    private static void copyAllCommits(HashSet<String> commitHashes, File from, File to) {
        // copy commits
        for (String commitHash : commitHashes) {
            File fromCommitFile = join(from, "commits", commitHash);
            File toCommitFile = join(to, "commits", commitHash);
            Commit commit = readObject(fromCommitFile, Commit.class);
            writeObject(toCommitFile, commit);
            // copy blobs
            for (String blobHash : commit.getFileSnapshots().values()) {
                File fromBlobFile = join(from, "objects", blobHash);
                File toBlobFile = join(to, "objects", blobHash);
                String blobContent = readContentsAsString(fromBlobFile);
                writeContents(toBlobFile, blobContent);
            }
        }
    }

    private static File getRemoteDir(String remoteName) {
        File remoteRepo = join(REMOTES_REPO_DIR, remoteName);
        if (!remoteRepo.exists()) {
            throw error("A remote with that name does not exist.");
        }
        remoteRepo = join(readContentsAsString(remoteRepo));
        if (!remoteRepo.exists()) {
            throw error("Remote directory not found.");
        }
        return remoteRepo;
    }

    public static void push(String remoteName, String remoteBranchName) {
        checkInit();
        File remoteRepo = getRemoteDir(remoteName);
        String currentBranch = readContentsAsString(HEAD_FILE);
        String currentCommitHash = readContentsAsString(join(HEADS_DIR, currentBranch));
        HashSet<String> allAncestors = getAllAncestors(currentCommitHash, GITLET_DIR);
        File remoteBranchFile = join(remoteRepo, "refs", "heads", remoteBranchName);
        if (remoteBranchFile.exists()) {
            String remoteCommitHash = readContentsAsString(remoteBranchFile);
            if (!allAncestors.contains(remoteCommitHash)) {
                throw error("Please pull down remote changes before pushing.");
            }
        }
        // copy all commits from local to remote
        copyAllCommits(allAncestors, GITLET_DIR, remoteRepo);
        // update remote branch to point to head of current branch
        writeContents(remoteBranchFile, currentCommitHash);
    }

    public static void fetch(String remoteName, String remoteBranchName) {
        checkInit();
        File remoteRepo = getRemoteDir(remoteName);
        File remoteBranchFile = join(remoteRepo, "refs", "heads", remoteBranchName);
        if (!remoteBranchFile.exists()) {
            throw error("That remote does not have that branch.");
        }
        // copy all commits from remote branch to local
        String remoteCommitHash = readContentsAsString(remoteBranchFile);
        copyAllCommits(getAllAncestors(remoteCommitHash, remoteRepo), remoteRepo, GITLET_DIR);

        // create a new branch point to head of fetched remote branch
        writeContents(join(REMOTES_HEADS_DIR, remoteName, remoteBranchName), remoteCommitHash);
    }

    public static void pull(String remoteName, String remoteBranchName) {
        checkInit();
        fetch(remoteName, remoteBranchName);
        merge(remoteName + File.separator + remoteBranchName);
    }
}
