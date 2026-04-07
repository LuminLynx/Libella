package com.example.foss101.data.repository

import com.example.foss101.model.Category
import com.example.foss101.model.GlossaryTerm

class MockGlossaryRepository : GlossaryRepository {

    private val categories = listOf(
        Category(
            id = "foss",
            name = "FOSS",
            description = "Free and open source software concepts."
        ),
        Category(
            id = "ai",
            name = "AI",
            description = "Artificial intelligence concepts and terms."
        ),
        Category(
            id = "dev",
            name = "Development",
            description = "Software development tools and practices."
        ),
        Category(
            id = "security",
            name = "Security",
            description = "Security-related concepts and practices."
        )
    )

    private val terms = listOf(
        GlossaryTerm(
            id = "open-source",
            term = "Open Source",
            shortDefinition = "Software whose source code is available to inspect, modify, and share.",
            fullExplanation = "Open source software makes its source code available under a license that allows people to study, modify, and redistribute it.",
            categoryId = "foss",
            tags = listOf("license", "software", "community"),
            relatedTerms = listOf("free-software"),
            exampleUsage = "Linux is a well-known open source project.",
            source = null
        ),
        GlossaryTerm(
            id = "free-software",
            term = "Free Software",
            shortDefinition = "Software that respects users' freedom to run, study, modify, and share it.",
            fullExplanation = "Free software emphasizes user freedoms rather than price. It is closely related to open source, though the philosophies are not identical.",
            categoryId = "foss",
            tags = listOf("freedom", "license"),
            relatedTerms = listOf("open-source"),
            exampleUsage = "GNU is strongly associated with the free software movement.",
            source = null
        ),
        GlossaryTerm(
            id = "machine-learning",
            term = "Machine Learning",
            shortDefinition = "A field of AI where systems learn patterns from data.",
            fullExplanation = "Machine learning uses data and algorithms to build models that can make predictions or decisions without being explicitly programmed for every rule.",
            categoryId = "ai",
            tags = listOf("ai", "models", "data"),
            relatedTerms = listOf("neural-network"),
            exampleUsage = "Spam filtering often uses machine learning.",
            source = null
        ),
        GlossaryTerm(
            id = "neural-network",
            term = "Neural Network",
            shortDefinition = "A machine learning model inspired by interconnected processing units.",
            fullExplanation = "A neural network is a layered model used in machine learning, especially deep learning, to recognize patterns in data.",
            categoryId = "ai",
            tags = listOf("deep-learning", "ai"),
            relatedTerms = listOf("machine-learning"),
            exampleUsage = "Image recognition systems often use neural networks.",
            source = null
        ),
        GlossaryTerm(
            id = "api",
            term = "API",
            shortDefinition = "A defined way for software systems to communicate with each other.",
            fullExplanation = "An API, or Application Programming Interface, defines how one software component can request or exchange data with another.",
            categoryId = "dev",
            tags = listOf("backend", "integration"),
            relatedTerms = listOf("rest-api"),
            exampleUsage = "The Android app calls the backend API to fetch glossary terms.",
            source = null
        ),
        GlossaryTerm(
            id = "rest-api",
            term = "REST API",
            shortDefinition = "A web API designed around resources and standard HTTP methods.",
            fullExplanation = "A REST API commonly uses endpoints, JSON, and HTTP methods like GET, POST, PUT, and DELETE to work with resources.",
            categoryId = "dev",
            tags = listOf("http", "json", "backend"),
            relatedTerms = listOf("api"),
            exampleUsage = "GET /terms is an example of a REST API endpoint.",
            source = null
        ),
        GlossaryTerm(
            id = "encryption",
            term = "Encryption",
            shortDefinition = "The process of converting data into a protected form.",
            fullExplanation = "Encryption transforms readable data into encoded data so only authorized parties can access the original information.",
            categoryId = "security",
            tags = listOf("privacy", "data"),
            relatedTerms = listOf("authentication"),
            exampleUsage = "HTTPS uses encryption to protect data in transit.",
            source = null
        ),
        GlossaryTerm(
            id = "authentication",
            term = "Authentication",
            shortDefinition = "The process of verifying identity.",
            fullExplanation = "Authentication confirms that a user or system is who it claims to be, often using passwords, tokens, or biometric methods.",
            categoryId = "security",
            tags = listOf("identity", "access"),
            relatedTerms = listOf("encryption"),
            exampleUsage = "Logging into an app usually requires authentication.",
            source = null
        ),
        GlossaryTerm(
        id = "linux",
        term = "Linux",
        shortDefinition = "An open source operating system kernel used in many systems.",
        fullExplanation = "Linux is an open source kernel that powers many operating systems, including servers, desktops, and Android devices.",
        categoryId = "foss",
        tags = listOf("kernel", "operating-system", "open-source"),
        relatedTerms = listOf("open-source"),
        exampleUsage = "Many web servers run on Linux.",
        source = null
        ),
        GlossaryTerm(
        id = "gnu",
        term = "GNU",
        shortDefinition = "A free software project and operating system initiative.",
        fullExplanation = "GNU is a free software project launched to create a Unix-like operating system made entirely of free software components.",
        categoryId = "foss",
        tags = listOf("free-software", "project"),
        relatedTerms = listOf("free-software", "linux"),
        exampleUsage = "GNU tools are widely used on Linux systems.",
        source = null
       ),
       GlossaryTerm(
       id = "copyleft",
       term = "Copyleft",
       shortDefinition = "A licensing approach that requires derived works to remain under the same license terms.",
       fullExplanation = "Copyleft uses copyright law to preserve software freedom by requiring modified or redistributed versions to stay under compatible open terms.",
       categoryId = "foss",
       tags = listOf("license", "freedom"),
       relatedTerms = listOf("free-software", "open-source"),
       exampleUsage = "The GPL is a well-known copyleft license.",
       source = null
       ),
       GlossaryTerm(
       id = "github",
       term = "GitHub",
       shortDefinition = "A platform for hosting and collaborating on Git repositories.",
       fullExplanation = "GitHub is a widely used platform for source control, code collaboration, pull requests, and issue tracking.",
       categoryId = "dev",
       tags = listOf("git", "repository", "collaboration"),
       relatedTerms = listOf("git"),
       exampleUsage = "The project source code is hosted on GitHub.",
       source = null
       ),
       GlossaryTerm(
       id = "git",
       term = "Git",
       shortDefinition = "A distributed version control system.",
       fullExplanation = "Git tracks changes in source code and supports branching, merging, and collaboration across distributed teams.",
       categoryId = "dev",
       tags = listOf("version-control", "repository"),
       relatedTerms = listOf("github"),
       exampleUsage = "Developers use Git to manage code history.",
       source = null
        ),
        GlossaryTerm(
        id = "frontend",
        term = "Frontend",
        shortDefinition = "The user-facing part of an application.",
        fullExplanation = "Frontend refers to the interface users interact with directly, such as screens, buttons, forms, and navigation.",
        categoryId = "dev",
        tags = listOf("ui", "client"),
        relatedTerms = listOf("backend"),
        exampleUsage = "The Android app is the frontend for the glossary system.",
        source = null
        ),
        GlossaryTerm(
        id = "backend",
        term = "Backend",
        shortDefinition = "The server-side part of an application that handles data and logic.",
        fullExplanation = "The backend manages business logic, APIs, databases, and data flow for the client application.",
        categoryId = "dev",
        tags = listOf("server", "api", "database"),
        relatedTerms = listOf("frontend", "api"),
        exampleUsage = "The glossary terms are served by the backend API.",
        source = null
        ),
        GlossaryTerm(
        id = "database",
        term = "Database",
        shortDefinition = "A system for storing, organizing, and retrieving data.",
        fullExplanation = "A database stores application data in a structured way so it can be queried, updated, and maintained efficiently.",
        categoryId = "dev",
        tags = listOf("storage", "data"),
        relatedTerms = listOf("backend", "api"),
        exampleUsage = "The backend reads glossary data from the database.",
        source = null
        ),
        GlossaryTerm(
        id = "prompt",
        term = "Prompt",
        shortDefinition = "Input given to an AI system to guide its response.",
        fullExplanation = "A prompt is the instruction or input text provided to an AI model to shape what it generates or how it responds.",
        categoryId = "ai",
        tags = listOf("ai", "input"),
        relatedTerms = listOf("machine-learning"),
        exampleUsage = "A clear prompt usually leads to better AI output.",
        source = null
        ),
        GlossaryTerm(
        id = "dataset",
        term = "Dataset",
        shortDefinition = "A collection of data used for analysis or model training.",
        fullExplanation = "A dataset is an organized set of examples or records used in software, analytics, or machine learning workflows.",
        categoryId = "ai",
        tags = listOf("data", "training"),
        relatedTerms = listOf("machine-learning"),
        exampleUsage = "The model was trained on a large dataset.",
        source = null
        ),
        GlossaryTerm(
        id = "deep-learning",
        term = "Deep Learning",
        shortDefinition = "A subset of machine learning based on multi-layer neural networks.",
        fullExplanation = "Deep learning uses neural networks with many layers to model complex patterns in data such as images, text, and sound.",
        categoryId = "ai",
        tags = listOf("ai", "neural-network"),
        relatedTerms = listOf("machine-learning", "neural-network"),
        exampleUsage = "Deep learning is widely used in image recognition.",
        source = null
        ),
        GlossaryTerm(
        id = "model",
        term = "Model",
        shortDefinition = "A learned representation used by software to make predictions or generate outputs.",
        fullExplanation = "In AI and machine learning, a model is the result of training on data so it can perform tasks such as classification, prediction, or generation.",
        categoryId = "ai",
        tags = listOf("ai", "prediction"),
        relatedTerms = listOf("machine-learning", "dataset"),
        exampleUsage = "The app may later call a language model through an API.",
        source = null
        ),
        GlossaryTerm(
        id = "authorization",
        term = "Authorization",
        shortDefinition = "The process of deciding what an authenticated user is allowed to do.",
        fullExplanation = "Authorization controls permissions after identity is verified, determining what resources or actions are allowed.",
        categoryId = "security",
        tags = listOf("permissions", "access-control"),
        relatedTerms = listOf("authentication"),
        exampleUsage = "An admin action may require authorization checks.",
        source = null
       ),
        GlossaryTerm(
        id = "https",
        term = "HTTPS",
        shortDefinition = "A secure version of HTTP that protects data in transit.",
        fullExplanation = "HTTPS uses TLS encryption to secure communication between clients and servers over the web.",
        categoryId = "security",
        tags = listOf("web", "encryption", "tls"),
        relatedTerms = listOf("encryption"),
        exampleUsage = "APIs should use HTTPS to protect requests and responses.",
        source = null
        ),
        GlossaryTerm(
        id = "token",
        term = "Token",
        shortDefinition = "A value used in authentication, authorization, or text processing contexts.",
        fullExplanation = "A token can represent access rights in security systems or a unit of text in language model processing, depending on context.",
        categoryId = "security",
        tags = listOf("authentication", "access"),
        relatedTerms = listOf("authentication", "authorization"),
        exampleUsage = "An API may require a token for secure access.",
        source = null
        ),
        GlossaryTerm(
        id = "vulnerability",
        term = "Vulnerability",
        shortDefinition = "A weakness that can be exploited to compromise a system.",
        fullExplanation = "A vulnerability is a flaw in software, hardware, or configuration that could allow unauthorized access or other harmful actions.",
        categoryId = "security",
        tags = listOf("risk", "security"),
        relatedTerms = listOf("encryption", "authentication"),
        exampleUsage = "Outdated software may contain known vulnerabilities.",
        source = null
        )

    )

    override fun getAllTerms(): List<GlossaryTerm> = terms

    override fun getTermById(id: String): GlossaryTerm? {
        return terms.find { it.id == id }
    }

    override fun getAllCategories(): List<Category> = categories

    override fun searchTerms(query: String): List<GlossaryTerm> {
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

    override fun getTermsByCategory(categoryId: String): List<GlossaryTerm> {
        return terms.filter { it.categoryId == categoryId }
    }
}