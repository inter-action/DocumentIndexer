package com.github.interaction.docsearcher

import org.scalatest._

class SearcherSpec extends FlatSpec with Matchers {

  "ArgumentParser" should "work" in {
    val str = "-arg1 name1 -arg2 name2 -arg3"
    val args: Map[String, String] = ArgumentParser.parse(str)
    args.get("arg1").get should be ("name1")
    args.get("arg2").get should be ("name2")
    args.get("arg3").get should be ("")
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {

  }
}