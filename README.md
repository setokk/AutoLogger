# AutoLogger
AutoLogger is a custom Maven Plugin that allows automatic logging of methods using Log4J for both production and debugging purposes. 
AutoLogger works by manipulating the bytecode of compiled classes
### Usage Example
Marking a class with the ```@AutoLog``` annotation will add logging sections on all methods.
Below is a more complex configuration that excludes a method from having logging statements, and sets the
debugEnabled property to true:
```java
import org.setokk.atl.annotation.AutoLog;

import static org.setokk.atl.annotation.AutoLog.LogLevel;

@AutoLog(
        level = LogLevel.INFO,
        excludedMethods = {"getUsers"},
        debugEnabled = true
)
public class UserService {
    public void loginUser() throws InterruptedException {
        Thread.sleep(1000);
    }

    public void registerUser() throws InterruptedException {
        Thread.sleep(2000);
    }

    public void getUsers() throws InterruptedException {
        Thread.sleep(3000);
    }
}
```
When called, these methods will produce the output:
```shell
2025-02-08 18:14:35 [Main.main()] INFO  - UserService->loginUser - ENTER
2025-02-08 18:14:36 [Main.main()] INFO  - UserService->loginUser - LEAVE, time taken: 1.022s
2025-02-08 18:14:36 [Main.main()] INFO  - UserService->registerUser - ENTER
2025-02-08 18:14:38 [Main.main()] INFO  - UserService->registerUser - LEAVE, time taken: 2.009s
```
### Importing on Maven
While right now the specific version of log4j cannot be modified, 
it will be possible to do so in the future.

Dependencies section:
```xml
<dependencies>
    <dependency>
        <groupId>org.setokk.atl</groupId>
        <artifactId>autologger-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.24.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.24.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.24.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
Plugins section:
```xml
<build>
<plugins>
    <plugin>
        <groupId>org.setokk.atl</groupId>
        <artifactId>autologger-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <id>auto-run-logger</id>
                <phase>process-classes</phase>
                <goals>
                    <goal>add-loggers</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
</build>
```