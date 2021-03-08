package com.google.android.gms.samples.wallet

import android.app.Activity
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Contiene métodos estáticos auxiliares para manejar la API de pagos.
 *
 * Muchos de los parámetros utilizados en el código son opcionales y se vendió aquí
 * simplemente para llamar a su existencia. Consulte la documentación para obtener más
 * información y siéntase libre de eliminar los que no relevante para su implementación.
 */
object PaymentsUtil {
    val MICROS = BigDecimal(1000000.0)

    /**
     * Cree un objeto de solicitud base de API de con propiedades
     * utilizadas en todas las solicitudes.
     *
     * @return Pay API objeto de solicitud base.
     * @throws JSONException
     */
    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    /**
     * Integración de la puerta de enlace: identifique su puerta de enlace y el
     * identificador de comerciante de la puerta de enlace de su aplicación.
     *
     *
     * La respuesta de la API de Google Pay devolverá un método de pago
     * encriptado que se puede cobrar por una puerta de enlace compatible
     * después de la autorización del pagador.
     *
     *
     * TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
     *
     * @return Datos de pago tokenization para el método de pago TARJETA.
     * @throws JSONException
     * @see [PaymentMethodTokenizationSpecification](https://-----)
     */
    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(Constants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS))
        }
    }

    /**
     * Integración `DIRECT`: Descifre una respuesta directamente en sus servidores.
     * Esta configuración tiene requisitos de seguridad de datos adicionales de
     * Google y complejidad adicional de cumplimiento de PCI DSS.
     *
     *
     * Consulte la documentación para obtener más información sobre la integración `DIRECT`. los
     * El tipo de integración que utiliza depende de su procesador de pagos.
     *
     * @return Datos de pago tokenization para el método de pago TARJETA.
     * @throws JSONException
     * @see [PaymentMethodTokenizationSpecification](https://---)
     */
    private fun directTokenizationSpecification(): JSONObject {
        if (Constants.DIRECT_TOKENIZATION_PUBLIC_KEY == "REPLACE_ME" ||
                (Constants.DIRECT_TOKENIZATION_PARAMETERS.isEmpty() ||
                 Constants.DIRECT_TOKENIZATION_PUBLIC_KEY.isEmpty())) {

            throw RuntimeException(
                    "Edite el archivo Constants.java para agregar la versión del protocolo y la clave pública.")
        }

        return JSONObject().apply {
            put("type", "DIRECT")
            put("parameters", JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS))
        }
    }

    /**
     * Redes de tarjetas compatibles con su aplicación y su puerta de enlace.
     *
     *
     * TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
     *
     * @return Redes de tarjetas permitidas
     * @see [CardParameters](https://-------
     */
    private val allowedCardNetworks = JSONArray(Constants.SUPPORTED_NETWORKS)

    /**
     * Métodos de autenticación de tarjetas compatibles con su aplicación y su puerta de enlace.
     *
     *
     * TODO: Confirm your processor supports Android device tokens on your supported card networks
     * and make updates in Constants.java.
     *
     * @return Métodos de autenticación de tarjetas permitidos.
     * @see [CardParameters](https://------------------)
     */
    private val allowedCardAuthMethods = JSONArray(Constants.SUPPORTED_METHODS)

    /**
     * Describe la compatibilidad de tu aplicación con el método de pago CARD.
     *
     *
     * Las propiedades proporcionadas son aplicables tanto a IsReadyToPayRequest como a
     * PaymentDataRequest.
     *
     * @return Un objeto que describe las tarjetas aceptadas
     * @throws JSONException
     * @see [PaymentMethod](https://------------------------)
     */
    // Opcionalmente, puede agregar la dirección de facturación / número de teléfono
    // asociado con un método de pago con TARJETA.
    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }

    /**
     * Describa los datos de pago devueltos esperados para el método de pago con TARJETA
     *
     * @return Un método de pago con TARJETA que describe las tarjetas
     * aceptadas y los campos opcionales.
     * @throws JSONException
     * @see [PaymentMethod](https://-----)
     */
    private fun cardPaymentMethod(): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification())

        return cardPaymentMethod
    }

    /**
     * Un objeto que describe las formas de pago aceptadas por su aplicación, que se utiliza para determinar la
     * disposición a pagar.
     *
     * @return API versión y métodos de pago admitidos por la aplicación.
     * @see [IsReadyToPayRequest](https://------
     */
    fun isReadyToPayRequest(): JSONObject? {
        return try {
            val isReadyToPayRequest = JSONObject(baseRequest.toString())
            isReadyToPayRequest.put(
                    "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))

            isReadyToPayRequest

        } catch (e: JSONException) {
            null
        }
    }

    /**
     * Información sobre el comerciante que solicita información de pago
     *
     * @return Información sobre el comerciante.
     * @throws JSONException
     * @see [MerchantInfo](https://-------)
     */
    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", "Example Merchant")

    /**
     * Crea una instancia de [PaymentsClient] para usar en una [Actividad] usando el
     * entorno y tema establecidos en [Constantes].
     *
     * @param activity es la actividad de la persona que llama.
     */
    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
                .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
                .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Proporcione a la API de Pay un monto de pago, una moneda y un estado de monto.
     *
     * @return información sobre el pago solicitado.
     * @throws JSONException
     * @see [TransactionInfo](https://----------
     */
    @Throws(JSONException::class)
    private fun getTransactionInfo(price: String): JSONObject {
        return JSONObject().apply {
            put("totalPrice", price)
            put("totalPriceStatus", "FINAL")
            put("countryCode", Constants.COUNTRY_CODE)
            put("currencyCode", Constants.CURRENCY_CODE)
        }
    }

    /**
     *
    Un objeto que describe la información solicitada en una hoja de pago
     *
     * @return Datos de pago esperados por su aplicación.

     * @see [PaymentDataRequest](https://---------------------
     */
    fun getPaymentDataRequest(price: String): JSONObject? {
        try {
            return JSONObject(baseRequest.toString()).apply {
                put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod()))
                put("transactionInfo", getTransactionInfo(price))
                put("merchantInfo", merchantInfo)

                // Un requisito de dirección de envío opcional es una propiedad de nivel superior del
                // Objeto JSON PaymentDataRequest.
                val shippingAddressParameters = JSONObject().apply {
                    put("phoneNumberRequired", false)
                    put("allowedCountryCodes", JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES))
                }
                put("shippingAddressRequired", true)
                put("shippingAddressParameters", shippingAddressParameters)
            }
        } catch (e: JSONException) {
            return null
        }

    }
}

/**
 * Convierte micros a un formato de cadena aceptado por [PaymentsUtil.getPaymentDataRequest].
 *
 * @param micros valor del precio.
 */
fun Long.microsToString() = BigDecimal(this)
        .divide(PaymentsUtil.MICROS)
        .setScale(2, RoundingMode.HALF_EVEN)
        .toString()
