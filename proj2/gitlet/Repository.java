package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.additionUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     *
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */


    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");



    /* TODO: fill in the rest of this class. */

    /**
     * Default branch master branch, .gitLet store repository , .commit store commits, .stage store file
     * being added or being removed; .blob store all byte[] contents of files;
     * Head always point to the most recent commit, may be use head.DIR to store?
     * initially head point to the initial commit at master branch;
     */

    //staging directory
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    //object directory

    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    //branch directory
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File BRANCH_HEADS_DIR = join(REFS_DIR, "heads");
    public static final File REMOTE_DIR = join(REFS_DIR, "remote");

    //head current branch name default master
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final File STAGE = join(GITLET_DIR, "stage");


    public static void init() {
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            exit("A Gitlet version-control system already exists in the current directory");
        }
        //create directory;
        List<File> dirs = List.of(GITLET_DIR, REFS_DIR, STAGING_DIR, BLOBS_DIR, COMMIT_DIR,
                BRANCH_HEADS_DIR);
        dirs.forEach(File::mkdir);
        writeObject(STAGE, new Stage());
        //create initial commit;
        Commit initialCommit = new Commit();
        writeCommit(initialCommit);
        String id = initialCommit.getId();

        /**
         * make head point to master and master point to commit
         * It's reasonal  to just write the name of the branch to HEAD, no need to
         * fully serialize it
         *  */


        writeContents(HEAD, "master");
        File masterFile = join(BRANCH_HEADS_DIR, "master");
        writeContents(masterFile, id);
    }

    public void add(String filename) {
        File file = join(CWD, filename);
        if (!file.exists()) {
            exit("File does not exist");
        }
        Commit curr = currCommit();
        Stage stage = readStage();
        Blob blob = new Blob(filename, CWD);
        String blobId = blob.getId();
        String currTrackBlob = curr.getTrackedFiles().getOrDefault(filename, "");
        String stageAddedBlob = stage.toBeAdded().getOrDefault(filename, "");

        /**
         * Compare blobId with commit.trackedFiles, if same and stage has a different blob;delete;
         * else add file to stage； if stage is not empty, delete first;
         */
        if (blobId.equals(currTrackBlob)) {
            if (!stageAddedBlob.equals(blobId)) {
                join(STAGING_DIR, stageAddedBlob).delete();
                //remove blobId(value) in stage
                stage.toBeAdded().remove(stageAddedBlob);
                stage.toBeRemoved().remove(filename);
                writeObject(STAGE, stage);
            }
        } else {
            //delete the original
            if (!stageAddedBlob.equals("")) {
                join(STAGING_DIR, stageAddedBlob).delete();
            }
            writeObject(join(STAGING_DIR, blobId), blob);
            stage.addFiles(filename, blobId);
            writeObject(STAGE, stage);

        }

    }

    public void commit(String message) {
        if (message.equals("")) {
            exit("Please enter a commit message");
        }
        //get the parents
        Commit curr = currCommit();
        Stage stage = readStage();
        if (stage.isEmpty()) {
            exit("No changes added to the commit");
        }
        Commit commit = new Commit(message, List.of(curr), stage);
        //The staging area is cleared after a commit
        clearStage(readStage());
        writeCommit(commit);
        String commitId = commit.getId();
        //branch file updated with the latest commitID
        writeCommitToBranch(commitId);
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * fail case:If the file is neither staged nor tracked by the head commit,
     * print the error message No reason to remove the file.
     */
    public void rm(String filename) {
        File file = join(CWD, filename);
//        String blobId = new Blob(filename,CWD).getId();
        Blob blob = new Blob(filename, CWD);
        String blobId = blob.getId();
        Commit curr = currCommit();
        Stage stage = readStage();
        String currTrackBlob = curr.getTrackedFiles().getOrDefault(filename, "");
        String stageAddedBlob = stage.toBeAdded().getOrDefault(filename, "");
        if (currTrackBlob.equals("") && stageAddedBlob.equals("")) {
            exit("No reason to remove the file");
        }
        if (!stageAddedBlob.equals("")) {
            stage.toBeAdded().remove(filename);
        } else {
            stage.toBeRemoved().add(filename);
        }
        //hashMap<String, String> trackedFiles, key filename, value blobId
        //if they share the same name, but different contents, the id gonna be different
        if (blob.getContent() != null && blobId.equals(currTrackBlob)) {
            restrictedDelete(file);
        }
        writeObject(STAGE, stage);

    }


    /**
     * Display list of commits and print Id,timestamp, message
     * we only care parent[0]
     * recursive maybe
     */
    public void log() {
        Commit curr = currCommit();
        log(curr);

    }

    private void log(Commit commit) {
        if (commit == null) {
            return;
        }
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        List<String> parents = commit.getParents();
        if (parents.size() == 2) {
            System.out.println("Merge: " + parents.get(0).substring(0, 7) +
                    " " + parents.get(1).substring(0, 7));
        }
        System.out.println("Date: " + commit.dateToString());
        System.out.println(commit.getMessage() + "\n");
        log(getCommitUsingId(commit.getFirstParentsId()));

    }

    public void global_log() {
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        for (String filename : filenames) {
            Commit commit = getCommitUsingId(filename);
            String logMessage = String.format("===\ncommit %s\nDate: %s\n%s\n",
                    commit.getId(), commit.dateToString(), commit.getMessage());
            System.out.println(logMessage);
        }
    }

    public void find(String query) {
        StringBuilder Id = new StringBuilder();
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        for (String filename : filenames) {
            Commit commit = getCommitUsingId(filename);
            if (commit.getMessage().contains(query)) {
                Id.append(commit.getId() + "\n");
            }
        }
        if (Id.length() == 0) {
            exit("Found no commit with that message.");
        }
        System.out.println(Id);

    }

    public void status() {
        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(BRANCH_HEADS_DIR);
        String currentBranchName = readContentsAsString(HEAD);
        for (String branchName : branchNames) {
            if (branchName.equals(currentBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println("\n=== Staged Files ===");
        Stage stage = readStage();
        for (String filename : stage.toBeAdded().keySet()) {
            System.out.println(filename);
        }

        System.out.println("\n=== Removed Files ===");
        for (String filename : stage.toBeRemoved()) {
            System.out.println(filename);
        }

        //extra credit
        System.out.println("\n=== Modifications Not Staged For Commit ===\n");

        System.out.println("=== Untracked Files ===\n");

    }

    /**
     * get the version of file from headCommit,
     * update it to CWD, create or rewrite (write content)
     * do not add it to stage
     *
     * @param filename There is an issue that it's not rewrite CWD file successfully
     */
    public void checkOutHead(String filename) {
        Commit head = currCommit();
        checkOutCommit(head, filename);

    }

    private void checkOutCommit(Commit commit, String filename) {
        String blobId = commit.getTrackedFiles().getOrDefault(filename, "");
        checkOutBlob(blobId);
    }

    private void checkOutBlob(String blobId) {
        if (blobId.equals("")) {
            exit("File does not exist in that commit.");
        }
        Blob blob = readBlob(blobId);
        String filename = blob.getFilename();
        File file = join(CWD, filename);
        //create or overWrite currFile with blob's Content
        writeContents(file, blob.getContent());
    }

    public void checkOutWithPrefix(String prefixId, String filename) {
        String commitId = getFullCommitID(prefixId);
        File file = join(COMMIT_DIR, commitId);
        if (!file.exists()) {
            exit("No commit with that id exists.");
        }
        Commit targetCommit = readObject(file, Commit.class);
        checkOutCommit(targetCommit, filename);
    }

    public void checkOutBranches(String branchName) {
        File branchFile = join(BRANCH_HEADS_DIR, branchName);
        //failure case
        if (!branchFile.exists()) {
            exit("No such branch exists.");
        }
        if (branchName.equals(readContentsAsString(HEAD))) {
            exit("No need to checkout the current branch.");
        }
        String targetCommitId = readContentsAsString(branchFile);
        Commit targetCommit = getCommitUsingId(targetCommitId);
        //if these are untracked files,means in CWD,different from targetCommit,not tracked in curr
       CheckExitErrorFileWillBeOverWritten(targetCommit);
        /**
         * get all blobId from targetCommit,
         * checkoutBlob
         */
        resetSpace();
        for (Map.Entry<String, String> pairs : targetCommit.getTrackedFiles().entrySet()) {
            String blobId = pairs.getValue();
            checkOutBlob(blobId);
        }
        writeContents(HEAD, branchName);
    }

    public void branch(String branchName) {
        File branchFile = join(BRANCH_HEADS_DIR, branchName);
        List<String> names = plainFilenamesIn(BRANCH_HEADS_DIR);
        if (names.contains(branchName)) {
            exit("A branch with that name already exists.");
        }
        String headBranchName = readContentsAsString(HEAD);
        File file = join(BRANCH_HEADS_DIR, headBranchName);
        String commitId = readContentsAsString(file);
        writeContents(branchFile, commitId);
    }

    public void rm_branch(String branchName) {
        File branchFile = join(BRANCH_HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            exit("A branch with that name does not exist.");
        }
        if (branchName.equals(readContentsAsString(HEAD))) {
            exit("Cannot remove the current branch.");
        }
        branchFile.delete();
    }

    public void reset(String commitId) {
        File file = join(COMMIT_DIR, commitId);
        if (!file.exists()) {
            exit("No commit with that id exists.");
        }
//        Commit commit = getCommitUsingId(commitId);
//        List<String> untrackedFiles = getUntrackedFiles();
//        if (!untrackedFiles.isEmpty()) {
//            for (String name : untrackedFiles) {
//                String blobId = new Blob(name, CWD).getId();
//                String targetId = commit.getTrackedFiles().getOrDefault(name, "");
//                if (!blobId.equals(targetId)) {
//                    exit("There is an untracked file in the way; delete it, or add and commit it first.");
//                }
//            }
//        }
        Commit commit = getCommitUsingId(commitId);
        CheckExitErrorFileWillBeOverWritten(commit);
        resetSpace();
        for (Map.Entry<String, String> pairs : commit.getTrackedFiles().entrySet()) {
            String blobId = pairs.getValue();
            checkOutBlob(blobId);
        }
        clearStage(readStage());
        String currBranchName = readContentsAsString(HEAD);
        writeContents(join(BRANCH_HEADS_DIR, currBranchName), commitId);
    }

    public void merge (String givenBranchName){
        Stage stage = readStage();
        String headBranchName = readContentsAsString(HEAD);
        if(!stage.isEmpty()){
            exit("You have uncommitted changes.");
        }

        File branchFile = join(BRANCH_HEADS_DIR,givenBranchName);
        if(!branchFile.exists()){
            exit("A branch with that name does not exist.");
        }
        if(givenBranchName.equals(headBranchName)){
            exit("Cannot merge a branch with itself.");
        }
        String otherCommitId = readContentsAsString(branchFile);
        Commit curr = currCommit();
        Commit otherCommit = getCommitUsingId(otherCommitId);
        Commit LCA = findLatestCommonAncestor(curr,otherCommit);

        merge(curr,otherCommit,LCA,givenBranchName);
        String message = "Merged" + givenBranchName + "into" + headBranchName +".";
        List<Commit > parent = List.of(curr,otherCommit);
        commit(message,parent);


    }


    //Bunch of Helpers

    private void commit(String message, List<Commit> parent){
        Stage stage = readStage();
        if(stage.isEmpty()){
            exit("No changes added to the commit.");
        }
        Commit commit = new Commit(message,parent,stage);
        clearStage(stage);
        writeCommit(commit);
        String commitId = commit.getId();
        writeCommitToBranch(commitId);

    }

    private void merge(Commit currentCommit, Commit otherCommit, Commit LCA,String givenBranchName){

        if(LCA.getId().equals(otherCommit.getId())){
            exit("Given branch is an ancestor of the current branch.");
        }
        if(LCA.getId().equals(currentCommit.getId())){
            checkOutBranches(givenBranchName);
            exit("Current branch fast-forwarded.");
        }
        Set<String> fileNames = new HashSet<>();
        fileNames.addAll(currentCommit.getTrackedFiles().keySet());
        fileNames.addAll(otherCommit.getTrackedFiles().keySet());
        fileNames.addAll(LCA.getTrackedFiles().keySet());

        //container
        List<String> remove = new LinkedList<>();
        List<String> overwrite = new LinkedList<>();
        List<String> conflicted = new LinkedList<>();

        for(String name:fileNames) {
            String currBlobId = currentCommit.getTrackedFiles().getOrDefault(name, "");
            String otherBlobId = otherCommit.getTrackedFiles().getOrDefault(name, "");
            String ancestorId = LCA.getTrackedFiles().getOrDefault(name, "");

            if (ancestorId.equals(currBlobId) && !ancestorId.equals(otherBlobId)) {
                overwrite.add(otherBlobId);
            }
            if (ancestorId.equals(otherBlobId) && !ancestorId.equals(currBlobId)) {
                continue;
            }
            if (currBlobId.equals(otherBlobId)) {
                continue;
            }
            if (ancestorId.equals("")) {
                if (!otherCommit.getTrackedFiles().keySet().contains(name) &&
                        currentCommit.getTrackedFiles().keySet().contains(name)) {
                    continue;
                }
                if(!currentCommit.getTrackedFiles().keySet().contains(name) &&
                        otherCommit.getTrackedFiles().keySet().contains(name)) {
                    overwrite.add(otherBlobId);
                }

            }
            if(LCA.getTrackedFiles().keySet().contains(name)) {
                if(ancestorId.equals(currBlobId)&& otherBlobId.equals("")){
                    remove.add(name);
                }
                if(otherBlobId.equals(ancestorId)&& currBlobId.equals("")){
                    continue;
                }
            }
            if(!ancestorId.equals(currBlobId) && ! ancestorId.equals(otherBlobId)){
                if(!currBlobId.equals(otherBlobId)){
                    conflicted.add(name);
                }
            }

        }
        merge(remove,overwrite,conflicted,otherCommit,currentCommit);
    }

    private void merge(List<String> remove, List<String> overwrite,List<String> conflicted,
                       Commit currCommit, Commit otherCommit){
        List<String> untrackedFiles = getUntrackedFiles();
        for(String name: untrackedFiles){
            if(remove.contains(name) || overwrite.contains(name) ||
            conflicted.contains(name)){
                exit("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        if(!overwrite.isEmpty()){
            for (String id: overwrite){
                Blob blob = readBlob(id);
                checkOutBlob(id);
                String fileName = blob.getFilename();
                add(fileName);
            }
        }

        if(!remove.isEmpty()) {
            for (String name : remove) {
                rm(name);
            }
        }

        if(!conflicted.isEmpty()){
            for(String name: conflicted){
                String currBlobId = currCommit.getTrackedFiles().getOrDefault(name,"");
                String otherBlobId = otherCommit.getTrackedFiles().getOrDefault(name,"");
                String currContent = readBlobAsString(currBlobId);
                String otherContent = readBlobAsString(otherBlobId);
                System.out.println("<<<<<<< HEAD");
                System.out.println(currContent);
                System.out.println("========");
                System.out.println(otherContent);
                System.out.println(">>>>>>>");
                exit("Encountered a merge conflict.");

            }
        }

    }

    private String readBlobAsString(String blobId){
        if (blobId.equals("")){
            return "";
        }
        return readBlob(blobId).getContentAsString();
    }


    public static void CheckExitErrorFileWillBeOverWritten(Commit commit){
        List<String> files = getUntrackedFiles();
        if (!files.isEmpty()) {
            /**if untracked files has different blobId with given commit in target branch,
             * it will get overwritten;
             */
            for (String name : files) {
                String blobId = new Blob(name, CWD).getId();
                String targetId = commit.getTrackedFiles().getOrDefault(name, "");
                if (!blobId.equals(targetId)) {
                    exit("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }
    }

    public static List<String> getUntrackedFiles() {
        List<String> untrackedFiles = new ArrayList<>();
        //All file in toBeAdded && toBeRemoved
        List<String> StagedFileNames = readStage().getStagedFileName();
        Map<String, String> headBlobs = currCommit().getTrackedFiles();
        for (String name : plainFilenamesIn(CWD)) {
            if (!StagedFileNames.contains(name) && !headBlobs.containsKey(name)) {
                untrackedFiles.add(name);
            }
        }
        Collections.sort(untrackedFiles);
        return untrackedFiles;

    }

    private Commit findLatestCommonAncestor(Commit head,Commit other){
        HashSet<String> headAncestor = getHeadAncestor(head);
        Queue<Commit> otherQueue = new LinkedList<>();
        otherQueue.add(other);
        while(!otherQueue.isEmpty()){
            Commit otherCommit = otherQueue.poll();
            if(headAncestor.contains(otherCommit.getId())){
                return otherCommit;
            }
            if(!otherCommit.getParents().isEmpty()){
                for(String id: otherCommit.getParents()){
                    otherQueue.add(getCommitUsingId(id));
                }
            }
        }
        return null;
    }

    private HashSet<String> getHeadAncestor(Commit head){
        HashSet<String> ancestor = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(head);

        while(!queue.isEmpty()){
            Commit commit = queue.poll();
            if(!ancestor.contains(commit.getId()) && !commit.getParents().isEmpty()){
                for(String parentId: commit.getParents() ){
                    queue.add(getCommitUsingId(parentId));
                }
            }
            ancestor.add(commit.getId());
        }
        return ancestor;

    }


    private static void writeCommit(Commit commit) {
        File file = join(COMMIT_DIR, commit.getId());
        writeObject(file, commit);
    }

    /**
     * how to retrieve commit
     * get to the branch where head point at
     * get the commitId from branch
     * go to the Commit_DIR
     * readObject(join(Commit_DIR,id))
     */

    private static Commit currCommit() {
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(BRANCH_HEADS_DIR, branchName);
        String commitId = readContentsAsString(branchFile);
        Commit curr = getCommitUsingId(commitId);
        if (curr.equals(null)) {
            exit("Can not find HEAD.");
        }
        return curr;

    }


    private static Commit getCommitUsingId(String id) {
        File file = join(COMMIT_DIR, id);
        if (id.equals("null") || !file.exists()) {
            return null;
        }
        return readObject(file, Commit.class);

    }

    private static Stage readStage() {
        return readObject(STAGE, Stage.class);
    }

    public static void checkWorkingDirectory() {
        if (!GITLET_DIR.isDirectory()) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    private void clearStage(Stage stage) {
        File[] files = STAGING_DIR.listFiles();
        if (files == null) {
            return;
        }
        moveFromStageToBlobs(files);
        Stage newStage = new Stage();
        writeObject(STAGE, newStage);

    }

    public void writeCommitToBranch(String Id) {
        String branchName = readContentsAsString(HEAD);
        File branch = join(BRANCH_HEADS_DIR, branchName);
        //Write the result of concatenating the bytes in CONTENTS to FILE,
        //     *  creating or overwriting it as needed.
        // branch store the latest commitID;
        writeContents(branch, Id);
    }

    private Blob readBlob(String blobId) {
        File file = join(BLOBS_DIR, blobId);
        return readObject(file, Blob.class);
    }

    private String getFullCommitID(String prefixId) {
        if (prefixId.length() == UID_LENGTH) {
            return prefixId;
        }
        if (prefixId.length() < 6) {
            exit("should contain at least 6 characters");
        }
        String[] Ids = COMMIT_DIR.list();
        for (String commitId : Ids) {
            if (commitId.startsWith(prefixId)) {
                return commitId;
            }
        }
        return null;
    }

    private void moveFromStageToBlobs(File[] files) {
        Path targetDir = BLOBS_DIR.toPath();
        for (File file : files) {
            Path source = file.toPath();
            Path target = targetDir.resolve(source.getFileName());
            try {
                Files.move(source, target, REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void resetSpace() {
        File[] files = CWD.listFiles();
        for (File file : files) {
            if (!file.getName().equals(".gitlet")) {
                burn(file);
            }
        }
    }

    private void burn(File file) {
        if (file.isDirectory()) {
            for (File things : file.listFiles()) {
                burn(things);
            }
        }
        file.delete();
    }


}
