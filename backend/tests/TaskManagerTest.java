package tests;

import services.TaskManager;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager manager;
    private final CountDownLatch release = new CountDownLatch(1);
    private final int workerLimit = Math.max(2, Runtime.getRuntime().availableProcessors() / 4);

    @BeforeEach
    void setUp() { manager = new TaskManager(); }

    @AfterEach
    void tearDown() {
        release.countDown();
        manager.shutdown();
    }

    // Keep work running until the test explicitly releases it.
    private String blockedWork() throws InterruptedException {
        if (!release.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Test gate timed out");
        }
        return "result-1";
    }

    private JSONObject readTask(String id) {
        return new JSONObject(manager.getTask(id));
    }

    // Poll with a deadline, rather than assuming a worker finishes immediately.
    private JSONObject awaitStatus(String id, String expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        JSONObject task;
        do {
            task = readTask(id);
            if (expected.equals(task.optString("status"))) return task;
            Thread.sleep(10);
        } while (System.nanoTime() < deadline);
        assertEquals(expected, task.optString("status"), task.toString());
        return task;
    }

    @Test
    public void returnsTaskIdImmediately() {
        String id = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> manager.submitTask("TEST", this::blockedWork));
        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals(id, readTask(id).getString("taskId"));
        assertEquals(1L, release.getCount());
    }

    @Test
    public void reportsQueuedAndProcessingStates() throws Exception {
        CountDownLatch started = new CountDownLatch(workerLimit);
        String running = null;
        for (int i = 0; i < workerLimit; i++) {
            running = manager.submitTask("TEST", () -> {
                started.countDown();
                return blockedWork();
            });
        }
        assertTrue(started.await(3, TimeUnit.SECONDS));
        assertEquals("PROCESSING", readTask(running).optString("status"));
        String queued = manager.submitTask("TEST", () -> "queued-result");
        assertEquals("QUEUED", readTask(queued).optString("status"));
        release.countDown();
        awaitStatus(queued, "COMPLETE");
    }

    @Test
    public void completesSuccessfulTaskWithResult() throws Exception {
        String id = manager.submitTask("TEST", () -> "result-1");
        JSONObject task = awaitStatus(id, "COMPLETE");
        assertEquals("result-1", task.getString("resultId"));
        assertEquals("TEST", task.getString("taskType"));
    }

    @Test
    public void recordsFailedTaskWithError() throws Exception {
        String id = manager.submitTask("TEST", () -> { throw new IllegalStateException("failure"); });
        JSONObject task = awaitStatus(id, "FAILED");
        assertEquals("Task failed", task.getString("error"));
    }

    @Test
    public void limitsConcurrentWorkers() throws Exception {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(workerLimit);
        CountDownLatch extraStarted = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(workerLimit + 1);
        for (int i = 0; i <= workerLimit; i++) {
            final boolean extra = i == workerLimit;
            manager.submitTask("TEST", () -> {
                int count = active.incrementAndGet();
                peak.accumulateAndGet(count, Math::max);
                if (extra) extraStarted.countDown(); else started.countDown();
                try { return blockedWork(); }
                finally { active.decrementAndGet(); finished.countDown(); }
            });
        }
        assertTrue(started.await(3, TimeUnit.SECONDS));
        assertFalse(extraStarted.await(200, TimeUnit.MILLISECONDS));
        release.countDown();
        assertTrue(finished.await(3, TimeUnit.SECONDS));
        assertEquals(workerLimit, peak.get());
    }

    @Test
    public void shutsDownWorkerPool() throws Exception {
        String id = manager.submitTask("TEST", this::blockedWork);
        manager.shutdown();
        assertThrows(RejectedExecutionException.class,
                () -> manager.submitTask("TEST", () -> "new work"));
        release.countDown();
        awaitStatus(id, "COMPLETE"); // Shutdown allows accepted work to finish.
        // Inspect the private executor because TaskManager has no awaitTermination API.
        var field = TaskManager.class.getDeclaredField("executor");
        field.setAccessible(true);
        ExecutorService pool = (ExecutorService) field.get(manager);
        assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
        assertTrue(pool.isTerminated());
    }
}

//Sources: geeksforgeeks, w3schools
