package com.ricky.collaboration.collaboration.domain.ot;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OperationTransformer {

    public TextOperation transform(TextOperation clientOp, TextOperation serverOp) {
        if (serverOp == null) {
            return clientOp;
        }

        TextOperationType clientType = clientOp.getType();
        TextOperationType serverType = serverOp.getType();

        if (clientType == TextOperationType.INSERT && serverType == TextOperationType.INSERT) {
            return transformInsertInsert(clientOp, serverOp);
        }

        if (clientType == TextOperationType.INSERT && serverType == TextOperationType.DELETE) {
            return transformInsertDelete(clientOp, serverOp);
        }

        if (clientType == TextOperationType.DELETE && serverType == TextOperationType.INSERT) {
            return transformDeleteInsert(clientOp, serverOp);
        }

        if (clientType == TextOperationType.DELETE && serverType == TextOperationType.DELETE) {
            return transformDeleteDelete(clientOp, serverOp);
        }

        return clientOp;
    }

    private TextOperation transformInsertInsert(TextOperation clientOp, TextOperation serverOp) {
        int clientPos = clientOp.getPosition();
        int serverPos = serverOp.getPosition();
        int serverLen = serverOp.getContent() != null ? serverOp.getContent().length() : 0;

        if (clientPos < serverPos) {
            return clientOp;
        } else if (clientPos > serverPos) {
            return TextOperation.insert(
                    clientOp.getUserId(),
                    clientPos + serverLen,
                    clientOp.getContent(),
                    clientOp.getClientVersion()
            );
        } else {
            if (clientOp.getUserId().compareTo(serverOp.getUserId()) < 0) {
                return clientOp;
            } else {
                return TextOperation.insert(
                        clientOp.getUserId(),
                        clientPos + serverLen,
                        clientOp.getContent(),
                        clientOp.getClientVersion()
                );
            }
        }
    }

    private TextOperation transformInsertDelete(TextOperation clientOp, TextOperation serverOp) {
        int clientPos = clientOp.getPosition();
        int serverPos = serverOp.getPosition();
        int serverLen = serverOp.getLength();

        if (clientPos <= serverPos) {
            return clientOp;
        } else if (clientPos >= serverPos + serverLen) {
            return TextOperation.insert(
                    clientOp.getUserId(),
                    clientPos - serverLen,
                    clientOp.getContent(),
                    clientOp.getClientVersion()
            );
        } else {
            return TextOperation.insert(
                    clientOp.getUserId(),
                    serverPos,
                    clientOp.getContent(),
                    clientOp.getClientVersion()
            );
        }
    }

    private TextOperation transformDeleteInsert(TextOperation clientOp, TextOperation serverOp) {
        int clientPos = clientOp.getPosition();
        int clientLen = clientOp.getLength();
        int serverPos = serverOp.getPosition();
        int serverLen = serverOp.getContent() != null ? serverOp.getContent().length() : 0;

        if (clientPos >= serverPos) {
            return TextOperation.delete(
                    clientOp.getUserId(),
                    clientPos + serverLen,
                    clientLen,
                    clientOp.getClientVersion()
            );
        } else if (clientPos + clientLen <= serverPos) {
            return clientOp;
        } else {
            return TextOperation.delete(
                    clientOp.getUserId(),
                    clientPos,
                    clientLen + serverLen,
                    clientOp.getClientVersion()
            );
        }
    }

    private TextOperation transformDeleteDelete(TextOperation clientOp, TextOperation serverOp) {
        int clientPos = clientOp.getPosition();
        int clientLen = clientOp.getLength();
        int serverPos = serverOp.getPosition();
        int serverLen = serverOp.getLength();

        if (clientPos >= serverPos + serverLen) {
            return TextOperation.delete(
                    clientOp.getUserId(),
                    clientPos - serverLen,
                    clientLen,
                    clientOp.getClientVersion()
            );
        } else if (clientPos + clientLen <= serverPos) {
            return clientOp;
        } else if (clientPos >= serverPos && clientPos + clientLen <= serverPos + serverLen) {
            return TextOperation.delete(
                    clientOp.getUserId(),
                    serverPos,
                    0,
                    clientOp.getClientVersion()
            );
        } else if (clientPos < serverPos && clientPos + clientLen > serverPos + serverLen) {
            return TextOperation.delete(
                    clientOp.getUserId(),
                    serverPos,
                    clientLen - serverLen,
                    clientOp.getClientVersion()
            );
        } else if (clientPos < serverPos) {
            int overlap = clientPos + clientLen - serverPos;
            return TextOperation.delete(
                    clientOp.getUserId(),
                    clientPos,
                    clientLen - overlap,
                    clientOp.getClientVersion()
            );
        } else {
            int overlap = serverPos + serverLen - clientPos;
            return TextOperation.delete(
                    clientOp.getUserId(),
                    serverPos,
                    clientLen - overlap,
                    clientOp.getClientVersion()
            );
        }
    }

    public List<TextOperation> transformBatch(List<TextOperation> clientOps, List<TextOperation> serverOps) {
        List<TextOperation> result = new ArrayList<>(clientOps);

        for (TextOperation serverOp : serverOps) {
            List<TextOperation> transformed = new ArrayList<>();
            for (TextOperation clientOp : result) {
                TextOperation transformedOp = transform(clientOp, serverOp);
                if (transformedOp.getLength() > 0 || transformedOp.isInsert()) {
                    transformed.add(transformedOp);
                }
            }
            result = transformed;
        }

        return result;
    }
}
