# Origins: Legacy
This is an unofficial forward port of the original [Origins](https://modrinth.com/mod/origins) mod, based from the official MC 1.20.1 build, for supporting
newer versions of Minecraft while also offering backwards compatibility for Origins datapacks created for 1.20.1 and prior.

The reason why this fork had to occur was due to 3 reasons:
- The newer builds of Origins are horribly buggy, having experienced heavy refactors to the codebase that also end up breaking
  the Origins datapacks that came prior, requiring extensive changes to support the new versions.
- Pull requests and issues with the current Origins codebase take a long time to be resolved.
  - I have submitted a [pull request](https://github.com/apace100/origins-fabric/pull/801) to fix a bug where the [`origins_server.json` file gets ignored for disabling powers](https://github.com/apace100/origins-fabric/issues/794).
    The PR was seen a month later, and as of May 11th, it has still not been merged. It is 3 lines of code.
- The existing Origins for Forge port is almost impossible to work with as a developer. If you're making an addon mod,
  the existing Forge port does NOT abide by any of the rules you had thought of previously for creating addons. No joke, it was
  easier to use Connector with the Origins mod in order to make a Forge addon compared to trying to learn this entirely different
  codebase.

If you need help with **creating data packs** for the mod, you can visit [the official wiki](https://origins.readthedocs.io/en/1.10.0/). The official wiki is still sufficient for
making datapacks, as long as you are under the `1.10.0` branch, which is what this fork is based on.

If you want to **report a bug**, please visit [the issue tracker](https://git.devos.one/BluSpring/origins-legacy/issues). Make sure to check other existing issues first, and post detailed information about which mods and which version you are using, what you would expect to happen, and what happens instead. Also always include the log of your client and the server if possible.
