package uii.ang.base.data.ktor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by "Mohamad Abuzaid" on 26/05/2024.
 * Email: m.abuzaid.ali@gmail.com
 */
@Serializable
data class RemoteResponse<T>(
    @SerialName("page")
    val page: Int?,
    @SerialName("total_pages")
    val totalPages: Int?,
    @SerialName("total_results")
    val totalResults: Int?,
    @SerialName("results")
    val results: T?,

    //Error Response
    @SerialName("success")
    val success: Boolean?,
    @SerialName("status_code")
    val statusCode: Int?,
    @SerialName("status_message")
    val statusMessage: String?
)