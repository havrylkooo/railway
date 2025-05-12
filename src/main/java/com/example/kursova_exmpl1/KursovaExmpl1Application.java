package com.example.kursova_exmpl1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Controller
public class KursovaExmpl1Application {

    private static final String URL = "jdbc:mysql://kursova.mysql.database.azure.com:3306/kursova?useSSL=true&requireSSL=true&verifyServerCertificate=false";
    private static final String USERNAME = "andriy";
    private static final String PASSWORD = "Havrylko11";

    public static void main(String[] args) {
        SpringApplication.run(KursovaExmpl1Application.class, args);
    }

    // Головна сторінка
    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html
    }

    // Сторінка з формою
    @GetMapping("/form")
    public String showForm() {
        return "form"; // templates/form.html
    }

    // Додавання нового потяга
    @PostMapping("/addTrain")
    public String addTrain(@RequestParam String direction,
                           @RequestParam String wagonType,
                           @RequestParam int wagonCount,
                           @RequestParam String wagonNumbers,
                           @RequestParam String time,
                           @RequestParam String executor,
                           Model model) {

        List<Integer> wagonNumberList = new ArrayList<>();
        String[] wagonNumberArray = wagonNumbers.split(" ");
        for (String number : wagonNumberArray) {
            wagonNumberList.add(Integer.parseInt(number));
        }

        Train train = new Train(direction, wagonType, wagonNumberList, time, executor);

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO train (direction, wagon_type, wagons, time, executor) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, train.getDirection());
                statement.setString(2, train.getWagonType());
                String wagonsJson = new ObjectMapper().writeValueAsString(train.getWagonNumbers());
                statement.setString(3, wagonsJson);
                statement.setString(4, train.getTime());
                statement.setString(5, train.getExecutor());
                statement.executeUpdate();
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }

        model.addAttribute("train", train);
        model.addAttribute("message", "Дані успішно записано");
        return "report"; // templates/report.html
    }

    // Зчитування усіх потягів
    @GetMapping("/readTrains")
    public String readTrains(Model model) {
        List<Train> trains = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM train";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String direction = resultSet.getString("direction");
                        String wagonType = resultSet.getString("wagon_type");
                        String wagonNumbers = resultSet.getString("wagons");
                        String time = resultSet.getString("time");
                        String executor = resultSet.getString("executor");

                        List<Integer> wagonNumberList = objectMapper.readValue(wagonNumbers, List.class);
                        trains.add(new Train(direction, wagonType, wagonNumberList, time, executor));
                    }
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }

        model.addAttribute("trains", trains);
        return "report"; // templates/report.html
    }

    // Новий метод для сторінки "Наші працівники"
    @GetMapping("/employees")
    public String showEmployees(Model model) {
        model.addAttribute("employees", getEmployees());
        return "employees"; // templates/employees.html
    }

    // Метод для отримання працівників
    private List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Михайлинин Максим", "Менеджер з напрямків", "Максим відповідає за координацію маршрутів та організацію графіків руху поїздів."));
        employees.add(new Employee("Лілія Луцик", "Спеціаліст з планування маршрутів", "Лілія займається розробкою нових та оптимізацією існуючих маршрутів."));
        employees.add(new Employee("Гаврилко Андрій", "Технічний директор", "Андрій керує всіма технічними аспектами залізничного транспорту."));
        employees.add(new Employee("Вікторія Романів", "Диспетчер", "Вікторія відповідає за управління рухом поїздів."));
        employees.add(new Employee("Комарин Сергій", "Інженер з безпеки", "Сергій займається забезпеченням безпеки залізничного транспорту."));
        return employees;
    }

    // Клас працівника
    public static class Employee {
        private String name;
        private String position;
        private String description;

        public Employee(String name, String position, String description) {
            this.name = name;
            this.position = position;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getPosition() {
            return position;
        }

        public String getDescription() {
            return description;
        }
    }

    // Клас поїзда
    public static class Train {
        private String direction;
        private String wagonType;
        private List<Integer> wagonNumbers;
        private String time;
        private String executor;

        public Train(String direction, String wagonType, List<Integer> wagonNumbers, String time, String executor) {
            this.direction = direction;
            this.wagonType = wagonType;
            this.wagonNumbers = wagonNumbers;
            this.time = time;
            this.executor = executor;
        }

        public String getDirection() {
            return direction;
        }

        public String getWagonType() {
            return wagonType;
        }

        public List<Integer> getWagonNumbers() {
            return wagonNumbers;
        }

        public String getTime() {
            return time;
        }

        public String getExecutor() {
            return executor;
        }
    }
}
