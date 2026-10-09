package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class CodeFileReader {

    private static final Set<String> RELEVANT_EXTENSIONS = Set.of(
            ".java", ".md", ".yml", ".yaml", ".properties", ".xml", ".json"
    );

    private static final Set<String> EXCLUDED_DIR_NAMES = Set.of(
            ".git", "target", "build", "node_modules", ".idea", ".mvn"
    );

    public record CodeFile(Path path, String relativePath, String content) {}

    public List<CodeFile> readRelevantFiles(Path repoRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(repoRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isNotInExcludedDirectory)
                    .filter(this::hasRelevantExtension)
                    .map(path -> toCodeFile(repoRoot, path))
                    .filter(file -> !file.content().isBlank())
                    .toList();
        }
    }

    private boolean isNotInExcludedDirectory(Path path) {
        return StreamSupport.stream(path.spliterator(), false)
                .noneMatch(part -> EXCLUDED_DIR_NAMES.contains(part.toString()));
    }

    private boolean hasRelevantExtension(Path path) {
        String fileName = path.getFileName().toString();
        return RELEVANT_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private CodeFile toCodeFile(Path repoRoot, Path path) {
        String relativePath = repoRoot.relativize(path).toString();
        try {
            return new CodeFile(path, relativePath, Files.readString(path));
        } catch (IOException e) {
            return new CodeFile(path, relativePath, "");
        }
    }
}
