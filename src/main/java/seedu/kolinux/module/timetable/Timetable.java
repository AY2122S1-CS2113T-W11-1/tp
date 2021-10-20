package seedu.kolinux.module.timetable;

import seedu.kolinux.exceptions.KolinuxException;
import seedu.kolinux.module.ModuleDetails;
import seedu.kolinux.module.ModuleList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import static seedu.kolinux.module.timetable.Lesson.getIndex;
import static seedu.kolinux.module.timetable.Lesson.schoolHours;
import static seedu.kolinux.module.timetable.Lesson.days;

/**
 * Timetable class that represents the methods to interact with the 2D timetable array and Array list for storage.
 */
public class Timetable {

    private TimetableStorage timetableStorage = new TimetableStorage();
    private ModuleList moduleList;
    private static final int ROW_SIZE = 16;
    private static final int COLUMN_SIZE = 6;
    private static final int COLUMN_LAST_INDEX = 5;
    public String [][] timetableData = new String[ROW_SIZE][COLUMN_SIZE];
    public static ArrayList<Lesson> lessonStorage = new ArrayList<>();
    public static String filePath = "./data/timetable.txt";
    public static File file = new File(filePath);
    private static final String UPDATING_TO_SAME_TIMING = "You are updating the lesson to the same "
            +
            "timing as before.\nPlease update lesson to a different timing.";
    private static final String INVALID_HOURS_INPUT = "Please ensure the timing for the "
            +
            "lesson falls within the school hours: 0600 - 2100";
    public static final String INVALID_UPDATE_FORMAT = "Please check the format of updating timetable:\n"
            +
            "timetable update MODULE_CODE_/LESSON_TYPE/OLD_DAY/NEW_DAY/NEW_START_TIME\n"
            +
            "Please ensure the timing for the "
            +
            "lesson falls within the school hours: 0600 - 2100";
    public static final String INVALID_DELETE_FORMAT = "Please check the format of deleting from timetable:\n"
            +
            "timetable delete MODULE_CODE/LESSON_TYPE/DAY\n"
            +
            "e.g. timetable delete CS1010/TUT/Monday\n"
            +
            "Please ensure the timing for the "
            +
            "lesson falls within the school hours: 0600 - 2100";
    public static final String MISSING_LESSON_TO_DELETE = " does not exist in timetable.\n"
            +
            "Please input valid lesson to remove.";
    public static final String INVALID_ADD_FORMAT = "Please check the format of adding to timetable:\n"
            +
            "timetable add MODULE_CODE/LESSON_TYPE/DAY/START_TIME/END_TIME\n"
            +
            "e.g. timetable add CS1010/TUT/Monday/1200/1400";
    public static final String INACCESSIBLE_PERIOD = "Please choose another slot as the "
            +
            "period is already occupied by another lesson";
    public static final String MISSING_LESSON_TO_UPDATE = "Lesson does not exist in timetable.\n"
            +
            "Try adding lesson to timetable with: timetable add";
    public static final String CORRUPT_STORAGE = "Your timetable storage file is corrupted, "
            +
            "it will be reset and cleared";
    private static final String TIMETABLE_HEADER = "+-------------+--------------------+---------"
            +
            "-----------+--------------------+--------------------+--------------------+\n"
            +
            "|             |       MONDAY       |       TUESDAY      |      WEDNESDAY     "
            +
            "|      THURSDAY      |       FRIDAY       |\n+-------------+-----------------"
            +
            "---+--------------------+--------------------+--------------------+--------------------+";
    private static final String TIMETABLE_ROW_DIVIDER = "+-------------+--------------------+----------"
            +
            "----------+--------------------+--------------------+--------------------+";
    private static final int TABLE_COLUMN_WIDTH = 20;
    private static final int TABLE_FIRST_COLUMN_WIDTH = 13;

    public Timetable() {

    }

    public Timetable(ModuleList moduleList) {
        this.moduleList = moduleList;
    }

    /**
     * Initializes the timetable with the data from timetable.txt when Kolinux starts up.
     *
     * @throws KolinuxException If the format of the data in the file is incorrect
     */
    public void initTimetable() throws KolinuxException {
        ArrayList<String> fileContents = new ArrayList<>();
        try {
            Scanner s = new Scanner(file);
            while (s.hasNext()) {
                fileContents.add(s.nextLine());
            }
            loadContent(fileContents);
        } catch (FileNotFoundException exception) {
            timetableStorage.createFilePath(filePath);
        } catch (ArrayIndexOutOfBoundsException exception) {
            clearTimetable();
            timetableStorage.clearFile();
            throw new KolinuxException(CORRUPT_STORAGE);
        }
    }

