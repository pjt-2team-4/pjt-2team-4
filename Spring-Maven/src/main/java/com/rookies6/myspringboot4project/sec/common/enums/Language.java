package com.rookies6.myspringboot4project.sec.common.enums;

import java.util.Arrays;
import java.util.Set;

/**
 * 파일 확장자로 판별하는 코드 언어. Scanner가 적용할 룰을 고르는 기준이 된다.
 */
public enum Language {

    JAVA("Java", Set.of("java")),
    JAVASCRIPT("JavaScript", Set.of("js", "jsx", "mjs", "cjs")),
    TYPESCRIPT("TypeScript", Set.of("ts", "tsx")),
    PYTHON("Python", Set.of("py")),
    HTML("HTML", Set.of("html", "htm", "jsp")),
    UNKNOWN("Unknown", Set.of());

    private final String displayName;
    private final Set<String> extensions;

    Language(String displayName, Set<String> extensions) {
        this.displayName = displayName;
        this.extensions = extensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSupported() {
        return this != UNKNOWN;
    }

    public static Language fromFileName(String fileName) {
        if (fileName == null) {
            return UNKNOWN;
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0) {
            return UNKNOWN;
        }
        return fromExtension(fileName.substring(idx + 1));
    }

    public static Language fromExtension(String extension) {
        if (extension == null) {
            return UNKNOWN;
        }
        String lower = extension.toLowerCase();
        return Arrays.stream(values())
                .filter(language -> language.extensions.contains(lower))
                .findFirst()
                .orElse(UNKNOWN);
    }
}