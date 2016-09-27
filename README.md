https://mvnrepository.com/
http://winterbe.com/posts/2015/03/25/java8-examples-string-number-math-files/
http://lucene.apache.org/core/5_5_0/demo/overview-summary.html#IndexFiles

Akka Rest
! https://danielasfregola.com/2016/02/07/how-to-build-a-rest-api-with-akka-http/
http://doc.akka.io/docs/akka/2.4.2/scala/http/client-side/connection-level.html
http://alvinalexander.com/scala/scala-akka-actors-ping-pong-simple-example


Akka 2.0.4 ref:
http://doc.akka.io/docs/akka/2.4.10/scala.html

Akka:
! http://doc.akka.io/docs/akka/2.4.10/scala.html

Akka-Http:
http://doc.akka.io/docs/akka/2.4.10/scala/http/index.html

Low-Level Server-Side API





###
Test RestAPI:

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






### todos:

    do i need to add file filter on indexer?
    try to apply some machine learning, calc similarity etc
    add scalaz lib & utilize it.
    add docker packaging
    provide match context & highlight
    provide restful services, we can use akka-http

    try to pack webapp in one jar


