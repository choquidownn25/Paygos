package com.google.android.gms.samples.wallet

import com.google.android.gms.wallet.WalletConstants

/**
 * Este archivo contiene varias constantes que debe editar antes de continuar.
 * Eche un vistazo a PaymentsUtil.java para ver dónde se utilizan las constantes y potencialmente
 * elimine los que no sean relevantes para su integración.
 *
 *
 * Cambios requeridos:
 *
 * 1. Actualice SUPPORTED_NETWORKS y SUPPORTED_METHODS si es necesario (consulte a su procesador si
 * inseguro)
 * 1. Actualice CURRENCY_CODE a la moneda que utiliza.
 * 1. Actualice SHIPPING_SUPPORTED_COUNTRIES para enumerar los países a los que realiza envíos actualmente. Si esto
 * no se aplica a su aplicación, elimine los bits relevantes de PaymentsUtil.java.
 * 1. Si se está integrando con su "PAYMENT_GATEWAY", actualice
 * PAYMENT_GATEWAY_TOKENIZATION_NAME y PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS según el
 * instrucciones que proporcionaron. No es necesario que actualice DIRECT_TOKENIZATION_PUBLIC_KEY.
 * 1. Si está utilizando la integración `DIRECT`, edite la versión del protocolo y la clave pública como
 * según las instrucciones.
 */
object Constants {
    /**
     * Cambiar esto a ENVIRONMENT_PRODUCTION hará que la API devuelva la información de la tarjeta con cargo.
     * Consulte la documentación para leer sobre los pasos necesarios para habilitar
     * ENVIRONMENT_PRODUCTION.
     *
     * @value #PAYMENTS_ENVIRONMENT
     */

    const val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

    /**
     * Las redes permitidas que se solicitarán desde la API. Si el usuario tiene tarjetas de
     * redes no especificado aquí en su cuenta, no se les ofrecerá elegir en la ventana emergente.
     *
     * @value #SUPPORTED_NETWORKS
     */
    val SUPPORTED_NETWORKS = listOf(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA")

    /**
     * La API de Google Pay puede devolver tarjetas archivadas en Google.com (PAN_ONLY) y / o un
     * token de dispositivo en un dispositivo Android autenticado con un criptograma
     * seguro 3-D (CRYPTOGRAM_3DS).
     *
     * @value #SUPPORTED_METHODS
     */
    val SUPPORTED_METHODS = listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS")

    /**
     * Requerido por la API, pero no visible para el usuario.
     *
     * @value #COUNTRY_CODE Pais
     */
    const val COUNTRY_CODE = "US"

    /**
     * Requerido por la API, pero no visible para el usuario.
     *
     * @value #CURRENCY_CODE Tu moneda local
     */
    const val CURRENCY_CODE = "USD"

    /**
     * Países admitidos para el envío (utilice los códigos de país ISO 3166-1 alpha-2).
     * Relevante solo cuando solicitando una dirección de envío.
     * @value #SHIPPING_SUPPORTED_COUNTRIES
     */
    val SHIPPING_SUPPORTED_COUNTRIES = listOf("US", "GB")

    /**
     * El nombre de su procesador de pagos / pasarela. Consulte su documentación para obtener más
     * información.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_NAME
     */
    const val PAYMENT_GATEWAY_TOKENIZATION_NAME = "example"

    /**
     * Parámetros personalizados requeridos por el procesador / puerta de enlace.
     * En muchos casos, su procesador / puerta de enlace solo requerirá un gatewayMerchantId.
     * Consulte la documentación de su procesador para obtener más información.
     * El número de parámetros obligatorio y sus nombres varían según el procesador.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
     */
    val PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS = mapOf(
            "gateway" to PAYMENT_GATEWAY_TOKENIZATION_NAME,
            "gatewayMerchantId" to "exampleGatewayMerchantId"
    )

    /**
     * Solo se utiliza para la tokenización `DIRECT`. Se puede eliminar cuando se usa
     * `PAYMENT_GATEWAY` tokenización.
     *
     * @value #DIRECT_TOKENIZATION_PUBLIC_KEY
     */
    const val DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME"

    /**
     * Parámetros necesarios para `DIRECT` tokenization.
     * Solo se usa para `DIRECT` tokenization. Se puede quitar cuando se usa `PAYMENT_GATEWAY`
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PARAMETERS
     */
    val DIRECT_TOKENIZATION_PARAMETERS = mapOf(
            "protocolVersion" to "ECv1",
            "publicKey" to DIRECT_TOKENIZATION_PUBLIC_KEY
    )
}
