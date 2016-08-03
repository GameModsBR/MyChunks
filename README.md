#**This project was reworked on: https://github.com/GameModsBR/MineCity , this branch is no longer being developed**

# MyChunks

MyChunks is a chunk protection plugin for Minecraft servers using Sponge. It's just starting to be developed right now so all help is appreciated.

[![Build Status](https://travis-ci.org/GameModsBR/MyChunks.svg?branch=master)](https://travis-ci.org/GameModsBR/MyChunks) [![Code Quality](https://img.shields.io/codacy/6e4a916b46a8493881d709244c977e87/development.svg)](https://www.codacy.com/app/jose-rob-jr/MyChunks/dashboard) [![Maintenance](https://img.shields.io/maintenance/no/2015.svg)]()

## Goals
* The main goal is to protect chunks from modifications by unauthorized players in all possible ways using permissions.
* The plugin will not require mods or plugins installed on the player's game but we plain to create client GUI later.
* Chunks can be grouped to create a region that shares the same configurations
* Chunks will also have it's own configurations that overrides the region, for example it can allow players to open chests only on that chunk

### Development goal
* The protections must be highly optimized for performance
* All chunk data must be loaded and unloaded with the chunk
* The data must be saved in binary form without databases
* An optional database support that uses standardized SQL, so the server owners can use any database.
* A way to allow the server owners to change the data storage engine (database or binary) without loosing data.
