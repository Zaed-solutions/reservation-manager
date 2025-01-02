package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.EmployeeType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmployeeRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : EmployeeRemoteDataSource {
    companion object {
        private val TAG = "EmployeeRemoteDataSource"
        private val EMPLOYEE_COLLECTION = "employees"
        private val RESERVATION_COLLECTION = "reservations"

    }

    override fun createEmployee(employee: Employee): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore
                .collection(EMPLOYEE_COLLECTION)
                .where(
                    Filter.or(
                        Filter.equalTo("name", employee.name),
                        Filter.equalTo("phoneNumber1", employee.phoneNumber1)
                    )
                ).get().addOnSuccessListener { data ->
                    if (data.isEmpty) {
                        val document = firestore.collection(EMPLOYEE_COLLECTION).document()
                        document.set(employee.copy(id = document.id)).addOnSuccessListener {
                            trySend(Result.success(true))
                        }.addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                    } else {
                        trySend(Result.success(false))
                    }
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun updateEmployee(employee: Employee): Flow<Result<Boolean>> = callbackFlow {
        try {
            val reservations = firestore
                .collection(RESERVATION_COLLECTION)
                .where(
                    Filter.or(
                        Filter.equalTo("driverId", employee.id),
                        Filter.equalTo("tourismEmployeeId", employee.id)
                    )
                ).get().await()
            firestore.collection(EMPLOYEE_COLLECTION)
                .where(
                    Filter.and(
                        Filter.equalTo("phoneNumber1", employee.phoneNumber1),
                        Filter.notEqualTo("id", employee.id)
                    )
                )
                .get()
                .addOnSuccessListener { data ->
                    if (data.isEmpty) {
                        val batch = firestore.batch()
                        val employeeRef =
                            firestore.collection(EMPLOYEE_COLLECTION).document(employee.id)
                        batch.set(employeeRef, employee)
                        val updates = when (employee.position) {
                            EmployeeType.DRIVER.name -> mapOf(
                                "driver" to employee.name,
                                "driverPhoneNumber" to employee.phoneNumber1
                            )

                            else -> mapOf(
                                "tourismEmployee" to employee.name,
                                "tourismEmployeePhone" to employee.phoneNumber1
                            )
                        }
                        reservations.forEach {
                            batch.update(it.reference, updates)
                        }

                        batch.commit().addOnSuccessListener {
                            trySend(Result.success(true))
                        }.addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                    } else {
                        trySend(Result.success(false))
                    }
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun deleteEmployee(employeeId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection(EMPLOYEE_COLLECTION).document(employeeId).delete()
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getEmployees(): Flow<Result<List<Employee>>> = callbackFlow {
        try {
            firestore.collection(EMPLOYEE_COLLECTION)
                .whereNotEqualTo("position", EmployeeType.DRIVER.name)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                    } else {
                        val employees = value?.toObjects(Employee::class.java)
                        trySend(Result.success(employees ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getDrivers(): Flow<Result<List<Employee>>> = callbackFlow {
        try {
            firestore.collection(EMPLOYEE_COLLECTION)
                .whereEqualTo("position", EmployeeType.DRIVER.name)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                    } else {
                        val employees = value?.toObjects(Employee::class.java)
                        trySend(Result.success(employees ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getEmployeesByCompany(companyId: String): Flow<Result<List<Employee>>> =
        callbackFlow {
            try {
                Log.d(TAG, "getEmployeesByCompany: $companyId")
                firestore.collection(EMPLOYEE_COLLECTION)
                    .whereEqualTo("companyId", companyId)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            trySend(Result.failure(error))
                        } else {
                            val employees = value?.toObjects(Employee::class.java)
                            Log.d(TAG, "getEmployeesByCompany: $employees")
                            trySend(Result.success(employees ?: emptyList()))
                        }
                    }
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose { }
        }
}