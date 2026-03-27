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

## Build

To build the project and generate shaded JARs for all platforms:

```bash
./gradlew build
```

The resulting JARs will be found in:
- `core-bukkit/build/libs/`
- `core-velocity/build/libs/`
- `core-bungeecord/build/libs/`