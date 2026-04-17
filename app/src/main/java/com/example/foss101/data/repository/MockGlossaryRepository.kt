package com.example.foss101.data.repository

import com.example.foss101.model.AskGlossaryResponse
import com.example.foss101.model.Category
import com.example.foss101.model.GeneratedArtifactResult
import com.example.foss101.model.GlossaryTerm
import com.example.foss101.model.LearningChallenge
import com.example.foss101.model.LearningScenario

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
            name = "LLMs and Prompting",
            description = "Language model concepts, prompting patterns, and response behavior."
        ),
        Category(
            id = "safety_eval",
            name = "Safety and Evaluation",
            description = "Reliability, quality, and responsible-AI guardrail concepts."
        ),
        Category(
            id = "deployment_ops",
            name = "Deployment and Ops",
            description = "Serving, latency, and infrastructure concepts for AI apps."
        )
    )

    private val terms = listOf(
        GlossaryTerm(
            id = "artificial-intelligence",
            term = "Artificial Intelligence",
            shortDefinition = "A field focused on building systems that perform tasks requiring human-like intelligence.",
            fullExplanation = "Artificial intelligence covers techniques that enable software to reason, predict, generate, and make decisions from data or rules.",
            categoryId = "ai_fundamentals",
            tags = listOf("ai", "overview"),
            relatedTerms = listOf("machine-learning", "model"),
            exampleUsage = "This app introduces artificial intelligence terms for beginners.",
            source = null
        ),
        GlossaryTerm(
            id = "machine-learning",
            term = "Machine Learning",
            shortDefinition = "A branch of AI where models learn patterns from examples.",
            fullExplanation = "Machine learning trains mathematical models on data so they can make predictions or decisions without being explicitly programmed for every case.",
            categoryId = "ml_training",
            tags = listOf("ai", "training", "models"),
            relatedTerms = listOf("dataset", "training", "inference"),
            exampleUsage = "Fraud detection systems often rely on machine learning.",
            source = null
        ),
        GlossaryTerm(
            id = "model",
            term = "Model",
            shortDefinition = "A learned representation used to produce predictions or generated outputs.",
            fullExplanation = "In AI, a model is the trained artifact produced from data and algorithms. It is later used during inference to complete a task.",
            categoryId = "ai_fundamentals",
            tags = listOf("ai", "prediction"),
            relatedTerms = listOf("training", "inference"),
            exampleUsage = "The chatbot sends your prompt to a language model.",
            source = null
        ),
        GlossaryTerm(
            id = "dataset",
            term = "Dataset",
            shortDefinition = "A structured collection of examples used for training or evaluation.",
            fullExplanation = "A dataset contains records such as text, images, or labels that help models learn patterns and support quality measurement.",
            categoryId = "ml_training",
            tags = listOf("data", "training"),
            relatedTerms = listOf("training", "benchmark"),
            exampleUsage = "The team cleaned the dataset before training the model.",
            source = null
        ),
        GlossaryTerm(
            id = "training",
            term = "Training",
            shortDefinition = "The process of adjusting model parameters using data.",
            fullExplanation = "Training repeatedly compares model outputs with expected results and updates weights to reduce error over time.",
            categoryId = "ml_training",
            tags = listOf("optimization", "learning"),
            relatedTerms = listOf("fine-tuning", "inference"),
            exampleUsage = "Training can take hours or days depending on model size.",
            source = null
        ),
        GlossaryTerm(
            id = "inference",
            term = "Inference",
            shortDefinition = "The stage where a trained model is used to generate predictions.",
            fullExplanation = "Inference happens after training, when a model receives new input and returns an output such as a classification, score, or generated text.",
            categoryId = "deployment_ops",
            tags = listOf("serving", "prediction"),
            relatedTerms = listOf("model", "latency"),
            exampleUsage = "Users experience inference each time they ask a question.",
            source = null
        ),
        GlossaryTerm(
            id = "llm",
            term = "Large Language Model (LLM)",
            shortDefinition = "A neural network trained on large-scale text to understand and generate language.",
            fullExplanation = "LLMs learn statistical language patterns from massive corpora and are commonly used for chat, summarization, reasoning, and coding tasks.",
            categoryId = "llm_prompting",
            tags = listOf("llm", "language"),
            relatedTerms = listOf("prompt", "token", "context-window"),
            exampleUsage = "The assistant uses an LLM to draft explanations.",
            source = null
        ),
        GlossaryTerm(
            id = "prompt",
            term = "Prompt",
            shortDefinition = "The input instruction that guides a model's response.",
            fullExplanation = "A prompt can include task instructions, examples, constraints, and context that shape model output quality and style.",
            categoryId = "llm_prompting",
            tags = listOf("input", "instruction"),
            relatedTerms = listOf("system-prompt", "temperature"),
            exampleUsage = "A clear prompt usually gives better responses.",
            source = null
        ),
        GlossaryTerm(
            id = "system-prompt",
            term = "System Prompt",
            shortDefinition = "A high-priority instruction that sets assistant behavior.",
            fullExplanation = "System prompts define role, boundaries, and response expectations before user prompts are applied.",
            categoryId = "llm_prompting",
            tags = listOf("instruction-hierarchy", "assistant"),
            relatedTerms = listOf("prompt", "guardrails"),
            exampleUsage = "The system prompt can enforce concise output style.",
            source = null
        ),
        GlossaryTerm(
            id = "token",
            term = "Token",
            shortDefinition = "A unit of text processed by language models.",
            fullExplanation = "Models break text into tokens for training and inference. Token count affects cost, context usage, and response limits.",
            categoryId = "llm_prompting",
            tags = listOf("text", "context-window"),
            relatedTerms = listOf("llm", "context-window"),
            exampleUsage = "Long prompts consume more tokens.",
            source = null
        ),
        GlossaryTerm(
            id = "context-window",
            term = "Context Window",
            shortDefinition = "The maximum amount of tokenized content a model can consider at once.",
            fullExplanation = "The context window includes system instructions, conversation history, and current input. Exceeding it causes earlier content to be dropped.",
            categoryId = "llm_prompting",
            tags = listOf("limits", "tokens"),
            relatedTerms = listOf("token", "retrieval-augmented-generation"),
            exampleUsage = "Large context windows help with long documents.",
            source = null
        ),
        GlossaryTerm(
            id = "temperature",
            term = "Temperature",
            shortDefinition = "A decoding setting that controls randomness in generated text.",
            fullExplanation = "Lower temperature tends to make outputs more deterministic, while higher temperature can increase variety and creativity.",
            categoryId = "llm_prompting",
            tags = listOf("generation", "decoding"),
            relatedTerms = listOf("prompt", "inference"),
            exampleUsage = "Set a lower temperature for repeatable responses.",
            source = null
        ),
        GlossaryTerm(
            id = "hallucination",
            term = "Hallucination",
            shortDefinition = "A confident model response that is incorrect or unsupported.",
            fullExplanation = "Hallucinations happen when a model generates plausible but false content. Mitigation often combines better prompting, retrieval, and validation.",
            categoryId = "safety_eval",
            tags = listOf("reliability", "quality"),
            relatedTerms = listOf("evaluation", "retrieval-augmented-generation"),
            exampleUsage = "The answer sounded right, but it was a hallucination.",
            source = null
        ),
        GlossaryTerm(
            id = "evaluation",
            term = "Evaluation",
            shortDefinition = "The process of measuring model quality against defined criteria.",
            fullExplanation = "Evaluation can include automated benchmarks, human review, and task-specific metrics to verify usefulness, accuracy, and safety.",
            categoryId = "safety_eval",
            tags = listOf("metrics", "quality"),
            relatedTerms = listOf("benchmark", "hallucination"),
            exampleUsage = "Each model update requires a fresh evaluation pass.",
            source = null
        ),
        GlossaryTerm(
            id = "benchmark",
            term = "Benchmark",
            shortDefinition = "A standardized test used to compare model performance.",
            fullExplanation = "Benchmarks provide repeatable tasks and scoring so teams can track improvements and compare different models under similar conditions.",
            categoryId = "safety_eval",
            tags = listOf("testing", "comparison"),
            relatedTerms = listOf("evaluation", "dataset"),
            exampleUsage = "The model improved by 8 points on the benchmark.",
            source = null
        ),
        GlossaryTerm(
            id = "guardrails",
            term = "Guardrails",
            shortDefinition = "Rules and controls that constrain unsafe or off-policy behavior.",
            fullExplanation = "Guardrails can include policy prompts, output filters, and routing logic to reduce harmful or non-compliant model responses.",
            categoryId = "safety_eval",
            tags = listOf("safety", "policy"),
            relatedTerms = listOf("system-prompt", "evaluation"),
            exampleUsage = "The app applies guardrails before showing model output.",
            source = null
        ),
        GlossaryTerm(
            id = "retrieval-augmented-generation",
            term = "Retrieval-Augmented Generation (RAG)",
            shortDefinition = "A pattern that combines external knowledge retrieval with model generation.",
            fullExplanation = "RAG retrieves relevant documents at query time and provides them to the model, improving factual grounding and reducing hallucinations.",
            categoryId = "deployment_ops",
            tags = listOf("rag", "knowledge", "retrieval"),
            relatedTerms = listOf("hallucination", "context-window"),
            exampleUsage = "RAG helps the assistant answer with current internal docs.",
            source = null
        ),
        GlossaryTerm(
            id = "latency",
            term = "Latency",
            shortDefinition = "The time delay between a request and the model response.",
            fullExplanation = "Latency impacts user experience in AI apps and depends on model size, infrastructure, network conditions, and prompt length.",
            categoryId = "deployment_ops",
            tags = listOf("performance", "serving"),
            relatedTerms = listOf("inference", "throughput"),
            exampleUsage = "Reducing latency makes chat feel more responsive.",
            source = null
        ),
        GlossaryTerm(
            id = "throughput",
            term = "Throughput",
            shortDefinition = "The amount of work a system can process in a given time.",
            fullExplanation = "For AI services, throughput often means requests per second or tokens per second handled while maintaining acceptable latency.",
            categoryId = "deployment_ops",
            tags = listOf("scaling", "performance"),
            relatedTerms = listOf("latency", "inference"),
            exampleUsage = "The new deployment doubled inference throughput.",
            source = null
        ),
        GlossaryTerm(
            id = "fine-tuning",
            term = "Fine-Tuning",
            shortDefinition = "Additional training of a pre-trained model on specialized data.",
            fullExplanation = "Fine-tuning adapts a general model to domain-specific tasks or tone by continuing training on targeted examples.",
            categoryId = "ml_training",
            tags = listOf("adaptation", "training"),
            relatedTerms = listOf("training", "dataset"),
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
                term.shortDefinition.lowercase().contains(normalizedQuery) ||
                term.fullExplanation.lowercase().contains(normalizedQuery) ||
                term.tags.any { it.lowercase().contains(normalizedQuery) }
        }
    }

    override suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm> {
        return terms.filter { it.categoryId == categoryId }
    }


    override suspend fun askGlossary(question: String, termId: String?): AskGlossaryResponse {
        val term = termId?.let { getTermById(it) }
        val prefix = term?.let { "For ${it.term}: " } ?: ""
        return AskGlossaryResponse(
            answer = prefix + "${question.trim()} -> Start with the short definition, then connect to an example.",
            summary = "Use glossary definitions and examples to ground your understanding.",
            relatedTermIds = term?.relatedTerms ?: emptyList()
        )
    }

    override suspend fun generateScenario(
        termId: String,
        forceRefresh: Boolean
    ): GeneratedArtifactResult<LearningScenario> {
        val term = getTermById(termId) ?: error("Missing term")
        return GeneratedArtifactResult(
            artifact = LearningScenario(
                title = "Scenario: ${term.term} in product planning",
                difficulty = "beginner",
                context = "You are evaluating an AI feature for a customer support product.",
                objective = "Explain ${term.term} and how it changes design choices.",
                tasks = listOf("Define ${term.term}", "Identify one risk", "Propose one mitigation"),
                reflectionQuestions = listOf("What would fail without this concept?", "How would you measure success?")
            ),
            cached = !forceRefresh
        )
    }

    override suspend fun generateChallenge(
        termId: String,
        forceRefresh: Boolean
    ): GeneratedArtifactResult<LearningChallenge> {
        val term = getTermById(termId) ?: error("Missing term")
        return GeneratedArtifactResult(
            artifact = LearningChallenge(
                title = "Challenge: apply ${term.term}",
                difficulty = "beginner",
                prompt = "Write a short plan that applies ${term.term} in a real app.",
                successCriteria = listOf("Uses correct definition", "Includes an example", "Mentions one tradeoff"),
                hint = "Anchor your answer in the glossary definition and one production scenario."
            ),
            cached = !forceRefresh
        )
    }

}
