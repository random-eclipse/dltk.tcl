/**
 * <copyright>
 * </copyright>
 *
 * $Id: CheckerConfig.java,v 1.1 2009/02/17 11:52:10 apanchenk Exp $
 */
package org.eclipse.dltk.tcl.tclchecker.model.configs;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Checker Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isSummary <em>Summary</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getCommandLineOptions <em>Command Line Options</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getMode <em>Mode</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getMessageStates <em>Message States</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isUseTclVer <em>Use Tcl Ver</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isIndividualMessageStates <em>Individual Message States</em>}</li>
 *   <li>{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isReadOnly <em>Read Only</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig()
 * @model
 * @generated
 */
public interface CheckerConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Summary</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Summary</em>' attribute.
	 * @see #setSummary(boolean)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_Summary()
	 * @model
	 * @generated
	 */
	boolean isSummary();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isSummary <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Summary</em>' attribute.
	 * @see #isSummary()
	 * @generated
	 */
	void setSummary(boolean value);

	/**
	 * Returns the value of the '<em><b>Command Line Options</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command Line Options</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command Line Options</em>' attribute.
	 * @see #setCommandLineOptions(String)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_CommandLineOptions()
	 * @model
	 * @generated
	 */
	String getCommandLineOptions();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getCommandLineOptions <em>Command Line Options</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command Line Options</em>' attribute.
	 * @see #getCommandLineOptions()
	 * @generated
	 */
	void setCommandLineOptions(String value);

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
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Mode</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerMode}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mode</em>' attribute.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerMode
	 * @see #setMode(CheckerMode)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_Mode()
	 * @model
	 * @generated
	 */
	CheckerMode getMode();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#getMode <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mode</em>' attribute.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerMode
	 * @see #getMode()
	 * @generated
	 */
	void setMode(CheckerMode value);

	/**
	 * Returns the value of the '<em><b>Message States</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link org.eclipse.dltk.tcl.tclchecker.model.configs.MessageState},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message States</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message States</em>' map.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_MessageStates()
	 * @model mapType="org.eclipse.dltk.tcl.tclchecker.model.configs.MessageStateMap<org.eclipse.emf.ecore.EString, org.eclipse.dltk.tcl.tclchecker.model.configs.MessageState>"
	 * @generated
	 */
	EMap<String, MessageState> getMessageStates();

	/**
	 * Returns the value of the '<em><b>Use Tcl Ver</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Use Tcl Ver</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Use Tcl Ver</em>' attribute.
	 * @see #setUseTclVer(boolean)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_UseTclVer()
	 * @model default="true"
	 * @generated
	 */
	boolean isUseTclVer();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isUseTclVer <em>Use Tcl Ver</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Use Tcl Ver</em>' attribute.
	 * @see #isUseTclVer()
	 * @generated
	 */
	void setUseTclVer(boolean value);

	/**
	 * Returns the value of the '<em><b>Individual Message States</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Individual Message States</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Individual Message States</em>' attribute.
	 * @see #setIndividualMessageStates(boolean)
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_IndividualMessageStates()
	 * @model
	 * @generated
	 */
	boolean isIndividualMessageStates();

	/**
	 * Sets the value of the '{@link org.eclipse.dltk.tcl.tclchecker.model.configs.CheckerConfig#isIndividualMessageStates <em>Individual Message States</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Individual Message States</em>' attribute.
	 * @see #isIndividualMessageStates()
	 * @generated
	 */
	void setIndividualMessageStates(boolean value);

	/**
	 * Returns the value of the '<em><b>Read Only</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Read Only</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Read Only</em>' attribute.
	 * @see org.eclipse.dltk.tcl.tclchecker.model.configs.ConfigsPackage#getCheckerConfig_ReadOnly()
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	boolean isReadOnly();

} // CheckerConfig