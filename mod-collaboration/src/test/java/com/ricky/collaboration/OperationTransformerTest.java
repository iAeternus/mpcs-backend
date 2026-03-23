package com.ricky.collaboration;

import com.ricky.collaboration.collaboration.domain.ot.OperationTransformer;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationTransformerTest {

    private OperationTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new OperationTransformer();
    }

    @Nested
    @DisplayName("INSERT-INSERT transformation")
    class InsertInsertTests {

        @Test
        void should_transform_insert_insert_when_client_insert_before_server() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "World", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals("Hello", result.getContent());
        }

        @Test
        void should_transform_insert_insert_when_client_insert_after_server() {
            TextOperation clientOp = TextOperation.insert("user1", 10, "Hello", 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "World", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(15, result.getPosition());
            assertEquals("Hello", result.getContent());
        }

        @Test
        void should_transform_insert_insert_when_client_insert_at_same_position() {
            TextOperation clientOp = TextOperation.insert("user1", 5, "A", 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "B", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(5, result.getPosition());
        }

        @Test
        void should_transform_insert_insert_when_both_insert_at_beginning() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "A", 0);
            TextOperation serverOp = TextOperation.insert("user2", 0, "B", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_transform_multi_char_insert() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);
            TextOperation serverOp = TextOperation.insert("user2", 3, "XXX", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals("Hello", result.getContent());
        }
    }

    @Nested
    @DisplayName("INSERT-DELETE transformation")
    class InsertDeleteTests {

        @Test
        void should_transform_insert_delete_when_insert_before_delete() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);
            TextOperation serverOp = TextOperation.delete("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_transform_insert_delete_when_insert_after_delete() {
            TextOperation clientOp = TextOperation.insert("user1", 10, "Hello", 0);
            TextOperation serverOp = TextOperation.delete("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(7, result.getPosition());
        }

        @Test
        void should_transform_insert_delete_when_insert_overlaps_delete() {
            TextOperation clientOp = TextOperation.insert("user1", 3, "Hello", 0);
            TextOperation serverOp = TextOperation.delete("user2", 0, 5, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_transform_insert_delete_when_delete_removes_everything_before() {
            TextOperation clientOp = TextOperation.insert("user1", 10, "Hi", 0);
            TextOperation serverOp = TextOperation.delete("user2", 0, 15, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }
    }

    @Nested
    @DisplayName("DELETE-INSERT transformation")
    class DeleteInsertTests {

        @Test
        void should_transform_delete_insert_when_delete_before_insert() {
            TextOperation clientOp = TextOperation.delete("user1", 0, 3, 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "Hi", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals(3, result.getLength());
        }

        @Test
        void should_transform_delete_insert_when_delete_after_insert() {
            TextOperation clientOp = TextOperation.delete("user1", 10, 3, 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "Hi", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(12, result.getPosition());
        }

        @Test
        void should_transform_delete_insert_when_delete_at_same_position() {
            TextOperation clientOp = TextOperation.delete("user1", 5, 3, 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "A", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(6, result.getPosition());
        }

        @Test
        void should_transform_delete_insert_when_insert_in_middle_of_delete() {
            TextOperation clientOp = TextOperation.delete("user1", 0, 10, 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "X", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals(11, result.getLength());
        }
    }

    @Nested
    @DisplayName("DELETE-DELETE transformation")
    class DeleteDeleteTests {

        @Test
        void should_transform_delete_delete_no_overlap_before() {
            TextOperation clientOp = TextOperation.delete("user1", 0, 3, 0);
            TextOperation serverOp = TextOperation.delete("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_transform_delete_delete_with_overlap() {
            TextOperation clientOp = TextOperation.delete("user1", 3, 5, 0);
            TextOperation serverOp = TextOperation.delete("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(3, result.getPosition());
        }

        @Test
        void should_transform_delete_delete_when_both_delete_same_position() {
            TextOperation clientOp = TextOperation.delete("user1", 5, 3, 0);
            TextOperation serverOp = TextOperation.delete("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(5, result.getPosition());
        }

        @Test
        void should_transform_delete_delete_when_client_delete_before_server() {
            TextOperation clientOp = TextOperation.delete("user1", 0, 5, 0);
            TextOperation serverOp = TextOperation.delete("user2", 3, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_transform_delete_delete_when_server_covers_client() {
            TextOperation clientOp = TextOperation.delete("user1", 5, 3, 0);
            TextOperation serverOp = TextOperation.delete("user2", 0, 10, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals(0, result.getLength());
        }
    }

    @Nested
    @DisplayName("Batch transformation")
    class BatchTests {

        @Test
        void should_transform_batch_operations() {
            List<TextOperation> clientOps = List.of(
                    TextOperation.insert("user1", 0, "A", 0),
                    TextOperation.insert("user1", 1, "B", 1)
            );
            List<TextOperation> serverOps = List.of(
                    TextOperation.insert("user2", 0, "X", 1)
            );

            List<TextOperation> result = transformer.transformBatch(clientOps, serverOps);

            assertEquals(2, result.size());
        }

        @Test
        void should_transform_empty_client_ops() {
            List<TextOperation> clientOps = List.of();
            List<TextOperation> serverOps = List.of(
                    TextOperation.insert("user2", 0, "X", 1)
            );

            List<TextOperation> result = transformer.transformBatch(clientOps, serverOps);

            assertEquals(0, result.size());
        }

        @Test
        void should_transform_empty_server_ops() {
            List<TextOperation> clientOps = List.of(
                    TextOperation.insert("user1", 0, "A", 0)
            );
            List<TextOperation> serverOps = List.of();

            List<TextOperation> result = transformer.transformBatch(clientOps, serverOps);

            assertEquals(1, result.size());
        }

        @Test
        void should_transform_complex_batch() {
            List<TextOperation> clientOps = List.of(
                    TextOperation.insert("user1", 0, "Hello", 0),
                    TextOperation.delete("user1", 5, 2, 1)
            );
            List<TextOperation> serverOps = List.of(
                    TextOperation.insert("user2", 0, "X", 1),
                    TextOperation.insert("user2", 3, "Y", 2)
            );

            List<TextOperation> result = transformer.transformBatch(clientOps, serverOps);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Null handling")
    class NullTests {

        @Test
        void should_return_client_op_when_server_op_is_null() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);

            TextOperation result = transformer.transform(clientOp, null);

            assertEquals(clientOp, result);
        }

        @Test
        void should_handle_null_client_op_gracefully() {
            TextOperation serverOp = TextOperation.insert("user2", 0, "Hello", 1);

            assertThrows(NullPointerException.class, () -> {
                transformer.transform(null, serverOp);
            });
        }

        @Test
        void should_return_empty_list_when_both_ops_are_empty() {
            List<TextOperation> result = transformer.transformBatch(List.of(), List.of());

            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("RETAIN operation")
    class RetainTests {

        @Test
        void should_handle_retain_after_insert() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "A", 0);
            TextOperation serverOp = TextOperation.retain("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
            assertEquals(TextOperationType.INSERT, result.getType());
        }

        @Test
        void should_handle_retain_before_insert() {
            TextOperation clientOp = TextOperation.insert("user1", 10, "A", 0);
            TextOperation serverOp = TextOperation.retain("user2", 5, 3, 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(10, result.getPosition());
            assertEquals(TextOperationType.INSERT, result.getType());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        void should_handle_empty_string_insert_at_zero() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "", 0);
            TextOperation serverOp = TextOperation.insert("user2", 0, "X", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(0, result.getPosition());
        }

        @Test
        void should_handle_large_position_values() {
            TextOperation clientOp = TextOperation.insert("user1", 1000, "A", 0);
            TextOperation serverOp = TextOperation.insert("user2", 500, "B", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertEquals(1001, result.getPosition());
        }

        @Test
        void should_preserve_operation_metadata() {
            TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);
            TextOperation serverOp = TextOperation.insert("user2", 5, "X", 1);

            TextOperation result = transformer.transform(clientOp, serverOp);

            assertNotNull(result);
            assertEquals(TextOperationType.INSERT, result.getType());
        }
    }
}
