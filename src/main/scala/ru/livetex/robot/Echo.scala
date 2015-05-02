package ru.livetex.robot

import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging}
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.simple.SimpleQueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.NIOFSDirectory
import ru.livetex.StopWords
import spray.can.Http
import spray.http.{HttpCharsets, HttpRequest, HttpResponse}

class Echo extends Actor with ActorLogging {
  val domain = "livetex.ru"
  val analyzer = new RussianAnalyzer(StopWords.RUSSIAN)
  val index = new NIOFSDirectory(Paths.get("/tmp/index-" + domain))

  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(_, _, _, entity, _) =>
      val query = new SimpleQueryParser(analyzer, "body").parse(
        entity.asString(HttpCharsets.`UTF-8`))

      Console.println("Parsed query " + query + ".")

      val searcher = new IndexSearcher(DirectoryReader.open(index))
      val results = searcher.search(query, 1).scoreDocs

      if (results.length == 0) {
        sender ! HttpResponse(entity = "Извините, ничем не могу вам помочь.")
      } else {
        sender ! HttpResponse(entity =
          searcher.doc(results(0).doc).get("url") + "\n")
      }

  }
}
