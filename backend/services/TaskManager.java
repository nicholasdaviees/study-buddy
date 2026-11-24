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
        if (task == null){
            throw new IllegalArgumentException("No tasks submitted for processing");
            
        }
        String taskID = UUID.randomUUID().randomUUID().toString();
        
        TaskRecord record = new TaskRecord(taskID, taskType);
        tasks.put(taskID, record);
        executor.submit(() -> runTask(record, task));
        return taskID;
    }
    //Executes a task and records its status
    private void runTask(TaskRecord record, Callable<String> task){
        record.status= Status.PROCESSING;

        try{
            String result = task.call();

            if (result == null || result.isBlank()) {
                throw new IllegalStateException("Task returned no result");
            }
            
            record.resultId = result;
            record.status = Status.COMPLETE;
        } catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            record.error = "Task failed";
            record.status = Status.FAILED;
        } catch(Exception exception) {
            record.error = "Task failed";
            record.status = Status.FAILED;
        }
    }

    // Returns task status, result, and error information.
    public String getTask(String taskId) {
        if(taskId == null || taskId.isBlank()){
            throw new IllegalArgumentException("Task ID is null");
        }

        TaskRecord record = tasks.get(taskId);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();

        if (record == null){
            response.put("status", "NOT_FOUND");
            response.put("error", "No tasks found");
        } else {
            response.put("taskId", record.taskID);
            response.put("taskType", record.taskType);

            Status status = record.status;
            response.put("status", status.name());
            
            if (status == Status.COMPLETE){
                response.put("resultId", record.resultId);
            } else if(status == Status.FAILED){
                response.put("error", record.error);
            }
        }

        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception){
            throw new IllegalStateException("could not serialize task response", exception);
        }
        
    }

    // Stops the worker pool when the application shuts down.
    public void shutdown() {
        executor.shutdown();
    }
}
