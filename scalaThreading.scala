import java.util.concurrent._
import scala.swing._
import scala.io._
import scala.util.parsing.combinator._
import scala.collection.mutable.ListBuffer
import java.io._
import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.actors.Actor._

object scalaThreading {
	class ProcessChapter (val dictionary: List[String], val input: List[String], val chapter: Int, val actor: Actor) extends Actor {
	    private var chapterSet = List[String]()
	    private var internalIndex = Map[String, List[Float]]()
	    var lineNum = 1
		def act(){
	    for (x <- input){
	    	if(lineNum > 5){
	    	//remove invalid characters (non decimal or alphabet)
	         var filtX = (x.toLowerCase()) map (ch => convertChar(ch).toChar)
	         
	         for(y <- filtX.split("[ ]+").toList){
	        	 chapterSet ::= (y+"---"+lineNum)
	         }
	    	}
	    	 lineNum += 1      
	    }

		  for (x <- chapterSet){
		    var word = x.split("[---]+").toList
		    if(dictionary.contains(word(0))){
			    if (internalIndex.contains(word(0))){
			      val temp = internalIndex(word(0))
			      internalIndex += (word(0) -> ((chapter + "." + word(1).toInt).toFloat :: temp))
			    }
			    else {
			      var newList = List[Float]()
			      newList ::= (chapter + "." + word(1).toInt).toFloat
			      internalIndex += (word(0) -> newList)
			    }
		    }
		  }
		  
		  actor ! internalIndex
		}
	}
    var mainIndex = Map[String, List[Float]]()
    class MainIndexer(numChapters: Int) extends Actor {
      var completedProcesses = 0
      def act() = {
        loop {
          react{
            case map: Map[String, List[Float]] => {
              for (x <- map){
              	if (mainIndex.contains(x._1)){
			      mainIndex += (x._1 -> (mainIndex(x._1) ::: map(x._1)))
			    }
			    else {
			      mainIndex += (x)
			    }
              }
              if(completedProcesses == numChapters){
            	  val output = new FileWriter("indexScala.txt")
            	  for(x <- (mainIndex.toList).sortWith((x1, x2) => (x1._1 < x2._1))){
            		  var wordIndex = ""
            		     wordIndex += x._1 + " --> "
            			 wordIndex += (x._2).sortWith((x1, x2) => x1 < x2)
            			 output.write(wordIndex + "\n")
            	  }
            	  output.close
                exit()
              }
              completedProcesses += 1
            }
          }
        }
      }
      
    }
   
   def convertChar(ch: Int): Int = {
     if (ch==32 || (48 to 57).contains(ch) || (97 to 122).contains(ch)) ch else 32
   }
	
   def readMessage = {
    val chooser = new FileChooser
    val result = chooser.showOpenDialog(null)
    try {
      if (result == FileChooser.Result.Approve)
        Source.fromFile(chooser.selectedFile).getLines.toList
      else List("")
    } finally {
      Source.fromFile(chooser.selectedFile).close
    }
   }
   def readMessage(input: File) = {
	    try {
	        Source.fromFile(input).getLines.toList
	    } finally {
	      Source.fromFile(input).close
	    }
   }
   
   /**
   * Formats a list of strings to output only a list of words
   * @input The file list of strings
   * @return A list of words
   */
   def getDirectoryListing(title: String = ""): Option[Array[File]] = {
    val chooser = new FileChooser(null)
    chooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    chooser.title = title
    val result = chooser.showOpenDialog(null)
    if (result == FileChooser.Result.Approve) {
      Some(chooser.selectedFile.listFiles())
    } else None
  }

   def getIndexWords(input: List[String]): List[String] = {
     var dictionary = List[String]()
     for (x <- input){
       dictionary ++= getWordsFromText(x)
     }
     return dictionary
   }
   
   def getWordsFromText(text: String): List[String] = {
    ("""[a-zA-Z]+""".r findAllIn text) toList
   }
   
   def main (args: Array[String]) {
	 val dictionary = getIndexWords(readMessage)	
	 val chapters = getDirectoryListing("Chapters").get
	 val mainActor = (new MainIndexer(chapters.length -1 )).start()
	 for(i <- 0 to (chapters.length - 1)){
	     val chapter = Source.fromFile(chapters(i)).getLines.toList
	     (new ProcessChapter(dictionary, chapter, (i+1), mainActor)).start()
	 }
   }
}