package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author onemeter
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * init
     * add filename
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw Utils.error("Please enter a command.");
            }
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    Utils.validateNumArgs(args, 1);
                    Repository.init();
                    break;
                case "add":
                    Utils.validateNumArgs(args, 2);
                    Repository.add(args[1]);
                    break;
                case "commit":
                    Utils.validateNumArgs(args, 2);
                    Repository.commit(args[1]);
                    break;
                case "checkout":
                    if (args.length == 3 && args[1].equals("--")) {
                        Repository.checkoutHeadCommitFile(args[2]);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        Repository.checkoutCommitFile(args[1], args[3]);
                    } else if (args.length == 2) {
                        Repository.checkoutBranch(args[1]);
                    } else {
                        throw Utils.error("Incorrect operands.");
                    }
                    break;
                case "log":
                    Utils.validateNumArgs(args, 1);
                    Repository.log();
                    break;
                default:
                    throw Utils.error("No command with that name exists.");
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }
}
