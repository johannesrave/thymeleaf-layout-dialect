/*
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.Decorator
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.TitlePatternProcessor
import nz.net.ultraq.thymeleaf.layoutdialect.models.ElementMerger
import nz.net.ultraq.thymeleaf.layoutdialect.models.ModelBuilder
import nz.net.ultraq.thymeleaf.layoutdialect.models.TitleExtractor

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel

import groovy.transform.TupleConstructor

/**
 * Decorator for the {@code <title>} part of the template to handle the special
 * processing required for the {@code layout:title-pattern} processor.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class HtmlTitleDecorator implements Decorator {

	final ITemplateContext context

	/**
	 * Special decorator for the {@code <title>} part, accumulates the important
	 * processing parts for the {@code layout:title-pattern} processor.
	 *
	 * @param targetTitleModel
	 * @param sourceTitleModel
	 * @return A new {@code <title>} model that is the result of decorating the
	 *   {@code <title>}s.
	 */
	@Override
	@SuppressWarnings('SpaceAroundOperator')
	IModel decorate(IModel targetTitleModel, IModel sourceTitleModel) {

		def modelBuilder = new ModelBuilder(context)
		def layoutDialectPrefix = context.getPrefixForDialect(LayoutDialect)

		// Get the title pattern to use
		def titlePatternProcessorRetriever = { titleModel ->
			return titleModel?.first()?.getAttribute(layoutDialectPrefix, TitlePatternProcessor.PROCESSOR_NAME)
		}
		def titlePatternProcessor =
			titlePatternProcessorRetriever(sourceTitleModel) ?:
			titlePatternProcessorRetriever(targetTitleModel) ?:
			null

		def resultTitle

		// Set the title pattern to use on a new model, as well as the important
		// title result parts that we want to use on the pattern.
		if (titlePatternProcessor) {
			def titleExtractor = new TitleExtractor(context)
			titleExtractor.extract(sourceTitleModel, TitlePatternProcessor.CONTENT_TITLE_KEY_OLD)
			titleExtractor.extract(targetTitleModel, TitlePatternProcessor.LAYOUT_TITLE_KEY_OLD)

			resultTitle = modelBuilder.build {
				title((titlePatternProcessor.attributeCompleteName): titlePatternProcessor.value)
			}
		}
		else {
			resultTitle = new ElementMerger(context).merge(targetTitleModel, sourceTitleModel)
		}

		return resultTitle
	}
}
