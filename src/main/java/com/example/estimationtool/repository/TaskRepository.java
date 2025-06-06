package com.example.estimationtool.repository;

import com.example.estimationtool.model.enums.Status;
import com.example.estimationtool.repository.interfaces.ITaskRepository;
import com.example.estimationtool.model.Task;
import com.example.estimationtool.toolbox.rowMappers.TaskRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TaskRepository implements ITaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //------------------------------------ Create() ------------------------------------

    @Override
    public Task create(Task task) {

        String sql = """
                INSERT INTO task (subProjectID, name, description, deadline, estimatedTime, timeSpent, status) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Bruger PreparedStatement sammen med vores GeneratedKeyHolder til at kunne autogenerere et nyt id
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, task.getSubProjectId());
            ps.setString(2, task.getName());
            ps.setString(3, task.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(task.getDeadline())); // Konverterer LocalDate til java.sql.Date
            ps.setInt(5, task.getEstimatedTime());
            ps.setInt(6, task.getTimeSpent());
            ps.setString(7, task.getStatus().name());
            return ps;
        }, keyHolder);

        int generatedId = keyHolder.getKey().intValue();
        task.setTaskId(generatedId);  // Sæt det genererede ID i objektet

        return task;
    }


    //------------------------------------ Read() ------------------------------------
    @Override
    public List<Task> readAll() {

        String sql = "SELECT id, subProjectID, name, description, deadline, estimatedTime, timeSpent, status FROM task";

        return jdbcTemplate.query(sql, new TaskRowMapper());
    }

    @Override
    public Task readById(Integer taskId) {

        String sql = """
                SELECT
                id, subProjectID, name, description, deadline, estimatedTime, timeSpent, status
                FROM task
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, new TaskRowMapper(), taskId);
    }

    //------------------------------------ Update() ------------------------------------

    @Override
    public Task update(Task task) {

        String sql = """
                UPDATE task
                SET name = ?, description = ?, deadline = ?, estimatedTime = ?, timeSpent = ?, status = ?
                WHERE id = ?
                """;

        jdbcTemplate.update( // Henter disse værdier, så de kan opdateres
                sql,
                task.getName(),
                task.getDescription(),
                task.getDeadline(),
                task.getEstimatedTime(),
                task.getTimeSpent(),
                task.getStatus().name(), // Konverteres til String for at gemmes i databasen
                task.getTaskId()); // Parameter -> id til WHERE

        return task;
    }

    //------------------------------------ Delete() ------------------------------------

    @Override
    public void deleteById(Integer taskId) {

        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql, taskId);

    }


    //---------------------------------- Til DTO'er ------------------------------------

    // --- Read() tasks ud fra bruger-ID ---

    @Override
    public List<Task> readAllByUserId(Integer userId) {

        // Bruger JOIN til at hente alle tasks for ét brugerID
        String sql = """
                SELECT
                    task.subProjectID,
                    task.id,
                    task.estimatedTime,
                    task.timeSpent,
                    task.name,
                    task.description,
                    task.deadline,
                    task.status
                FROM task
                JOIN users_task ON task.id = users_task.taskID
                WHERE users_task.userID = ?
                """;

        return jdbcTemplate.query(sql, new TaskRowMapper(), userId);
    }

    // --- Read() tasks ud fra subprojekt-ID ---

    @Override
    public List<Task> readAllBySubProjectId(Integer subProjectId) {

        String sql = """
                SELECT
                    id,
                    subProjectID,
                    estimatedTime,
                    timeSpent,
                    name,
                    description,
                    deadline,
                    status
                FROM task
                WHERE subProjectID = ?
                """;

        return jdbcTemplate.query(sql, new TaskRowMapper(), subProjectId);
    }


    //---------------------------------- Assign User --------------------------------

    // ----------------- Task tildeles en bruger efter oprettelse -------------------

    @Override
    public void assignUserToTask(Integer userId, Integer taskId) {

        String sql = "INSERT INTO Users_Task (userID, taskID) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, taskId);
    }


}



