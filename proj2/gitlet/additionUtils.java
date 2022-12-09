package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.writeObject;

public class additionUtils {


    public static void exit(String message){
        if(message != null){
            System.out.println(message);
        }
        System.exit(0);
    }


}
