# Minecraft Environment Manager

A cross-platform (Bukkit, Velocity, BungeeCord) tool to manage environment-specific configurations for Minecraft servers and proxies.

## Features

- **Cross-Platform**: Support for Paper/Spigot, Velocity, and BungeeCord.
- **Centralized Environment Keys**: Store global environment variables in a single `config.yml`.
- **Target Mapping**: Automatically apply environment keys to specific paths and keys in other plugin configuration files (YAML, JSON supported).
- **Reloadable**: Apply changes instantly using `/envmanager reload`.
- **Extensible API**: Built on [Configurate](https://github.com/SpongePowered/Configurate) for easy extension to other file formats.

## How it Works

The plugin uses a `config.yml` to define mappings between environment keys and target configuration files.

### Environment Manager `config.yml`
```yaml
# Global environment variables
environments:
  DB_HOST: "localhost"
  API_SECRET: "secret-123"

# Targets to apply environment keys to
targets:
  - path: "plugins/MyPlugin/config.yml"
    mappings:
      "mysql.host": "DB_HOST"
      "authentication.key": "API_SECRET"
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