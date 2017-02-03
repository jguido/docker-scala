package com.unrlab.dockerscala.client

import okhttp3._

trait OkHttp extends HttpClient {
  val client: OkHttpClient = new OkHttpClient()

  protected def post(uri: String, body: Option[String] = None): HttpResponse = {
      val response = client.newCall(new Request.Builder()
        .url(uri)
        .post(body match {
          case Some(b) =>
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), b)
          case None =>
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "")
        })
        .build()).execute()

      buildResponse(response)
  }

  protected def get(uri: String): HttpResponse = {
    val response = client.newCall(new Request.Builder()
      .url(uri)
      .get()
      .build()).execute()

    buildResponse(response)
  }

  protected def delete(uri: String): HttpResponse = {
    val response: Response = client.newCall(new Request.Builder()
      .url(uri)
      .delete()
      .build()).execute()

    buildResponse(response)
  }

  private def buildResponse(response: Response): HttpResponse = {
    HttpResponse(response.code(), response.body().string())
  }
}
