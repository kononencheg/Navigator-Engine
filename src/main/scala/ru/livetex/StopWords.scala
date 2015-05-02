package ru.livetex

import java.nio.charset.StandardCharsets

import org.apache.lucene.analysis.util.WordlistLoader
import org.apache.lucene.util.IOUtils


object StopWords {
  val RUSSIAN = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(
    getClass, "stop-words.txt", StandardCharsets.UTF_8))
}
