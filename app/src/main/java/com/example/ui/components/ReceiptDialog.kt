package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PaymentMethod
import com.example.data.model.Sale
import com.example.ui.theme.EMolaOrange
import com.example.ui.theme.FiadoBlue
import com.example.ui.theme.MKeshYellow
import com.example.ui.theme.MPesaRed
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@Composable
fun ReceiptDialog(
    sale: Sale,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = NavySurface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Success Check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(NexoraGreenSuccess.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = NexoraGreenSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Venda Concluída!",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = sale.receiptNumber,
                                color = NexoraBlueLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Printable Receipt Canvas
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = NavyBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Store info
                        Text(
                            text = "NEXORA COMÉRCIO GERAL",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Maputo, Moçambique • [A PREENCHER: NUIT da Empresa]\nTel: +258 84 700 8899",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recibo comercial, sem valor fiscal",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Details
                        ReceiptRow(label = "Data / Hora", value = Formatters.formatDateTime(sale.timestamp))
                        ReceiptRow(label = "Operador", value = sale.operatorName)
                        if (sale.customerName != null) {
                            ReceiptRow(label = "Cliente", value = sale.customerName)
                        }
                        if (sale.paymentReference != null) {
                            ReceiptRow(label = "Referência", value = sale.paymentReference)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items list
                        Text(
                            text = "ARTIGOS",
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.quantity}x ${item.product.name}",
                                        color = TextLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${Formatters.formatMoney(item.unitPrice)} un",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = Formatters.formatMoney(item.subtotal),
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals
                        ReceiptRow(label = "Subtotal", value = Formatters.formatMoney(sale.subtotal))
                        if (sale.discount > 0.0) {
                            ReceiptRow(
                                label = "Desconto Aplicado",
                                value = "- ${Formatters.formatMoney(sale.discount)}",
                                valueColor = NexoraGreenSuccess
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL GERAL",
                                color = TextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = Formatters.formatMoney(sale.total),
                                color = NexoraBlueLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Payment specifics
                        val paymentBadgeColor = when (sale.paymentMethod) {
                            PaymentMethod.DINHEIRO -> NexoraGreenSuccess
                            PaymentMethod.MPESA -> MPesaRed
                            PaymentMethod.EMOLA -> EMolaOrange
                            PaymentMethod.MKESH -> MKeshYellow
                            PaymentMethod.FIADO -> FiadoBlue
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Método:", color = TextMuted, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .background(paymentBadgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sale.paymentMethod.displayName,
                                    color = paymentBadgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (sale.paymentMethod == PaymentMethod.DINHEIRO) {
                            ReceiptRow(label = "Valor Entregue", value = Formatters.formatMoney(sale.amountPaid))
                            ReceiptRow(label = "Troco", value = Formatters.formatMoney(sale.change), valueColor = NexoraGreenSuccess)
                        } else if (sale.paymentMethod == PaymentMethod.FIADO && sale.dueDate != null) {
                            ReceiptRow(
                                label = "Data de Pagamento",
                                value = Formatters.formatDate(sale.dueDate),
                                valueColor = FiadoBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Obrigado pela preferência!\nConserve este recibo para eventuais trocas.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: WhatsApp share, Print, Done
                Button(
                    onClick = {
                        shareReceiptViaWhatsApp(context, sale)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("share_whatsapp_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partilhar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Recibo por WhatsApp",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dismiss_receipt_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Imprimir", tint = TextLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimir")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("new_sale_done_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                    ) {
                        Text("Nova Venda", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = TextLight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun shareReceiptViaWhatsApp(context: Context, sale: Sale) {
    val itemsSummary = sale.items.joinToString("\n") {
        "• ${it.quantity}x ${it.product.name}: ${Formatters.formatMoney(it.subtotal)}"
    }

    val paymentInfo = when (sale.paymentMethod) {
        PaymentMethod.DINHEIRO -> "Dinheiro (Entregue: ${Formatters.formatMoney(sale.amountPaid)}, Troco: ${Formatters.formatMoney(sale.change)})"
        PaymentMethod.MPESA -> "M-Pesa (Ref: ${sale.paymentReference ?: "N/A"})"
        PaymentMethod.EMOLA -> "e-Mola (Ref: ${sale.paymentReference ?: "N/A"})"
        PaymentMethod.MKESH -> "mKesh (Ref: ${sale.paymentReference ?: "N/A"})"
        PaymentMethod.FIADO -> "Fiado (Vencimento: ${sale.dueDate?.let { Formatters.formatDate(it) } ?: "A combinar"})"
    }

    val message = """
        🧾 *RECIBO COMERCIAL - NEXORA*
        _(Recibo comercial, sem valor fiscal)_
        --------------------------------
        *Comprovativo:* ${sale.receiptNumber}
        *Data:* ${Formatters.formatDateTime(sale.timestamp)}
        *Operador:* ${sale.operatorName}
        ${if (sale.customerName != null) "*Cliente:* ${sale.customerName}\n" else ""}
        --------------------------------
        *Artigos:*
        $itemsSummary
        --------------------------------
        *Subtotal:* ${Formatters.formatMoney(sale.subtotal)}
        ${if (sale.discount > 0) "*Desconto:* -${Formatters.formatMoney(sale.discount)}\n" else ""}*TOTAL GERAL:* ${Formatters.formatMoney(sale.total)}
        *Pagamento:* $paymentInfo
        --------------------------------
        Obrigado pela sua preferência!
        NEXORA • Gestão Comercial Inteligente
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val phone = sale.customerPhone?.replace("[^0-9+]".toRegex(), "")
            data = if (!phone.isNullOrBlank()) {
                val cleanPhone = if (phone.startsWith("+")) phone.substring(1) else phone
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to standard share intent
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Partilhar Recibo NEXORA"))
    }
}
