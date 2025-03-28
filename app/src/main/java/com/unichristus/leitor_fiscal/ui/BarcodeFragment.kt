package com.unichristus.leitor_fiscal.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.common.Barcode
import com.unichristus.leitor_fiscal.ScannerActivity
import com.unichristus.leitor_fiscal.databinding.FragmentBarcodeBinding

class BarcodeFragment : Fragment() {
    private var _binding: FragmentBarcodeBinding? = null
    private val binding get() = _binding!!

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcodeValues = result.data?.getStringArrayListExtra("barcodes")
            barcodeValues?.forEach { value ->
                updateUI(value)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startScanner()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonOpenScanner.setOnClickListener { checkCameraPermission() }

        binding.textViewBarcodeType.setOnLongClickListener {
            val content = binding.textViewBarcodeType.text
            if (content.isNotEmpty()) {
                copyToClipboard(content)
                Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> startScanner()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showPermissionRationale()

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanner() {
        ScannerActivity.startScanner(
            requireContext(),
            listOf(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_CODE_128
            )
        )
        scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(barcodeValue: String) {
        binding.textViewBarcodeType.text = "Código: $barcodeValue"
    }

    private fun copyToClipboard(text: CharSequence) {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Código de Barras", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun showPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Permissão de Câmera Necessária")
            .setMessage("O aplicativo precisa da permissão da câmera para escanear códigos de barras.")
            .setPositiveButton("Entendi") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}