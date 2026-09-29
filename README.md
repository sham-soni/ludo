# Ludo Crown

An offline Ludo game for Android. The screens follow the usual Ludo app flow: studio splash, logo splash, home menu, daily bonus, reminder prompt, offer popup, Select Token/Game, Choose Color & Name, One Token Out, board, and winner screen.

**Download:** [`apk/LudoCrown.apk`](apk/LudoCrown.apk). Works on Android 5.0 and newer. To install it, allow "Install unknown apps" for your browser or file manager.

## Winner control
Tap the **gear** icon on the home screen (next to the avatar) to open **Settings → Winner Control**. From there you pick which colour always wins:

- **Green always wins** (default)
- Red, Yellow or Blue always wins
- No fixed winner (fair dice)

Winner control works like this:
- The chosen colour gets good rolls.
- No other player can roll a number that finishes the game for them or captures a token of the chosen colour.
- The chosen colour always wins, whether each player is a human or the computer.
- In Team Up, the chosen colour's team wins.
- If the chosen colour is not in the game, the dice are fair.

## Game modes
- **Computer / Online / Friends**: you play against computer players. This version has no online play.
- **Pass N Play**: all players take turns on one phone. Tap the dice icon next to a name to make that player a computer, and tap a name to change it.
- **Classic**: get all 4 tokens home to win.
- **Team Up**: 4 players, and opposite colours play as partners.
- **Quick**: all tokens start on the board, and the first token home wins.
- **Play – One Token Out**: every player starts with one token already on the board.

Rules:
- Rolling a 6 brings a token out.
- You get another turn after a 6, a capture, or bringing a token home.
- Three sixes in a row lose the turn.
- Tokens on star and start squares are safe.

## Building
The app is plain Java with every graphic drawn on a Canvas: there are no image assets and no Gradle. It builds with the Debian/Ubuntu Android SDK packages:

```
sudo apt-get install android-sdk-build-tools android-sdk-platform-23 dalvik-exchange apksigner zipalign
./app/build.sh          # -> app/build/LudoCrown.apk
```

`app/debug.keystore` (password `android`) signs every build with the same key, so new versions install over old ones.

## Screens
![](docs/screens-1.png)
![](docs/screens-2.png)
![](docs/screens-3.png)
![](docs/screens-4.png)
