package com.zaed.reservationmanager.ui.company.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.component.DatePickerFieldToModal
import com.zaed.reservationmanager.ui.util.InputValidator
import org.bouncycastle.asn1.x500.style.RFC4519Style.title

@Composable
fun AddPaymentBottomSheetContent(
    modifier: Modifier = Modifier,
    initialPayment: CompanyPayment = CompanyPayment(),
    onSavePayment: (CompanyPayment) -> Unit,
    onDismiss: () -> Unit
) {
    var payment by remember { mutableStateOf(initialPayment) }
    var paymentError by remember {
        mutableStateOf(PaymentError.NONE)
    }
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        TitledTextField(
            title = stringResource(R.string.amount),
            initialValue =if (payment.amount == 0.0) "" else payment.amount.toString(),
            onValueChanged = { newText ->
                if (newText.toDoubleOrNull() != null) {
                    payment = payment.copy(
                        amount = newText.toDouble()
                    )
                }
            },
            isOptional = true,
            isError = paymentError.messageRes == PaymentError.AMOUNT_IS_REQUIRED.messageRes,
            errorMessageRes = paymentError.messageRes,
            keyboardType = KeyboardType.Number
        )
        DatePickerFieldToModal(
            initialValue = initialPayment.createdAtEpochSeconds,
            isError = paymentError == PaymentError.DATE_IS_REQUIRED,
            errorMessageRes = paymentError.messageRes,
            onDateSelected = { newDate ->
                payment = payment.copy(
                    createdAtEpochSeconds = newDate ?: 0L
                )
            }
        )
        TitledTextField(
            title = stringResource(R.string.description),
            initialValue =  payment.description,
            onValueChanged = { newText ->
                payment = payment.copy(
                    description = newText
                )
            },
            isOptional = true,
            isError = paymentError == PaymentError.DESCRIPTION_IS_REQUIRED,
            errorMessageRes = paymentError.messageRes,
            keyboardType = KeyboardType.Text
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = { onDismiss() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel)
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    val error = InputValidator.validatePayment(payment)
                    if (error != null) {
                        paymentError = error
                    } else {
                        onSavePayment(payment)
                        payment = CompanyPayment()
                        paymentError = PaymentError.NONE
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_payment))
            }
        }
    }
}