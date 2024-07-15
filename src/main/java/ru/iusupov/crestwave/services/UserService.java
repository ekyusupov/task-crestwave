package ru.iusupov.crestwave.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.iusupov.crestwave.models.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Ernest Iusupov
 */
@Service
public class UserService {
    private static final Path JSON_FILE = Path.of("src/main/resources/static/storage.json");
    private Storage storage = new Storage();

    @PostConstruct
    public void getUsersFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader reader = Files.newBufferedReader(JSON_FILE)) {
            storage = mapper.readValue(reader, Storage.class);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    public List<User> getUsers() {
        return storage.getData();
    }

    public void save(User user) {
        storage.getData().add(user);
    }

    public void saveAll() {
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedWriter writer = Files.newBufferedWriter(JSON_FILE)) {
            mapper.writeValue(writer, storage);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    private static class Storage {
        private int record;
        private String selected;
        private List<User> data;

        public int getRecord() {
            return record;
        }

        public void setRecord(int record) {
            this.record = record;
        }

        public String getSelected() {
            return selected;
        }

        public void setSelected(String selected) {
            this.selected = selected;
        }

        public List<User> getData() {
            return data;
        }

        public void setData(List<User> data) {
            this.data = data;
        }
    }

}
