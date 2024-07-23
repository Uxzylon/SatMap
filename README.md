# CustomModelTools

Spigot Plugin with tools for CustomModelData.

## Features

- Enforce ResourcePack Download
- Parsing of CustomModelData from the ResourcePack
- Give and Place CustomModelData Items by Category and Name
- Position and Rotation Editor

## How it works

Your resource pack will obviously need to be configured to use CustomModelData.

The plugin will search for items in "assets/minecraft/models/item/"

From there, it will look for the "predicate" fields in the json files, then the "custom_model_data" field and "model" field.

The category will be the first part of the "model" field without the namespace (e.g. "item" in "mypack:item/mymodel").

The name will be the second part of the "model" field without the namespace (e.g. "mymodel" in "mypack:item/mymodel").

The plugin will then use the category and name in the commands and in the tab completion.

## Commands

- `/model3d` - Main Command
- `/model3d reload` - Reloads the Plugin
- `/model3d give` - Opens a GUI to get a CustomModelData Item
- `/model3d give <category> <name>` - Gives the Player the Item with the given Category and Name
- `/model3d place` - Opens a GUI to place a CustomModelData Item
- `/model3d place <category> <name>` - Places the Item with the given Category and Name at the Players Location
- `/model3d select` - Selects the nearest ArmorStand
- `/model3d unselect` - Deselects the selected ArmorStand
- `/model3d move` - Moves the selected ArmorStand using a GUI
- `/model3d switch` - Switches the selected CustomModelData between ArmorStand Hand and Head
- `/model3d remove` - Removes the selected ArmorStand

## Permissions

- `custommodeltools.command.reload` - Allows the use of `/model3d reload`
- `custommodeltools.command.give` - Allows the use of `/model3d give`
- `custommodeltools.command.place` - Allows the use of `/model3d place`
- `custommodeltools.command.select` - Allows the use of `/model3d select`
- `custommodeltools.command.unselect` - Allows the use of `/model3d unselect`
- `custommodeltools.command.move` - Allows the use of `/model3d move`
- `custommodeltools.command.switch` - Allows the use of `/model3d switch`
- `custommodeltools.command.remove` - Allows the use of `/model3d remove`
- `custommodeltools.command.*` - Allows the use of all commands
- `custommodeltools.bypass.resourcepack` - Allows the Player to bypass the ResourcePack Download
- `custommodeltools.bypass.*` - Allows the Player to bypass all restrictions
- `custommodeltools.*` - Allows everything

## Config

Every text in the plugin can be changed in the config.yml.

You also have several options to change the behavior of the plugin:

- `ResourcePack.url` - The URL to the ResourcePack
- `ResourcePack.hash` - The SHA-1 Hash of the ResourcePack
- `ResourcePack.kickOnFail` - Whether to kick the Player if the ResourcePack isn't applied