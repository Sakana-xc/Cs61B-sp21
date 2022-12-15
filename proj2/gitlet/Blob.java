package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private  byte[] content;

    private  String id;

    private  String  filename;

    private File file;



    public Blob(String filename,File CWD){
        this.filename = filename;
        this.file = join(Repository.BLOBS_DIR,filename);
        if (file.exists()){
            this.content = readContents(join(CWD,filename));
            this.id = sha1(filename,content);}
        else{this.content = null;
            this.id = sha1(filename);}
    }

    public String getFilename(){
        return filename;
    }

    public String getId(){
        return this.id;
    }

    public byte[] getContent(){
        return this.content;
    }

    public File getFile(){
        return this.file;
    }





}
