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
package org.scalaml.supervised.svm

import org.scalaml.core.design.Model
import libsvm.svm_model
import SVMModel._


		/**
		 * <p>Defined a model for support vector machine. The model is composed of the svm_model
		 * parameters of <b>LIBSVM</b> library and the accuracy computed during training.<br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>svmmodel</b>	Model parameters as defined in <b>LIBSVM</b>
		 * <b>accuracy</b>	Accuracy of the training process
		 * </span></pre></p>
		 * @constructor Create a SVMModel with a given LIBSVM model and accuracy
		 * @throws IllegalArgumentExceptin if LIBSVM model is undefined.
		 * @author Patrick Nicolas
		 * @since April 19, 2014
		 * @note Scala for Machine Learning  Chapter 8 Kernel models and support vector machines
		 */
final protected class SVMModel(val svmmodel: svm_model, val accuracy: Double) extends Model {
	require(svmmodel != null, "SVMModel LIBSVM model smmodel is undefined")
	
	val persists = "models/svm"
  	 
	override def toString: String = {
		val description = new StringBuilder("SVM model\n")
		val kernel_type: Int  = svmmodel.param.kernel_type
		description.append(s"${KernelTypes.get(kernel_type).get}\n")
  	  
		val nodes = svmmodel.SV
		if(nodes != null && nodes.size > 0) {
			description.append("SVM nodes:\n")
			nodes.foreach(w => {
				w.foreach(c => description.append(s"${c.index}->${c.value}, "))
				description.append("\n")
			})
		}
  	  
		val coefs = svmmodel.sv_coef
		if(coefs != null && coefs.size > 0) {
			description.append("SVM basis functions:\n")
			coefs.foreach(w => {
				w.foreach(c => description.append(s"$c,"))
				description.append("\n")
			})
		}
		description.append(s"Accuracy: $accuracy\n")
		description.toString
	}
}


		/**
		 * Companion object for the SVM model. This singleton defines
		 * the type and description of the kernel available to SVM
		 */
object SVMModel {
	val KernelTypes = Map[Int, String]( 
		0 -> "linear kernel", 
		1 -> "Polynomial kernel", 
		2 -> "RBF kernel", 
		3 -> "Sigmoid kernel", 
		4 -> "Custom kernel", 
		5 -> "Laplace kernel" )
}

// -------------------------  EOF ------------------------------