package com.regnosys.rosetta.ui.autoedit

import org.eclipse.jface.text.IDocument
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider

class RosettaAutoEditStrategyProvider extends DefaultAutoEditStrategyProvider {

	override protected configure(IEditStrategyAcceptor acceptor) {
		acceptor.accept(singleLineTerminals.newInstance("<\"", "\">"), IDocument.DEFAULT_CONTENT_TYPE);
		super.configure(acceptor)
	}

}
