INSERT INTO categories (id, name, description) VALUES
('cat-ml-foundations', 'ML Foundations', 'Core concepts that explain how machine learning models learn from data.'),
('cat-llm-concepts', 'LLM Concepts', 'Important ideas for understanding large language model behavior and usage.'),
('cat-inference-serving', 'Inference & Serving', 'How trained models are run, exposed, and optimized in applications.'),
('cat-data-training', 'Data & Training', 'Data-centric and optimization concepts used to train reliable AI systems.'),
('cat-ai-safety', 'AI Safety', 'Concepts for safer model behavior, risk management, and responsible usage.')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description;

INSERT INTO terms (
    id, term, short_definition, full_explanation, category_id,
    tags, related_terms, example_usage, source, created_at, updated_at
) VALUES
('term-transformer', 'Transformer', 'A neural network architecture based on self-attention.', 'Transformer models process token sequences in parallel and learn relationships using attention layers instead of recurrent steps. They are the foundation for many modern LLMs because they scale effectively with data and compute.', 'cat-ml-foundations', 'neural-networks,attention,architecture', 'term-attention,term-token', 'Most current LLMs are Transformer-based.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-attention', 'Attention Mechanism', 'A method that lets models focus on relevant input parts.', 'Attention computes weighted relationships between tokens so the model can prioritize context that matters for the current prediction. Self-attention applies this within the same sequence.', 'cat-ml-foundations', 'attention,context,transformer', 'term-transformer,term-token', 'Attention helps models connect distant words in a sentence.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-token', 'Token', 'A unit of text that a language model processes.', 'Before training or inference, text is split into tokens based on a tokenizer vocabulary. Token count affects context window usage, latency, and cost.', 'cat-llm-concepts', 'tokenization,llm,context-window', 'term-context-window,term-transformer', 'Prompt length is often measured in tokens, not characters.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-context-window', 'Context Window', 'The maximum number of tokens a model can consider at once.', 'A model context window limits how much input and prior output can be retained for a response. Exceeding the limit requires truncation, summarization, or retrieval patterns.', 'cat-llm-concepts', 'llm,tokens,memory', 'term-token,term-rag', 'Long conversations can exceed the model context window.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-rag', 'Retrieval-Augmented Generation (RAG)', 'A pattern that adds external knowledge retrieval before response generation.', 'RAG systems retrieve relevant documents from a knowledge source and inject them into model context, improving factual grounding and freshness without retraining the model.', 'cat-llm-concepts', 'rag,retrieval,llm', 'term-embedding,term-vector-database', 'A support bot can use RAG to answer product questions from documentation.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-embedding', 'Embedding', 'A dense numeric vector representing semantic meaning.', 'Embeddings map text or other data into vector space so similar concepts are numerically close. They are commonly used for semantic search, clustering, and retrieval pipelines.', 'cat-data-training', 'embeddings,vectors,semantic-search', 'term-vector-database,term-rag', 'Two semantically similar sentences should have nearby embeddings.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-vector-database', 'Vector Database', 'A database optimized for nearest-neighbor search over vectors.', 'Vector databases index embedding vectors to support fast similarity search. They are frequently used as retrieval stores in RAG architectures.', 'cat-inference-serving', 'vector-search,retrieval,database', 'term-embedding,term-rag', 'A vector database returns the most similar chunks to a user query.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-inference', 'Inference', 'The process of running a trained model to produce outputs.', 'Inference happens after training and can run in batch or real-time systems. Performance factors include model size, hardware, precision, and serving strategy.', 'cat-inference-serving', 'inference,serving,latency', 'term-quantization,term-latency', 'Chat response generation is an inference workload.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-quantization', 'Quantization', 'A technique that reduces model precision to lower memory and compute costs.', 'Quantization converts model weights and sometimes activations to lower-bit formats, often reducing latency and infrastructure cost with manageable quality tradeoffs.', 'cat-inference-serving', 'optimization,latency,model-serving', 'term-inference,term-latency', 'Deploying an 8-bit model can reduce serving memory usage.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-latency', 'Latency', 'The time required for a system to return a response.', 'In AI apps, latency combines network, retrieval, and model generation time. Lower latency improves user experience but often requires careful tradeoffs.', 'cat-inference-serving', 'performance,ux,serving', 'term-inference,term-quantization', 'Streaming tokens can improve perceived latency.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-fine-tuning', 'Fine-tuning', 'Additional training of a pre-trained model on task-specific data.', 'Fine-tuning adapts general-purpose models for specialized tasks or domains. It can improve task performance but requires curation, evaluation, and safety checks.', 'cat-data-training', 'training,transfer-learning,adaptation', 'term-overfitting,term-evaluation', 'Teams fine-tune models for domain-specific support language.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-overfitting', 'Overfitting', 'When a model learns training data too specifically and generalizes poorly.', 'Overfitting occurs when a model memorizes training patterns that do not transfer to unseen data. Regularization, validation, and data diversity reduce this risk.', 'cat-data-training', 'generalization,training,evaluation', 'term-fine-tuning,term-evaluation', 'A model with high train accuracy but low test accuracy may be overfitting.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-evaluation', 'Model Evaluation', 'The process of measuring model quality using defined metrics and tests.', 'Evaluation combines automated metrics and scenario-based checks to assess accuracy, safety, and reliability. It is necessary before and after deployment changes.', 'cat-data-training', 'metrics,benchmarking,quality', 'term-overfitting,term-hallucination', 'Prompt-based regression tests are part of model evaluation.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-hallucination', 'Hallucination', 'When a model generates incorrect or fabricated information confidently.', 'Hallucinations are unsupported outputs that appear plausible but are not grounded in reliable evidence. Mitigation includes retrieval, prompt design, and verification workflows.', 'cat-ai-safety', 'safety,reliability,grounding', 'term-rag,term-evaluation', 'Answer verification is important when hallucination risk is high.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-guardrails', 'Guardrails', 'Controls that constrain model behavior and outputs.', 'Guardrails include policy filters, system prompts, validation checks, and moderation steps to reduce harmful or non-compliant responses in production systems.', 'cat-ai-safety', 'policy,safety,moderation', 'term-hallucination,term-red-teaming', 'A safety layer can block disallowed output categories.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
('term-red-teaming', 'Red Teaming', 'Structured adversarial testing of AI systems.', 'Red teaming probes systems with challenging or malicious scenarios to find vulnerabilities in safety, robustness, and policy compliance before broad release.', 'cat-ai-safety', 'safety,testing,robustness', 'term-guardrails,term-evaluation', 'Teams run red-team prompts before model launch.', 'Internal glossary seed', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO UPDATE SET
    term = EXCLUDED.term,
    short_definition = EXCLUDED.short_definition,
    full_explanation = EXCLUDED.full_explanation,
    category_id = EXCLUDED.category_id,
    tags = EXCLUDED.tags,
    related_terms = EXCLUDED.related_terms,
    example_usage = EXCLUDED.example_usage,
    source = EXCLUDED.source,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at;

-- Canonical content fidelity pass:
-- Ensure canonical columns are populated from structured source content and that
-- humor/controversy data is explicitly present for detail rendering parity.
UPDATE terms
SET
    definition = short_definition,
    explanation = full_explanation,
    humor = CASE id
        WHEN 'term-transformer' THEN 'Transformers replaced recurrence and never looked back.'
        WHEN 'term-attention' THEN 'Attention is the model way of saying: "this part matters."'
        WHEN 'term-token' THEN 'Tokens are tiny, but your billing statement notices every one.'
        WHEN 'term-context-window' THEN 'When context runs out, models get selective amnesia.'
        WHEN 'term-rag' THEN 'RAG is basically "open book exam mode" for LLMs.'
        WHEN 'term-embedding' THEN 'Embeddings turn meaning into math coordinates.'
        WHEN 'term-vector-database' THEN 'Needles in haystacks, found by cosine similarity.'
        WHEN 'term-inference' THEN 'Inference is where training bragging meets production traffic.'
        WHEN 'term-quantization' THEN 'Quantization: same idea, fewer bits, faster bills.'
        WHEN 'term-latency' THEN 'Users call anything over a second "forever."'
        WHEN 'term-fine-tuning' THEN 'Fine-tuning is teaching a valedictorian your house style.'
        WHEN 'term-overfitting' THEN 'Great on homework, confused by the final exam.'
        WHEN 'term-evaluation' THEN 'If you cannot measure it, you are just vibe-checking.'
        WHEN 'term-hallucination' THEN 'Confidently incorrect is still incorrect.'
        WHEN 'term-guardrails' THEN 'Guardrails keep demos from becoming incident reports.'
        WHEN 'term-red-teaming' THEN 'Red teaming asks the spicy questions before the internet does.'
        ELSE humor
    END,
    controversy_level = CASE id
        WHEN 'term-hallucination' THEN 2
        WHEN 'term-guardrails' THEN 2
        WHEN 'term-red-teaming' THEN 2
        WHEN 'term-rag' THEN 1
        WHEN 'term-fine-tuning' THEN 1
        ELSE 0
    END;

INSERT INTO term_relations (term_id, related_term_id) VALUES
('term-transformer', 'term-attention'),
('term-transformer', 'term-token'),
('term-attention', 'term-transformer'),
('term-attention', 'term-token'),
('term-token', 'term-context-window'),
('term-token', 'term-transformer'),
('term-context-window', 'term-token'),
('term-context-window', 'term-rag'),
('term-rag', 'term-embedding'),
('term-rag', 'term-vector-database'),
('term-embedding', 'term-vector-database'),
('term-embedding', 'term-rag'),
('term-vector-database', 'term-embedding'),
('term-vector-database', 'term-rag'),
('term-inference', 'term-quantization'),
('term-inference', 'term-latency'),
('term-quantization', 'term-inference'),
('term-quantization', 'term-latency'),
('term-latency', 'term-inference'),
('term-latency', 'term-quantization'),
('term-fine-tuning', 'term-overfitting'),
('term-fine-tuning', 'term-evaluation'),
('term-overfitting', 'term-fine-tuning'),
('term-overfitting', 'term-evaluation'),
('term-evaluation', 'term-overfitting'),
('term-evaluation', 'term-hallucination'),
('term-hallucination', 'term-rag'),
('term-hallucination', 'term-evaluation'),
('term-guardrails', 'term-hallucination'),
('term-guardrails', 'term-red-teaming'),
('term-red-teaming', 'term-guardrails'),
('term-red-teaming', 'term-evaluation')
ON CONFLICT (term_id, related_term_id) DO NOTHING;
