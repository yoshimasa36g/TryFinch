package database

import io.getquill.{CamelCase, FinagleMysqlContext}

class DbContext(configPrefix: String) extends FinagleMysqlContext(CamelCase, configPrefix)

object DbContext {
  def forDevelopment(): DbContext = new DbContext("db")
  def forTest(): DbContext = new DbContext("test_db")
}
