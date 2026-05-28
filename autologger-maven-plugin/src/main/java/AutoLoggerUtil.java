import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;

public class AutoLoggerUtil {
    public static String getClassName(File classFile, String classesDir) {
        String relativePath = classFile.getAbsolutePath()
                .replace(classesDir, "")
                .replace(File.separator, ".")
                .replace(".class", "");

        return relativePath.startsWith(".") ? relativePath.substring(1) : relativePath;
    }

    public static boolean fieldNotExists(CtClass ctClass, String fieldName) {
        try {
            ctClass.getField(fieldName);
            return false;
        } catch (NotFoundException e) {
            return true;
        }
    }
}
