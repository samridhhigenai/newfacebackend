package com.faceattendance.repository;

import com.faceattendance.model.Attendance;
import com.faceattendance.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    // Tenant-specific queries
    List<Attendance> findByTenantIdOrderByAttendanceDateDesc(String tenantId);

    List<Attendance> findByTenantIdAndAttendanceDate(String tenantId, LocalDate date);

    @Query("{ 'tenantId': ?0, 'employee': ?1, 'attendanceDate': ?2 }")
    List<Attendance> findByTenantIdAndEmployeeAndAttendanceDateOrderByCreatedAtDesc(String tenantId, Employee employee, LocalDate date);

    @Query("{ 'tenantId': ?0, 'employee.id': ?1, 'attendanceDate': { $gte: ?2, $lte: ?3 } }")
    List<Attendance> findByTenantIdAndEmployeeIdAndDateRange(String tenantId, String employeeId,
                                                            LocalDate startDate,
                                                            LocalDate endDate);

    @Query("{ 'tenantId': ?0, 'attendanceDate': ?1 }")
    List<Attendance> findTodayAttendanceByTenantId(String tenantId, LocalDate date);

    @Query(value = "{ 'tenantId': ?0, 'employee.id': ?1, 'attendanceDate': { $gte: ?2, $lte: ?3 } }", count = true)
    Long countAttendanceByTenantIdAndEmployeeAndDateRange(String tenantId, String employeeId,
                                                         LocalDate startDate,
                                                         LocalDate endDate);

    boolean existsByTenantIdAndEmployeeAndAttendanceDate(String tenantId, Employee employee, LocalDate date);

    @Query("{ 'tenantId': ?0, 'attendanceDate': { $gte: ?1, $lte: ?2 } }")
    List<Attendance> findByTenantIdAndAttendanceDateBetween(String tenantId,
                                                           LocalDate startDate,
                                                           LocalDate endDate);

    @Query("{ 'tenantId': ?0 }")
    List<Attendance> findAllByTenantIdOrderByAttendanceDateDesc(String tenantId);

    // Legacy methods (deprecated - use tenant-specific versions)
    @Deprecated
    List<Attendance> findByEmployeeOrderByAttendanceDateDesc(Employee employee);

    @Deprecated
    List<Attendance> findByAttendanceDate(LocalDate date);

    @Deprecated
    @Query("{ 'employee': ?0, 'attendanceDate': ?1 }")
    List<Attendance> findByEmployeeAndAttendanceDateOrderByCreatedAtDesc(Employee employee, LocalDate date);

    @Deprecated
    Optional<Attendance> findByEmployeeAndAttendanceDate(Employee employee, LocalDate date);

    @Deprecated
    @Query("{ 'employee.id': ?0, 'attendanceDate': { $gte: ?1, $lte: ?2 } }")
    List<Attendance> findByEmployeeIdAndDateRange(String employeeId,
                                                  LocalDate startDate,
                                                  LocalDate endDate);

    @Deprecated
    @Query("{ 'attendanceDate': ?0 }")
    List<Attendance> findTodayAttendance(LocalDate date);

    @Deprecated
    @Query(value = "{ 'employee.id': ?0, 'attendanceDate': { $gte: ?1, $lte: ?2 } }", count = true)
    Long countAttendanceByEmployeeAndDateRange(String employeeId,
                                              LocalDate startDate,
                                              LocalDate endDate);

    @Deprecated
    boolean existsByEmployeeAndAttendanceDate(Employee employee, LocalDate date);

    @Deprecated
    @Query("{ 'employee.id': ?0, 'attendanceDate': { $gte: ?1, $lte: ?2 } }")
    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(String employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    @Deprecated
    @Query("{ 'attendanceDate': { $gte: ?0, $lte: ?1 } }")
    List<Attendance> findByAttendanceDateBetween(LocalDate startDate,
                                                 LocalDate endDate);

    @Deprecated
    @Query("{}")
    List<Attendance> findAllByOrderByAttendanceDateDesc();
}
