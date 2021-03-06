package no.kristiania.DAO;

import no.kristiania.HTTP.WorkerOptionsController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkerDaoTest {
    private WorkerDao workerDao;
    private static Random random = new Random();
    private TaskDao taskDao;


    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:testdatabase;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        workerDao = new WorkerDao(dataSource);
        taskDao = new TaskDao(dataSource);
    }

    @Test
    void shouldListInsertedWorkers() throws SQLException {
        Worker worker1 = exampleWorker();
        Worker worker2 = exampleWorker();
        workerDao.insert(worker1);
        workerDao.insert(worker2);
        assertThat(workerDao.list())
                .extracting(Worker::getFirstName)
                .contains(worker1.getFirstName(), worker2.getFirstName());
    }

    @Test
    void shouldRetrieveAllWorkerProperties() throws SQLException {
        workerDao.insert(exampleWorker());
        workerDao.insert(exampleWorker());
        Worker worker = exampleWorker();
        workerDao.insert(worker);
        assertThat(worker).hasNoNullFieldsOrPropertiesExcept("taskId");
        assertThat(workerDao.retrieve(worker.getId()))
                .usingRecursiveComparison()
                .isEqualTo(worker);
    }

    @Test
    void shouldReturnWorkersAsOptions() throws SQLException {
        WorkerOptionsController controller = new WorkerOptionsController(workerDao);
        Worker worker = WorkerDaoTest.exampleWorker();
        workerDao.insert(worker);

        assertThat(controller.getBody())
                .contains("<option value=" + worker.getId() + ">" + worker.getFirstName() + "</option>");
    }

    public static Worker exampleWorker() {
        Worker worker = new Worker();
        worker.setFirstName(exampleFirstName());
        worker.setLastName(exampleLastName());
        worker.setEmailAddress(exampleEmailAddress());
        return worker;
    }

    /** Returns a random first name */
    private static String exampleFirstName() {
        String[] options = {"Johnny", "Bravo", "Luke", "Eiffel", "Toward"};
        return options[random.nextInt(options.length)];
    }
    /** Returns a random last name */
    private static String exampleLastName() {
        String[] options = {"Arnoldsson", "Benet", "Whiskyardszen", "Henrhenrrikssnlollson", "Armgutten"};
        return options[random.nextInt(options.length)];
    }
    /** Returns a random email-address */
    private static String exampleEmailAddress() {
        String[] options = {"menhej@egms.no", "all@ts.no", "fell@fell.no", "exploring@lol.no", "slotmachines@egms.nu"};
        return options[random.nextInt(options.length)];
    }
}