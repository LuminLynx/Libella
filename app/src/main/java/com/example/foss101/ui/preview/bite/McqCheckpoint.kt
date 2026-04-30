package com.example.foss101.ui.preview.bite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Final-bite comprehension check.
 *
 * 3 multiple-choice questions. The user taps an answer per question; the chosen
 * option immediately reveals correct/incorrect feedback (it does not retry the
 * same question — once answered, the answer is locked). After all questions
 * are answered the score is summarised.
 *
 * Designed to be the last bite in a concept's feed: it confirms understanding
 * before the user moves on to the next term in their path.
 */
data class Mcq(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun McqCheckpoint(questions: List<Mcq>) {
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var index by remember { mutableIntStateOf(0) }

    if (index >= questions.size) {
        ResultsCard(questions, answers)
        return
    }

    val question = questions[index]
    val chosen = answers[index]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Question ${index + 1} of ${questions.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium
        )

        question.options.forEachIndexed { optionIndex, option ->
            OptionRow(
                text = option,
                state = optionState(
                    chosen = chosen,
                    optionIndex = optionIndex,
                    correctIndex = question.correctIndex
                ),
                enabled = chosen == null,
                onClick = { answers[index] = optionIndex }
            )
        }

        if (chosen != null) {
            ExplanationCard(
                isCorrect = chosen == question.correctIndex,
                explanation = question.explanation
            )
            OutlinedButton(
                onClick = { index++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (index == questions.size - 1) "See score" else "Next question")
            }
        }
    }
}

private enum class OptionState { Idle, ChosenCorrect, ChosenWrong, RevealedCorrect }

private fun optionState(chosen: Int?, optionIndex: Int, correctIndex: Int): OptionState =
    when {
        chosen == null -> OptionState.Idle
        optionIndex == chosen && chosen == correctIndex -> OptionState.ChosenCorrect
        optionIndex == chosen && chosen != correctIndex -> OptionState.ChosenWrong
        optionIndex == correctIndex && chosen != correctIndex -> OptionState.RevealedCorrect
        else -> OptionState.Idle
    }

@Composable
private fun OptionRow(
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val container = when (state) {
        OptionState.ChosenCorrect, OptionState.RevealedCorrect -> MaterialTheme.colorScheme.primaryContainer
        OptionState.ChosenWrong -> MaterialTheme.colorScheme.errorContainer
        OptionState.Idle -> MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = when (state) {
        OptionState.ChosenCorrect, OptionState.RevealedCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
        OptionState.ChosenWrong -> MaterialTheme.colorScheme.onErrorContainer
        OptionState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            when (state) {
                OptionState.ChosenCorrect, OptionState.RevealedCorrect -> Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                OptionState.ChosenWrong -> Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                OptionState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun ExplanationCard(isCorrect: Boolean, explanation: String) {
    val container = if (isCorrect) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = if (isCorrect) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isCorrect) "Correct" else "Not quite",
                style = MaterialTheme.typography.titleSmall
            )
            Text(text = explanation, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultsCard(questions: List<Mcq>, answers: Map<Int, Int>) {
    val correctCount = questions.indices.count { answers[it] == questions[it].correctIndex }
    val container = if (correctCount == questions.size) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = if (correctCount == questions.size) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Score: $correctCount / ${questions.size}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = when {
                    correctCount == questions.size -> "All correct. The Tokenization concept is locked in."
                    correctCount >= questions.size - 1 -> "Strong. Skim the bite you missed and you've got this."
                    else -> "Some gaps. The bites are still in your feed — swipe back and revisit."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
