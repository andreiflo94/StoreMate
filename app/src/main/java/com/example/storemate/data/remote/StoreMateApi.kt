package com.example.storemate.data.remote

import com.example.storemate.data.remote.dto.AuthRequestDto
import com.example.storemate.data.remote.dto.AuthResponseDto
import com.example.storemate.data.remote.dto.CreateTransactionDto
import com.example.storemate.data.remote.dto.CurrentUserDto
import com.example.storemate.data.remote.dto.PageDto
import com.example.storemate.data.remote.dto.ProductDto
import com.example.storemate.data.remote.dto.SupplierDto
import com.example.storemate.data.remote.dto.TransactionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The on-prem StoreMate REST API.
 *
 * The base URL is a placeholder — [ServerUrlInterceptor] rewrites every request
 * to whichever server address the shop configured on the login screen.
 */
interface StoreMateApi {

    // --- Auth -------------------------------------------------------------
    @POST("api/auth/login")
    suspend fun login(@Body body: AuthRequestDto): AuthResponseDto

    @POST("api/auth/register")
    suspend fun register(@Body body: AuthRequestDto): AuthResponseDto

    @GET("api/auth/me")
    suspend fun me(): CurrentUserDto

    // --- Products ---------------------------------------------------------
    @GET("api/products")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PageDto<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Long): ProductDto

    @POST("api/products")
    suspend fun createProduct(@Body body: ProductDto): ProductDto

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body body: ProductDto): ProductDto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)

    // --- Suppliers --------------------------------------------------------
    @GET("api/suppliers")
    suspend fun getSuppliers(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PageDto<SupplierDto>

    @POST("api/suppliers")
    suspend fun createSupplier(@Body body: SupplierDto): SupplierDto

    @PUT("api/suppliers/{id}")
    suspend fun updateSupplier(@Path("id") id: Long, @Body body: SupplierDto): SupplierDto

    @DELETE("api/suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") id: Long)

    // --- Transactions -----------------------------------------------------
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PageDto<TransactionDto>

    @POST("api/transactions")
    suspend fun createTransaction(@Body body: CreateTransactionDto): TransactionDto
}
