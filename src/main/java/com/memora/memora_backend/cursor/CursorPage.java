package com.memora.memora_backend.cursor;

import java.util.List;

public record CursorPage<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {}