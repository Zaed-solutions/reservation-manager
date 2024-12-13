package com.zaed.reservationmanager.app.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object CustomNavType {
    val CompanyType = object: NavType<Company>(
        isNullableAllowed = false,
    ){
        override fun get(bundle: Bundle, key: String): Company? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Company {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Company): String {
            return Uri.encode(Json.encodeToString(value))
        }
        override fun put(bundle: Bundle, key: String, value: Company) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }
    val EmployeeType = object: NavType<Employee>(
        isNullableAllowed = false,
    ){
        override fun get(bundle: Bundle, key: String): Employee? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Employee {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Employee): String {
            return Uri.encode(Json.encodeToString(value))
        }
        override fun put(bundle: Bundle, key: String, value: Employee) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }
    val CustomerType = object: NavType<Customer>(
        isNullableAllowed = false,
    ){
        override fun get(bundle: Bundle, key: String): Customer? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Customer {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Customer): String {
            return Uri.encode(Json.encodeToString(value))
        }
        override fun put(bundle: Bundle, key: String, value: Customer) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }
    val ReservationType = object: NavType<Reservation>(
        isNullableAllowed = false,
    ){
        override fun get(bundle: Bundle, key: String): Reservation? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Reservation {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Reservation): String {
            return Uri.encode(Json.encodeToString(value))
        }
        override fun put(bundle: Bundle, key: String, value: Reservation) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }

}