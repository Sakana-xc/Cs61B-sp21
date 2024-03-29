package gitlet;

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
        if (args.length == 0) {
            exit("Please enter a command");
        }
        String firstArg = args[0];

        switch (firstArg) {
            case "init":
                // TODO: handle the `init` command
                // get the current working dir;
                checkNumOfArgs(args,1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().commit(args[1]);
                break;

            case "rm":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().rm(args[1]);
                break;

            case "log":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,1);
                new Repository().log();
                break;

            case "global-log":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,1);
                new Repository().global_log();
                break;
            case "find":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().find(args[1]);
                break;
            case "status":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,1);
                new Repository().status();
                break;
            case "checkout":
                if (args.length < 2 || args.length > 4){
                    exit("Incorrect operands");
                }
                Repository.checkWorkingDirectory();
                if (args.length == 3){
                    checkEqual(args[1],"--");
                    new Repository().checkOutHead(args[2]);
                }

                if (args.length == 4) {
                    checkEqual(args[2],"--");
                    new Repository().checkOutWithPrefix(args[1],args[3]);
                }
                if (args.length == 2) {
                    new Repository().checkOutBranches(args[1]);
                }
                break;
            case "branch":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().branch(args[1]);
                break;
            case "rm-branch":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().rm_branch(args[1]);
                break;
            case "reset":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().reset(args[1]);
                break;
            case "merge":
                Repository.checkWorkingDirectory();
                checkNumOfArgs(args,2);
                new Repository().merge(args[1]);
                break;
            default:
                exit("No command with that name exists.");
        }
    }
    private static void checkNumOfArgs(String [] args, Integer n){
        if(args.length != n){
            exit("Incorrect operands.");
        }
    }
    private static void checkEqual(String actual, String expected){
        if (!actual.equals(expected)) {
            exit("Incorrect operands.");
        }
    }
}
