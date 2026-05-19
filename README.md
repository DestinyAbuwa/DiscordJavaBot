# 🎲 Java Discord Games Bot

A modular, event-driven Discord bot built with [JDA (Java Discord API)](https://github.com/discord-jda/JDA). This bot is designed with a scalable architecture to host interactive text-based games directly inside Discord channels.

Currently, the bot features a fully playable **Pig Dice Game** and a custom **Connect 4 Engine**, utilizing modern Discord Interactive Components (Buttons) for a fast, responsive user experience.

## ✨ Core Features

* **Multi-Server Scalability:** Uses a custom `Session Manager` backed by `ConcurrentHashMap` to track game states by Channel ID. This ensures multiple servers can play games simultaneously without variable overwrites or game-state leaks.
* **Modern UI:** Replaces slow, rate-limited Discord Reaction Emojis with instant **Discord Buttons** for seamless gameplay.
* **Dynamic Embeds:** The game boards update in real-time using rich Discord Embeds to track player turns, interactive grids, and scores.
* **Modular Design:** Built with expansion in mind, making it easy to plug in new game modes in the future.

---

## 🎮 Included Games

### 🔴 Connect 4 Arena 🟡
The classic vertical checker-dropping game!
* **Command to start:** `/connect-4`
* **How to play:**
    1. Two players join the lobby via the interactive buttons.
    2. Players take turns dropping their colored discs (🔴 or 🟡) into a 7-column grid using the numbered Action Row buttons.
    3. The custom 2D-matrix engine automatically simulates gravity drops and calculates win conditions.
    4. First to align 4 discs horizontally, vertically, or diagonally wins the crown!

### 🐷 Piggy Dice Challenge
A high-stakes, push-your-luck dice game!
* **Command to start:** `/pig-dice`
* **How to play:**
    1. Two players join the lobby.
    2. Players take turns rolling a virtual die to accumulate points in their temporary "Turn Pool".
    3. A player can **Bank** at any time to safely add their pool to their permanent score.
    4. If a player rolls a **1**, they **Pig Out!** They lose all unbanked points for that turn.
    5. First player to 100 points wins!

---

## 🛠️ Prerequisites & Tech Stack

* **Language:** Java (JDK 11 or higher recommended)
* **Library:** JDA (Java Discord API)
* **Build Tool:** Maven

---

## 🚀 Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/DestinyAbuwa/DiscordJavaBot.git](https://github.com/DestinyAbuwa/DiscordJavaBot.git)