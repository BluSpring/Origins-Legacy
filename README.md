# Origins: Legacy
This is an unofficial forward port of the original [Origins](https://modrinth.com/mod/origins) mod, based from the official MC 1.20.1 build, for supporting
newer versions of Minecraft while also offering backwards compatibility for Origins datapacks created for 1.20.1 and prior.

The reason why this fork had to occur was because:
- The newer builds of Origins were horribly buggy at the time of writing, having experienced heavy refactors to the codebase that also end up breaking the Origins datapacks that came prior, often requiring extensive changes to properly support the new versions.
- Origins is being *very* slow to update to newer versions of Minecraft. I just found it easier to work on it here myself, and also be able to update all the old 1.20.1 Origins datapacks and mods and make them all work here myself. Genuinely made things significantly easier than having to deal with the updated codebase.

If you need help with **creating data packs** for the mod, you can visit [the official wiki](https://origins.readthedocs.io/en/1.10.0/). The official wiki is still sufficient for
making datapacks, as long as you are under the `1.10.0` branch, which is what this fork is based on.

If you want to **report a bug**, please visit [the issue tracker](https://git.devos.one/BluSpring/origins-legacy/issues). Make sure to check other existing issues first, and post detailed information about which mods and which version you are using, what you would expect to happen, and what happens instead. Also always include the log of your client and the server if possible.

## Why isn't this published to Modrinth / CurseForge?
Currently waiting for Modrinth's resolution on Origins: Legacy, I realized I was supposed to submit something to
prove this codebase was different from official. [Currently available on CurseForge though](https://www.curseforge.com/minecraft/mc-mods/origins-legacy).

## Does this support custom origins made for Origins 1.21.1?
Not really, since the codebases, feature sets and even the main data formats have changed quite significantly
in the new codebase. On the other hand, Origins: Legacy tries to retain compatibility with the 1.20.1 origins datapacks as best as possible,
usually only requiring changes at the regular Minecraft datapack level, which is usually much better documented.  

## Additional Features
Any new features added by Origins: Legacy will be documented here, as I've likely added and needed them for some things myself.

### Data Types
#### Behavior Type
| Value   | Description                                                                                                                                                  |
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| passive | Tries to prevent the following entity from targeting the actor by any means necessary.                                                                       |
| neutral | Entities will not target the actor immediately, unless provoked. If the target has already been set externally, it will override the neutral behavior here.  |
| hostile | Entities will always try to target this actor. Pretty much useless if the entity doesn't actually have an attack goal.                                       |

### Power Types
#### Modify Behavior
Changes the behaviour of certain mobs that match the specified conditions.

| Field              | Type                                                                                                 | Default | Description                                                                                                                                                          |
|--------------------|------------------------------------------------------------------------------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| bientity_condition | [Bi-entity Condition Type](https://origins.readthedocs.io/en/1.10.0/types/bientity_condition_types/) |         | Allows selecting matching the entity based on the following bi-entity condition. The `target` refers to the entity that is to target the `actor`, who has the power. |
| behavior_type      | Behavior Type                                                                                        |         | Sets the behavior type for entities that match the condition.                                                                                                        |

### Condition Types
#### Distance to Ground (Entity)
Checks how far the entity is from the ground. Will be refactored later to support comparisons, but will not require any changes to support it. 

| Field    | Type          | Default | Description                                             |
|----------|---------------|---------|---------------------------------------------------------|
| distance | Integer       |         | The maximum distance between the entity and the ground. |