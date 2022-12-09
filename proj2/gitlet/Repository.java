package gitlet;

import java.io.File;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.additionUtils.exit;

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
         * create head
         *  */
        writeContents(HEAD,defaultBranch);
        File master = join(BRANCH_HEADS_DIR,defaultBranch);
        //make master point to commit;
        writeContents(master,id);
        //rewrite
        writeContents(HEAD,defaultBranch);
        writeContents(CONFIG,"");





    }
    private static void writeCommit(Commit commit){
        File file = join(COMMIT_DIR,commit.getId());
        writeObject(file,commit);
    }

}
