package com.dlesl.cpmini;

public class Command {
    public static void run(String[] command) throws Exception {
        var process = new ProcessBuilder(command)
                .inheritIO()
                .start();
        var retval = process.waitFor();
        if (retval != 0) {
            throw new CommandFailedException(retval);
        }
    }

    public static class CommandFailedException extends RuntimeException {
        CommandFailedException(int code) {
            super("Command failed with exit code " + code);
        }
    }
}
