import org.setokk.atl.annotation.AutoLog;
import javassist.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "add-loggers", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class AutoLoggerMojo  extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String classesDir = project.getBuild().getOutputDirectory();
        File root = new File(classesDir);
        if (!root.exists()) {
            getLog().warn("No classes output directory found");
            return;
        }
        List<File> classes = this.searchClasses(root.toPath());
        if (classes.isEmpty()) {
            getLog().warn("No classes found");
            return;
        }
        this.addLoggersToClasses(classes, classesDir);
    }

    private List<File> searchClasses(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(path -> path.toString().endsWith(".class"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            getLog().warn("Error searching classes", e);
            return Collections.emptyList();
        }
    }

    private void addLoggersToClasses(List<File> classes, String classesDir) {
        try {
            ClassPool classPool = ClassPool.getDefault();
            classPool.insertClassPath(classesDir); // Project files
            classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader())); // External dependencies
            for (File file : classes) {
                String className = getClassName(file, classesDir);
                CtClass ctClass = classPool.get(className);
                if (!ctClass.hasAnnotation(AutoLog.class)) {
                    continue;
                }
                AutoLog annotation = (AutoLog) ctClass.getAnnotation(AutoLog.class);
                CtField loggerField = CtField.make(
                        "private static final org.apache.logging.log4j.Logger LOGGER = " +
                                "org.apache.logging.log4j.LogManager.getLogger(" + ctClass.getSimpleName() + ".class);",
                        ctClass
                );
                ctClass.addField(loggerField);
                for (CtMethod method : ctClass.getDeclaredMethods()) {
                    boolean isMethodExcluded = Arrays.stream(annotation.excludedMethods()).anyMatch(m -> m.equals(method.getName()));
                    if (isMethodExcluded) {
                        continue;
                    }
                    String loggerMsgBefore = annotation.beforeMsgPattern()
                            .replace(AutoLog.CLASS_PLACEHOLDER, ctClass.getSimpleName())
                            .replace(AutoLog.METHOD_PLACEHOLDER, method.getName());
                    String loggerMsgAfter = annotation.afterMsgPattern()
                            .replace(AutoLog.CLASS_PLACEHOLDER, ctClass.getSimpleName())
                            .replace(AutoLog.METHOD_PLACEHOLDER, method.getName());
                    String logLevel = annotation.level().name().toLowerCase();

                    method.insertBefore(String.format("LOGGER.%s(\"%s\");", logLevel, loggerMsgBefore));
                    String timeTaken = "\"\"";
                    if (annotation.debugEnabled()) {
                        method.addLocalVariable("start", CtPrimitiveType.longType);
                        method.insertBefore("start = System.currentTimeMillis();");
                        timeTaken = "\", time taken: \" + String.valueOf((System.currentTimeMillis() - start) / 1000.0) + \"s\"";
                    }
                    method.insertAfter(String.format("LOGGER.%s(\"%s\" + %s);", logLevel, loggerMsgAfter, timeTaken), false);
                }
                ctClass.writeFile(classesDir);
                getLog().info("Add logger to class: " + ctClass.getSimpleName());
            }
        } catch (Exception e) {
            getLog().error("Error modifying classes", e);
        }
    }

    private String getClassName(File classFile, String classesDir) {
        String relativePath = classFile.getAbsolutePath().replace(classesDir, "")
                .replace(File.separator, ".").replace(".class", "");
        return relativePath.startsWith(".") ? relativePath.substring(1) : relativePath;
    }
}
