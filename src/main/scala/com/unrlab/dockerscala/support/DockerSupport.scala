package com.unrlab.dockerscala.support

import com.unrlab.dockerscala.client._
import com.unrlab.dockerscala.config.Loggable


trait MysqlDockerSupport extends Loggable {
  def buildMysqlContainer(): MysqlDockerDbConfig = {
    val password: String = TestUtil.randomString
    val dbName: String = TestUtil.randomString
    val dbPort: Int = TestUtil.randomPort
    logInfo(s"Docker Mysql user : root | password : $password")
    logInfo(s"Docker Mysql dbName : $dbName | port : $dbPort")

    val mysqlContainer = new DockerContainer()
      .withEnv(Seq(EnvVariable("MYSQL_ROOT_PASSWORD", password)))
      .withPortMapping(Seq(PortMapping(3306, dbPort)))
      .withName(dbName)
      .withImage("mysql:5.6")

    Docker.run(mysqlContainer)
    val dbUrl = s"jdbc:mysql://0.0.0.0:$dbPort/$dbName?createDatabaseIfNotExist=true"

    MysqlDockerDbConfig(url = dbUrl, password = password, containerName = dbName)
  }

  def cleanMysqlContainer(containerName: String): Unit = {
    Docker.find(containerName) match {
      case true =>
        Docker.stop(containerName)
        Docker.remove(containerName)
      case false =>

    }
  }
}

case class MysqlDockerDbConfig(url: String, jdbcDriver: String = "com.mysql.jdbc.Driver", username: String = "root", password: String, containerName: String)