package com.unrlab.dockerscala.support

import java.util.UUID

import scala.util.Random

object TestUtil {
  val random = new Random(System.currentTimeMillis())

  def randomString = UUID.randomUUID().toString.split("-").head

  def randomPort = random.nextInt(10000) + 9000

  def randomSmallInt = random.nextInt(20)

  def randomBytes = randomString.getBytes
}
