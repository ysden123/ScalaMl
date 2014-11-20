/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.95e
 */
package org.scalaml.workflow.data


import org.scalaml.core.XTSeries
import org.scalaml.core.types.ScalaMl._
import org.scalaml.core.design.PipeOperator
import java.io.{FileNotFoundException, IOException, PrintWriter}
import scala.util.{Try, Success, Failure}
import org.apache.log4j.Logger
import org.scalaml.util.Display


		/**
		 * <p>Generic class to load or save files into either HDFS or local files system. The persistency
		 * of data is defined as a data transformation and therefore inherit from the PipeOperator<br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>sinkName</b>  Name of the storage (file, database, ..).
		 * </span></pre></p>
		 * @constructor Create a DataSink transform associated to a specific path name or database name. 		 
		 * @throws IllegalArgumentException if the name of the storage is undefined
		 *
		 * @author Patrick Nicolas
		 * @since December 15, 2013
		 * @note Scala for Machine Learning
		 */
final protected class DataSink[T <% String](sinkName: String) extends PipeOperator[List[XTSeries[T]], Int] {
	import XTSeries._, DataSource._
	import scala.io.Source
   
	require(sinkName != null && sinkName.length > 1, "DataSink Name of the storage is undefined")
   
	private val logger = Logger.getLogger("DataSink")
    
	
		/**
		 * <p>Write the content into the storage with sinkName as identifier.</p>
		 * @param content Stringized data to be stored
		 * @return true if the content has been successfully stored, false otherwise
		 * @throws IllegalArgumentException If the content is not defined.
		 */
	def write(content: String): Boolean = {
		require(content != null && content.length > 1, "DataSink.write content undefined")
		
		import java.io.{PrintWriter, IOException, FileNotFoundException}
     
		var printWriter: PrintWriter = null
		Try {
			printWriter = new PrintWriter(sinkName)
			printWriter.write(content)
			true
		} match {
			case Success(res) => res
			case Failure(e) => {
				Display.error("DataSink.write: failed writing into file", logger, e)
    		 
				if( printWriter != null) {
					Try { printWriter.close; false }
					match {
						case Success(res) => res
						case Failure(e) =>  Display.error("DataSink.write", logger, e); false
					}
				}
				else false
			}
		}
	}

   

		/**
		 * <p>Write the content of a vector into the storage with sinkName as identifier.</p>
		 * @param vector of type Double to be stored
		 * @return true if the vector has been successfully stored, false otherwise
		 * @throws IllegalArgumentException If the vector is either undefined or empty.
		 */
	def write(v: DblVector) : Boolean = {
		require(v != null && v.size > 0, "DataSink.write Cannot persist an undefined vector")
		
		val content = v.foldLeft(new StringBuilder)((b, x) => b.append(x).append(CSV_DELIM))
		content.setCharAt(content.size-1, ' ')
		this.write(content.toString.trim)
	}
	
		/**
		 * <p>Persists a set of time series into a predefined storage.</p>
		 * @throws MatchError if the list of time series is either undefined or empty
   		 * @return PartialFunction of a list of parameterized time series as input and the number of time series saved as output
		 */
	override def |> : PartialFunction[List[XTSeries[T]], Int] = {
		case xs: List[XTSeries[T]] if(xs != null && xs.length > 0) => {
			import java.io.{PrintWriter, IOException, FileNotFoundException}
			
			var printWriter: PrintWriter = null
			Try {
				val content = new StringBuilder
				val numValues = xs(0).size-1
				val last = xs.size-1

				var k = 0
				while( k < numValues) {
					val values = xs.toArray
					Range(0, last) foreach(j => content.append(s"${values(j)(k)},") )
					content.append(s"${values(last)(k)}\n")
					k += 1
				}
	   
				val printWriter = new PrintWriter(sinkName)
				printWriter.write(content.toString)
				k
			} 
			match {
				case Success(k) => k
				case Failure(e) => {
					Display.error("DataSink.|> ", logger, e)
	    		  
					if( printWriter != null) {
						Try {printWriter.close; 1 }
						match {
							case Success(res) => res
							case Failure(e) => Display.error("DataSink.|> ", logger, e)
						}
					}
					else Display.error("DataSink.|> no printWrite", logger)
				}
			}
		}
	}
}


		/**
		 * <p>Companion object to the class DataSink used to defined its constructor.</p>
		 */
object DataSink {
	import scala.annotation.implicitNotFound
  
		/**
		 * <p>Create a DataSink with an implicit conversion of the type parameter to a string.</p>
		 * @param sinkPath name of the storage.
		 */
	@implicitNotFound("Conversion of paramerized type to String for DataSink undefined")
	def apply[T](sinkPath: String)(implicit f: T => String= (t:T) => t.toString): DataSink[T] 
		= new DataSink[T](sinkPath)
}

// ----------------------------------   EOF ----------------------------------------------