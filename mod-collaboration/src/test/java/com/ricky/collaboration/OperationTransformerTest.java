package com.ricky.collaboration;

import com.ricky.collaboration.domain.ot.OperationTransformer;
import com.ricky.collaboration.domain.ot.TextOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationTransformerTest {

    private OperationTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new OperationTransformer();
    }

    @Test
    @DisplayName("insert/insert at same position converges with user id tie-breaker")
    void should_converge_for_insert_insert_at_same_position() {
        TextOperation userA = TextOperation.insert("user-a", 0, "A", 0);
        TextOperation userB = TextOperation.insert("user-b", 0, "B", 0);

        assertConverges("", userA, userB, "AB");
    }

    @Test
    @DisplayName("insert after remote insert shifts by inserted length")
    void should_shift_insert_after_remote_insert() {
        TextOperation clientOp = TextOperation.insert("user1", 5, "!", 1);
        TextOperation serverOp = TextOperation.insert("user2", 0, "Hello", 0);

        TextOperation transformed = transformer.transform(clientOp, serverOp);

        assertEquals(10, transformed.getPosition());
        assertEquals("!", transformed.getContent());
        assertEquals("Helloabcde!", apply("abcde", serverOp, transformed));
    }

    @Test
    @DisplayName("insert at remote insert end boundary still shifts by inserted length")
    void should_shift_insert_at_remote_insert_end_boundary() {
        TextOperation clientOp = TextOperation.insert("user1", 5, "World", 1);
        TextOperation serverOp = TextOperation.insert("user2", 0, "Hello", 0);

        TextOperation transformed = transformer.transform(clientOp, serverOp);

        assertEquals(10, transformed.getPosition());
        assertEquals("HelloabcdeWorld", apply("abcde", serverOp, transformed));
    }

    @Test
    @DisplayName("insert inside deleted range moves to delete start")
    void should_move_insert_to_delete_start() {
        TextOperation clientOp = TextOperation.insert("user1", 3, "X", 0);
        TextOperation serverOp = TextOperation.delete("user2", 1, 4, 0);

        TextOperation transformed = transformer.transform(clientOp, serverOp);

        assertEquals(1, transformed.getPosition());
        assertEquals("aXf", apply("abcdef", serverOp, transformed));
    }

    @Test
    @DisplayName("delete spanning remote insert expands to keep semantic target")
    void should_expand_delete_when_remote_insert_splits_range() {
        TextOperation clientOp = TextOperation.delete("user1", 2, 4, 0);
        TextOperation serverOp = TextOperation.insert("user2", 4, "ZZ", 0);

        TextOperation transformed = transformer.transform(clientOp, serverOp);

        assertEquals(2, transformed.getPosition());
        assertEquals(6, transformed.getLength());
        assertEquals("abgh", apply("abcdefgh", serverOp, transformed));
    }

    @Test
    @DisplayName("delete fully covered by remote delete becomes no-op")
    void should_turn_covered_delete_into_noop() {
        TextOperation clientOp = TextOperation.delete("user1", 4, 3, 0);
        TextOperation serverOp = TextOperation.delete("user2", 0, 10, 0);

        TextOperation transformed = transformer.transform(clientOp, serverOp);

        assertEquals(0, transformed.getPosition());
        assertEquals(0, transformed.getLength());
    }

    @Test
    @DisplayName("batch transform drops deletes reduced to zero length")
    void should_filter_zero_length_delete_from_batch() {
        List<TextOperation> clientOps = List.of(
                TextOperation.delete("user1", 4, 3, 0),
                TextOperation.insert("user1", 0, "A", 0)
        );
        List<TextOperation> serverOps = List.of(
                TextOperation.delete("user2", 0, 10, 0)
        );

        List<TextOperation> transformed = transformer.transformBatch(clientOps, serverOps);

        assertEquals(1, transformed.size());
        assertTrue(transformed.get(0).isInsert());
    }

    private void assertConverges(String base, TextOperation first, TextOperation second, String expected) {
        TextOperation firstPrime = transformer.transform(first, second);
        TextOperation secondPrime = transformer.transform(second, first);

        String applySecondThenFirst = apply(base, second, firstPrime);
        String applyFirstThenSecond = apply(base, first, secondPrime);

        assertEquals(expected, applySecondThenFirst);
        assertEquals(expected, applyFirstThenSecond);
    }

    private String apply(String base, TextOperation... operations) {
        String result = base;
        for (TextOperation operation : operations) {
            if (operation.isInsert()) {
                result = result.substring(0, operation.getPosition())
                        + operation.getContent()
                        + result.substring(operation.getPosition());
            } else if (operation.isDelete() && operation.getLength() > 0) {
                result = result.substring(0, operation.getPosition())
                        + result.substring(operation.getPosition() + operation.getLength());
            }
        }
        return result;
    }
}