    /**
     * Loads the content of the timetable.txt file onto the 2D timetable array and lessonStorage array list
     * to carry out any timetable commands given by user.
     *
     * @param fileContents Array list of the contents of the timetable text file
     * @throws KolinuxException If the format of the file content for timetable inputting is incorrect
     */
    private void loadContent(ArrayList<String> fileContents)
            throws KolinuxException {
        for (String fileContent : fileContents) {
            String[] content = fileContent.split("/");
            switch (content[1]) {
            case "TUT":
                addLessonToTimetable(new Tutorial(content));
                break;
            case "LEC":
                addLessonToTimetable(new Lecture(content));
                break;
            case "LAB":
                addLessonToTimetable(new Lab(content));
                break;
            default:
                timetableStorage.clearFile();
                throw new KolinuxException(CORRUPT_STORAGE);
            }
        }
    }

    /**
     * Adds lesson to timetable based on the time and day of the lesson and
     * saves the timetable to the local storage by writing to timetable.txt
     * based on the time and day of the lesson.
     *
     * @param lesson Lesson which is to be added to the timetable
     * @throws KolinuxException If the format of user input is incorrect
     */
    public void addLessonToTimetable(Lesson lesson) throws KolinuxException {
        String moduleCode = lesson.getModuleCode();
        String lessonType = lesson.getLessonType();
        String description = moduleCode + " " + lessonType;
        int dayIndex = lesson.getDayIndex();
        int startTimeIndex = lesson.getStartTimeIndex();
        int endTimeIndex = lesson.getEndTimeIndex();
        if (startTimeIndex == -1 || dayIndex == -1 || endTimeIndex == -1 || startTimeIndex >= endTimeIndex) {
            throw new KolinuxException(INVALID_ADD_FORMAT);
        }
        if (!isPeriodFree(startTimeIndex, endTimeIndex, dayIndex)) {
            throw new KolinuxException(INACCESSIBLE_PERIOD);
        }
        for (int i = startTimeIndex; i < endTimeIndex; i++) {
            assert dayIndex < COLUMN_SIZE;
            assert i < ROW_SIZE;
            timetableData[i][dayIndex] = description;
        }
        lessonStorage.add(lesson);
        timetableStorage.writeToFile();
    }

    /**
     * Prints the timetable to the CLI.
     */
    public void viewTimetable() {
        System.out.println(TIMETABLE_HEADER);
        for (int i = 1; i < ROW_SIZE; i++) {
            String time = schoolHours[i - 1] + " - " + schoolHours[i];
            System.out.print("|" + time + getSpaces((TABLE_FIRST_COLUMN_WIDTH - time.length())) + "|");
            for (int j = 1; j < COLUMN_LAST_INDEX; j++) {
                System.out.print(toPrint(timetableData[i][j]));
            }
            System.out.println(toPrint(timetableData[i][COLUMN_LAST_INDEX]));
            System.out.println(TIMETABLE_ROW_DIVIDER);
        }
    }

    /**
     * Clears all the entries of the timetable, ending up with an empty timetable.
     */
    public void clearTimetable() {
        for (int i = 0; i < ROW_SIZE; i++) {
            for (int j = 0; j < COLUMN_SIZE; j++) {
                timetableData[i][j] = null;
            }
        }
        lessonStorage.clear();
        timetableStorage.writeToFile();
    }

