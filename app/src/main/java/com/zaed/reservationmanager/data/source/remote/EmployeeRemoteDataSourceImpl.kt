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

    override fun createEmployee(employee: Employee): Flow<Result<Pair<Boolean, String>>> =
        callbackFlow {
            try {
                val phoneNumber1Filter = Filter.or(
                    Filter.inArray(
                        "phoneNumber1",
                        listOf(employee.phoneNumber1)
                    ),
                    Filter.inArray(
                        "phoneNumber2",
                        listOf(employee.phoneNumber1)
                    )
                )
                val phoneNumber2Filter = Filter.or(
                    Filter.inArray(
                        "phoneNumber1",
                        listOf(employee.phoneNumber2)
                    ),
                    Filter.inArray(
                        "phoneNumber2",
                        listOf(employee.phoneNumber2)
                    )
                )
                val employeeNameFilter = Filter.equalTo("name", employee.name)

                val phoneNumber1Result = firestore
                    .collection(EMPLOYEE_COLLECTION)
                    .where(
                        phoneNumber1Filter
                    ).get().await().documents.isEmpty()
                val phoneNumber2Result = if (employee.phoneNumber2.isNotBlank()) {
                    firestore
                        .collection(EMPLOYEE_COLLECTION)
                        .where(
                            phoneNumber2Filter
                        ).get().await().documents.isEmpty()
                } else true
                val employeeNameResult = firestore
                    .collection(EMPLOYEE_COLLECTION)
                    .where(
                        employeeNameFilter
                    ).get().await().documents.isEmpty()

                if (phoneNumber1Result && phoneNumber2Result && employeeNameResult) {
                    val document = firestore.collection(EMPLOYEE_COLLECTION).document()
                    document.set(employee.copy(id = document.id)).addOnSuccessListener {
                        trySend(Result.success(true to document.id))
                    }.addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                } else if (!employeeNameResult) {
                    trySend(Result.success(false to "name"))
                } else if (!phoneNumber1Result) {
                    trySend(Result.success(false to "phoneNumber1"))
                } else {
                    trySend(Result.success(false to "phoneNumber2"))
                }

            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose { }
        }

    override fun updateEmployee(employee: Employee): Flow<Result<Pair<Boolean, String>>> =
        callbackFlow {
            try {
                val reservations = firestore
                    .collection(RESERVATION_COLLECTION)
                    .where(
                        Filter.or(
                            Filter.equalTo("driverId", employee.id),
                            Filter.equalTo("tourismEmployeeId", employee.id)
                        )
                    ).get().await()
                val phoneNumber1Filter = Filter.or(
                    Filter.inArray(
                        "phoneNumber1",
                        listOf(employee.phoneNumber1)
                    ),
                    Filter.inArray(
                        "phoneNumber2",
                        listOf(employee.phoneNumber1)
                    )
                )
                val phoneNumber2Filter = Filter.or(
                    Filter.inArray(
                        "phoneNumber1",
                        listOf(employee.phoneNumber2)
                    ),
                    Filter.inArray(
                        "phoneNumber2",
                        listOf(employee.phoneNumber2)
                    )
                )
                val employeeNameFilter = Filter.equalTo("name", employee.name)

                val phoneNumber1Result = firestore
                    .collection(EMPLOYEE_COLLECTION)
                    .where(
                        phoneNumber1Filter
                    ).get().await().documents.isEmpty()
                val phoneNumber2Result = if (employee.phoneNumber2.isNotBlank()) {
                    firestore
                        .collection(EMPLOYEE_COLLECTION)
                        .where(
                            phoneNumber2Filter
                        ).get().await().documents.isEmpty()
                } else true
                val employeeNameResult = firestore
                    .collection(EMPLOYEE_COLLECTION)
                    .where(
                        employeeNameFilter
                    ).get().await().documents.isEmpty()

                if (phoneNumber1Result && phoneNumber2Result && employeeNameResult) {
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
                        trySend(Result.success(true to employee.id))
                    }.addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                } else if (!employeeNameResult) {
                    trySend(Result.success(false to "name"))
                } else if (!phoneNumber1Result) {
                    trySend(Result.success(false to "phoneNumber1"))
                } else {
                    trySend(Result.success(false to "phoneNumber2"))
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