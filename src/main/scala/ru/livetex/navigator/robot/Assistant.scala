package ru.livetex.navigator.robot

import java.nio.file.Paths

import akka.actor.Actor
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.simple.SimpleQueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.NIOFSDirectory
import ru.livetex.navigator.StopWords
import spray.can.Http
import spray.http.HttpHeaders.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import spray.http._


class Assistant extends Actor {
  val domain = "livetex.ru"
  val analyzer = new RussianAnalyzer(StopWords.RUSSIAN)
  val index = new NIOFSDirectory(Paths.get("/tmp/index-" + domain))
  val headers = List(
    `Access-Control-Allow-Origin`(AllOrigins),
    `Access-Control-Allow-Methods`(HttpMethods.POST),
    `Access-Control-Allow-Credentials`(allow=true)
  )

  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(_, _, _, entity, _) =>
      val query = new SimpleQueryParser(analyzer, "body").parse(
        entity.asString(HttpCharsets.`UTF-8`))

      Console.println("Parsed query " + query + ".")

      val searcher = new IndexSearcher(DirectoryReader.open(index))
      val results = searcher.search(query, 1).scoreDocs

      if (results.length == 0) {
        sender ! HttpResponse(headers = headers)
      } else {
        sender ! HttpResponse(headers = headers,
          entity = searcher.doc(results(0).doc).get("url"))
      }

  }
}
