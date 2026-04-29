package com.example.foss101.data.repository

import com.example.foss101.model.AskGlossaryResponse
import com.example.foss101.model.Category
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningScenario
import com.example.foss101.model.TermDraftSubmission
import com.example.foss101.model.TermDraftSubmissionResult

class MockGlossaryRepository : GlossaryRepository {

    private val categories = listOf(
        Category(
            id = "ai_fundamentals",
            name = "AI Fundamentals",
            description = "Core concepts used across modern AI systems."
        ),
        Category(
            id = "ml_training",
            name = "ML and Training",
            description = "How models learn from data and improve performance."
        ),
        Category(
            id = "llm_prompting",
            name = "LLM Concepts",
            description = "Language model concepts, prompting patterns, and response behavior."
        ),
        Category(
            id = "safety_eval",
            name = "AI Safety",
            description = "Reliability, quality, and responsible-AI guardrail concepts."
        ),
        Category(
            id = "deployment_ops",
            name = "Inference & Serving",
            description = "Serving, latency, and infrastructure concepts for AI apps."
        )
    )

    private val terms = listOf(
        GlossaryTerm(
            id = "artificial-intelligence",
            slug = "artificial-intelligence",
            term = "Artificial Intelligence",
            definition = "A field focused on building systems that perform tasks requiring human-like intelligence.",
            explanation = "Artificial intelligence covers techniques that enable software to reason, predict, generate, and make decisions from data or rules.",
            humor = "The grand plan to make computers helpful, impressive, and occasionally far too confident.",
            categoryId = "ai_fundamentals",
            tags = listOf("ai", "overview"),
            seeAlso = listOf("Machine Learning", "Model"),
            controversyLevel = 0,
            exampleUsage = "This app introduces artificial intelligence terms for beginners.",
            source = null
        ),
        GlossaryTerm(
            id = "machine-learning",
            slug = "machine-learning",
            term = "Machine Learning",
            definition = "A branch of AI where models learn patterns from examples.",
            explanation = "Machine learning trains mathematical models on data so they can make predictions or decisions without being explicitly programmed for every case.",
            humor = "Teaching statistics to develop opinions at scale.",
            categoryId = "ml_training",
            tags = listOf("ai", "training", "models"),
            seeAlso = listOf("Dataset", "Training", "Inference"),
            controversyLevel = 0,
            exampleUsage = "Fraud detection systems often rely on machine learning.",
            source = null
        ),
        GlossaryTerm(
            id = "model",
            slug = "model",
            term = "Model",
            definition = "A learned representation used to produce predictions or generated outputs.",
            explanation = "In AI, a model is the trained artifact produced from data and algorithms. It is later used during inference to complete a task.",
            humor = "The part everyone blames when the rest of the system misbehaves.",
            categoryId = "ai_fundamentals",
            tags = listOf("ai", "prediction"),
            seeAlso = listOf("Training", "Inference"),
            controversyLevel = 0,
            exampleUsage = "The chatbot sends your prompt to a language model.",
            source = null
        ),
        GlossaryTerm(
            id = "dataset",
            slug = "dataset",
            term = "Dataset",
            definition = "A structured collection of examples used for training or evaluation.",
            explanation = "A dataset contains records such as text, images, or labels that help models learn patterns and support quality measurement.",
            humor = "Garbage in, benchmark slides out.",
            categoryId = "ml_training",
            tags = listOf("data", "training"),
            seeAlso = listOf("Training", "Benchmark"),
            controversyLevel = 0,
            exampleUsage = "The team cleaned the dataset before training the model.",
            source = null
        ),
        GlossaryTerm(
            id = "training",
            slug = "training",
            term = "Training",
            definition = "The process of adjusting model parameters using data.",
            explanation = "Training repeatedly compares model outputs with expected results and updates weights to reduce error over time.",
            humor = "A very expensive way of teaching math to stop being wrong as often.",
            categoryId = "ml_training",
            tags = listOf("optimization", "learning"),
            seeAlso = listOf("Fine-Tuning", "Inference"),
            controversyLevel = 0,
            exampleUsage = "Training can take hours or days depending on model size.",
            source = null
        ),
        GlossaryTerm(
            id = "inference",
            slug = "inference",
            term = "Inference",
            definition = "The process of running a trained model to produce outputs.",
            explanation = "Inference happens after training and can run in batch or real-time systems. Performance factors include model size, hardware, precision, and serving strategy.",
            humor = "The moment all the expensive training finally has to do something useful.",
            categoryId = "deployment_ops",
            tags = listOf("inference", "latency", "serving"),
            seeAlso = listOf("Model", "Latency"),
            controversyLevel = 0,
            exampleUsage = "Chat response generation is an inference workload.",
            source = null
        ),
        GlossaryTerm(
            id = "llm",
            slug = "llm",
            term = "Large Language Model (LLM)",
            definition = "A neural network trained on large-scale text to understand and generate language.",
            explanation = "LLMs learn statistical language patterns from massive corpora and are commonly used for chat, summarization, reasoning, and coding tasks.",
            humor = "Autocomplete with ambition, infrastructure, and a PR team.",
            categoryId = "llm_prompting",
            tags = listOf("llm", "language"),
            seeAlso = listOf("Prompt", "Token", "Context Window"),
            controversyLevel = 1,
            exampleUsage = "The assistant uses an LLM to draft explanations.",
            source = null
        ),
        GlossaryTerm(
            id = "prompt",
            slug = "prompt",
            term = "Prompt",
            definition = "The input instruction that guides a model's response.",
            explanation = "A prompt can include task instructions, examples, constraints, and context that shape model output quality and style.",
            humor = "Half instruction, half wishful thinking.",
            categoryId = "llm_prompting",
            tags = listOf("input", "instruction"),
            seeAlso = listOf("System Prompt", "Temperature"),
            controversyLevel = 0,
            exampleUsage = "A clear prompt usually gives better responses.",
            source = null
        ),
        GlossaryTerm(
            id = "system-prompt",
            slug = "system-prompt",
            term = "System Prompt",
            definition = "A high-priority instruction that sets assistant behavior.",
            explanation = "System prompts define role, boundaries, and response expectations before user prompts are applied.",
            humor = "The invisible boss note the model is supposed to obey before improvising anyway.",
            categoryId = "llm_prompting",
            tags = listOf("instruction-hierarchy", "assistant"),
            seeAlso = listOf("Prompt", "Guardrails"),
            controversyLevel = 1,
            exampleUsage = "The system prompt can enforce concise output style.",
            source = null
        ),
        GlossaryTerm(
            id = "token",
            slug = "token",
            term = "Token",
            definition = "A unit of text processed by language models.",
            explanation = "Models break text into tokens for training and inference. Token count affects cost, context usage, and response limits.",
            humor = "A tiny billing unit disguised as language structure.",
            categoryId = "llm_prompting",
            tags = listOf("text", "context-window"),
            seeAlso = listOf("LLM", "Context Window"),
            controversyLevel = 0,
            exampleUsage = "Long prompts consume more tokens.",
            source = null
        ),
        GlossaryTerm(
            id = "context-window",
            slug = "context-window",
            term = "Context Window",
            definition = "The maximum amount of tokenized content a model can consider at once.",
            explanation = "The context window includes system instructions, conversation history, and current input. Exceeding it causes earlier content to be dropped.",
            humor = "The model’s memory, except it is very literal and not nearly as generous as people hope.",
            categoryId = "llm_prompting",
            tags = listOf("llm", "memory", "tokens"),
            seeAlso = listOf("Token", "Retrieval-Augmented Generation (RAG)"),
            controversyLevel = 0,
            exampleUsage = "Large context windows help with long documents.",
            source = null
        ),
        GlossaryTerm(
            id = "temperature",
            slug = "temperature",
            term = "Temperature",
            definition = "A decoding setting that controls randomness in generated text.",
            explanation = "Lower temperature tends to make outputs more deterministic, while higher temperature can increase variety and creativity.",
            humor = "The knob people turn when they want either discipline or chaos and pretend it is a science.",
            categoryId = "llm_prompting",
            tags = listOf("generation", "decoding"),
            seeAlso = listOf("Prompt", "Inference"),
            controversyLevel = 0,
            exampleUsage = "Set a lower temperature for repeatable responses.",
            source = null
        ),
        GlossaryTerm(
            id = "hallucination",
            slug = "hallucination",
            term = "Hallucination",
            definition = "A confident model response that is incorrect or unsupported.",
            explanation = "Hallucinations happen when a model generates plausible but false content. Mitigation often combines better prompting, retrieval, and validation.",
            humor = "The AI equivalent of answering immediately, beautifully, and completely out of its depth.",
            categoryId = "safety_eval",
            tags = listOf("grounding", "reliability", "safety"),
            seeAlso = listOf("Evaluation", "Retrieval-Augmented Generation (RAG)"),
            controversyLevel = 2,
            exampleUsage = "The answer sounded right, but it was a hallucination.",
            source = null
        ),
        GlossaryTerm(
            id = "evaluation",
            slug = "evaluation",
            term = "Evaluation",
            definition = "The process of measuring model quality against defined criteria.",
            explanation = "Evaluation can include automated benchmarks, human review, and task-specific metrics to verify usefulness, accuracy, and safety.",
            humor = "The part where optimism meets data and usually loses.",
            categoryId = "safety_eval",
            tags = listOf("metrics", "quality"),
            seeAlso = listOf("Benchmark", "Hallucination"),
            controversyLevel = 1,
            exampleUsage = "Each model update requires a fresh evaluation pass.",
            source = null
        ),
        GlossaryTerm(
            id = "benchmark",
            slug = "benchmark",
            term = "Benchmark",
            definition = "A standardized test used to compare model performance.",
            explanation = "Benchmarks provide repeatable tasks and scoring so teams can track improvements and compare different models under similar conditions.",
            humor = "A scoreboard people trust until it disagrees with the demo.",
            categoryId = "safety_eval",
            tags = listOf("testing", "comparison"),
            seeAlso = listOf("Evaluation", "Dataset"),
            controversyLevel = 1,
            exampleUsage = "The model improved by 8 points on the benchmark.",
            source = null
        ),
        GlossaryTerm(
            id = "guardrails",
            slug = "guardrails",
            term = "Guardrails",
            definition = "Rules and controls that constrain unsafe or off-policy behavior.",
            explanation = "Guardrails can include policy prompts, output filters, and routing logic to reduce harmful or non-compliant model responses.",
            humor = "Software trying very hard to stop confidence from outrunning judgment.",
            categoryId = "safety_eval",
            tags = listOf("safety", "policy"),
            seeAlso = listOf("System Prompt", "Evaluation"),
            controversyLevel = 1,
            exampleUsage = "The app applies guardrails before showing model output.",
            source = null
        ),
        GlossaryTerm(
            id = "retrieval-augmented-generation",
            slug = "retrieval-augmented-generation-rag",
            term = "Retrieval-Augmented Generation (RAG)",
            definition = "A pattern that retrieves external information before generation so the model stops pretending its frozen weights are a complete library.",
            explanation = "RAG retrieves relevant documents at query time and supplies them to the model, improving grounding and freshness without retraining the base model.",
            humor = "A polite way of admitting the model should look things up before speaking.",
            categoryId = "deployment_ops",
            tags = listOf("rag", "knowledge", "retrieval"),
            seeAlso = listOf("Hallucination", "Context Window"),
            controversyLevel = 1,
            exampleUsage = "RAG helps the assistant answer with current internal docs.",
            source = null
        ),
        GlossaryTerm(
            id = "latency",
            slug = "latency",
            term = "Latency",
            definition = "The time delay between a request and the model response.",
            explanation = "Latency impacts user experience in AI apps and depends on model size, infrastructure, network conditions, and prompt length.",
            humor = "The uncomfortable silence between a request and the machine pretending it was thinking the whole time.",
            categoryId = "deployment_ops",
            tags = listOf("performance", "serving"),
            seeAlso = listOf("Inference", "Throughput"),
            controversyLevel = 0,
            exampleUsage = "Reducing latency makes chat feel more responsive.",
            source = null
        ),
        GlossaryTerm(
            id = "throughput",
            slug = "throughput",
            term = "Throughput",
            definition = "The amount of work a system can process in a given time.",
            explanation = "For AI services, throughput often means requests per second or tokens per second handled while maintaining acceptable latency.",
            humor = "How many problems per second your infrastructure can survive before everyone starts blaming the model.",
            categoryId = "deployment_ops",
            tags = listOf("scaling", "performance"),
            seeAlso = listOf("Latency", "Inference"),
            controversyLevel = 0,
            exampleUsage = "The new deployment doubled inference throughput.",
            source = null
        ),
        GlossaryTerm(
            id = "fine-tuning",
            slug = "fine-tuning",
            term = "Fine-Tuning",
            definition = "Additional training of a pre-trained model on specialized data.",
            explanation = "Fine-tuning adapts a general model to domain-specific tasks or tone by continuing training on targeted examples.",
            humor = "Taking a model that knows too much about everything and teaching it to care about your particular mess.",
            categoryId = "ml_training",
            tags = listOf("adaptation", "training"),
            seeAlso = listOf("Training", "Dataset"),
            controversyLevel = 1,
            exampleUsage = "They used fine-tuning for industry-specific terminology.",
            source = null
        )
    )

