package com.github.interaction.docsearcher

import github.interaction.docsearcher.utils.ResouceUtil
import org.scalatest._

class UtilsSpec extends FlatSpec with Matchers {

  "ArgumentParser" should "work" in {
    val resource = new {
      def close(): Unit = {
        println("close is called")
      }
    }

    val x = ResouceUtil.withResoure(resource){r =>{
      3
    }}

    x.isRight should be (true)
  }


}