# QLox

A [Lox](http://www.craftinginterpreters.com/the-lox-language.html) interpreter written in Kotlin.

## Usage

### Requirements

- Eclipse Temurin™ OpenJDK 17 (https://adoptium.net/temurin/releases/)
- **[optional]** JetBrains IntelliJ IDEA (https://www.jetbrains.com/idea/download/)

### Use pre-built JAR

1. Get artifact from latest GitHub Actions run
2. Unzip the artifact
3. Create a plain text file with the Lox code you want to run
4. Check the version of the interpreter of the JAR with the `-version` flag

    ```bash
    java -jar QLox-[version]-[<stage>]-[revision].jar -version
    ```
   
5. Run the JAR with the file as argument

    ```bash
    java -jar QLox-[version]-[<stage>]-[revision].jar <file>
    ```

### Build from source

1. Clone the repository
2. Open the project in IntelliJ IDEA
3. Wait for Gradle to configure the project
4. Run the Gradle `shadowJar` task (under `Tasks > shadow`)
5. Find the built JAR in `build/distributions`
6. Follow the steps from the previous section to run the JAR
