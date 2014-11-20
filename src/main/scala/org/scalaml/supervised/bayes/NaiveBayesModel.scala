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
package org.scalaml.supervised.bayes

import org.scalaml.stats.Stats
import NaiveBayesModel._



		/**
		 * <b>Abstract class for all the Naive Bayes model. The purpose of the model is to 
		 * classify a new set of observations.<br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>density</b>   Function used in computing the conditional probability <b>p(C|x)</b>
		 * </span></pre></p>
		 * @constructor Create a generic Naive Bayes Model using a specific density.
		 * @throws IllegalArgumentException if density function is undefined
		 * @author Patrick Nicolas
		 * @since March 8, 2014
		 * @note Scala for Machine Learning Chapter 5 Naive Bayes Models
		 */
abstract class NaiveBayesModel[T <% Double](val density: Density) {
	require(density != null, "Cannot compute conditional prob with NB for undefined prob density")
	def classify(values: Array[T]): Int
}


		/**
		 * <p>Companion object to the abstract NaiveBayesModel class. This singletong 
		 * is used to define the signature of the Density function.
		 */
object NaiveBayesModel {
	
		/**
		 * <p>Signature of the Density function used in Naive Bayes Model.
		 */
	type Density= (Double*) => Double
}


		/**
		 * <p>Implements the binomial (or 2-class) Naive Bayes model with a likelihood for positive and 
		 * negative outcome and for a specific density function.<br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>positives</b>   Priors for the class of positive outcomes.
		 * <b>negatives</b>   Priors for the class of negatives outcomes.
		 * <b>density</b>     Probability density function used in computing the conditional probability <b>p(C|x)</b>
		 * </span></pre></p>
		 * @constructor Instantiation of a Binomial Naive Bayes model. 
		 * @throws IllegalArgumentException if any of the class parameters is undefined
		 * @see org.scalaml.supervised.bayes.NaiveBayesModel
		 * 
		 * @author Patrick Nicolas
		 * @since February 11, 2014
		 * @note Scala for Machine Learning Chapter 5 Naive Bayes Models / Naive Bayes Classifiers
		 */
protected class BinNaiveBayesModel[T <% Double](positives: Likelihood[T], negatives: Likelihood[T], density: Density) 
			extends NaiveBayesModel[T](density) {
	require(positives !=null, "BinNaiveBayesModel Cannot classify an observation with undefine positive labels")
	require(negatives !=null, "BinNaiveBayesModel Cannot classify an observation with undefine negative labels")
    
		/**
		 * <p>Classify a new observation (features vector) using the Binomial Naive Bayes model.</p>
		 * @param x new observation
		 * @return 1 if the observation belongs to the positive class, 0 otherwise
		 * @throws IllegalArgumentException if any of the observation is undefined.
		 */
	override def classify(x: Array[T]): Int = {
		require(x != null && x.size > 0, "BinNaiveBayesModel.classify Cannot classify an undefined observation")
		if (positives.score(x, density) > negatives.score(x, density)) 1 else 0
	}
	
	override def toString: String = s"\nPositive cases: ${positives.toString}\nNegative cases: ${negatives.toString}"
}




		/**
		 * <p>Companion object for the Binomial Naive Bayes Model. This singleton is used
		 * to define the constructor of the BinNaiveBayesModel class.</p>
		 * 		 
		 * @author Patrick Nicolas
		 * @since February 11, 2014
		 * @note Scala for Machine Learning  Chapter 5 Naive Bayes Models / Naive Bayes Classifiers
		 */
object BinNaiveBayesModel {
	def apply[T <% Double](positives: Likelihood[T], negatives: Likelihood[T], density: Density): BinNaiveBayesModel[T] = 
		new BinNaiveBayesModel(positives, negatives, density)
}



		/**
		 * <p>Defines a Multi-class (or multinomial) Naive Bayes model for n classes.The number of classes is defined
		 * as likelihoodSet.size. The binomial Naive Bayes model, BinNaiveBayesModel, should be used for the 
		 * two class problem.<br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>likelihoodSet</b>  List of likelihood or priors for every classes in the model.
		 * <b>density</b>        Probability density function used in computing the conditional probability p(C|x).
		 * </span></pre></p>
		 * @constructor Instantiates a Multinomial Naive Bayes model (number classes > 2)
		 * @throws IllegalArgumentException if any of the class parameters is undefined
		 * 
		 * @author Patrick Nicolas
		 * @since February 11, 2014
		 * @note Scala for Machine Learning  Chapter 5 Naive Bayes Models / Naive Bayes Classifiers
		 */
protected class MultiNaiveBayesModel[T <% Double](likelihoodSet: List[Likelihood[T]], density: Density) 
					extends NaiveBayesModel[T](density) {
	require(likelihoodSet != null && likelihoodSet.size > 0, "MultiNaiveBayesModel Cannot classify using Multi-NB with undefined classes")
  
		/**
		 * <p>Classify a new observation (or vector) using the Multinomial Naive Bayes model.</p>
		 * @param x new observation
		 * @return the class ID the new observations has been classified.
		 * @throws IllegalArgumentException if any of the observation is undefined.
		 */
	override def classify(x: Array[T]): Int = {
		require(x != null && x.size > 0, "MultiNaiveBayesModel.classify Vector input is undefined")
		likelihoodSet.sortWith((p1, p2) => p1.score(x, density) > p2.score(x, density)).head.label
	}
}



		/**
		 * Companion object for the Multinomial Naive Bayes Model. The singleton
		 * is used to define the constructor of MultiNaiveBayesModel
		 * 
		 * @author Patrick Nicolas
		 * @since February 10, 2014
		 * @note Scala for Machine Learning
		 */
object MultiNaiveBayesModel {
	def apply[T <% Double](likelihoodSet: List[Likelihood[T]], density: Density): MultiNaiveBayesModel[T] = 
		new MultiNaiveBayesModel[T](likelihoodSet, density)
}


// --------------------------------  EOF --------------------------------------------------------------