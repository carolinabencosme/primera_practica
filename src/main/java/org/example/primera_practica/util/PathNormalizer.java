package org.example.primera_practica.util;

public final class PathNormalizer {

    private PathNormalizer() {
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return "/";
        }
        if (!trimmed.startsWith("/")) {
            return "/" + trimmed;
        }
        return trimmed;
    }
}
