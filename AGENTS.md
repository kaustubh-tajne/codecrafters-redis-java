# AGENTS.md - Codecrafters Redis Java Clone

## Project Overview
This is a Java implementation of a Redis server clone for the Codecrafters challenge. The project builds a toy Redis that handles basic commands like `PING`, `SET`, and `GET`, teaching concepts like event loops and the Redis protocol.

## Architecture
- **Entry Point**: `src/main/java/Main.java` - Single main class that starts the server
- **Server**: Listens on port 6379 using `ServerSocket` with `setReuseAddress(true)` to avoid "Address already in use" errors during testing
- **Future Components** (as stages progress):
  - RESP protocol parser for Redis serialization format
  - Command handler for Redis commands (PING, SET, GET, etc.)
  - In-memory key-value store
  - Concurrent client handling (threads or NIO)

## Development Workflow
- **Build**: `mvn -q -B package -Ddir=/tmp/codecrafters-build-redis-java` (quiet, batch mode)
- **Run Locally**: `./your_program.sh` - Builds and executes the fat JAR with `java --enable-preview`
- **Submit Solution**: `git commit -am "pass Xth stage"; git push origin master` - Test output streams to terminal
- **Debug**: Use `System.out.println()` - Logs appear in test output

## Technical Setup
- **Java Version**: 25 with preview features enabled (`--enable-preview`)
- **Build Tool**: Maven with assembly plugin for fat JAR (`jar-with-dependencies`)
- **Dependencies**: None initially - pure Java standard library
- **IDE**: IntelliJ IDEA (`.idea/` directory present)

## Coding Patterns
- **Server Setup**: Bind to port 6379, accept client connections in a loop
- **Error Handling**: Catch `IOException`, log messages, ensure socket cleanup in `finally` block
- **Concurrency**: Will need thread-per-client or NIO for multiple connections (future stages)
- **Protocol**: Implement RESP (Redis Serialization Protocol) - arrays, bulk strings, simple strings

## Key Files
- `pom.xml`: Maven config with Java 25, preview features, assembly plugin
- `your_program.sh`: Local run script (compiles and executes)
- `.codecrafters/compile.sh` & `run.sh`: Remote build/run scripts (match local)
- `codecrafters.yml`: Debug logging toggle, Java version selection

## Stage Progression
1. Uncomment server socket code in `Main.java` to bind and accept connections
2. Implement RESP parsing and basic command handling
3. Add SET/GET with in-memory storage
4. Handle multiple clients concurrently
5. Add more Redis commands and features

Focus on incremental implementation - each stage builds on the previous.</content>
<parameter name="filePath">D:\Projects\Redis Clone\codecrafters-redis-java\AGENTS.md
