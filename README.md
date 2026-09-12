<img width="1280" height="640" alt="AutoWrong Banner" src="https://github.com/user-attachments/assets/8920b256-2ba8-4988-b824-5351134eb4bd" />

# AutoWrong 🎯
> The Anti-Autocorrect Keyboard for Android — It misspells correctly-spelled words and leaves misspelled words untouched.

## Basic Details
### Team Name: autoWrong

### Team Members
- Team Lead: Prajwal - [github.com/prajwal-56](https://github.com/prajwal-56)

### Project Description
AutoWrong is a real, installable Android keyboard (Input Method Editor / IME) that completely inverts autocorrect: every single valid dictionary word you type is automatically swapped with a realistic QWERTY-adjacent fat-finger typo (e.g., "hello" → "heklo"), while actual typos and misspelled words are left completely untouched.

### The Problem (that doesn't exist)
In modern society, humans spend way too much effort crafting grammatically pristine messages, intimidating recipients with immaculate spelling and unrealistic perfection. People are suffering from an acute shortage of embarrassing typos in professional emails, texts to crushes, and group chats.

### The Solution (that nobody asked for)
AutoWrong solves this non-problem once and for all:
- **Zero Effort Typos**: You can type like a Rhodes Scholar, and AutoWrong will make you sound like you're typing with oven mitts while riding a mechanical bull.
- **Reverse Autocorrect**: Type `hello` → becomes `heklo`. Type `world` → becomes `wotld`.
- **Misspelling Immunity**: Already spelled it wrong? (`recieve`, `teh`) AutoWrong respects your authentic mistakes and leaves them alone.
- **Words Destroyed Counter**: Built-in brag screen in Settings showing lifetime and session tallies of innocent words ruined.
- **Panic Backspace**: Hit Backspace right after a mangled word, and AutoWrong will reluctantly revert it to your originally typed word.

---

## Technical Details

### Technologies/Components Used
- **Languages**: Kotlin, Java, C++ (NDK for native dictionary engine)
- **Frameworks**: Android InputMethodService (AOSP IME architecture), Jetpack Compose (Settings & Stats UI), Material 3
- **Base Project**: Fork of [HeliBoard](https://github.com/Helium314/HeliBoard) (GPLv3)
- **Tools**: Android SDK (Platform 37, Build-Tools), Android NDK 28, Gradle 9.7, Adoptium OpenJDK 17, ADB

---

## How It Works (Architecture)

1. **Inverted Autocorrection Pipeline** (`InputLogic.java`):
   HeliBoard's dictionary evaluation determines whether a word is valid via `mSuggestedWords.mTypedWordValid`.
   - If `mTypedWordValid == true` (word is correctly spelled) → AutoWrong routes the word to `TypoGenerator.mangle()` and increments `WordsDestroyedCounter`.
   - If `mTypedWordValid == false` (word is already a typo) → committed as-is, untouched.
2. **QWERTY Adjacency Typo Generator** (`TypoGenerator.kt`):
   - Standalone Kotlin engine with zero Android dependencies (fully unit-tested).
   - Maps each letter of the alphabet to its physical QWERTY neighbours (e.g., `e` → `w`, `s`, `d`, `r`).
   - Targets an interior letter for words > 3 characters (preserves first and last letters for realistic fat-finger illusion).
   - Preserves uppercase/lowercase casing.
   - Exempts all-caps acronyms (`USA`, `NASA`) and short words (< 3 characters).
3. **Persistent Stats Screen** (`WordsDestroyedCounter.kt` & `AutoWrongStatsScreen.kt`):
   - All-time count persisted in `SharedPreferences`.
   - Session counter tracking words ruined since the keyboard was opened.
   - Custom Jetpack Compose UI accessible directly from keyboard settings.
4. **Instant Revert Mechanism**:
   - Commits mangled words as `COMMIT_TYPE_DECIDED_WORD`.
   - Pressing **Backspace** immediately undoes the typo and restores your original intended word.

---

## Implementation

### Prerequisites
- JDK 17 (`export JAVA_HOME=...`)
- Android SDK (`platforms;android-37.0`, `ndk;28.0.13004108`)
- Device with USB Debugging enabled

### Build from Source
```bash
cd HeliBoard

# Build the debug APK (debugNoMinify variant)
./gradlew assembleDebugNoMinify --no-daemon -x test
```

The APK will be generated at:
```
HeliBoard/app/build/outputs/apk/debugNoMinify/HeliBoard_4.1-AutoWrong-debugNoMinify.apk
```

### Install via ADB
```bash
adb install -r HeliBoard/app/build/outputs/apk/debugNoMinify/HeliBoard_4.1-AutoWrong-debugNoMinify.apk

# Enable and set as active keyboard
adb shell ime enable autowrong.keyboard.debug/helium314.keyboard.latin.LatinIME
adb shell ime set autowrong.keyboard.debug/helium314.keyboard.latin.LatinIME
```

---

## Project Documentation

### Screenshots
| AutoWrong Keyboard in Action | "Words Destroyed" Stats Screen | Settings & Credits |
|:---:|:---:|:---:|
| ![Keyboard Typing](HeliBoard/art/launcher_icon/man_fire_writing.jpg) | ![Stats Screen](HeliBoard/art/launcher_icon/man_fire_writing.jpg) | ![Settings](HeliBoard/art/launcher_icon/man_fire_writing.jpg) |
| *AutoWrong active keyboard with custom branding* | *Live counter of valid words ruined* | *Custom about screen with developer profile* |

### Architecture Flow
```
User types characters → Space / Punctuation pressed
         │
         ▼
InputLogic.onCodeInput() / handleSeparatorEvent()
         │
         ├──► Is typed word in dictionary? (mTypedWordValid)
         │       │
         │       ├── YES (Correctly spelled):
         │       │     ├── Pass through TypoGenerator.mangle()
         │       │     ├── Replace with realistic adjacent QWERTY key
         │       │     ├── Increment WordsDestroyedCounter
         │       │     └── Commit mangled typo to text view!
         │       │
         │       └── NO (Already misspelled):
         │             └── Commit original text as-is!
         │
         └── User presses Backspace immediately?
                 └── Revert back to original correctly-spelled word
```

---

## Team Contributions
- **Prajwal** ([github.com/prajwal-56](https://github.com/prajwal-56)):
  - Forked and adapted HeliBoard AOSP input logic.
  - Implemented QWERTY adjacency typo algorithm and unit test suite.
  - Intercepted autocorrect commit pipeline and inverted dictionary signal.
  - Built "Words Destroyed" persistent counter and Jetpack Compose stats screen.
  - Designed custom app icon, adaptive mipmap drawables, and branding.

---
Made with ❤️ at TinkerHub Useless Projects 

![Static Badge](https://img.shields.io/badge/TinkerHub-24?color=%23000000&link=https%3A%2F%2Fwww.tinkerhub.org%2F)
![Static Badge](https://img.shields.io/badge/UselessProjects--26-26?link=https%3A%2F%2Ftinkerhub.org%2Fevents%2F1M8ORET9A1%2Fuseless-projects-3.0)