    override suspend fun getAllTerms(): List<GlossaryTerm> = terms

    override suspend fun getTermById(id: String): GlossaryTerm? {
        return terms.find { it.id == id }
    }

    override suspend fun getAllCategories(): List<Category> = categories

    override suspend fun searchTerms(query: String): List<GlossaryTerm> {
        val normalizedQuery = query.trim().lowercase()

        if (normalizedQuery.isBlank()) {
            return terms
        }

        return terms.filter { term ->
            term.term.lowercase().contains(normalizedQuery) ||
                    term.definition.lowercase().contains(normalizedQuery) ||
                    term.explanation.lowercase().contains(normalizedQuery) ||
                    term.tags.any { it.lowercase().contains(normalizedQuery) } ||
                    term.seeAlso.any { it.lowercase().contains(normalizedQuery) }
        }
    }

    override suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm> {
        return terms.filter { it.categoryId == categoryId }
    }

    override suspend fun askGlossary(question: String, termId: String?): AskGlossaryResponse {
        val term = termId?.let { getTermById(it) }
        val prefix = term?.let { "For ${it.term}: " } ?: ""
        return AskGlossaryResponse(
            answer = prefix + "${question.trim()} -> Start with the definition, then connect it to a practical example.",
            summary = "Use glossary definitions, explanations, and examples to ground your understanding.",
            relatedTermIds = term?.seeAlso?.map { related ->
                terms.find { it.term == related }?.id ?: related.lowercase().replace(" ", "-")
            } ?: emptyList()
        )
    }

