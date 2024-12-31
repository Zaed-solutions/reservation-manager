package com.zaed.reservationmanager.ui.company.details.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.CompanyPayment

@Composable
fun PaymentsList(
    modifier: Modifier = Modifier,
    payments: List<CompanyPayment> = emptyList(),
    onEditPayment: (CompanyPayment) -> Unit = {},
    onDeletePayment: (CompanyPayment) -> Unit = {}
) {
    AnimatedContent(
        modifier = modifier.fillMaxWidth(),
        targetState = payments.isEmpty()
    ) { state ->
        when{
            state -> {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 36.dp),
                    text = stringResource(R.string.no_payments_added),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = payments,
                        key = { it.id}
                    ) { payment ->
                        PaymentItem(
                            modifier = Modifier.animateItem(),
                            payment = payment,
                            onEditPayment = { onEditPayment(payment) },
                            onDeletePayment = { onDeletePayment(payment) }
                        )
                    }
                }
            }
        }
    }
}