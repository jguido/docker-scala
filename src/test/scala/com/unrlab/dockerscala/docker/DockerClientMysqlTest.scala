package com.unrlab.dockerscala.docker

import com.unrlab.dockerscala.client.{Docker, DockerContainer, EnvVariable, PortMapping}
import com.unrlab.dockerscala.support.TestUtil
import org.scalatest.{FlatSpec, Matchers}

class DockerClientMysqlTest extends FlatSpec with Matchers{
  "Docker" should "return the container builded from a mysql image" in {
    val password: String = TestUtil.randomString
    val mysqlContainer = new DockerContainer()
      .withEnv(Seq(EnvVariable("MYSQL_ROOT_PASSWORD", password)))
      .withPortMapping(Seq(PortMapping(3306, TestUtil.randomPort)))
      .withName(s"roger")
      .withImage("mysql:5.6")

    val result = Docker.run(mysqlContainer)

    result.statusCode should be (204)
  }

  "Docker" should "return a container data when making a find request" in {
    val password: String = TestUtil.randomString
    val dockerContainer = new DockerContainer()
      .withName("hello-world")
      .withImage("hello-world")

    Docker.run(dockerContainer)

    Thread.sleep(2000)
    Docker.find("hello-world") should be(true)
    Docker.stop("hello-world") should be (true)
    Docker.remove("hello-world") should be (true)
    Docker.find("hello-world") should be(false)
  }
}
