package no.kristiania.HTTP;

import no.kristiania.DAO.*;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;


public class WorkerTaskGetController implements HttpController {
    private TaskDao taskDao;
    private WorkerDao workerDao;

    public WorkerTaskGetController(TaskDao taskDao, WorkerDao workerDao) {
        this.taskDao = taskDao;
        this.workerDao = workerDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        String body = "";
        StringBuilder stringBuilder = new StringBuilder();

        List<Task> tasks = taskDao.list();
        for (Task task : tasks) {
            String taskId = "" + task.getId();
            String sql = "select w.* from worker_task wt " +
                    "join worker w on wt.worker_id = w.id where wt.task_id = " + taskId;
            String workerName = "";

            String status = task.getStatusColorCode();
            if (status == null) {
                status = "No status";
            }

            List<Worker> workers = workerDao.list(sql);
            System.out.println("size: " + workers.size());
            for (Worker worker : workers) {
                workerName = workerName + worker.getFirstName() + ", ";
            }
            stringBuilder.append("<hr> <article>\n" +
                    "<h1> Task: " + task.getName() + "</h1>\n" +
                    "<p><strong> Status:</strong> " + status + "</p>\n" +
                    "<p><strong> Workers:</strong> " + workerName + "</p>\n" +
                    "\n" +
                    "    </article>");
        }
        body += stringBuilder;
        body += "<hr>";

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;


        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());
        return request;
    }
}