    public void inputLesson(String[] parsedArguments) throws KolinuxException {
        try {
            String lessonType = parsedArguments[1].toUpperCase();
            String moduleCode = parsedArguments[0].toUpperCase();
            if (!isLessonInModuleList(moduleList, moduleCode)) {
                throw new KolinuxException(moduleCode + " not found in module list");
            }
            int requiredHours = getHours(moduleList, moduleCode, lessonType);
            checkZeroWorkload(requiredHours, moduleCode, lessonType);
            int inputHours = getIndex(parsedArguments[4], schoolHours) - getIndex(parsedArguments[3], schoolHours);
            int storageHours = getStorageHours(moduleCode, lessonType) + inputHours;
            checkExceedingWorkload(requiredHours, storageHours, moduleCode, lessonType);

            if (lessonType.startsWith("TUT")) {
                addLessonToTimetable(new Tutorial(parsedArguments));
            } else if (lessonType.startsWith("LEC")) {
                addLessonToTimetable(new Lecture(parsedArguments));
            } else if (lessonType.startsWith("LAB")) {
                addLessonToTimetable(new Lab(parsedArguments));
            } else {
                throw new KolinuxException(INVALID_ADD_FORMAT);
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new KolinuxException(INVALID_ADD_FORMAT);
        }
    }

    public void deleteFromTimetable(String moduleCode, String lessonType, int dayIndex) {
        String description = moduleCode + " " + lessonType;
        for (int i = 0; i < ROW_SIZE; i++) {
            assert dayIndex < COLUMN_SIZE;
            if (Objects.equals(timetableData[i][dayIndex], description)) {
                timetableData[i][dayIndex] = null;
            }
        }
    }

    public void deleteLesson(String[] parsedArguments) throws KolinuxException {
        try {
            String moduleCode = parsedArguments[0].toUpperCase();
            String lessonType = parsedArguments[1].toUpperCase();
            String day = parsedArguments[2].toLowerCase();
            int dayIndex = getIndex(day,days);
            deleteFromTimetable(moduleCode, lessonType, dayIndex);
            int removeIndex = -1;
            for (int j = 0; j < lessonStorage.size(); j++) {
                String typeInStorage = lessonStorage.get(j).getLessonType();
                String codeInStorage = lessonStorage.get(j).getModuleCode();
                String dayInStorage = lessonStorage.get(j).getDay();
                if (typeInStorage.equals(lessonType) && codeInStorage.equals(moduleCode)
                        && dayInStorage.equals(day)) {
                    removeIndex = j;
                }
            }
            String description = moduleCode + " " + lessonType;
            if (removeIndex != -1) {
                lessonStorage.remove(removeIndex);
                timetableStorage.writeToFile();
            } else {
                throw new KolinuxException(description + MISSING_LESSON_TO_DELETE);
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new KolinuxException(INVALID_DELETE_FORMAT);
        }
    }

    public void deleteAllOfModule(String moduleCode) {
        for (int i = 0; i < ROW_SIZE; i++) {
            for (int j = 0; j < COLUMN_SIZE; j++) {
                if (timetableData[i][j] != null && timetableData[i][j].contains(moduleCode)) {
                    timetableData[i][j] = null;
                }
            }
        }
        lessonStorage.removeIf(lesson -> lesson.getModuleCode().equals(moduleCode));
        timetableStorage.writeToFile();
    }

    public void updateTimetable(String[] parsedArguments) throws KolinuxException {
        try {
            String moduleCode = parsedArguments[0].toUpperCase();
            String lessonType = parsedArguments[1].toUpperCase();
            String oldDay = parsedArguments[2].toLowerCase();
            String newDay = parsedArguments[3].toLowerCase();
            String newStartTiming = parsedArguments[4];
            int startIndex = getIndex(newStartTiming, schoolHours);
            int endIndex = startIndex + getOldLessonHours(moduleCode, lessonType, oldDay);
            String[] oldTimings = getOldTimings(moduleCode, lessonType, oldDay);
            String newEndTiming = schoolHours[endIndex - 1];
            if (Objects.equals(oldDay, newDay) && Objects.equals(oldTimings[0], newStartTiming)
                    && Objects.equals(oldTimings[1], newEndTiming)) {
                throw new KolinuxException(UPDATING_TO_SAME_TIMING);
            }
            String[] parameters = new String[] {moduleCode, lessonType, newDay, newStartTiming, newEndTiming};
            if (isLessonInTimetable(moduleCode, lessonType, oldDay)) {
                deleteLesson(parsedArguments);
                inputLesson(parameters);
            } else {
                throw new KolinuxException(MISSING_LESSON_TO_UPDATE);
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new KolinuxException(INVALID_UPDATE_FORMAT);
        }
    }

    /**
     * Formats the string of the lesson which is to be printed in each box of the
     * timetable by adding spaces to the front and back of the lesson entry.
     *
     * @param data The lesson information found in the timetableData
     * @return The formatted string to be printed in each entry of the timetable
     */
    private String toPrint(String data) {
        if (data != null) {
            int spacesFront = (TABLE_COLUMN_WIDTH - data.length()) / 2;
            int spacesBack = (TABLE_COLUMN_WIDTH - data.length()) / 2 + checkOddOrEven(data);
            return getSpaces(spacesFront) + data + getSpaces(spacesBack) + "|";
        }
        return getSpaces(TABLE_COLUMN_WIDTH) + "|";
    }

    /**
     * Adds spaces to format the timetable properly.
     *
     * @param numberOfSpaces The number of spaces to be added in each entry of the timetable
     * @return The string with the spaces to be added to each entry of the timetable
     */
    private String getSpaces(int numberOfSpaces) {
        return String.format("%1$" + numberOfSpaces + "s", "");
    }

    /**
     * Checks if the length of the description for the timetable entry is even or odd,
     * this is done in order to ensure the description is in the middle each box in the timetable.
     * Ensures the format of the timetable is neat.
     *
     * @param lesson The description of the lesson
     * @return The number of extra spaces to be added to the string to ensure proper formatting
     */
    private int checkOddOrEven(String lesson) {
        if (lesson.length() % 2 == 0) {
            return 0;
        }
        return 1;
    }

    public void checkExceedingWorkload(int requiredHours, int storageHours, String moduleCode,
                                              String lessonType) throws KolinuxException {
        if (storageHours > requiredHours) {
            throw new KolinuxException("Input hours for " + moduleCode + " " + lessonType
                    +
                    " exceeds the total workload\nIt exceeds " + requiredHours + " hours\n"
                    +
                    "Please readjust the input timings or modify timetable to continue\n"
                    +
                    "with adding this lesson to the timetable.");
        }
    }

    private void checkZeroWorkload(int requiredHours, String moduleCode, String lessonType)
            throws KolinuxException {
        if (requiredHours == 0) {
            throw new KolinuxException(moduleCode + " has no " + lessonType
                    +
                    ".\nPlease add a different type of lesson.");

        }
    }

    public boolean isLessonInTimetable(String lessonCode, String lessonType, String day) {
        for (Lesson storedLesson : lessonStorage) {
            String storedCode = storedLesson.getModuleCode();
            String storedType = storedLesson.getLessonType();
            String storedDay = storedLesson.getDay();
            if (storedCode.equals(lessonCode) && storedType.equals(lessonType) && storedDay.equals(day)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPeriodFree(int startIndex, int endIndex, int dayIndex) throws KolinuxException {
        try {
            for (int i = startIndex; i < endIndex; i++) {
                if (timetableData[i][dayIndex] != null) {
                    return false;
                }
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new KolinuxException(INVALID_HOURS_INPUT);
        }
    }

    private boolean isLessonInModuleList(ModuleList moduleList, String moduleCode) {
        for (ModuleDetails module : moduleList.myModules) {
            if (Objects.equals(module.moduleCode, moduleCode)) {
                return true;
            }
        }
        return false;
    }

    private int getHours(ModuleList moduleList, String moduleCode, String lessonType) {
        for (ModuleDetails module : moduleList.myModules) {
            if (lessonType.equals("TUT") && module.moduleCode.equals(moduleCode)) {
                return (int) Math.round(module.getTutorialHours());
            } else if (lessonType.equals("LEC") && module.moduleCode.equals(moduleCode)) {
                return (int) Math.round(module.getLectureHours());
            } else if (lessonType.equals("LAB") && module.moduleCode.equals(moduleCode)) {
                return (int) Math.round(module.getLabHours());
            }
        }
        return 0;
    }

    private int getOldLessonHours(String moduleCode, String lessonType, String day) {
        for (Lesson lesson : lessonStorage) {
            if (lesson.getModuleCode().equals(moduleCode) && lesson.getLessonType().equals(lessonType)
                    && lesson.getDay().equals(day)) {
                return lesson.getEndTimeIndex() - lesson.getStartTimeIndex();
            }
        }
        return -1;
    }

    private int getStorageHours(String moduleCode, String lessonType) {
        int hourCount = 0;
        for (Lesson storedLesson : lessonStorage) {
            if (storedLesson.getModuleCode().equals(moduleCode)
                    && storedLesson.getLessonType().equals(lessonType)) {
                hourCount += storedLesson.getHours();
            }
        }
        return hourCount;
    }

    private String[] getOldTimings(String moduleCode, String lessonType, String day) {
        String[] timings = new String[2];
        for (Lesson lesson : lessonStorage) {
            if (lesson.getModuleCode().equals(moduleCode) && lesson.getLessonType().equals(lessonType)
                    && lesson.getDay().equals(day)) {
                timings[0] = lesson.getStartTime();
                timings[1] = lesson.getEndTime();
            }
        }
        return timings;
    }

}
