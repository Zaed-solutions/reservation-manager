package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Employee
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class EmployeeRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : EmployeeRemoteDataSource{
    companion object{
        private val TAG = "EmployeeRemoteDataSource"
        private val EMPLOYEE_COLLECTION = "employees"
    }

    override fun createEmployee(employee: Employee): Flow<Result<Unit>> = callbackFlow{
        try{
            firestore
                .collection(EMPLOYEE_COLLECTION)
                .where(Filter.or(
                    Filter.equalTo("name", employee.name),
                    Filter.equalTo("phoneNumber", employee.phoneNumber1)
                )).get().addOnSuccessListener { data ->
                    if(data.isEmpty){
                        val document = firestore.collection(EMPLOYEE_COLLECTION).document()
                        document.set(employee.copy(id = document.id)).addOnSuccessListener {
                            trySend(Result.success(Unit))
                        }.addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                    } else {
                        trySend(Result.failure(Exception("Employee with this name or phone number already exists")))
                    }
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun updateEmployee(employee: Employee): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(EMPLOYEE_COLLECTION).document(employee.id).set(employee).addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun deleteEmployee(employeeId: String): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(EMPLOYEE_COLLECTION).document(employeeId).delete().addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun getEmployees(): Flow<Result<List<Employee>>> = callbackFlow {
        try{
            firestore.collection(EMPLOYEE_COLLECTION).addSnapshotListener { value, error ->
                if(error != null){
                    trySend(Result.failure(error))
                } else {
                    val employees = value?.toObjects(Employee::class.java)
                    trySend(Result.success(employees ?: emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }
}