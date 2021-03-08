/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.util.Json
import com.google.android.gms.wallet.*
import kotlinx.android.synthetic.main.activity_checkout.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToLong


/**
 * Implementación de pago para la aplicación
 */
class CheckoutActivity : Activity() {

    /**
     * Un cliente para interactuar con la API
     *
     * @see [PaymentsClient](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient)
     */
    private lateinit var paymentsClient: PaymentsClient
    private val shippingCost = (90 * 1000000).toLong()

    private lateinit var garmentList: JSONArray
    private lateinit var selectedGarment: JSONObject

    /**
     * Entero constante elegido arbitrariamente que define para realizar un seguimiento de una
     * solicitud de actividad de datos de pago.
     *
     * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
     */
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    /**
     * Inicialice la API
     *
     * @see Activity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Configure la información simulada para nuestro artículo en la interfaz de usuario.
        selectedGarment = fetchRandomGarment()
        displayGarment(selectedGarment)

        // Inicialice un cliente de API de Google Pay para un entorno adecuado para pruebas.
        // Se recomienda crear el objeto PaymentsClient dentro del método onCreate.
        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        possiblyShowGooglePayButton()

        googlePayButton.setOnClickListener { requestPayment() }
    }

    /**
     * Determine la capacidad del espectador para pagar con un método de pago compatible
     * con su aplicación y muestre un Botón de pago
     *
     * @see [](https:// ------
    ) */
    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // La llamada a Ready To Pay es asincrónica y devuelve una tarea. Necesitamos proporcionar
        // OnCompleteListener se activará cuando se conozca el resultado de la llamada.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    /**
    * Si isReadyToPay devolvió `true`, muestre el botón y oculte el texto de" comprobación ".
     * De lo contrario, notificar al usuario que Google Pay no está disponible.
     * Ajústelo para que se adapte a su flujo de usuarios. No es necesario que le informe
     * explícitamente al usuario si isReadyToPay devuelve "falso".
    *
    * @param disponible respuesta de API isReadyToPay.
    */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                    this,
                    "Lamentablemente, Pay no está disponible en este dispositivo.",
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private fun requestPayment() {

        // Desactiva el botón para evitar varios clics.
        googlePayButton.isClickable = false

        // El precio proporcionado a la API debe incluir impuestos y envío.
        // Este precio no se muestra a la usuaria.
        val garmentPriceMicros = (selectedGarment.getDouble("price") * 1000000).roundToLong()
        val price = (garmentPriceMicros + shippingCost).microsToString()

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price)
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "No se puede recuperar la solicitud de datos de pago")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Dado que loadPaymentData puede mostrar la interfaz de usuario pidiendo al
        // usuario que seleccione un método de pago, utilizamos
        // AutoResolveHelper para esperar a que el usuario interactúe con él. Una vez completado,
        // onActivityResult se llamará con el resultado
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }

    /**
     * Maneja una actividad resuelta desde la hoja de pago
     *
     * @param requestCode Solicite el código suministrado originalmente
     *                    a AutoResolveHelper en requestPayment().
     * @param resultCode  Código de resultado devuelto por API.
     * @param data Intent de la API que contiene datos de pago o error.
     * @see [Obtengs un result
     * de una Activity](https://-------)
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // valor pasado en AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    Activity.RESULT_CANCELED -> {
                        // No hay nada que hacer aquí normalmente: el usuario simplemente canceló sin seleccionar un
                        // método de pago.
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                //Vuelve a habilitar el botón de pago.
                googlePayButton.isClickable = true
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            /**
             * El objeto de respuesta PaymentData contiene la información de pago,
             * así como cualquier información solicitada, como dirección de facturación y envío.
             *
             * @param paymentData Objeto de respuesta devuelto por después
             * de que un pagador aprueba el pago.
             * @see [Payment
             * Data](https://----)
             */
            // El token será nulo si PaymentDataRequest no se construyó usando fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")

            // Si la puerta de enlace se establece en "ejemplo", no se devuelve
            // información de pago; en su lugar, el
            // El token solo constará de "examplePaymentMethodToken".
            if (paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("token") == "examplePaymentMethodToken") {

                AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }

            val billingName = paymentMethodData.getJSONObject("info")
                    .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show()

            //Inicio de seccion de token string.
            Log.d("GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token"))

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
        }

    }

    /**
     * En esta etapa, el usuario ya ha visto una ventana emergente que le
     * informa que ocurrió un error. Normalmente, solo se requiere registro.
     *
     * @param statusCode mantendrá el valor de cualquier constante de CommonStatusCode o uno de los
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://-------------)
     */
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    private fun fetchRandomGarment() : JSONObject {
        if (!::garmentList.isInitialized) {
            garmentList = Json.readFromResources(this, R.raw.tshirts)
        }

        val randomIndex:Int = Math.round(Math.random() * (garmentList.length() - 1)).toInt()
        return garmentList.getJSONObject(randomIndex)
    }

    private fun displayGarment(garment:JSONObject) {
        detailTitle.setText(garment.getString("title"))
        detailPrice.setText("\$${garment.getString("price")}")

        val escapedHtmlText:String = Html.fromHtml(garment.getString("description")).toString()
        detailDescription.setText(Html.fromHtml(escapedHtmlText))

        val imageUri = "@drawable/${garment.getString("image")}"
        val imageResource = resources.getIdentifier(imageUri, null, packageName)
        detailImage.setImageResource(imageResource)
    }
}
