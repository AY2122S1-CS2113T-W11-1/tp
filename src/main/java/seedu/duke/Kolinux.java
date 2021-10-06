package seedu.duke;

import seedu.duke.commands.Command;
import seedu.duke.commands.CommandResult;
import seedu.duke.commands.ExitCommand;
import seedu.duke.exceptions.KolinuxException;
import seedu.duke.module.ModuleDb;
import seedu.duke.planner.Planner;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class Kolinux {

    private Ui ui = new Ui();
    private ModuleDb db = new ModuleDb();

    private void runCommandInLoop() {

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String userInput = scanner.nextLine();
                Command command = Parser.parseCommand(db, userInput);
                CommandResult result = command.executeCommand();
                ui.showResultToUser(result);
                if (command instanceof ExitCommand) {
                    break;
                }
            } catch (KolinuxException | FileNotFoundException exception) {
                assert exception instanceof KolinuxException;
                ui.showErrorMessage((KolinuxException) exception);
            }
        }
    }

    public void run() {
        ui.greetUser();
        db.initModuleDb();
        Planner.initPlanner();
        runCommandInLoop();
    }
}
