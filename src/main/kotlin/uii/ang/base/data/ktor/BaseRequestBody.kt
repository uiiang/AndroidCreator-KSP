package uii.ang.base.data.ktor

import kotlinx.serialization.Serializable

//{"msgType":"GetBranch","_user":{"UName":"DEVICE_NAME","UTypeID":"UT004"}}
interface BaseRequestBody {

  var msgType: String
//  var _user: BaseRequestBodyUser
}
//
//@Serializable
//data class BaseRequestBodyUser(var UName: String, var UTypeID: String)