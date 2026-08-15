package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

@Composable
fun QrCodeView(
    payload: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val matrix = remember(payload) {
        generateQrMatrix(payload, 25)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 28.dp)) {
            val moduleCount = matrix.size
            val moduleSize = this.size.width / moduleCount

            for (row in 0 until moduleCount) {
                for (col in 0 until moduleCount) {
                    if (matrix[row][col]) {
                        drawRoundRect(
                            color = Color(0xFF0F0C20),
                            topLeft = Offset(col * moduleSize, row * moduleSize),
                            size = Size(moduleSize, moduleSize),
                            cornerRadius = CornerRadius(moduleSize * 0.25f, moduleSize * 0.25f)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(data: String, dimension: Int): Array<BooleanArray> {
    val matrix = Array(dimension) { BooleanArray(dimension) }

    // Standard QR finder patterns (top-left, top-right, bottom-left)
    drawFinderPattern(matrix, 0, 0)
    drawFinderPattern(matrix, dimension - 7, 0)
    drawFinderPattern(matrix, 0, dimension - 7)

    // Timing patterns
    for (i in 7 until dimension - 7) {
        matrix[6][i] = (i % 2 == 0)
        matrix[i][6] = (i % 2 == 0)
    }

    // Deterministic pseudo data from hash
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(data.toByteArray())
    var bitIndex = 0

    for (row in 0 until dimension) {
        for (col in 0 until dimension) {
            if (isReservedFinder(row, col, dimension) || row == 6 || col == 6) continue

            val byteVal = hash[bitIndex % hash.size].toInt()
            val bit = (byteVal shr ((bitIndex % 8))) and 1
            matrix[row][col] = (bit == 1)
            bitIndex++
        }
    }

    return matrix
}

private fun drawFinderPattern(matrix: Array<BooleanArray>, startRow: Int, startCol: Int) {
    for (r in 0 until 7) {
        for (c in 0 until 7) {
            val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
            val isCenter = (r in 2..4 && c in 2..4)
            matrix[startRow + r][startCol + c] = isBorder || isCenter
        }
    }
}

private fun isReservedFinder(r: Int, c: Int, dim: Int): Boolean {
    if (r < 8 && c < 8) return true // Top-left
    if (r >= dim - 8 && c < 8) return true // Bottom-left
    if (r < 8 && c >= dim - 8) return true // Top-right
    return false
}
