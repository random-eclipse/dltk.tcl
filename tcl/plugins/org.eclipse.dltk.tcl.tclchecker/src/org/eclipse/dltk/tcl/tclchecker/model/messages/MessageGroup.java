/**
 * <copyright>
 * </copyright>
 *
 * $Id: MessageGroup.java,v 1.1 2009/01/27 18:43:47 apanchenk Exp $
 */
package org.eclipse.dltk.tcl.tclchecker.model.messages;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Message Group</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageGroup#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageGroup#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageGroup#getMessages <em>Messages</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getMessageGroup()
 * @model
 * @generated
 */
public interface MessageGroup extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getMessageGroup_Id()
	 * @model id="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageGroup#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getMessageGroup_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.messages.MessageGroup#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Messages</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.dltk.tcl.tclchecker.model.messages.CheckerMessage}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Messages</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Messages</em>' containment reference list.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.messages.MessagesPackage#getMessageGroup_Messages()
	 * @model containment="true"
	 *        extendedMetaData="name='message'"
	 * @generated
	 */
	EList<CheckerMessage> getMessages();

} // MessageGroup
