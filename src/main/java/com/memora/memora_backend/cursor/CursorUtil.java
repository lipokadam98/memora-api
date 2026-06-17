package com.memora.memora_backend.cursor;


import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.Base64;

public class CursorUtil {

    /**
     * Encode the uploadDate and id into a cursor string.
     * @param uploadDate the upload date of the multimedia
     * @param id the id of the multimedia
     * @return the encoded cursor string
     */
    public static String encode(Instant uploadDate, Long id) {
        String raw = uploadDate.toString() + "|" + id;
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    /**
     * Decode the cursor string into a pair of uploadDate and id.
     * @param cursor the cursor string to decode
     * @return the pair of uploadDate and id
     */
    public static Pair<Instant, Long> decode(String cursor) {
        if (cursor == null) return null;

        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split("\\|");

        return Pair.of(
                Instant.parse(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}