package com.faceattendance.repository;

import com.faceattendance.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    // Tenant-specific queries
    List<Employee> findByTenantId(String tenantId);

    List<Employee> findByTenantIdAndIsActiveTrue(String tenantId);

    Optional<Employee> findByTenantIdAndEmail(String tenantId, String email);

    Optional<Employee> findByTenantIdAndEmployeeId(String tenantId, String employeeId);

    @Query("{ 'tenantId': ?0, 'isActive': true, 'faceEncoding': { $ne: null } }")
    List<Employee> findActiveEmployeesWithFaceEncodingByTenantId(String tenantId);

    @Query("{ 'tenantId': ?0, 'department': ?1, 'isActive': true }")
    List<Employee> findByTenantIdAndDepartmentAndIsActiveTrue(String tenantId, String department);

    boolean existsByTenantIdAndEmail(String tenantId, String email);

    boolean existsByTenantIdAndEmployeeId(String tenantId, String employeeId);

    // External sync queries
    Employee findByExternalId(String externalId);

    List<Employee> findByTenantIdAndHasFaceImageFalse(String tenantId);

    List<Employee> findByTenantIdAndIsSyncedTrue(String tenantId);

    // Legacy methods (deprecated - use tenant-specific versions)
    @Deprecated
    Optional<Employee> findByEmail(String email);

    @Deprecated
    Optional<Employee> findByEmployeeId(String employeeId);

    @Deprecated
    List<Employee> findByIsActiveTrue();

    @Deprecated
    @Query("{ 'isActive': true, 'faceEncoding': { $ne: null } }")
    List<Employee> findActiveEmployeesWithFaceEncoding();

    @Deprecated
    @Query("{ 'department': ?0, 'isActive': true }")
    List<Employee> findByDepartmentAndIsActiveTrue(String department);

    @Deprecated
    boolean existsByEmail(String email);

    @Deprecated
    boolean existsByEmployeeId(String employeeId);
}
