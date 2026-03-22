package com.ricky.collaboration;

import com.ricky.collaboration.collaboration.domain.ot.OperationTransformer;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationTransformerTest {
    
    private OperationTransformer transformer;
    
    @BeforeEach
    void setUp() {
        transformer = new OperationTransformer();
    }
    
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
    void should_return_client_op_when_server_op_is_null() {
        TextOperation clientOp = TextOperation.insert("user1", 0, "Hello", 0);
        
        TextOperation result = transformer.transform(clientOp, null);
        
        assertEquals(clientOp, result);
    }
}
