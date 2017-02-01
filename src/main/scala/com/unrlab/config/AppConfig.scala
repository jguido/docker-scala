package com.unrlab.config

import com.typesafe.config.Config

trait AppConfig {
  val dockerConfig: DockerConfig
}

class DefaultAppConfig(val config: Config) extends AppConfig {

  override val dockerConfig: DockerConfig = new DefaultDockerconfig(config)
}
