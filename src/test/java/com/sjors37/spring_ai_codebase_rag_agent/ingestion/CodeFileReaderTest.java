package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeFileReaderTest {

    @TempDir
    Path tempDir;

    private CodeFileReader codeFileReader;

    @BeforeEach
    void setUp() {
        codeFileReader = new CodeFileReader();
    }

    @Test
    void readRelevantFiles_includesJavaAndMarkdownFiles() throws IOException {
        writeFile("src/Main.java", "public class Main {}");
        writeFile("README.md", "# Project");

        List<CodeFileReader.CodeFile> result = codeFileReader.readRelevantFiles(tempDir);

        assertThat(result).extracting(CodeFileReader.CodeFile::relativePath)
                .containsExactlyInAnyOrder(
                        Path.of("src", "Main.java").toString(),
                        "README.md"
                );
    }

    @Test
    void readRelevantFiles_excludesIrrelevantExtensions() throws IOException {
        writeFile("image.png", "fake-binary-content");
        writeFile("Main.java", "public class Main {}");

        List<CodeFileReader.CodeFile> result = codeFileReader.readRelevantFiles(tempDir);

        assertThat(result).extracting(CodeFileReader.CodeFile::relativePath)
                .containsExactly("Main.java");
    }

    @Test
    void readRelevantFiles_excludesFilesInExcludedDirectories() throws IOException {
        writeFile("target/classes/Main.java", "compiled stuff");
        writeFile("src/Main.java", "public class Main {}");
        writeFile(".git/config", "git internals");

        List<CodeFileReader.CodeFile> result = codeFileReader.readRelevantFiles(tempDir);

        assertThat(result).extracting(CodeFileReader.CodeFile::relativePath)
                .containsExactly(Path.of("src", "Main.java").toString());
    }

    @Test
    void readRelevantFiles_skipsBlankFiles() throws IOException {
        writeFile("Empty.java", "");
        writeFile("Main.java", "public class Main {}");

        List<CodeFileReader.CodeFile> result = codeFileReader.readRelevantFiles(tempDir);

        assertThat(result).extracting(CodeFileReader.CodeFile::relativePath)
                .containsExactly("Main.java");
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path filePath = tempDir.resolve(relativePath);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content);
    }
}