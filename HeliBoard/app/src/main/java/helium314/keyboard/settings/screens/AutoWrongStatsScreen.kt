// SPDX-License-Identifier: GPL-3.0-only
// AutoWrong: added as part of the AutoWrong fork of HeliBoard
package helium314.keyboard.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.latin.autowrong.WordsDestroyedCounter
import helium314.keyboard.latin.utils.BackButton
import helium314.keyboard.settings.SearchSettingsScreen
import java.text.NumberFormat

/**
 * AutoWrongStatsScreen — the "Words Destroyed" bragging rights screen.
 *
 * Shows:
 *   - Total all-time words destroyed (persisted in SharedPreferences)
 *   - This session's count (in-memory, resets on IME restart)
 *   - A cheeky tagline
 */
@Composable
fun AutoWrongStatsScreen(
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current
    val fmt = NumberFormat.getNumberInstance()

    // Read counts on composition. These are not reactive LiveData so we just read once on open.
    val totalCount by remember { mutableLongStateOf(WordsDestroyedCounter.getTotalCount(context)) }
    val sessionCount by remember { mutableIntStateOf(WordsDestroyedCounter.sessionCount) }

    SearchSettingsScreen(
        onClickBack = onClickBack,
        title = "💀 Words Destroyed",
        settings = emptyList(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Big all-time counter
                Text(
                    text = fmt.format(totalCount),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "words destroyed (all time)",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Session counter
                Text(
                    text = fmt.format(sessionCount),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "this session",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Cheeky tagline
                Text(
                    text = "— you're welcome.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Light,
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "AutoWrong • Crafted by Prajwal (github.com/prajwal-56)",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
