# Minecraft Environment Manager

A cross-platform (Bukkit, Velocity, BungeeCord) tool to manage environment-specific configurations for Minecraft servers and proxies.

## Features

- **Cross-Platform**: Support for Paper/Spigot, Velocity, and BungeeCord.
- **Multiple Formats**: Support for YAML, HOCON (`.conf`), and JSON configuration files.
- **Environment Variable Loading**:
    - **Bulk load from `.env`**: Automatically load multiple keys from a `.env` file.
    - **Specific key loading**: Load specific values from `.env` files.
    - **File content loading**: Use the raw content of any file as an environment value.
- **LIFO Conflict Resolution**: Later environment definitions override earlier ones.
- **Auto-Versioning & Migration**: Automatically updates configuration files to the latest version.
- **Reloadable**: Apply changes instantly using `/envmanager reload`.
- **Hijack Mode (Agent)**: Apply changes in-memory without modifying files on disk using a Java Agent.

## How it Works

The plugin uses a main configuration file (`config.yml`, `config.conf`, or `config.json`) to define mappings between environment keys and target configuration files.

### Configuration Example (`config.yml`)

```yaml
# Configuration version (managed automatically)
config-version: 1

# Global environment variables
environments:
  # Direct definition
  DB_HOST: "localhost"
  STAGING: "true"

  # Bulk load from a .env file (Last-in Wins / LIFO)
  "@": ".env"

  # Loading a specific key from a .env file
  DB_PASSWORD: "@secrets/.env"

  # Loading the entire content of a file (useful for scripts or keys)
  PROMOTIONAL_TEXT: "@promo.txt"

# Hijack Configuration (Requires Java Agent)
# If enabled, changes are applied in-memory and intercepted at the JVM level.
# No physical files are modified on disk.
hijack:
  enabled: false

# Definitions of files to be updated by this plugin
targets:
  # Apply to a YAML file
  - path: "plugins/MyPlugin/config.yml"
    mappings:
      "mysql.host": "DB_HOST"
      "mysql.password": "DB_PASSWORD"
      "debug_mode": "STAGING"

  # Apply to a JSON file
  - path: "plugins/MyPlugin/settings.json"
    mappings:
      "api.key": "PROMOTIONAL_TEXT"

  # Apply to a HOCON file
  - path: "plugins/MyPlugin/config.conf"
    mappings:
      "application.debug": "STAGING"
```

## Hijack Mode (Java Agent)

Hijack Mode is a powerful feature that allows the Environment Manager to intercept file access calls at the JVM level. When enabled, the plugin provides a "virtual" view of configuration files to other plugins, applying your environment variables in-memory without ever writing them to the physical file on disk.

### How to use
1. Enable `hijack.enabled: true` in your `config.yml`.
2. Add the agent to your server startup command:
   ```bash
   java -javaagent:EnvironmentManager-agent.jar -jar your-server.jar
   ```

### Pros and Cons

| Feature | Pros | Cons |
| :--- | :--- | :--- |
| **Persistence** | Original files on disk remain untouched (perfect for immutable/cloud setups). | If the agent is removed, the server reverts to original disk values immediately. |
| **Performance** | Bypasses disk I/O for environment-specific configuration values. | Minor overhead at startup for bytecode instrumentation. |
| **Compatibility**| Works with any plugin (e.g., LuckPerms, Essentials) regardless of their code. | Requires a `-javaagent` JVM startup argument. |
| **Maintenance** | No need to "revert" or "clean up" files on shutdown. | Debugging can be more complex as what you see on disk isn't what the plugin sees. |

## Build

To build the project and generate shaded JARs for all platforms:

```bash
./gradlew build
```

The resulting JARs will be found in:
- `core-bukkit/build/libs/`
- `core-velocity/build/libs/`
- `core-bungeecord/build/libs/`
- `core-agent/build/libs/` (The Java Agent)