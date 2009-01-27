/**
 * <copyright>
 * </copyright>
 *
 * $Id: CheckerMessage.java,v 1.1 2009/01/27 18:43:47 apanchenk Exp $
 */
package org.eclipse.dltk.tcl.tclchecker.model.messages;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Checker Message</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getMessageId <em>Message Id</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getExplanation <em>Explanation</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getCategory <em>Category</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getCheckerMessage()
 * @model extendedMetaData="name='message'"
 * @generated
 */
public interface CheckerMessage extends EObject {
	/**
	 * Returns the value of the '<em><b>Message Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message Id</em>' attribute.
	 * @see #setMessageId(String)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getCheckerMessage_MessageId()
	 * @model
	 * @generated
	 */
	String getMessageId();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getMessageId <em>Message Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message Id</em>' attribute.
	 * @see #getMessageId()
	 * @generated
	 */
	void setMessageId(String value);

	/**
	 * Returns the value of the '<em><b>Explanation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Explanation</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Explanation</em>' attribute.
	 * @see #setExplanation(String)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getCheckerMessage_Explanation()
	 * @model
	 * @generated
	 */
	String getExplanation();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getExplanation <em>Explanation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Explanation</em>' attribute.
	 * @see #getExplanation()
	 * @generated
	 */
	void setExplanation(String value);

	/**
	 * Returns the value of the '<em><b>Category</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageCategory}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Category</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Category</em>' attribute.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessageCategory
	 * @see #setCategory(MessageCategory)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getCheckerMessage_Category()
	 * @model
	 * @generated
	 */
	MessageCategory getCategory();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage#getCategory <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' attribute.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessageCategory
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(MessageCategory value);

} // CheckerMessage
