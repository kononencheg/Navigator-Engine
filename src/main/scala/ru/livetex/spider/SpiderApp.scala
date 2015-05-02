package ru.livetex.spider


import java.nio.file.Paths

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.NIOFSDirectory
import org.jsoup.Jsoup
import ru.livetex.StopWords
import spray.http.Uri

import scala.collection.JavaConversions._


object SpiderApp extends App {
  val domain = "livetex.ru"
  val history = collection.mutable.Set[String]()

  val analyzer = new RussianAnalyzer(StopWords.RUSSIAN)
  val index = new NIOFSDirectory(Paths.get("/tmp/index-" + domain))
  var writer = new IndexWriter(index, new IndexWriterConfig(analyzer))

  def locationId(location: String): String = {
    val uri = Uri.parseAbsolute(location)
    val path = uri.path.toString()
    val lastIndex = path.length - 1

    if (lastIndex >= 0 && path.charAt(lastIndex) == '/') {
      uri.authority.toString() + path.substring(0, lastIndex) +
        uri.query.toString()
    } else {
      uri.authority.toString() + path + uri.query.toString()
    }
  }

  def addPage(id: String, page: org.jsoup.nodes.Document): collection.mutable.Buffer[String] = {
    Console.println("Add page " + page.title() + " (" + id + ") to index")

    val doc = new Document()
    doc.add(new StringField("id", id, Field.Store.YES))
    doc.add(new StringField("title", page.title(), Field.Store.YES))
    doc.add(new StringField("url", page.location(), Field.Store.YES))
    doc.add(new TextField("body", page.select("body > *:not(footer, nav, header)").text(), Field.Store.YES))

    writer.addDocument(doc)

    page.select("a")
      .map(_.attr("abs:href").replaceAll("\\#.*", ""))
      .filter(_.indexOf(domain) >= 0)
  }

  def crawl(location: String): Unit = {
    val id = locationId(location)
    if (id.charAt(0) == '/' && !history(id)) {
      history += id

      try {
        addPage(id, Jsoup.connect(location).get()).foreach(crawl)
      } catch {
        case e: Exception =>
          Console.println("Unable to crawl page " + location + ".")
      }
    }
  }

  Console.println("Begin scanning " + domain + " domain.")

  crawl("http://" + domain)
  writer.close()

  Console.println("Found " + history.size + " unique pages.")
}
