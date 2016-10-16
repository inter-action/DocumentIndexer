# About this repo
这个项目是这样的，我个人由于平时阅读量比较大, 总会在电脑上记忆好多笔记。但是我个人特别喜欢sublime和vscode这样
的文本编辑器(因为我喜欢用缩进组织知识)。而evernote我虽然喜欢，但是主要用作日志和web clipper,毕竟文本编辑功能难用。然后这些文本
用网盘同步。但是由于文件数量逐渐增大, 要是有个全文搜索引擎来检索应该会比较好。所以这就是这个项目的目的。

这个项目主要提供REST的索引服务，考虑到灵活通用。因为我还有别的想法。考虑到最近学了angularjs2和koajs没有时间去应用。再加上
如果之提供一个索引功能也没什么大作用，

最好能整合electron, 这样检索出来之后能够用默认的编辑器打开。（这个需要考虑下）

最好能写个vs code插件, 我觉得这样可能比electron体验要好。这个再看,考虑到这个方向我更加不熟悉。

koajs估计短时间会应用不上, 如果接入angularjs写的前端UI, 目前直接对接这个项目就够用了。如果以后有机会应用些
机器学习的东西(这个项目估计很大程度会用python写, 至于还用不用http交换数据再看以后), 这就需要koajs或者hapi.js
提供一个facade来统一对前段UI路由。

最后docker也可以整合进来，做应用的打包。(docker整合进来之后, 如何freely access OS file system 可能会有问题，估计没有好的方案)
算了还是不考虑docker了
直接用sbt-native packager打包成本地binary吧

这是可见的将来的打算。

## 技术栈
* lucene
    其实我有考虑简单粗暴的mongodb提供内置的全文检索功能, 但是考虑到需要把文本数据重复导入到mongodb中我就有些蛋蛋的忧伤

* akka-http:
    没办法, 项目需要做成微服务, 提供RESTful接口是必须的。之所以选这个, 是因为我最早打算用spray http+akka做rest(轻量, 
    高并发, 不像传统jvm webserver内部abuse线程模型处理并发请求，虽说项目根本没啥并发请求，但是这个b我还是要强行装下的)，
    但是不知道从什么时候开始spray http+akka不被支持了, akka-http是未来的方向, akka团队内部大量使用spray http，
    所以他们打算整合http需求, spray-http就这样被废弃了。
    这样也好, 我也顺带学下akka( 少说500页文档要看 :(,  )，还有netty有可能我也不要看了 :), bitter sweet.
    早些年akka就声名远扬了, 项目起源是erlang项目, 爱立信发布的论文。然后就被某大神搞了个scala版的，jvm上处理并发的高端库。 


## Start It Up
run:

    > brew install sbt
    > cd <this_project_dir>
    > sbt
    > runMain github.interaction.docsearcher.Main

stop:
    
    hit Enter key or (any key)




## Test RestAPI:

Request:
```
curl -v -H "Content-Type: application/json" \
	 -X POST http://localhost:5000/questions \
	 -d '{"id": "test", "title": "MyTitle", "text":"The text of my question"}'
```

Request:
```
curl -v http://localhost:5000/questions/test
```

创建lucene索引 Request:
```
curl -v -H "Content-Type: application/json" \
	 -X PUT http://localhost:5000/docs \
	 -d '{ "docPath": "/Users/interaction/workspace/temp/testeddocs" }'
```


搜索lucene索引 Request:

```
curl -v http://localhost:5000/docs?query=scala
```


## [Packaging: sbt-native-packager] (http://www.scala-sbt.org/sbt-native-packager/gettingstarted.html#your-first-package)

sbt-native-packager 可以将scala项目打包成各种各样的格式。目前我也不是特别懂。目前我直接用最简单的universal packager
打包, 由于我的机器是mac我就尝试了mac下的dmg打包, 当然还有很多方式。


### native-packager 组成:
又 formats, Project Archetypes两部分组成
formats 主要控制打包的构建的目标格式
Project Archetypes 主要定义项目的格式, 由项目的格式经由formats生成最终的package. 项目的格式定义了什么该copy和增加一些而外的feature。

Formats:
* universal # 最通用最基础的打包格式
* JDKPackager Plugin # 用于构建原生的本地应用, 有图标, 有安装过程的那种


### universal to mac
切到工程目录, 执行`sbt universal:packageOsxDmg`

打包完的dmg会在 `/target/universal` 路径下： 主要包含的文件构成是 bash 脚本 + java lib 的dependencies

### universal to docker

http://www.scala-sbt.org/sbt-native-packager/formats/docker.html

`sbt docker:publishLocal` native-packager 会创建bash脚本和对应的Dockerfile。然后自动开启docker构建流程，发布到
 本机的docker server。当然你可以直接利用 native-packager生成好的文件手动build docker

执行完之后，你可以通过docker ps看见构建完的镜像,

    docker run 执行

执行过程有问题, 以后有时间看下



## Akka
marshalling 

    http://doc.akka.io/docs/akka/2.4.4/java/http/routing-dsl/marshalling.html


