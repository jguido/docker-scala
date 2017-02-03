package com.unrlab.dockerscala.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigLoader {
  private val tsConfig: Config = ConfigFactory.load("docker-scala.conf")
  val dockerConfig = new DefaultDockerconfig(tsConfig.getConfig("unrlab"))
}
