# Origins: Legacy
This is an unofficial forward port of the original [Origins](https://modrinth.com/mod/origins) mod, based from the official MC 1.20.1 build, for supporting
newer versions of Minecraft while also offering backwards compatibility for Origins datapacks created for 1.20.1 and prior.

The reason why this fork had to occur was due to 4 reasons:
- The newer builds of Origins are currently horribly buggy, having experienced heavy refactors to the codebase that also end up breaking
  the Origins datapacks that came prior, requiring extensive changes to support the new versions.
- Origins is being *very* slow to update to newer versions of Minecraft. I just found it easier to work on it here myself, and also be able to update all the old
  1.20.1 Origins and make them all work here. Genuinely made things significantly easier.
- Pull requests and issues with the current Origins codebase currently take a long time to be resolved.
  - I have submitted a [pull request](https://github.com/apace100/origins-fabric/pull/801) to fix a bug where the [`origins_server.json` file gets ignored for disabling powers](https://github.com/apace100/origins-fabric/issues/794).
    The PR was seen a month later, and as of May 11th, it has still not been merged. It is 3 lines of code.
- The existing Origins for Forge port is almost impossible to work with as a developer. If you're making an addon mod,
  the existing Forge port does NOT abide by any of the rules you had thought of previously for creating addons. No joke, it was
  easier to use Connector with the Origins mod in order to make a Forge addon compared to trying to learn this entirely different
  codebase. While at the moment Origins: Legacy does not support Forge/NeoForge, support will be implemented soon.

If you need help with **creating data packs** for the mod, you can visit [the official wiki](https://origins.readthedocs.io/en/1.10.0/). The official wiki is still sufficient for
making datapacks, as long as you are under the `1.10.0` branch, which is what this fork is based on.

If you want to **report a bug**, please visit [the issue tracker](https://git.devos.one/BluSpring/origins-legacy/issues). Make sure to check other existing issues first, and post detailed information about which mods and which version you are using, what you would expect to happen, and what happens instead. Also always include the log of your client and the server if possible.

## Why isn't this published to Modrinth / CurseForge?
I tried to publish it to Modrinth, but apparently it "wasn't significantly divergent from the source work". Will probably go on CurseForge, as much as I don't really like the platform.

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