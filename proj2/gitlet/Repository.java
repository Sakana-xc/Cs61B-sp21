package gitlet;

import org.eclipse.jetty.util.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.additionUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
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



    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");



    /* TODO: fill in the rest of this class. */

    /** Default branch master branch, .gitLet store repository , .commit store commits, .stage store file
     * being added or being removed; .blob store all byte[] contents of files;
     * Head always point to the most recent commit, may be use head.DIR to store?
     * initially head point to the initial commit at master branch;
     */

    //staging directory
    public static final File STAGING_DIR = join(GITLET_DIR,"staging");
    //object directory

    public static final File BLOBS_DIR = join(GITLET_DIR,"blobs");
    public static final File COMMIT_DIR = join(GITLET_DIR,"commit");
    //branch directory
    public static final File REFS_DIR = join(GITLET_DIR,"refs");
    public static final File BRANCH_HEADS_DIR = join(REFS_DIR,"heads");
    public static final File REMOTE_DIR = join(REFS_DIR,"remote");

    //head current branch name default master
    public static final File HEAD = join(GITLET_DIR,"HEAD");
    //configs date author merge strategy etc
    public static final File CONFIG = join(GITLET_DIR,"config");

    public static File STAGE;
    public static final String defaultBranch = "master";





    public static void init(){
        if(GITLET_DIR.exists() && GITLET_DIR.isDirectory()){
            exit("A Gitlet version-control system already exists in the current directory");
        }
        //create directory;
        List<File> dirs = List.of(GITLET_DIR, REFS_DIR, STAGING_DIR, BLOBS_DIR, COMMIT_DIR,
                BRANCH_HEADS_DIR,HEAD,CONFIG);
        dirs.forEach(File::mkdir);

        //create initial commit;
        Commit initialCommit = new Commit();
        writeCommit(initialCommit);
        String id = initialCommit.getId();

        /**
         * create master
         * create path
         * make head point to master and master point to commit
         *  */
        Branch m = new Branch(defaultBranch,"");
        File file = join(BRANCH_HEADS_DIR,defaultBranch);
        writeObject(HEAD,m);
        m.updateBranch();
        writeContents(file,id);

    }

    public void add(String filename){
        File file = join(CWD,filename);
        if(!file.exists()){
            exit("File does not exist");
        }
        Commit curr= currCommit();
        Stage stage = readStage();
        Blob blob = new Blob(filename,CWD);
        String blobId = blob.getId();
        String currTrackBlob = curr.getBlobs().getOrDefault(filename,"");
        String stageAddedBlob = stage.toBeAdded().getOrDefault(filename,"");

        /**
         * Compare blobId with commit.trackedFiles, if same and stage has a different blob;delete;
         * else add file to stage； if stage is not empty, delete first;
         */
        if(blobId.equals(currTrackBlob)){
            if(!stageAddedBlob.equals(blobId)){
                join(STAGING_DIR,stageAddedBlob).delete();
                //remove blobId(value) in stage
                stage.toBeAdded().remove(stageAddedBlob);
                stage.toBeRemoved().remove(filename);
                save(STAGING_DIR,stage);
            }
        } else {
            //delete the original
            if(!stageAddedBlob.equals("")){
                join(STAGING_DIR,stageAddedBlob).delete();
            }
            writeObject(join(STAGING_DIR,blobId),blob);
            stage.addFiles(filename,blobId);
            save(STAGING_DIR,stage);

        }

    }

    public void commit(String message){
        if(message.equals("")){
            exit("Please enter a commit message");
        }
        //get the parents
        Commit curr = currCommit();
        Stage stage = readStage();
        if(stage.isEmpty()){
            exit("No changes added to the commit");
        }
        Commit commit = new Commit(message,List.of(curr),stage);
        //The staging area is cleared after a commit
        clearStage(stage);
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
    public void rm(String filename){
        File file = join(CWD,filename);
        String blobId = new Blob(filename,CWD).getId();
        Commit curr = currCommit();
        Stage stage = readStage();
        String currTrackBlob = curr.getBlobs().getOrDefault(filename,"");
        String stageAddedBlob = stage.toBeAdded().getOrDefault(filename,"");
        if(currTrackBlob.equals("")&& stageAddedBlob.equals("")){
            exit("No reason to remove the file");
        }
        //UnStage the file if it is currently staged for addition.
        if(!stageAddedBlob.equals("")){
            stage.toBeAdded().remove(filename);
        // stage it for removal
        } else{
            stage.toBeRemoved().add(filename);
        }
        //hashMap<String, String> trackedFiles, key filename, value blobId
        if(blobId.equals(currTrackBlob)){
            restrictedDelete(file);
        }
        writeObject(STAGE,stage);

    }

    /**
     * Display list of commits and print Id,timestamp, message
     * we only care parent[0]
     * recursive maybe
     */
    public void log(){
        Commit curr = currCommit();
        log(curr);

    }
    private void log(Commit commit){
        if (commit== null){
            return;
        }
        System.out.println("commit" + commit.getId());
        List<String> parents = commit.getParents();
        if(parents.size() == 1){
            System.out.println("Merge: " + parents.get(0).substring(0, 7) +
                    " " + parents.get(1).substring(0, 7));
        }
        System.out.println("Date:" + commit.dateToString());
        System.out.println( commit.getMessage());
        log(getCommitUsingId(commit.getFirstParentsId()));

    }

    public void global_log() {
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        for (String filename : filenames) {
            Commit commit = getCommitUsingId(filename);
            String logMessage = String.format("===\ncommit %s\nDate: %s\n%s\n\n",
                    commit.getId(), commit.dateToString(), commit.getMessage());
            System.out.println(logMessage);
        }
    }

    public void find(String query) {
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        for (String filename : filenames) {
            Commit commit = getCommitUsingId(filename);
            if (commit.getMessage().contains(query)) {
                System.out.println(commit.getId());
            }
        }
        exit("Found no commit with that message");
    }

    public void status() {
        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(BRANCH_HEADS_DIR);
        String currentBranch = readContentsAsString(HEAD);
        for (String branchName : branchNames) {
            if (branchName.equals(currentBranch)) {
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
        System.out.println("=== Modifications Not Staged For Commit ===\n");

        System.out.println("=== Untracked Files ===\n");

    }

    public void checkOutHead(String filename){
        Commit head = currCommit();
        checkOutCommit(head,filename);

    }

    private void checkOutCommit(Commit commit, String filename){
        String blobId = commit.getTrackedFiles().getOrDefault(filename,"");
        checkOutBlob(blobId);
    }
    private void checkOutBlob(String blobId){
        if (blobId.equals("")){
            exit("File does not exist in that commit");
        }
        Blob blob = readBlob(blobId);
        File file = join(CWD,blob.getFilename());
        writeContents(file,blob.getContent());
    }

    public void checkOutSpecificCommit(String prefixId, String filename){
        String commitId = getFullCommitID(prefixId);
        if(commitId == null){
            exit("No commit with that id exists");
        }
        Commit targetCommit = getCommitUsingId(commitId);
        checkOutCommit(targetCommit,filename);
    }
    public void checkOutBranches(String branchName){
        if(!Branch.exist(branchName)){
            exit("No such branch exists");
        }
        Branch branch = Branch.readBranch(branchName);
        File branchFile = join(BRANCH_HEADS_DIR,branchName);
        Branch currBranch = Branch.readHEADAsBranch();
        if(currBranch.branchName.equals(branchName)){
            exit("No need to checkout the current branch");
        }
        Commit targetCommit = getCommitFromBranchFile(branchFile);
        //check untracked file and would be overwritten by checkout branch
        untrackedExit(targetCommit.getBlobs());
        clearStage(readStage());
        //overwrite CWD files
        Commit curr = currCommit();

        writeContents(HEAD, branchName);


    }


    //Bunch of Helpers
    public static List<String> getUntrackedFiles(){
        List<String> untrackedFiles = new ArrayList<>();
        List<String> StagedFileNames = readStage().getStagedFileName();
        Map<String,String> headBlobs = currCommit().getBlobs();
        for(String filename:plainFilenamesIn(CWD)){
            if(!StagedFileNames.contains(filename)&&!headBlobs.containsKey(filename)){
                untrackedFiles.add(filename);
            }
        }
        Collections.sort(untrackedFiles);
        return untrackedFiles;

    }


    private static void writeCommit(Commit commit){
        File file = join(COMMIT_DIR,commit.getId());
        writeObject(file,commit);
    }

    /**
     * how to retrieve commit
     * get to the branch where head point at
     * get the commitId from branch
     * go to the Commit_DIR
     * readObject(join(Commit_DIR,id))
     */

    private static Commit currCommit(){
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(BRANCH_HEADS_DIR,branchName);
        Commit curr = getCommitFromBranchFile(branchFile);
        if (curr.equals(null)){
            exit("Can not find HEAD");
        }
        return curr;

    }

    private static Commit getCommitFromBranchFile(File file){
        String commitId = readContentsAsString(file);
        return getCommitUsingId(commitId);
    }

    private static Commit getCommitUsingId(String id){
        File file = join(COMMIT_DIR,id);
        if(id.equals("null") || !file.exists()){
            return null;
        }
        return readObject(file, Commit.class);

    }

    private static Stage readStage(){
        return readObject(STAGE, Stage.class);
    }

    public static void checkWorkingDirectory(){
        if(!GITLET_DIR.isDirectory()){
            exit("Not in an initialized Gitlet directory");
        }
    }

    private void clearStage(Stage stage) {
        File[] files = STAGING_DIR.listFiles();
        if (files == null) {
            return;
        }
        Path targetDir = BLOBS_DIR.toPath();
        for (File file : files) {
            Path source = file.toPath();
            try {
                Files.move(source, targetDir.resolve(source.getFileName()), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Stage newStage = new Stage();
        writeObject(STAGE,newStage);



    }

   public void writeCommitToBranch(String Id){
        String branchName = readContentsAsString(HEAD);
        File branch = join(BRANCH_HEADS_DIR,branchName);
        //Write the result of concatenating the bytes in CONTENTS to FILE,
       //     *  creating or overwriting it as needed.
       // branch store the latest commitID;
        writeContents(branch,Id);
   }

   private Blob readBlob(String blobId){
        File file = join(BLOBS_DIR,blobId);
        return readObject(file, Blob.class);
   }

   private String getFullCommitID(String prefixId){
        if (prefixId.length()== UID_LENGTH){
            return prefixId;
        }
        if(prefixId.length() < 6){
            exit("should contain at least 6 characters");
        }
        String [] Ids = COMMIT_DIR.list();
        for(String commitId:Ids){
            if(commitId.startsWith(prefixId)){
                return commitId;
            }
        }
        return null;
   }







}
