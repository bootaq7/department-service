package sc.demo.services.department.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.web.bind.annotation.*;

import sc.demo.services.department.client.EmployeeClient;
import sc.demo.services.department.model.Department;
import sc.demo.services.department.model.Employee;
import sc.demo.services.department.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

@RestController
public class DepartmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentController.class);

    DepartmentRepository repository;
    EmployeeClient employeeClient;
    Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    public DepartmentController(DepartmentRepository repository, EmployeeClient employeeClient,
                                Resilience4JCircuitBreakerFactory circuitBreakerFactory) {
        this.repository = repository;
        this.employeeClient = employeeClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping("/feign")
    public List<Employee> listRest() {
        return employeeClient.findByDepartment("1");
    }

    @PostMapping("/")
    public Department add(@RequestBody Department department) {
        LOGGER.info("Department add: {}", department);
        return repository.save(department);
    }

    @GetMapping("/{id}")
    public Department findById(@PathVariable("id") String id) {
        LOGGER.info("Department find: id={}", id);
        return repository.findById(id).get();
    }

    @GetMapping("/{id}/with-employees")
    public Department findByIdWithEmployees(@PathVariable("id") String id) {
        LOGGER.info("Department findByIdWithEmployees: id={}", id);
        Optional<Department> optDepartment = repository.findById(id);
        if (optDepartment.isPresent()) {
            Department department = optDepartment.get();
            department.setEmployees(employeeClient.findByDepartment(department.getId()));
            return department;
        }
        return null;
    }

    @GetMapping("/{id}/with-employees-and-delay")
    public Department findByIdWithEmployeesAndDelay(@PathVariable("id") String id) {
        LOGGER.info("Department findByIdWithEmployees: id={}", id);
        Optional<Department> optDepartment = repository.findById(id);
        if (optDepartment.isPresent()) {
            Department department = optDepartment.get();
            Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("delayed-circuit");
            List<Employee> employees = circuitBreaker.run(() ->
                    employeeClient.findByDepartmentWithDelay(department.getId()));
            department.setEmployees(employees);
            return department;
        }
        return null;
    }

    @GetMapping("/")
    public Iterable<Department> findAll() {
        LOGGER.info("Department find");
        return repository.findAll();
    }

    @GetMapping("/organization/{organizationId}")
    public List<Department> findByOrganization(@PathVariable("organizationId") String organizationId) {
        LOGGER.info("Department find: organizationId={}", organizationId);
        return repository.findByOrganizationId(organizationId);
    }

    @GetMapping("/organization/{organizationId}/with-employees")
    public List<Department> findByOrganizationWithEmployees(@PathVariable("organizationId") String organizationId) {
        LOGGER.info("Department find: organizationId={}", organizationId);
        List<Department> departments = repository.findByOrganizationId(organizationId);
        departments.forEach(d -> d.setEmployees(employeeClient.findByDepartment(d.getId())));
        return departments;
    }

}
