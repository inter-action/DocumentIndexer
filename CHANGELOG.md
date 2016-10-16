2016-10-16 22:49:27 -

    rename logback.xml to logback-test.xml
      这个问题是由于引用的依赖 algorithms_of_the_intelligent_web 的项目自身打包了logback.xml
      导致classpath下有两个logback.xml, 然后根据google出来的解决, 重命名成那个之后就会override
      掉第三方jar包自带的logback.xml
      http://stackoverflow.com/questions/13788346/logback-xml-from-imported-jar-cause-a-warning

      