## scala

    // catch NonFatal error
    case NonFatal(e) => Failure(e)


## Lucene

- [TextField vs StringField](http://stackoverflow.com/questions/26752958/solr-text-field-and-string-field-different-search-behaviour):

* TextFields usually have a tokenizer and text analysis attached, meaning that the indexed content is broken into separate tokens where there is no need for an exact match - each word / token can be matched separately to decide if the whole document should be included in the response.
* StrFields cannot have any tokenization or analysis / filters applied, and will only give results for exact matches. If you need a StrField with analysis or filters applied, you can implement this using a TextField and a KeywordTokenizer.


## 项目依赖

    "com.github.interaction" %% "algorithms_of_the_intelligent_web" % "0.1.0",

这个项目依赖我github上的另一个项目`algorithms_of_the_intelligent_web`,现成的代码修改了下就用了.
主要是用于文档计算相似性cluster用的。但是由于分词的问题, terms 生成的有点乱, 效果还不是很好。以后解决下。


## [Logger Levels](http://stackoverflow.com/questions/5817738/how-to-use-log-levels-in-java)

We're using Log4J and the following levels:

* ERROR: Any error/exception that is or might be critical. Our Logger automatically sends an email for each such message on our servers (usage:  logger.error("message"); )
* WARN: Any message that might warn us of potential problems, e.g. when a user tried to log in with wrong credentials - which might indicate an attack if that happens often or in short periods of time (usage: logger.warn("message"); )
* INFO: Anything that we want to know when looking at the log files, e.g. when a scheduled job started/ended (usage: logger.info("message"); )
* DEBUG: As the name says, debug messages that we only rarely turn on. (usage: logger.debug("message"); )

The beauty of this is that if you set the log level to WARN, info and debug messages have next to no performance impact. If you need to get additional information from a production system you just can lower the level to INFO or DEBUG for a short period of time (since you'd get much more log entries which make your log files bigger and harder to read). Adjusting log levels etc. can normally be done at runtime (our JBoss instance checks for changes in that config every minute or so).




##---


    https://mvnrepository.com/
    http://winterbe.com/posts/2015/03/25/java8-examples-string-number-math-files/
    http://lucene.apache.org/core/5_5_0/demo/overview-summary.html#IndexFiles

    Akka Rest
    ! https://danielasfregola.com/2016/02/07/how-to-build-a-rest-api-with-akka-http/
    http://doc.akka.io/docs/akka/2.4.2/scala/http/client-side/connection-level.html
    http://alvinalexander.com/scala/scala-akka-actors-ping-pong-simple-example
    http://malaw.ski/2016/04/10/hakk-the-planet-implementing-akka-http-marshallers/

    Akka 2.0.4 ref:
    http://doc.akka.io/docs/akka/2.4.10/scala.html



### todos:
    done:
        异常处理, lucene创建索引的时候如果抛出异常, 目前没有按预期返回给请求者, 而是直接在系统里抛出异常了 :(
            ERROR [ActorSystemImpl]: Uncaught error from thread [quiz-management-service-akka.actor.default-dispatcher-8]
            scala.runtime.NonLocalReturnControl: null

        make app quit gracefully without quiting sbt
        make request able to respond with custom JSON object instead of plain string
        replace println with normal logger.
        provide restful services, we can use akka-http
        provide match context & highlight
            http://makble.com/how-to-do-lucene-search-highlight-example
        replace standard analyzer with optimized chiniese tokenizer
            http://codepub.cn/2016/03/23/Maven-project-integrating-Lucene-Chinese-Segmentation-tools-Jcseg-and-Ansj/
            最终用了lucene自带的smartcn analyzer, 因为项目要求不高，简单起见就用了这个，复杂的实现看上边的链接
        lucene term vector
            http://makble.com/what-is-term-vector-in-lucene
        suppress scala deprecated api warning
            ignored, seems scala dosnt support this feature
        trie:
            https://www.youtube.com/watch?v=RIUY7ieyH40
            https://en.wikipedia.org/wiki/Trie

        add sbt-native packager:
            http://doc.akka.io/docs/akka/2.4.10/intro/deployment-scenarios.html
            http://www.scala-sbt.org/sbt-native-packager/

        try to apply some machine learning, calc similarity etc & cluster
            formula:
                E termXInDocA * termXInDocB / sqrt(E termXsInDocA) * sqrt(E termXsInDocB)

            http://stackoverflow.com/questions/10649898/better-way-of-calculating-document-similarity-using-lucene
            https://gist.github.com/butlermh/4672977
            http://blog.christianperone.com/2013/09/machine-learning-cosine-similarity-for-vector-space-models-part-iii/
            http://scikit-learn.org/stable/modules/clustering.html

    pending:
        add scala linter


        add scalaz lib & utilize it & refactor codes 
        add file name to the sorting weight
        clear todos in code

        reading: http://doc.akka.io/docs/akka/2.4.2/scala/http/client-side/connection-level.html