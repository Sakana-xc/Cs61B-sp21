package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Repository.HEAD;
import static gitlet.Utils.*;
import static gitlet.additionUtils.exit;


public class Branch implements Serializable {
    public   String branchName;

    private String Head;

    public Branch(String branchName, String head){
        if(exist(branchName)){
            exit("branchName already exists");
        }
        this.branchName = branchName;
        this.Head = head;

    }

    public static Branch readBranch(String branchName){
        return readBranch(branchName,Repository.BRANCH_HEADS_DIR);
    }

    public static Branch readBranch(String branchName, File file){
        branchName = branchUsingFileName(branchName);
        File path = join(Repository.BRANCH_HEADS_DIR,branchName);
        if (!path.exists()) {
            return null;
        } else {
            return Utils.readObject(path, Branch.class);
        }
    }

    public void updateBranch(){
        //branchObject.string
       this.Head = readObject(HEAD,Branch.class).getHeadAsString();
       String name = branchUsingFileName(this.branchName);
       File path = join(Repository.BRANCH_HEADS_DIR,name);
       writeObject(path,this);

    }

    public static boolean exist(String branchName){
        branchName = branchUsingFileName(branchName);
        List<String> names = plainFilenamesIn(Repository.BRANCH_HEADS_DIR);
        return branchName != null && names.contains(branchName);


    }

    public static String branchUsingFileName(String branchName){
        return branchName.replace("/","_");
    }

    private String getHeadAsString(){
        return this.Head;
    }
    public static Branch readHEADAsBranch() {
        return readObject(HEAD, Branch.class);
    }

}
