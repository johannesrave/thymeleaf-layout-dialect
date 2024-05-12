/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.thymeleaf.layoutdialect.decorators.html

import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.xml.XmlDocumentDecorator

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.ICloseElementTag
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag

/**
 * A decorator made to work over an HTML document.  Decoration for a document
 * involves 2 sub-decorators: a special one for the {@code <head>} element, and
 * a standard one for the {@code <body>} element.
 *
 * @author Emanuel Rabina
 */
class HtmlDocumentDecorator extends XmlDocumentDecorator {

	final SortingStrategy sortingStrategy
	final boolean autoHeadMerging
	final boolean newTitleTokens

	/**
	 * Constructor, builds a decorator with the given configuration.
	 */
	HtmlDocumentDecorator(ITemplateContext context, SortingStrategy sortingStrategy, boolean autoHeadMerging,
		boolean newTitleTokens) {

		super(context)
		this.sortingStrategy = sortingStrategy
		this.autoHeadMerging = autoHeadMerging
		this.newTitleTokens = newTitleTokens
	}

	@Override
	IModel decorate(IModel targetDocumentModel, IModel sourceDocumentModel) {

		def modelFactory = context.modelFactory
		def resultDocumentModel = targetDocumentModel.cloneModel()

		// Head decoration
		def headModelFinder = { event -> event.isOpeningElementOf('head') }
		if (autoHeadMerging) {
			def targetHeadModel = resultDocumentModel.findModel(headModelFinder)
			def resultHeadModel = new HtmlHeadDecorator(context, sortingStrategy, newTitleTokens)
				.decorate(targetHeadModel, sourceDocumentModel.findModel(headModelFinder))
			if (resultHeadModel) {
				if (targetHeadModel) {
					resultDocumentModel.replaceModel(resultDocumentModel.findIndexOfModel(targetHeadModel), resultHeadModel)
				}
				else {
					resultDocumentModel.insertModelWithWhitespace(resultDocumentModel.findIndexOf { event ->
						return (event instanceof IOpenElementTag && event.elementCompleteName == 'body') ||
							(event instanceof ICloseElementTag && event.elementCompleteName == 'html')
					} - 1, resultHeadModel, modelFactory)
				}
			}
		}
		else {
			// TODO: If autoHeadMerging is false, this really shouldn't be needed as
			//       the basis for `resultDocumentModel` should be the source model.
			//       This 'hack' is OK for an experimental option, but the fact that
			//       it exists means I should rethink how the result model is made.
			resultDocumentModel.replaceModel(
				resultDocumentModel.findIndexOf(headModelFinder),
				sourceDocumentModel.findModel(headModelFinder)
			)
		}

		// Body decoration
		def bodyModelFinder = { event ->
			return event instanceof IOpenElementTag && event.elementCompleteName == 'body'
		}
		def targetBodyModel = resultDocumentModel.findModel(bodyModelFinder)
		def resultBodyModel = new HtmlBodyDecorator(context).decorate(
			targetBodyModel,
			sourceDocumentModel.findModel(bodyModelFinder)
		)
		if (resultBodyModel) {
			if (targetBodyModel) {
				resultDocumentModel.replaceModel(resultDocumentModel.findIndexOfModel(targetBodyModel), resultBodyModel)
			}
			else {
				resultDocumentModel.insertModelWithWhitespace(resultDocumentModel.findIndexOf { event ->
					return event.isClosingElementOf('html')
				} - 1, resultBodyModel, modelFactory)
			}
		}

		return super.decorate(resultDocumentModel, sourceDocumentModel)
	}
}
