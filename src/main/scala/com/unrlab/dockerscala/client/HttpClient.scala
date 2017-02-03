package com.unrlab.dockerscala.client

trait HttpClient {
  protected def post(url: String, body: Option[String] = None): HttpResponse
  protected def get(url: String): HttpResponse
  protected def delete(uri: String): HttpResponse
}

case class HttpResponse(statusCode: Int, body: String)
