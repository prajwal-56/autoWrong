# AutoWrong — The Anti-Autocorrect Keyboard

> **TinkerHub "Useless Projects" make-a-thon entry**

A fork of [HeliBoard](https://github.com/Helium314/HeliBoard) (GPLv3) that inverts autocorrect completely:

- ✅ **Correctly-spelled words** → silently replaced with a QWERTY fat-finger typo (e.g. "hello" → "heklo")
- ❌ **Already-misspelled words** → left completely untouched (e.g. "recieve" stays "recieve")

It's autocorrect — but backwards.

---

## What Was Changed vs Upstream HeliBoard

This is a **GPLv3 fork**. All modifications are listed here for compliance.

### New files (created for AutoWrong):
| File | Purpose |
|------|---------|
| `app/src/main/java/helium314/keyboard/latin/autowrong/TypoGenerator.kt` | Pure Kotlin QWERTY-adjacent-key typo generator. No Android dependencies. Unit-testable in isolation. |
| `app/src/main/java/helium314/keyboard/latin/autowrong/WordsDestroyedCounter.kt` | SharedPreferences-backed persistent counter + in-memory session counter. |
| `app/src/main/java/helium314/keyboard/settings/screens/AutoWrongStatsScreen.kt` | Compose stats screen showing "Words Destroyed" all-time and session counts. |
| `app/src/test/java/helium314/keyboard/latin/autowrong/TypoGeneratorTest.kt` | Unit tests for TypoGenerator (JVM, no Android needed). |

### Modified files:
| File | Change |
|------|--------|
| `app/src/main/java/helium314/keyboard/latin/inputlogic/InputLogic.java` | Added imports; modified `commitCurrentAutoCorrection()` and `commitTyped()` to check `mSuggestedWords.mTypedWordValid` and mangle valid words via `TypoGenerator` instead of committing them correctly. |
| `app/src/main/java/helium314/keyboard/latin/LatinIME.java` | Added import; added `WordsDestroyedCounter.resetSession()` call in `onCreate()` to reset session counter on each IME start. |
| `app/src/main/java/helium314/keyboard/settings/SettingsNavHost.kt` | Added import, `WordsDestroyed` destination constant, and composable route for the stats screen. |
| `app/src/main/java/helium314/keyboard/settings/screens/MainSettingsScreen.kt` | Added `onClickWordsDestroyed` parameter and "💀 Words Destroyed" menu entry. |
| `app/src/main/res/values/donottranslate.xml` | Changed `english_ime_name` from "HeliBoard" to "AutoWrong". |
| `app/src/main/res/values/strings.xml` | Changed `spell_checker_service_name`, `ime_settings`, `android_spell_checker_settings` to say "AutoWrong" instead of "HeliBoard". |
| `app/build.gradle.kts` | Changed `applicationId` to `autowrong.keyboard` (avoids conflict with real HeliBoard), changed `versionName` to `4.1-AutoWrong`. |

---

## How the Core Joke Works

### Architecture

HeliBoard's autocorrect pipeline (simplified):
```
Key press → onCodeInput() → handleSeparatorEvent()
  → if (autocorrectEnabled) commitCurrentAutoCorrection()  ← primary path
  → else commitTyped()                                      ← fallback path
```

**`commitCurrentAutoCorrection()`** normally checks if HeliBoard has a strong correction candidate and commits it. We replaced this logic:

**Before (HeliBoard original):**
```java
String stringToCommit = (autoCorrectionOrNull != null) ? autoCorrectionOrNull.mWord : typedWord;
```

**After (AutoWrong):**
```java
boolean wordIsValid = mSuggestedWords.mTypedWordValid;  // true = correctly spelled
String stringToCommit;
if (wordIsValid) {
    stringToCommit = TypoGenerator.mangle(typedWord);  // MANGLE IT
    WordsDestroyedCounter.increment(mLatinIME);
} else {
    stringToCommit = typedWord;  // already wrong — leave it alone
}
```

`mTypedWordValid` is computed by `Suggest.kt` line 152:
```kotlin
val isTypedWordValid = firstOccurrenceOfTypedWordInSuggestions > -1 || (!resultsArePredictions && !allowsToBeAutoCorrected)
```
- `true` = word found in dictionary = correctly spelled → **AutoWrong mangles it**
- `false` = word not found = already a typo → **AutoWrong leaves it**

### TypoGenerator

Pure Kotlin object, zero Android imports. Algorithm:
1. Skip words < 3 chars (too short to mangle convincingly)
2. Skip ALL-CAPS words (heuristic for acronyms like USA, NASA)
3. Pick a random interior position (for words > 3 chars, avoids first/last letter)
4. Look up QWERTY-adjacent keys for the character at that position
5. Pick a random adjacent key, preserving the original character's case
6. Return the word with that single substitution

---

## Build Instructions (Arch Linux)

### Prerequisites

```bash
# 1. Install JDK 17 (build.gradle requires Java 17 source compatibility)
sudo pacman -S jdk17-openjdk

# 2. Set JAVA_HOME to JDK 17 (HeliBoard requires 17, not newer)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 3. Install Android SDK cmdline-tools
mkdir -p ~/android-sdk/cmdline-tools
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools ~/android-sdk/cmdline-tools/latest

# 4. Install required SDK components
export ANDROID_HOME=~/android-sdk
~/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses  # accept all
~/android-sdk/cmdline-tools/latest/bin/sdkmanager \
    "platforms;android-37" \
    "build-tools;35.0.2" \
    "ndk;28.0.13004108" \
    "platform-tools"

# 5. Create local.properties in the HeliBoard repo root
echo "sdk.dir=$HOME/android-sdk" > /path/to/HeliBoard/local.properties
echo "ndk.dir=$HOME/android-sdk/ndk/28.0.13004108" >> /path/to/HeliBoard/local.properties
```

### Build

```bash
cd /home/prawmathean/playground/Projects/autoWrong/HeliBoard

# Use debugNoMinify variant for faster build (no ProGuard):
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
./gradlew assembleDebugNoMinify 2>&1 | tail -20

# The APK will be at:
# app/build/outputs/apk/debugNoMinify/HeliBoard_4.1-AutoWrong-debugNoMinify.apk
```

### Install via ADB

```bash
adb install -r app/build/outputs/apk/debugNoMinify/HeliBoard_4.1-AutoWrong-debugNoMinify.apk
```

### Enable the keyboard on your device

1. **Android Settings → System → Languages & input → On-screen keyboard → Manage keyboards**
2. Toggle **AutoWrong** to ON (grant permission when prompted)
3. Open any text field
4. In the notification bar or input switcher, select **AutoWrong** as the active keyboard

---

## Manual Test Checklist

| Test | Expected Result |
|------|----------------|
| Type "hello" + space | Commits a QWERTY-adjacent typo, e.g. "heklo " |
| Type "world" + space | Commits a typo, e.g. "wotld " |
| Type "recieve" + space | Commits "recieve " unchanged (already misspelled) |
| Type "teh" + space | Commits "teh " unchanged (already misspelled) |
| Type "USA" + space | Commits "USA " unchanged (all-caps acronym, exempt) |
| Type "it" + space | Commits "it " unchanged (word < 3 chars, exempt) |
| Press Backspace right after mangled word | Restores the original correctly-spelled word! |
| Open AutoWrong Settings | Tap "💀 Words Destroyed" to see the stats screen |
| Stats counter | Should increment each time a valid word is mangled |

---

## Author, Credits & License

- **AutoWrong Author**: [Prajwal](https://github.com/prajwal-56)
- **Built for**: TinkerHub "Useless Projects" make-a-thon
- **Based on**: [HeliBoard](https://github.com/Helium314/HeliBoard) by Helium314 and contributors (GPLv3)
- **Upstream lineage**: AOSP LatinIME and OpenBoard (Apache-2.0)

This project is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE). All modifications are documented in this repository in compliance with GPLv3 terms. Original copyright notices and licenses are preserved.
