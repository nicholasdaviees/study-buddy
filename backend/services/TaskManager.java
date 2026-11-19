package services;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.*;
import java.util.UUID;

public class TaskManager {

    public enum Status{
        QUEUED, PROCESSING, COMPLETE, FAILED
    }
    
    private static class TaskRecord{
        private final String taskID;
        private final String taskType;
        private volatile Status status;
        private volatile String resultId;
        private volatile String error;

        private TaskRecord(String taskId, String taskType){
            this.taskID = taskId;
            this.taskType = taskType;
            this.status = Status.QUEUED;
        }
    }

    private final ConcurrentMap<String, TaskRecord> tasks = new ConcurrentHashMap<>();
    private final int processors = Runtime.getRuntime().availableProcessors();
    private final int maxWorkers = Math.max(2, processors/4);
    private final ExecutorService executor = Executors.newFixedThreadPool(maxWorkers);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Submits work to a background worker and returns its task ID.
    public String submitTask(String taskType, Callable<String> task) {
        return null;
    }

    // Returns task status, result, and error information.
    public String getTask(String taskId) {
        return null;
        
    }

    // Stops the worker pool when the application shuts down.
    public void shutdown() {
    }
}
