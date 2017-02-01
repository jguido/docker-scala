package com.unrlab.config

import com.typesafe.config.Config

trait DockerConfig {
  val host: String
}

class DefaultDockerconfig(val conf: Config) extends DockerConfig {
  override val host: String = conf.getConfig("docker").getString("host")
}
