package gitlet;

import java.io.File;

import static gitlet.additionUtils.exit;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main  {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args.length == 0){
           exit("Please enter a command");
        }
        String firstArg = args[0];

        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                // get the current working dir;
                checkNumOfArgs(args,1);
                Repository.init();


                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
        }
    }
    private static void checkNumOfArgs(String [] args, Integer n){
        if(args.length != n){
            exit("Incorrect operands.");
        }
    }
}
