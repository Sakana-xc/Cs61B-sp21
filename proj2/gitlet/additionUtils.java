package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static gitlet.Repository.CWD;
import static gitlet.Utils.writeObject;

public class additionUtils {


    public static void exit(String message){
        if(message != null){
            System.out.println(message);
        }
        System.exit(0);
    }

    public static void save(File file, Serializable object){
        File dir = file.getParentFile();
        if(!dir.exists()){
            dir.mkdir();
        }
        writeObject(file,object);
    }

    public static void untrackedExit(Map<String,String> blobs){
        List<String> untrackedFiles = Repository.getUntrackedFiles();
        if(!untrackedFiles.isEmpty()){
            for(String filename:untrackedFiles){
                String blobId = new Blob(filename, CWD).getId();
                String checkOutBranchBlobId = blobs.getOrDefault(filename, "");
                if(!blobId.equals(checkOutBranchBlobId)){
                    exit("There is an untracked file in the way; delete it, or add and commit it first");
                }
            }
        }

    }


}
