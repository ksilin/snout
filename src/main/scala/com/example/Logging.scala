package com.example

import org.slf4j.LoggerFactory

trait Logging {
  val log = LoggerFactory.getLogger(getClass)
}
