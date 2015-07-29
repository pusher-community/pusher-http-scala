package com.pusher

import com.pusher.Signature.sign
import com.pusher.Types.{ValidationResponse, PusherResponse}
import com.pusher.Util.generateMD5Hash

import java.net.URI
import org.json4s.native.JsonMethods.parse
import org.json4s.DefaultFormats
import scala.util.Try
import scalaj.http.{Http, HttpRequest, HttpResponse}


object Request {

  implicit val formats = DefaultFormats

  /**
   * Generate authentication for the request
   * @param requestParams The request params object
   * @return Map[String, String]
   */
  private def generateAuth(requestParams: RequestParams): Map[String, String] = {
    val initialParams: Map[String, String] = Map(
      "auth_key" -> requestParams.config.key,
      "auth_version" -> "1.0",
      "auth_timestamp" -> (System.currentTimeMillis / 1000).toString
    )

    val optionalParams: Map[String, String] = requestParams.params.getOrElse(Map.empty[String, String])

    val bodyParams: Map[String, String] = (requestParams.verb, requestParams.body) match {
      case ("POST", Some(body)) => Map("body_md5" -> generateMD5Hash(body))
      case _ => Map.empty[String, String]
    }

    val authParams = initialParams ++ optionalParams ++ bodyParams

    val authString: String = List(
      requestParams.verb,
      new URI(generateEndpoint(requestParams.config, requestParams.path)).getPath,
      generateQueryString(authParams)
    ).mkString("\n")

    authParams ++ Map("auth_signature" -> sign(requestParams.config.secret, authString))
  }

  /**
   * Generate a query string using the parameters
   * @param queryParams Parameters used to generate the string
   * @return String
   */
  private def generateQueryString(queryParams: Map[String, String]): String = {
    queryParams.toSeq.sortBy(_._1).map { case(k, v) =>
      k + "=" + v
    }.mkString("&")
  }

  /**
   * Get the pusher endpoint
   * @param config The PusherConfig object
   * @return String
   */
  private def generateEndpoint(config: PusherConfig, path: String): String = {
    s"${config.scheme}://${config.getHost}:${config.getPort}/apps/${config.appId}$path"
  }

  /**
   * Handle HTTP responses
   * @param response The HTTP response object
   * @tparam T Type of the case class
   * @return PusherResponse
   */
  private def handleResponse[T <: PusherBaseResponse : Manifest]
                            (response: Try[HttpResponse[String]]): PusherResponse[T] = {
    val httpResponse: Either[PusherRequestFailedError, HttpResponse[String]] =
      if (response.isSuccess) {
        Right(response.get)
      } else {
        Left(PusherRequestFailedError(response.failed.get.getMessage))
      }

    httpResponse match {
      case Right(resp) =>
        val responseBody: String = resp.body

        resp.code match {
          case 200 =>
            parseResponse[T](responseBody) match {
              case Some(parsedValue) => Right(parsedValue)
              case None => Left(JSONParsingError(s"Failed to parse JSON: $responseBody"))
            }
          case 400 => Left(PusherBadRequestError(responseBody))
          case 401 => Left(PusherBadAuthError(responseBody))
          case 403 => Left(PusherForbiddenError(responseBody))
          case _ => Left(PusherBadStatusError(responseBody))
        }
      case Left(error) => Left(error)
    }
  }

  /**
   * Parse responses and extract them into case classes
   * @param responseBody The response string to parse
   * @tparam T Type of the case class
   * @return T
   */
  private def parseResponse[T <: PusherBaseResponse : Manifest](responseBody: String): Option[T] = {
    parse(responseBody).extractOpt[T]
  }

  /**
   * Make a new HTTP request
   * Generate all auth that is required to be sent
   * @param requestParams Params to make a request with
   * @tparam T Type of the case class
   * @return PusherResponse
   */
  def makeRequest[T <: PusherBaseResponse : Manifest]
                 (requestParams: RequestParams): PusherResponse[T] = {
    val initRequest: HttpRequest = Http(
      generateEndpoint(requestParams.config, requestParams.path)
    ).method(
        requestParams.verb
      ).params(
        generateAuth(requestParams)
      )

    val request: HttpRequest = requestParams.verb match {
      case "POST" =>
        initRequest.postData(
          requestParams.body.getOrElse("")
        ).header("Content-Type", "application/json")
      case _ => initRequest
    }

    handleResponse[T](Try(request.asString))
  }

  /**
   * Validate before making requests
   * @param requestParams Parameters for the request
   * @param validatorResponses List of ValidatorResponse
   * @tparam T Type of the case class
   * @return PusherResponse
   */
  def validateAndMakeRequest[T <: PusherBaseResponse : Manifest]
                            (requestParams: RequestParams,
                             validatorResponses: List[ValidationResponse]): PusherResponse[T] = {
    val results = validatorResponses.flatMap(x => x)

    if (results.nonEmpty) {
      Left(results.head)
    } else {
      makeRequest[T](requestParams)
    }
  }
}
