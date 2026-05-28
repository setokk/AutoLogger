public enum LoggerImplementation {
    LOG4J_1(
            "org.apache.log4j.Logger",
            "org.apache.log4j.Logger.getLogger(%s.class)"
    ),
    LOG4J_2(
            "org.apache.logging.log4j.Logger",
            "org.apache.logging.log4j.LogManager.getLogger(%s.class)"
    ),
    SLF4J_OR_LOGBACK(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory.getLogger(%s.class)"
    ),
    JUL(
            "java.util.logging.Logger",
            "java.util.logging.Logger.getLogger(%s.class.getName())"
    ),
    COMMONS_LOGGING(
            "org.apache.commons.logging.Log",
            "org.apache.commons.logging.LogFactory.getLog(%s.class)"
    ),
    JBOSS_LOGGING(
            "org.jboss.logging.Logger",
            "org.jboss.logging.Logger.getLogger(%s.class)"
    ),
    FLOGGER(
            "com.google.common.flogger.FluentLogger",
            "com.google.common.flogger.FluentLogger.forEnclosingClass()"
    ),
    SYSTEM_LOGGER(
            "java.lang.System.Logger",
            "java.lang.System.getLogger(%s.class.getName())"
    );

    private final String fieldType;
    private final String factoryMethod;
    private final String initializer;

    LoggerImplementation(String fieldType, String factoryMethod) {
        this.fieldType = fieldType;
        this.factoryMethod = factoryMethod;
        this.initializer = fieldType + " %s = " + factoryMethod + ";";
    }

    public String fieldType() {
        return fieldType;
    }

    public String factoryMethod() {
        return factoryMethod;
    }

    public String formattedInitializer(String loggerName, String className) {
        return String.format(initializer, loggerName, className);
    }
}
