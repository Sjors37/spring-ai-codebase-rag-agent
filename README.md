# Spring AI Codebase RAG Agent

A multi-agent RAG (Retrieval-Augmented Generation) system built with **Spring Boot 4** and **Spring AI 2.0** that lets you ask natural-language questions about a codebase. It combines vector search over indexed source code with a small team of specialized agents that plan, retrieve, answer, and validate — rather than a single agent doing everything at once.

This is the third project in a series exploring agentic AI patterns in the Java/Spring ecosystem. See [`ops-assistant-agent`](https://github.com/Sjors37/ops-assistant-agent) (tool-calling agent) and [`spring-ai-mcp-git-analyzer`](https://github.com/Sjors37/spring-ai-mcp-git-analyzer) (MCP server/client) for the first two.

## Why this project

A naive RAG pipeline — embed a query, retrieve similar chunks, generate an answer — is a fixed sequence of steps, not an agent: nothing decides anything. This project makes RAG **agentic** by wrapping it in autonomous decision-making:

- A **planner** decides whether an answer is good enough or whether to retry with a different search angle
- A **validation agent** checks whether an answer is actually grounded in the retrieved passages, rather than assuming the model's output is correct
- Retrieval, answering, and validation are handled by separate, specialized agents (each with its own focused system prompt), coordinated by the planner — instead of one agent with a long, do-everything prompt

This "agent calling other agents" pattern is how more complex, production-grade AI systems are typically structured once a task is too broad for a single agent with a pile of tools.

## What it does

1. **Ingest** a codebase (a local path or a remote Git URL) — source files and docs are chunked and embedded into a vector store
2. **Ask a question** about that codebase — the planner retrieves relevant passages, generates an answer, and validates that the answer is actually supported by what was retrieved. If not, it retries with an adjusted search before giving up and saying so explicitly

Example: *"How does the ticket creation flow work in this codebase?"*

## Tech stack

- **Java 21**
- **Spring Boot 4.0**
- **Spring AI 2.0** — Anthropic chat model, in-process ONNX embeddings (`spring-ai-starter-model-transformers`, no external embedding API or separate server needed), `SimpleVectorStore`
- **JGit** (for cloning/reading a target repository, reused from `spring-ai-mcp-git-analyzer`)
- **Maven**

## Getting started

### Prerequisites
- Java 21+
- Maven
- An Anthropic API key
- A codebase to point it at — a local path or a public Git URL

### Run it

```bash
export ANTHROPIC_API_KEY=your-anthropic-key-here
export OPENAI_API_KEY=your-openai-key-here
./mvnw spring-boot:run
```

On first run, Spring AI downloads and caches a small local embedding model (one-time, a few tens of MB).

### Try it out

First, index a codebase:
```bash
curl -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d '{"source": "/absolute/path/to/a/repo"}'
```

Then ask it something:
```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How does the ticket creation flow work in this codebase?"}'
```

## Running tests

```bash
./mvnw test
```

The ingestion layer (file filtering, chunking, repository cloning, and the ingestion orchestration) is covered by unit tests that don't require a Spring context or the embedding model, so they run the same on every machine without any setup.

## Known limitations

- **Latency and cost scale with the agent chain**: each question triggers at least three separate Claude calls (retrieval, answer, validation), and up to six if the first attempt isn't grounded — noticeably slower/costlier than a single-agent setup.
- **No conversation memory** — same as the earlier two projects; each `/chat` request is stateless.
- **`SimpleVectorStore` is in-memory** — the index is lost on restart; re-run `/ingest` after restarting the app.
- **Retry logic is capped at 2 attempts** — if the codebase genuinely doesn't contain the answer, the agent says so rather than retrying indefinitely.
