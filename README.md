# EagleEyePlugin
## A Minecraft plugin that protects inventories such as chests, hoppers, furnaces, etc.
Supported inventory blocks can by protected by a player by placing a WALL_SIGN against a protectable block and typing
the new owners name in a valid syntax format, `@<username>`,  e.g. `@Notch`.

Existing owners or Ops can add additional owners allowing players to share inventories.

Plugin interacts with Mojang API to convert to and from UUID's and Usernames. A memory based (volatile) cache is 
implemented to reduce the number of requests reaching Mojang API endpoint. Caching can be disabled from `config.yml`
yaml file. Plugin will also automatically update when a new release is available on Bukkit.org (Project ID: 340506). 

**Events intercepted by plugin**:
  - Player placing signs against valid inventory blocks
  - Sign change events used to understand player intentions if syntax is valid
  - Player joining automatically caches player details to reduce API load
  - User manipulating an inventory triggers ownership checks
  - User breaking inventory blocks triggers ownership checks
  
**Supported blocks**:
  - Chests, Trapped Chests and Barrels
  - Furnaces including Blast Furnace and Smokers
  - Dispensers and Droppers
  - Looms
  - Hoppers
  - Grindstones

## Default config.yml
```yaml
# Caching is used to store usernames and UUIDs (1 week by default)
Caching:
  enabled: true
  expiryInMinutes: 604800
AutoUpdate:
  enabled: true
```
