package com.example.foss101.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.foss101.model.ArtifactKind
import com.example.foss101.model.CompletionConfidence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionSheet(
    kind: ArtifactKind,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (CompletionConfidence, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confidence by remember { mutableStateOf(CompletionConfidence.Medium) }
    var reflection by remember { mutableStateOf("") }

    val title = when (kind) {
        ArtifactKind.Scenario -> "Mark scenario complete"
        ArtifactKind.Challenge -> "Mark challenge complete"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "How confident did you feel about it?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CompletionConfidence.values().forEachIndexed { index, level ->
                    SegmentedButton(
                        selected = level == confidence,
                        onClick = { confidence = level },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CompletionConfidence.values().size
                        )
                    ) {
                        Text(level.label)
                    }
                }
            }

            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                label = { Text("Reflection (optional)") },
                placeholder = { Text("What stuck with you? What was tricky?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp, max = 140.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PrimaryActionButton(
                text = if (isSubmitting) "Submitting..." else "Submit completion",
                onClick = { onSubmit(confidence, reflection.trim().ifBlank { null }) },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
