package gitlet;

// TODO: any imports you need here

import javax.xml.crypto.Data;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private  String message;
    private Date timeStamp;
    private List<String> parents;
    private HashMap<String,String> trackedFiles;
    private String id;



    /* TODO: fill in the rest of this class. */

    public Commit()  {
        this.message = "initial commit";
        this.timeStamp= new Date(0);
        this.parents = new ArrayList<>();
        this.trackedFiles = new HashMap<>();
        this.id = sha1(message,timeStamp.toString());

    }

    public Commit(String message,List<Commit> parents, Stage stage){
        this.message = message;
        this.timeStamp = new Date();


        /** for a commit maximum will encounter 2 parent merge,A List should serve enough
         it will take O(n) to track down a specific version of commits, use tree like structure
         can scale it down to O(logN) , may be a DAG */
        this.parents = new ArrayList<>(2);
        for(Commit i: parents){
            String id = i.getId();
            this.parents.add(id);
        }
        //use parent[0]'s tracked File
        this.trackedFiles = parents.get(0).getBlobs();
        //anything any change in commit will result in change in id
        this.id = sha1(message,timeStamp.toString(),parents.toString(),
                trackedFiles.toString());
        // add new blobs to the original HashMap;

        for(Map.Entry<String,String> item : stage.toBeAdded().entrySet()){
            String filename = item.getKey();
            String blobId = item.getValue();
            trackedFiles.put(filename,blobId);
        }
        // for blobs no longer tracked,e.g.replaced by new version, remove

        for(String filename: stage.toBeRemoved()){
            trackedFiles.remove(filename);
        }
    }
    public String getId(){
        return id;
    }

    public HashMap<String,String> getBlobs(){
        return this.trackedFiles;
    }

    // helpers for log

    public List<String> getParents(){
        return parents;
    }

    public String getFirstParentsId(){
        if (parents.isEmpty()) {
            return "null";
        }
        return parents.get(0);
    }

    public String dateToString (){
        DateFormat dateFormatted = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormatted.format(timeStamp);

    }

    public String getMessage(){
        return this.message;
    }

    public HashMap<String,String> getTrackedFiles(){
        return this.trackedFiles;
    }






}
