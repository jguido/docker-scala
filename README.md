## Scala docker client

Docker client written at first for testing purpose, for database migration managed by flyway and for avoiding issues on the creation or modification of tables.

### Installation

1. Clone this project and point a terminal to the root of the project
2. in terminal run 
```sbt assembly```
3. in terminal run 
```sbt publishLocal```
This will copy the jar to your local ivy2 folder (~/.ivy2/local/)
4. In your project's build.sbt add "com.unrlab" % "docker-scala_2.11" % "0.1.4" to the dependencies
5. That's it!

### Requirements
1. Scala 2.11+ / sbt 0.13+
2. docker installed with version 1.7+

### Pré-requis :
#### If your system manage process via systemctl (ubuntu 16 and later)

1. Stop the docker daemon
```sudo systemctl stop docker.service ```
2. Create a folder reserved for your docker configuration
```sudo mkdir /etc/systemd/system/docker.service.d```
3. Copy the default daemon configuration into the systemctl conf folder
```sudo cp  /lib/systemd/system/docker.service /etc/systemd/system/docker.service.d/docker.conf```
4. Remove the line starting with ExecStart
5. Add 
```
ExecStart=
ExecStart=/usr/bin/dockerd -H fd:// -H 127.0.0.1:55555
```
6. Reload daemon
```sudo systemctl daemon-reload```
7. Start docker daemon
```sudo systemctl start docker.service ```

#### If your system manage process via upstart (ubuntu 15 and earlier)

1. Stop the docker daemon
```sudo service docker stop```
2. Edit the default docker conf
```sudo vim /etc/default/docker```
3. Add the host of your desired daemon on the DOCKER_OPTS line
```DOCKER_OPTS="-H 127.0.0.1:55555"```
4. Start the docker daemon
```sudo service docker start```

### Usage

#### Mysql

##### With the Helper

You need to mix the Trait MysqlDockerSupport to your test class.

```scala
package com.unrlab.mysql

import com.unrlab.config.DbConfig
import com.unrlab.db.tools.Migrator
import com.unrlab.dockerscala.support.{MysqlDockerDbConfig, MysqlDockerSupport}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import slick.driver.JdbcDriver

class MySqlMigrationTest
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with MysqlDockerSupport {

  "Migrator" should "run against mysql" in {
    val conf: MysqlDockerDbConfig = buildMysqlContainer()
    val testDbconfig = new DbConfig {
      override def username: String = conf.username

      override def url: String = conf.url

      override def password: String = conf.password

      override def jdbcDriver: String = conf.jdbcDriver

      override def slickDriver: JdbcDriver =  slick.driver.MySQLDriver
    }

    import scala.concurrent.duration._
    var version = 0

    try {
      val migrator = new Migrator(testDbconfig)
      version = migrator.migrateRetry(10 seconds)
    } catch {
      case e: Throwable =>
        logError(s"[ERROR][MIGRATE] : ${e.getMessage}")
    } finally {
      cleanMysqlContainer(conf.containerName)
      version should be (5)
    }

  }
}
```
buildMysqlContainer() will create a docker container with mysql 5.6 and return a conf case class
```scala 
case class MysqlDockerDbConfig(url: String, jdbcDriver: String = "com.mysql.jdbc.Driver", username: String = "root", password: String, containerName: String)
```
With this case class, you will be able to recreate your database layer config.

#### Without the Helper

The Api Docker, need a DockerContainer object 

```scala
class DockerClientMysqlTest extends FlatSpec with Matchers{
  "Docker" should "return the container builded from a mysql image" in {
    val password: String = "password"
    val mysqlContainer = new DockerContainer()
      .withEnv(Seq(EnvVariable("MYSQL_ROOT_PASSWORD", password)))
      .withPortMapping(Seq(PortMapping(3306, 12345)))
      .withName(s"test_mysql")
      .withImage("mysql:5.6")

    val result = Docker.run(mysqlContainer)

    result.statusCode should be (204)
  }

  "Docker" should "return a container data when making a find request" in {
    val password: String = "password"
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
```

#### Docker API

* Docker.run(container: DockerContainer): HttpResponse

with HttpResponse defined as :
```scala 
case class HttpResponse(statusCode: Int, body: String)
```

__The run function will search if the container is up, and will try to stop and remove before running a new one.__

* Docker.find(name: String): Boolean
Where name is the name or the id of the container
* Docker.stop(name: String): Boolean
Where name is the name or the id of the container
* Docker.remove(name: String): Boolean
Where name is the name or the id of the container