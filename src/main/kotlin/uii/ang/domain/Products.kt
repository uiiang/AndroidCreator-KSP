package uii.ang.domain

import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.annotation.requestMethodPost

@Creator(
    generateResponse = true,
    responseClassName = "GetProductListResponse",
    generateRetrofitService = true,
    retrofitServiceClassName = "ProductsRetrofitService",
    method = requestMethodPost,
    url = "./?method=album.getproducts",
    methodName = "getProductsAsync",
    parameters = [
        Parameter(paramName = "name", paramType = "String", paramQueryType = "Field"),
        Parameter(paramName = "limit", paramType = "Int", paramQueryType = "Query", paramDefault = "11")
    ]
)
data class Products (
    val products: List<Product>,
    val total: Long,
    val skip: Long,
    val limit: Long
)

@Creator(
    generateResponse = true,
    responseClassName = "GetProductInfoResponse",
    generateRetrofitService = true,
    retrofitServiceClassName = "ProductsRetrofitService",
    method = requestMethodGet,
    url = "./?method=album.getproducts",
    methodName = "getProductInfoAsync",
    parameters = [
        Parameter(paramName = "id", paramType = "Long", paramQueryType = "Field"),
        Parameter(paramName = "limit", paramType = "Int", paramQueryType = "Query", paramDefault = "11")
    ]
)
data class Product (
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Long,
    val tags: List<String>,
    val brand: String? = null,
    val sku: String,
    val weight: Long,
    val dimensions: Dimensions,
    val warrantyInformation: String,
    val shippingInformation: String,
    val availabilityStatus: String,
    val reviews: List<Review>,
    val returnPolicy: String,
    val minimumOrderQuantity: Long,
    val meta: Meta,
    val images: List<String>,
    val thumbnail: String
)
@Creator
data class Dimensions (
    val width: Double,
    val height: Double,
    val depth: Double
)
@Creator
data class Meta (
    val createdAt: String,
    val updatedAt: String,
    val barcode: String,
    val qrCode: String
)
@Creator
data class Review (
    val rating: Long,
    val comment: String,
    val date: String,
    val reviewerName: String,
    val reviewerEmail: String
)
