# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based proxy service that bridges OpenHAB with Comelit SerialBridge hardware. The application acts as a REST API proxy that translates OpenHAB commands to SerialBridge API calls for controlling home automation devices (lights, shutters, etc.).

## Architecture

The project follows a layered architecture:

- **Main Entry Point**: `it.grep.openhab.comelit.proxy.Main` - Sets up Spark web server and routes
- **Controller Layer**: `it.grep.openhab.comelit.proxy.Controller` - Handles HTTP routes and request processing
- **SerialBridge Integration**: `it.grep.openhab.comelit.serialbridge.SerialBridgeAPI` - Manages communication with Comelit SerialBridge
- **State Management**: `it.grep.openhab.comelit.serialbridge.ItemsStateCache` - Caches device states for performance
- **Configuration**: `it.grep.openhab.comelit.config.*` - Configuration management with JSON support

### Key Components

- **Spark Framework**: Used for REST API endpoints
- **SerialBridgeAPI**: Core integration that communicates with Comelit hardware via HTTP
- **ItemsStateCache**: Singleton cache that manages device state with invalidation on commands
- **Device Types**: Supports `lights`, `shutters`, and `other` device categories

## Development Commands

### Build and Package
```bash
mvn compile                    # Compile the project
mvn package                    # Build JAR with dependencies
```

### Run Application
```bash
java -jar target/openhab-comelit-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Configuration Files
- `/etc/openhab-comelit-proxy.conf` - Main application configuration (JSON format)
- `/etc/openhab-comelit-proxy_log.conf` - Log4j2 configuration

## API Endpoints

The proxy exposes these REST endpoints:
- `GET /{type}/id/{id}/{cmd}` - Send command to device by ID
- `GET /{type}/id/{id}` - Get device status by ID  
- `GET /{type}/desc/{desc}` - Get device status by description
- `GET /{type}/all` - Get all devices status

Where `{type}` is one of: `lights`, `shutters`, `other`

## Dependencies

- Spark Java (web framework)
- Log4j2 (logging)
- Gson (JSON processing)
- Guava (utilities)
- Jetty HTTP Client (for SerialBridge communication)

## Testing

No test framework is currently configured. The application uses integration testing with the actual SerialBridge hardware.