    override suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean,
        preset: com.example.foss101.model.LearningPreset?
    ): GeneratedArtifactResult<LearningScenario> {
        val term = getTermById(termId) ?: error("Missing term")
        return GeneratedArtifactResult(
            artifact = LearningScenario(
                title = "Scenario: ${term.term} in product planning",
                difficulty = "beginner",
                context = "You are evaluating an AI feature for a customer support product.",
                objective = "Explain ${term.term} and how it changes design choices.",
                tasks = listOf("Define ${term.term}", "Identify one risk", "Propose one mitigation"),
                reflectionQuestions = listOf(
                    "What would fail without this concept?",
                    "How would you measure success?"
                )
            ),
            cached = !forceRefresh
        )
    }

    override suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean,
        preset: com.example.foss101.model.LearningPreset?
    ): GeneratedArtifactResult<LearningChallenge> {
        val term = getTermById(termId) ?: error("Missing term")
        return GeneratedArtifactResult(
            artifact = LearningChallenge(
                title = "Challenge: apply ${term.term}",
                difficulty = "beginner",
                prompt = "Write a short plan that applies ${term.term} in a real app.",
                successCriteria = listOf(
                    "Uses correct definition",
                    "Includes an example",
                    "Mentions one tradeoff"
                ),
                hint = "Anchor your answer in the glossary definition and one production scenario."
            ),
            cached = !forceRefresh
        )
    }

    override suspend fun submitTermDraft(draft: TermDraftSubmission): TermDraftSubmissionResult {
        return TermDraftSubmissionResult(
            id = "mock-draft-${System.currentTimeMillis()}",
            status = "draft"
        )
    }

    override suspend fun submitLearningCompletion(
        termId: String,
        artifactType: com.example.foss101.model.ArtifactKind,
        confidence: com.example.foss101.model.CompletionConfidence,
        reflectionNotes: String?,
        taskStates: List<com.example.foss101.model.TaskState>?,
        challengeResponse: String?,
        criteriaGrades: List<com.example.foss101.model.CriterionGrade>?
    ): com.example.foss101.model.LearningCompletionResult {
        val earned = when (artifactType) {
            com.example.foss101.model.ArtifactKind.Scenario ->
                minOf(10, 5 + (taskStates?.count { it.checked } ?: 0))
            com.example.foss101.model.ArtifactKind.Challenge -> {
                val grades = criteriaGrades.orEmpty()
                if (grades.isEmpty()) 0
                else (grades.count { it.met }.toDouble() / grades.size * 15).toInt()
            }
        }
        return com.example.foss101.model.LearningCompletionResult(
            completion = com.example.foss101.model.LearningCompletion(
                id = System.currentTimeMillis(),
                userId = "mock-user",
                termId = termId,
                artifactType = artifactType,
                confidence = confidence,
                reflectionNotes = reflectionNotes,
                taskStates = taskStates,
                challengeResponse = challengeResponse,
                criteriaGrades = criteriaGrades,
                earnedPoints = earned,
                completedAt = "2024-01-01T00:00:00Z"
            ),
            pointsAwarded = earned,
            alreadyCompleted = false
        )
    }
}