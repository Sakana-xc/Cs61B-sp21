package gitlet;

import org.eclipse.jetty.util.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.additionUtils.exit;
import static gitlet.additionUtils.save;
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

    public File STAGE;
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
        File master = join(BRANCH_HEADS_DIR,defaultBranch);
        writeObject(HEAD,m);
        m.updateBranch();
        writeContents(master,id);

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

    //Bunch of Helpers
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

    private Commit currCommit(){
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(BRANCH_HEADS_DIR,branchName);
        Commit curr = getCommitFromBranchFile(branchFile);
        if (curr.equals(null)){
            exit("Can not find HEAD");
        }
        return curr;

    }

    private  Commit getCommitFromBranchFile(File file){
        String commitId = readContentsAsString(file);
        return getCommitUsingId(commitId);
    }

    private  Commit getCommitUsingId(String id){
        File file = join(COMMIT_DIR,id);
        if(id.equals("null") || !file.exists()){
            return null;
        }
        return readObject(file, Commit.class);

    }

    private Stage readStage(){
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







}
