package seedu.duke.module;

import java.util.Map;

public class ModuleDb {
    private static JsonReader jsonReader = new JsonReader();
    private static Map<String, ModuleDetails> moduleDetailsMap;

    public static void initModuleDb() {
        assert moduleDetailsMap.isEmpty() : "moduleDetailsMap is already initialized";
        moduleDetailsMap = jsonReader.readModuleDb();
    }

    public static ModuleDetails getModuleInfo(String code) {
        return moduleDetailsMap.get(code);
    }
}
