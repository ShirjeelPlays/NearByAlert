# NearbyAlert

NearbyAlert is a lightweight client-side Fabric mod that warns you when other players are nearby — even through walls.

It displays a clean on-screen alert showing the player’s name and the direction they are coming from. When multiple players are close, the mod cycles through them automatically to keep your screen uncluttered.

## ✨ Features
- Detects nearby players within **150 blocks**
- Direction arrow (↑ ↓ ← →) based on camera direction
- Works through walls (no line-of-sight required)
- Cycles multiple players every **5 seconds**
- Dynamic color based on distance
- Smooth fade and scale animations
- Toggle on/off with **K**
- Client-side only, lightweight and performance friendly

## 🎨 Distance Colors
- **0–30 blocks:** Dark Red (Danger)
- **31–75 blocks:** Yellow (Warning)
- **76–150 blocks:** Green (Safe)

## ⚙ Requirements
- Minecraft **1.21**
- Fabric Loader
- Fabric API

## 📦 Installation
1. Install Fabric Loader for Minecraft 1.21  
2. Download the NearbyAlert `.jar` file  
3. Place it in your `mods` folder  
4. Launch Minecraft with Fabric

## 🧠 Notes
- Client-side only (no server required)
- Does not modify packets or gameplay
- Designed to be minimal and PvP-friendly

## 📝 License
MIT
