package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CodebaseIngestionService {

    private final GitRepositoryCloner gitRepositoryCloner;
    private final CodeFileReader codeFileReader;
    private final TextChunker textChunker;
    private final VectorStore vectorStore;

    public int ingest(String source) throws IOException, GitAPIException {
        Path repoRoot = gitRepositoryCloner.prepareLocalPath(source);

        List<Document> documents = codeFileReader.readRelevantFiles(repoRoot).stream()
                .flatMap(this::toDocuments)
                .toList();

        vectorStore.add(documents);
        return documents.size();
    }

    private Stream<Document> toDocuments(CodeFileReader.CodeFile file) {
        List<String> chunks = textChunker.chunk(file.content());
        return IntStream.range(0, chunks.size())
                .mapToObj(i -> new Document(
                        chunks.get(i),
                        Map.of("filePath", file.relativePath(), "chunkIndex", i)
                ));
    }
}
