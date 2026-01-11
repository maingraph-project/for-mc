# Maingraph for MC

![A badge showing "ModLoader" is "NeoForge"](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)

**Maingraph for MC** is a visual blueprint engine designed for Minecraft. It allows for custom logic through intuitive node connections, requiring no (or very little) coding.

---

Official Website: [mc.maingraph.opens.ltd](https://mc.maingraph.opens.ltd)

For detailed tutorials, please refer to [zhcn-docs.mc.maingraph.opens.ltd](http://zhcn-docs.mc.maingraph.opens.ltd/) (Note: Documentation is currently in Chinese).

---
# Compatibility of Maingraph for MC

Known compatibility with the following mods:

1. **IMBlocker**: Since v0.2.2, the Blueprint Editor uses Minecraft’s native EditBox, making it compatible with IMBlocker.
2. **Just Enough Characters**: Starting from v0.2.3, the Blueprint Editor fully supports JECh via reflection.
   - **Current Status**: When JECh is detected, the search feature automatically enables Pinyin initial matching (including node names, categories, port names, etc.).
3. **Physics Mod**: In F6 physics UI mode, the Blueprint Editor does not participate in physics simulation.
   - **Planned Fix**: No fix planned.
   - "Does anyone actually edit with this thing on...?"
4. **ImmediatelyFast**: Testing shows no noticeable FPS improvement in the Blueprint Editor after installing ImmediatelyFast.

## Quick Start

1. **Installation**: Place the mod in your Minecraft `mods` folder (requires NeoForge).
2. **Open Editor**: Once in-game, press `Ctrl + M` to open the Blueprint Manager.
3. **Create Blueprint**: Enter a name and click "Create".
4. **Start Authoring**: Right-click on the canvas to add nodes, connect white arrows (execution flow) and colored dots (data flow).
5. **Save & Run**: Click "Save", and your logic will start running in the background.

---

## Example Addon
If you want to develop your own nodes, check out the example addon:
[maingraph-project/mgmc-example](https://github.com/maingraph-project/mgmc-example)

---

This project is licensed under the MIT License.
