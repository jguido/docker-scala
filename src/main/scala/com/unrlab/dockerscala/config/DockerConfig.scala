package com.unrlab.dockerscala.config

import com.typesafe.config.Config

trait DockerConfig {
  val host: String
  var version: String
}

class DefaultDockerconfig(val conf: Config) extends DockerConfig {
  private val dockerConfig = conf.getConfig("docker")
  override val host: String = dockerConfig.getString("host")
  override var version: String = dockerConfig.getString("version")
}
