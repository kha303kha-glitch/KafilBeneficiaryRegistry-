package com.kafil.beneficiaries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.kafil.beneficiaries.ui.BeneficiaryScreen
import com.kafil.beneficiaries.ui.BeneficiaryViewModel
import com.kafil.beneficiaries.ui.theme.KafilBeneficiariesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BeneficiaryViewModel by viewModels {
        BeneficiaryViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KafilBeneficiariesTheme {
                BeneficiaryScreen(viewModel = viewModel)
            }
        }
    }
}
