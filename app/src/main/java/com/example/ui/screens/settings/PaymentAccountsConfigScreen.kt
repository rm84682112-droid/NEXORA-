package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.PlatformPaymentAccount
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.EMolaOrange
import com.example.ui.theme.MKeshYellow
import com.example.ui.theme.MPesaRed
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PaymentAccountsConfigScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by NexoraRepository.platformPaymentAccounts.collectAsState()
    val whatsappSupport by NexoraRepository.whatsappSupportNumber.collectAsState()

    val mpesaAcc = accounts.find { it.method == PaymentMethod.MPESA }
    val mkeshAcc = accounts.find { it.method == PaymentMethod.MKESH }
    val emolaAcc = accounts.find { it.method == PaymentMethod.EMOLA }

    var mpesaNum by remember(mpesaAcc) { mutableStateOf(mpesaAcc?.accountNumber ?: "84 682 1128") }
    var mpesaHolder by remember(mpesaAcc) { mutableStateOf(mpesaAcc?.accountHolder ?: "Robson Manuel") }

    var mkeshNum by remember(mkeshAcc) { mutableStateOf(mkeshAcc?.accountNumber ?: "83 568 1467") }
    var mkeshHolder by remember(mkeshAcc) { mutableStateOf(mkeshAcc?.accountHolder ?: "Robson Rebo") }

    var emolaNum by remember(emolaAcc) { mutableStateOf(emolaAcc?.accountNumber ?: "86 752 9000") }
    var emolaHolder by remember(emolaAcc) { mutableStateOf(emolaAcc?.accountHolder ?: "NEXORA TECNOLOGIAS MOÇAMBIQUE") }

    var whatsappInput by remember(whatsappSupport) { mutableStateOf(whatsappSupport) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("accounts_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = NexoraCyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Contas de Pagamento & Suporte",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Painel de Administração (M-Pesa, mKesh, e-Mola, WhatsApp)",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // M-PESA CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, MPesaRed.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MPesaRed, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conta M-Pesa (84 / 85)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = mpesaNum,
                    onValueChange = { mpesaNum = it },
                    label = { Text("Número de Telefone (ex: 84 682 1128)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_mpesa_num"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mpesaHolder,
                    onValueChange = { mpesaHolder = it },
                    label = { Text("Titular da Conta M-Pesa (ex: Robson Manuel)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_mpesa_holder"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // MKESH CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, MKeshYellow.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MKeshYellow, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conta mKesh (82 / 83)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = mkeshNum,
                    onValueChange = { mkeshNum = it },
                    label = { Text("Número de Telefone (ex: 83 568 1467)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_mkesh_num"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mkeshHolder,
                    onValueChange = { mkeshHolder = it },
                    label = { Text("Titular da Conta mKesh (ex: Robson Rebo)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_mkesh_holder"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // E-MOLA CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, EMolaOrange.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(EMolaOrange, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conta e-Mola (86 / 87)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = emolaNum,
                    onValueChange = { emolaNum = it },
                    label = { Text("Número de Telefone (ex: 86 752 9000)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_emola_num"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = emolaHolder,
                    onValueChange = { emolaHolder = it },
                    label = { Text("Titular da Conta e-Mola (ex: NEXORA TECNOLOGIAS)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_emola_holder"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // WHATSAPP SUPPORT CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexoraGreenSuccess.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = NexoraGreenSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Suporte WhatsApp Oficial (+258)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = whatsappInput,
                    onValueChange = { whatsappInput = it },
                    label = { Text("Número de Suporte (ex: +258 83 568 1467)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_num"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Button(
            onClick = {
                val newAccounts = listOf(
                    PlatformPaymentAccount("acc-mpesa", PaymentMethod.MPESA, mpesaNum.trim(), mpesaHolder.trim()),
                    PlatformPaymentAccount("acc-mkesh", PaymentMethod.MKESH, mkeshNum.trim(), mkeshHolder.trim()),
                    PlatformPaymentAccount("acc-emola", PaymentMethod.EMOLA, emolaNum.trim(), emolaHolder.trim())
                )
                NexoraRepository.updatePlatformPaymentAccounts(newAccounts, whatsappInput.trim())
                Toast.makeText(context, "Contas e suporte WhatsApp atualizados com sucesso no Firestore!", Toast.LENGTH_SHORT).show()
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_payment_accounts_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Alterações com Registos de Auditoria", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
