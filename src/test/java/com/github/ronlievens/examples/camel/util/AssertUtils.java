package com.github.ronlievens.examples.camel.util;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class AssertUtils {

    public static Path findFileInClasspath(@NonNull final String fileName) throws FileNotFoundException {
        try {
            return Paths.get(Objects.requireNonNull(AssertUtils.class.getClassLoader().getResource(fileName)).toURI());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException("Unable to find file: %s".formatted(fileName));
        }
    }

    public static String readFileAsStringFromClasspath(final String fileName) throws IOException {
        val file = findFileInClasspath(fileName);
        return Files.readString(file);
    }
}
