package sc.demo.services.department.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import sc.demo.services.department.model.Department;

public interface DepartmentRepository extends CrudRepository<Department, String> {

	List<Department> findByOrganizationId(String organizationId);
	
}
