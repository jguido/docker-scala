package com.unrlab.dockerscala.client

import com.unrlab.dockerscala.config.{ConfigLoader, Loggable}

class DockerContainer {
  private var image: String = _
  var name: String = _
  private var ports: Seq[PortMapping] = Seq()
  private var envs: Seq[EnvVariable] = Seq()

  def withImage(image: String): DockerContainer = {
    this.image = image
    this
  }
  def withPortMapping(ports: Seq[PortMapping]): DockerContainer = {
    this.ports = ports
    this
  }
  def withName(name: String): DockerContainer = {
    this.name = name
    this
  }
  def withEnv(envs: Seq[EnvVariable]): DockerContainer = {
    this.envs = envs
    this
  }
  def build(): Container = {
    Container(None, image, ports, Some(name), envs)
  }

}

case class PortMapping(internal: Int, external: Int)
case class EnvVariable(name: String, value: String)


object Docker extends ConfigLoader with OkHttp with Loggable {

  def run(container: DockerContainer): HttpResponse = {
    val buildedContainer: Container = container.build()

    val uri: String = s"$buildUrl/containers/create?name=${buildedContainer.name.getOrElse("")}"

    find(container.name) match {
      case true =>
        stop(container.name)
        remove(container.name)
      case false =>
        
    }

    val response = post(uri, Some(buildData(buildedContainer)))
    response.statusCode match {
      case 201 =>

        val id = response.body.split(",").head.split(":").last

        val startResult = post(s"$buildUrl/containers/${container.name}/start")

        startResult.statusCode match {
          case 204 =>
            startResult
          case 304 =>
            logInfo(s"[DOCKER][START][ALREADY_STARTED] Container ${container.name} is already started")

            startResult
          case 404 =>
            logError(s"[DOCKER][START][NO_SUCH_CONTAINER] Unable to start container : ${container.name}. Message : ${response.body}")
            startResult
          case _ =>
            logError(s"[DOCKER][START][SERVER_ERROR] Unable to start container : ${container.name}. Message : ${response.body}")
            startResult
        }

      case 400 =>
        logError(s"[DOCKER][CREATE][BAD_REQUEST] Unable to create container : ${container.name}. Message : ${response.body}")
        response
      case 404 =>
        logError(s"[DOCKER][CREATE][NO_SUCH_CONTAINER] Unable to create container : ${container.name}. Message : ${response.body}")
        response
      case 406 =>
        logError(s"[DOCKER][CREATE][ATTACH] Unable to create container : ${container.name}. Message : ${response.body}")
        response
      case 409 =>
        logError(s"[DOCKER][CREATE][CONFLICT] Unable to create container : ${container.name}. Message : ${response.body}")
        response
      case _ =>
        logError(s"[DOCKER][CREATE][SERVER_ERROR] Unable to create container : ${container.name}. Message : ${response.body}")
        response
    }
  }

  def stop(containerName: String): Boolean = {
    val uri = s"$buildUrl/containers/$containerName/stop"

    val response = post(uri)

    response.statusCode match {
      case 204 =>
        true
      case 304 =>
        true
      case 404 =>
        logError(s"[DOCKER][STOP][NO_SUCH_CONTAINER] Unable to stop container : $containerName. Message : ${response.body}")
        false
      case _ =>
        logError(s"[DOCKER][STOP][SERVER_ERROR] Unable to stop container : $containerName. Message : ${response.body}")
        false
    }
  }

  def remove(containerName: String) = {
    val uri = s"$buildUrl/containers/$containerName"

    val response = delete(uri)

    response.statusCode match {
      case 204 =>
        true
      case 400 =>
        logError(s"[DOCKER][REMOVE][BAD_REQUEST] Unable to remove container : $containerName. Message : ${response.body}")
        false
      case 404 =>
        logError(s"[DOCKER][REMOVE][NO_SUCH_CONTAINER] Unable to remove container : $containerName. Message : ${response.body}")
        false
      case _ =>
        logError(s"[DOCKER][REMOVE][SERVER_ERROR] Unable to remove container : $containerName. Message : ${response.body}")
        false
    }
  }

  def find(containerName: String): Boolean = {
    val uri = s"""$buildUrl/containers/json?all=1&filters={%22name%22:[%22$containerName%22]}"""

    val response = get(uri)

    response.statusCode match {
      case 200 =>
        response.body.contains(containerName) match {
          case false =>
            false
          case true =>
            true
        }
      case 400 =>
        logError(s"[DOCKER][FIND][BAD_REQUEST] Unable to find container : $containerName. Message : ${response.body}")
        false
      case _ =>
        logError(s"[DOCKER][FIND][SERVER_ERROR] Unable to find container : $containerName. Message : ${response.body}")
        false
    }
  }

  private val buildUrl: String = s"${dockerConfig.host}/v${dockerConfig.version}"

  private def buildData(container: Container): String = {
    val envs: String = container.env.map{ e =>
      s""""${e.name}=${e.value}""""
    }.mkString(",")
    val ports: String = container.port.map{ p =>
      s""" "${p.internal}/tcp":[{"HostPort":"${p.external}"}] """
    }.mkString("")

    s"""
      |{
      |   "Env":[
      |      $envs
      |   ],
      |   "Image":"${container.image}",
      |   "HostConfig":{
      |      "PortBindings":{
      |         $ports
      |      }
      |   }
      |}
    """.stripMargin
  }
}

case class Container(id: Option[String] = None, image: String, port: Seq[PortMapping], name: Option[String], env: Seq[EnvVariable])
