package no.kristiania.httpclient;

import no.kristiania.database.Worker;
import no.kristiania.database.WorkerDao;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:testdatabase;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
    }

    @Test
    void shouldReturnSuccessfulStatusCode() throws IOException {
        new HttpServer(10001, dataSource);
        HttpClient client = new HttpClient("localhost", 10001, "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccessfulStatusCode() throws IOException {
        new HttpServer(10002, dataSource);
        HttpClient client = new HttpClient("localhost", 10002, "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnContentLength() throws IOException {
        new HttpServer(10003, dataSource);
        HttpClient client = new HttpClient("localhost", 10003, "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException {
        new HttpServer(10004, dataSource);
        HttpClient client = new HttpClient("localhost", 10004, "/echo?body=HelloWorld");
        assertEquals("HelloWorld", client.getResponseBody());
    }

    @Test
    void shouldReturnFileFromDisk() throws IOException {
        HttpServer server = new HttpServer(10005, dataSource);
        File contentRoot = new File("target/test-classes");

        String fileContent = "Hello World " + new Date();
        Files.writeString(new File(contentRoot,"test.txt").toPath(), fileContent);

        HttpClient client = new HttpClient("localhost", 10005, "/test.txt");
        assertEquals(fileContent, client.getResponseBody());
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException {
        HttpServer server = new HttpServer(10006, dataSource);
        File contentRoot = new File("target/test-classes");

        Files.writeString(new File(contentRoot, "index.html").toPath(), "<h2>Hello World</h2>");

        HttpClient client = new HttpClient("localhost", 10006, "/index.html");
        assertEquals("text/html", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldReturn404IfFileNotFound() throws IOException {
        HttpServer server = new HttpServer(10007, dataSource);
        File contentRoot = new File("target/test-classes");

        HttpClient client = new HttpClient("localhost", 10007, "/notFound.txt");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldPostNewWorker() throws IOException, SQLException {
        HttpServer server = new HttpServer(10008, dataSource);
        String requestBody = "first_name=wali&email_address=wgbjork@gmail.com";
        HttpClient client = new HttpClient("localhost", 10008, "/api/newWorker", "POST", requestBody);
        assertEquals(200, client.getStatusCode());
        assertThat(server.getWorkers())
                .filteredOn(worker -> worker.getFirstName().equals("wali"))
                .isNotEmpty()
                .satisfies(w -> assertThat(w.get(0).getEmailAddress()).isEqualTo("wgbjork@gmail.com"));
    }

    @Test
    void shouldReturnExistingMembers() throws IOException, SQLException {
        HttpServer server = new HttpServer(10009, dataSource);
        WorkerDao workerDao = new WorkerDao(dataSource);
        Worker worker = new Worker();
        worker.setFirstName("wali");
        worker.setLastName("gustav");
        worker.setEmailAddress("wgbjork@gmail.com");
        workerDao.insert(worker);
        HttpClient client = new HttpClient("localhost", 10009, "/api/worker");
        assertThat(client.getResponseBody()).contains("<li>wali gustav wgbjork@gmail.com</li>");
    }

    @Test
    void shouldPostNewTask() throws IOException, SQLException {
        HttpServer server = new HttpServer(10010, dataSource);
        String requestBody = "taskName=Desk cleaning&color=black";
        HttpClient postClient = new HttpClient("localhost", 10010, "/api/newTask", "POST", requestBody);
        assertEquals(200, postClient.getStatusCode());

        HttpClient getClient = new HttpClient("localhost", 10010, "/api/tasks");
        assertThat(getClient.getResponseBody()).contains("<li>Desk cleaning</li>");
    }
}