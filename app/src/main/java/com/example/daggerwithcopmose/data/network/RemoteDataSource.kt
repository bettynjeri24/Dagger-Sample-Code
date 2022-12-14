package com.example.daggerwithcopmose.data.network

import com.example.daggerwithcopmose.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val BASE_URL = "https://jsonplaceholder.typicode.com/"
var AUTH_TOKEN = ""

class RemoteDataSource @Inject constructor(
    private val networkInterceptor: NetworkInterceptor
) {
    /**define our own request timeout so that retrofit doesn't use its default one for 10 sec*/
    private val REQUEST_TIMEOUT = 30L

    /**
     * This is a reusable function to perform API instances
     */
    fun <API> buildApi(api: Class<API>, baseurl: String = BASE_URL): API {
        return Retrofit.Builder()
            .baseUrl(baseurl)
            .client(getOkHttpClient(networkInterceptor))
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            )
            .build()
            .create(api)
    }

    /**
     * Interceptor helps logs outing request and the incoming response
     * */
    private fun getOkHttpClient(interceptor: Interceptor): OkHttpClient {
        val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .supportsTlsExtensions(true)
            .allEnabledTlsVersions()
            .allEnabledCipherSuites()
            /*.tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
            )*/
            .build()

        /**
         * Pinning certificates defends against attacks on certificate authorities.
         * It also prevents connections through man-in-the-middle certificate
         * authorities either known or unknown to the application's user.
         */
        val certificatePinner = CertificatePinner.Builder()
            .add(
                "*.ekenya.co.ke",
                "sha256/hkhkhfahaliuhrfjdbfiaulfhjkldbfaddkjdkj="
                // URL_FINGERPRINT
            )
            .build()

        return OkHttpClient().newBuilder().apply {
            /**configure timeouts settings via OkHTTP*/
            connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val loggingInterceptor =
                    HttpLoggingInterceptor().setLevel(level = HttpLoggingInterceptor.Level.BODY)
                addInterceptor(loggingInterceptor)
            }
            addInterceptor(interceptor)
            addInterceptor { chain ->
                chain.proceed(chain.request()).newBuilder().also {
                    it.addHeader("Accept", "application/json")
                }.build()
            }
            // certificatePinner(certificatePinner)
            // connectionSpecs(Collections.singletonList(spec))
        }.build()
    }
}
