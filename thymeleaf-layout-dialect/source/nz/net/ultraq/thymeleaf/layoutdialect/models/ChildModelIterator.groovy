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

package nz.net.ultraq.thymeleaf.layoutdialect.models

import org.thymeleaf.model.IModel

/**
 * An iterator that works with a model's immediate children, returning each one
 * as a model of its own.
 *
 * @author Emanuel Rabina
 */
class ChildModelIterator implements Iterator<IModel> {

	private final IModel parent
	private int currentIndex = 1  // Starts after the root element

	ChildModelIterator(IModel parent) {
		this.parent = parent
	}

	@Override
	boolean hasNext() {
		return currentIndex < (parent.size() - 1)
	}

	@Override
	IModel next() {
		def subModel = parent.getModel(currentIndex)
		currentIndex += subModel.size()
		return subModel
	}
}
