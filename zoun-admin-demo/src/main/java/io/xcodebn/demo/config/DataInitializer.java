package io.xcodebn.demo.config;

import io.xcodebn.demo.entity.*;
import io.xcodebn.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Initializes sample data for the demo application.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Environment environment;

    public DataInitializer(DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           Environment environment) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");

        // Create Departments
        Department engineering = new Department("Engineering", "Software development and IT infrastructure");
        Department hr = new Department("Human Resources", "Employee management and recruitment");
        Department sales = new Department("Sales", "Product sales and customer relations");
        Department marketing = new Department("Marketing", "Brand promotion and advertising");

        departmentRepository.save(engineering);
        departmentRepository.save(hr);
        departmentRepository.save(sales);
        departmentRepository.save(marketing);

        log.info("Created {} departments", departmentRepository.count());

        // Create Employees
        Employee emp1 = new Employee(
                "John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 5, 15), 33,
                new BigDecimal("85000.00"), engineering, EmploymentType.FULL_TIME
        );

        Employee emp2 = new Employee(
                "Jane", "Smith", "jane.smith@example.com",
                LocalDate.of(1988, 8, 22), 35,
                new BigDecimal("92000.00"), engineering, EmploymentType.FULL_TIME
        );

        Employee emp3 = new Employee(
                "Mike", "Johnson", "mike.johnson@example.com",
                LocalDate.of(1995, 3, 10), 28,
                new BigDecimal("65000.00"), hr, EmploymentType.FULL_TIME
        );

        Employee emp4 = new Employee(
                "Sarah", "Williams", "sarah.williams@example.com",
                LocalDate.of(1992, 11, 5), 31,
                new BigDecimal("78000.00"), sales, EmploymentType.FULL_TIME
        );

        Employee emp5 = new Employee(
                "Tom", "Brown", "tom.brown@example.com",
                LocalDate.of(2000, 1, 20), 24,
                new BigDecimal("45000.00"), marketing, EmploymentType.PART_TIME
        );

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);
        employeeRepository.save(emp4);
        employeeRepository.save(emp5);

        log.info("Created {} employees", employeeRepository.count());

        // Create Categories
        Category electronics = new Category("Electronics", "Electronic devices and accessories");
        Category clothing = new Category("Clothing", "Apparel and fashion items");
        Category books = new Category("Books", "Physical and digital books");
        Category home = new Category("Home & Garden", "Home improvement and garden supplies");

        categoryRepository.save(electronics);
        categoryRepository.save(clothing);
        categoryRepository.save(books);
        categoryRepository.save(home);

        log.info("Created {} categories", categoryRepository.count());

        // Create Products
        Product p1 = new Product(
                "Laptop Pro 15", "High-performance laptop with 16GB RAM and 512GB SSD",
                new BigDecimal("1299.99"), 25, "LAP-PRO-15", electronics
        );

        Product p2 = new Product(
                "Wireless Mouse", "Ergonomic wireless mouse with USB receiver",
                new BigDecimal("29.99"), 150, "MOUSE-WL-01", electronics
        );

        Product p3 = new Product(
                "T-Shirt Cotton", "100% cotton t-shirt, available in multiple colors",
                new BigDecimal("19.99"), 200, "TSHIRT-COT-M", clothing
        );

        Product p4 = new Product(
                "Running Shoes", "Lightweight running shoes with cushioned sole",
                new BigDecimal("89.99"), 75, "SHOE-RUN-42", clothing
        );

        Product p5 = new Product(
                "Java Programming Guide", "Comprehensive guide to modern Java development",
                new BigDecimal("49.99"), 50, "BOOK-JAVA-001", books
        );

        Product p6 = new Product(
                "Garden Tool Set", "Complete 10-piece garden tool set with carrying case",
                new BigDecimal("79.99"), 30, "GARDEN-SET-10", home
        );

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);
        productRepository.save(p6);

        log.info("Created {} products", productRepository.count());

        String port = environment.getProperty("server.port", "8080");

        log.info("Sample data initialization complete!");
        log.info("===================================================");
        log.info("Demo application is ready!");
        log.info("Access the Zoun Admin panel at: http://localhost:{}/zoun-admin", port);
        log.info("Login credentials:");
        log.info("  Username: admin");
        log.info("  Password: admin");
        log.info("===================================================");
    }
}
