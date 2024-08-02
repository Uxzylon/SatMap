# SatMap

Spigot Plugin that allows you to place satellite images in Minecraft.

BKCommonLib is required : https://github.com/bergerhealer/BKCommonLib

## Features

- Load and preview satellite images using lat/lon coordinates
- Place satellite images in Minecraft by converting pixel colors to blocks
- Rotate maps using x, y, z angles
- Blacklist certain blocks from being placed

## How it works

The plugin will download the minecraft client jar corresponding to the server version and extract the block textures from it. It will then use these textures to convert the pixel colors of the satellite image to blocks and items.

When loading a satellite image, the plugin will download images from Google Maps and display it on a map item.

When placing a satellite image, the plugin will convert the pixel colors of the image to blocks. The blocks will then be placed at the player's location, with the specified rotations.

## Commands

- `/satmap` - Main Command
- `/satmap reload` - Reloads the Plugin


- `/satmap load <lat> <lon> <width> <height> <ratio>` - Loads a Satellite Image. `<lat>` and `<lon>` are the GPS coordinates of the center of the image. `<width>` and `<height>` are the dimensions of the image in meters and `<ratio>` is the number of blocks per meter (1.0 will make 1 block = 1 meter).


- `/satmap place <x_rotation> <y_rotation> <z_rotation>` - Places the loaded Satellite Image with the specified rotations ("90 0 0" will make the image flat and face south).

## Permissions

- `satmap.command.reload` - Allows the use of `/satmap reload`
- `satmap.command.place` - Allows the use of `/satmap place`
- `satmap.command.load` - Allows the use of `/satmap load`
- `satmap.command.*` - Allows the use of all commands
- `satmap.*` - Allows everything

## Config

Every text in the plugin can be changed in the config.yml.

You can also edit the blocks that are blacklisted from being placed. You just need to edit the `forbidden-blocks` list with the `Material` names of the blocks you want to blacklist.