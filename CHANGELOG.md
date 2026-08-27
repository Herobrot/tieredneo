# 1.1.0
## Added:
- HerosLib dependency added
- Hero's Levels compat added
- Easy Anvils compat added
- Added spanish translation
## Fixed:
- Fixed ([#1](https://github.com/Herobrot/tieredneo/issues/1)) about the loot on chest not adding the tiers/modifiers
## Changed:
- Rework of the code. From the player's perspective, there shouldn't be any noticeable difference, other than better performance (I hope).
  - Deleted the implementation about tabs and now using the HerosLib feature with tabs.
- Now the luck (and the smithing level if Hero's Levels is present) is additive, reducing the bigger weight (Common)
  - Expect having more Epic and Legendary modifier. But Unique is still very rare.
---
# 1.0.1
## Added:
- Tears and shame from dev
## Fixed:
- Fixed ghost icons whenever a material was smaller than the slot on reforge
- Fixed shields ids on datapack
- Fixed quick move not modifying the items
- Fixed merchant always trade the same modified item
## Changed:
- &nbsp;
---
# 1.0.0
## Added:
- Initial release
## Fixed:
- &nbsp;
## Changed:
- Comparing to the Fabric version, the id of the mod now is `tieredneo`
  - Takes this note for updating the ids but, it should work flawless