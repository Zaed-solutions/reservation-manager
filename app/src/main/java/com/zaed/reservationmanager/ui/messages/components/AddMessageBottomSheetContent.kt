package com.zaed.reservationmanager.ui.messages.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Message
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.util.InputValidator

@Composable
fun AddMessageBottomSheetContent(
    modifier: Modifier = Modifier,
    initialMessage: Message,
    onSubmit: (Message) -> Unit
) {
    var message by remember {
        mutableStateOf(initialMessage)
    }
    var messageError by remember {
        mutableStateOf(MessageError.NONE)
    }
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitledTextField(
            title = stringResource(R.string.title),
            initialValue = initialMessage.title,
            onValueChanged = { title ->
                message = message.copy(title = title)
            },
            isOptional = false,
            isError = messageError == MessageError.TITLE_IS_REQUIRED,
            errorMessageRes = messageError.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledTextField(
            singleLine = false,
            title = stringResource(R.string.message),
            initialValue = initialMessage.message,
            onValueChanged = { messageText ->
                message = message.copy(message = messageText.trim())
            },
            isOptional = false,
            isError = messageError == MessageError.MESSAGE_IS_REQUIRED,
            errorMessageRes = messageError.messageRes,
            keyboardType = KeyboardType.Text
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val error = InputValidator.validateMessage(message)
                if (error != null) {
                    messageError = error
                } else {
                    onSubmit(message)
                    message = Message()
                    messageError = MessageError.NONE
                }
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.save_message))
        }
    }
}

enum class MessageError(@StringRes val messageRes: Int = 0){
    NONE,
    TITLE_IS_REQUIRED(R.string.title_is_required),
    MESSAGE_IS_REQUIRED(R.string.message_is_required),
}