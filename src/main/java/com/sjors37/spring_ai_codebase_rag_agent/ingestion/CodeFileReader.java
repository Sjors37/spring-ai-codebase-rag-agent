package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    .filter(this::isInExcludedDirectory)
                    .filter(this::hasRelevantExtension)
                    .map(path -> toCodeFile(repoRoot, path))
                    .filter(file -> !file.content().isBlank())
                    .collect(Collectors.toList());
        }
    }

    private boolean isInExcludedDirectory(Path path) {
        for (Path part : path) {
            if (EXCLUDED_DIR_NAMES.contains(part.toString())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRelevantExtension(Path path) {
        String fileName = path.getFileName().toString();
        return RELEVANT_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private CodeFile toCodeFile(Path repoRoot, Path path) {
        try {
            String content = Files.readString(path);
            String relativePath = repoRoot.relativize(path).toString();
            return new CodeFile(path, relativePath, content);
        } catch (IOException e) {
            return new CodeFile(path, repoRoot.relativize(path).toString(), "");
        }
    }
}