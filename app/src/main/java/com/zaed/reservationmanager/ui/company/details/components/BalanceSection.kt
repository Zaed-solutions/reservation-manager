package com.zaed.reservationmanager.ui.company.details.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.formatMoney
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceSection(
    modifier: Modifier = Modifier,
    companyType: CompanyType = CompanyType.TOURISM,
    balance: CompanyBalance = CompanyBalance(),
) {
    val totalBalance = remember(balance){
        when(companyType) {
            CompanyType.TOURISM -> balance.totalSelling - balance.totalCollected
            CompanyType.TRAVEL -> balance.totalBuying - balance.totalCollected
        }
    }
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.total_balance),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(totalBalance)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        /*
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseCardSection(
                    title = stringResource(R.string.buying),
                    amount = balance.totalBuying
                )
                FadedVerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                IncomeExpenseCardSection(
                    title = stringResource(R.string.selling),
                    amount = balance.totalSelling
                )
                FadedVerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                IncomeExpenseCardSection(
                    title = stringResource(R.string.collected),
                    amount = balance.totalCollected
                )
            }
        }
         */
    }
}

@Composable
fun IncomeExpenseCardSection(
    modifier: Modifier = Modifier,
    title: String,
    amount: Int
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
        Text(
            text = amount.formatMoney(),
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            lineHeight = 33.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }

}

@Composable
fun FadedVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    height: Dp = 57.dp,
    color: Color = MaterialTheme.colorScheme.onSecondary
) {
    Canvas(
        modifier = modifier
            .width(thickness)
            .height(height)
            .alpha(0.3f)
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0f),
                    color,
                    color.copy(alpha = 0f)
                ),
                startY = 0f,
                endY = size.height
            )
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun Preview() {
    ReservationManagerTheme {
        BalanceSection(
            modifier = Modifier.padding(16.dp),
            companyType = CompanyType.TOURISM,
            balance = CompanyBalance(
                totalBuying = 3500.0,
                totalSelling = 2500.0,
                totalCollected = 2000.0
            )
        )
    }
}