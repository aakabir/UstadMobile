package com.ustadmobile.port.gwt.xmlpull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.*;

/**
 * GWT implementation of XmlPullParser 
 * @author varuna
 *
 */
public class XmlPullParserGWT implements XmlPullParser {
	
	Document xmlDoc;
	
	String xmlStr;
	
	String inputEncoding = UTF8;
	
	int currentDepth = -1;
	
	int lineNumber = -1;

	private ArrayList<PositionEntry> treeStack;
	
	private int initStage = 0;
	
	private boolean docStarted = false;
	
	private boolean rootElStarted = false;
	
	private boolean rootElFirstChildStarted = false;
	
	private boolean FEATURE_PROCESS_NAMESPACES = false;
	public static final String FEATURE_PROCESS_NAMESPACES_TEXT = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
	private boolean FEATURE_REPORT_NAMESPACE_ATTRIBUTES = false;
	public static final String FEATURE_REPORT_NAMESPACE_ATTRIBUTES_TEXT = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";
	private boolean FEATURE_PROCESS_DOCDECL = false;
	public static final String FEATURE_PROCESS_DOCDECL_TEXT = "http://xmlpull.org/v1/doc/features.html#process-docdecl";
	private boolean FEATURE_VALIDATION = false;
	public static final String FEATURE_VALIDATION_TEXT = "http://xmlpull.org/v1/doc/features.html#validation";
	private boolean FEATURE_XML_ROUNDTRIP = false;
	public static final String FEATURE_XML_ROUNDTRIP_TEXT = "http://xmlpull.org/v1/doc/features.html#xml-roundtrip";
	
	private static final int INIT_DOC_START = 0;
	
	private static final int INIT_ROOT_EL = 1;
	
	private static final int INIT_ROOT_EL_FIRST_CHILD = 2;
	
	private static final int INIT_DONE = 3;
	
	private static final String UTF8 = "UTF-8";
	
	private static class PositionEntry {
		
		Element element;
		
		/**
		 * Where an element has n children
		 * 
		 * 0-(n-1) child elements
		 * n = end_tag event
		 */
		int eventIndex;
		
		private PositionEntry(Element element, int eventIndex) {
			this.element = element;
			this.eventIndex = eventIndex;
		}
		
		int getEventType() {
			if(eventIndex == element.getChildNodes().getLength()) {
				return XmlPullParser.END_TAG;
			}else {
				Node evtNode = element.getChildNodes().item(eventIndex);
				if(evtNode.getNodeType() == Node.TEXT_NODE) {
					return XmlPullParser.TEXT;
				}else if(evtNode.getNodeType() == Node.ELEMENT_NODE) {
					return XmlPullParser.START_TAG;
				}
			}
			
			return -1;
		}
		
		Element getCurrentElement() {
			if(eventIndex == element.getChildNodes().getLength()) {
				return element;
			}else{
				
				try{
					//TODO: Throw xml pull parse exception if we're not on an element
					NodeList elementNodeList = element.getChildNodes();
					Node elementItem = elementNodeList.item(eventIndex);
					Element nodeToElement = (Element)elementItem;
					return nodeToElement;
					//return (Element)element.getChildNodes().item(eventIndex);		
				}catch(Exception e){
					GWT.log("XmlPullParserGWT EXCEOPTION in getCurrnetElement(): " +e.toString());
					
					return null;
				}
						
			}
		}
		
		Node getCurrentNode() {
			//TODO: Add checks. 
			return element.getChildNodes().item(eventIndex);
		}
		
	}
	
	public XmlPullParserGWT(){
		treeStack = new ArrayList<>();
	}
	
	public XmlPullParserGWT(String xmlStr) {
		// parse the XML document into a DOM
	    Document xmlDoc = XMLParser.parse(xmlStr);
	    this.xmlDoc = xmlDoc;
	    this.xmlStr = xmlStr;
	    
	    treeStack = new ArrayList<>();
	    treeStack.add(new PositionEntry(xmlDoc.getDocumentElement(), 0));
	}
	
	public XmlPullParserGWT(InputStream xmlStream) {
		String xmlStr = "";
		//Convert stream to string.

		// parse the XML document into a DOM
	    Document xmlDoc = XMLParser.parse(xmlStr);
	    this.xmlDoc = xmlDoc;
	    this.xmlStr = xmlStr;
	    treeStack = new ArrayList<>();
	    treeStack.add(new PositionEntry(xmlDoc.getDocumentElement(), 0));
	}
	
	
	@Override
	public void setFeature(String name, boolean state) throws XmlPullParserException {
		switch(name){
			case FEATURE_PROCESS_NAMESPACES_TEXT:
				FEATURE_PROCESS_NAMESPACES = state;
				break;
			case FEATURE_REPORT_NAMESPACE_ATTRIBUTES_TEXT:
				FEATURE_REPORT_NAMESPACE_ATTRIBUTES = state;
				break;
			case FEATURE_PROCESS_DOCDECL_TEXT:
				FEATURE_PROCESS_DOCDECL = state;
				break;
			case FEATURE_VALIDATION_TEXT:
				FEATURE_VALIDATION = state;
				break;
			case FEATURE_XML_ROUNDTRIP_TEXT:
				FEATURE_XML_ROUNDTRIP = state;
				break;
			default:
				GWT.log("Unknown Feature");
				break;
		}
	}

	@Override
	public boolean getFeature(String name) {
		switch(name){
			case FEATURE_PROCESS_NAMESPACES_TEXT:
				return FEATURE_PROCESS_NAMESPACES;
			case FEATURE_REPORT_NAMESPACE_ATTRIBUTES_TEXT:
				return FEATURE_REPORT_NAMESPACE_ATTRIBUTES;
			case FEATURE_PROCESS_DOCDECL_TEXT:
				return FEATURE_PROCESS_DOCDECL;
			case FEATURE_VALIDATION_TEXT:
				return FEATURE_VALIDATION;
			case FEATURE_XML_ROUNDTRIP_TEXT:
				return FEATURE_XML_ROUNDTRIP;
			default:
				GWT.log("Unknown Feature");
				return false;
		}
	}

	@Override
	public void setProperty(String name, Object value) throws XmlPullParserException {
		//this.xmlDoc  . getProperty ??
		GWT.log("ERROR: setProperty() Unimplemented.");
	}

	@Override
	public Object getProperty(String name) {
		//this.xmlDoc  .setProperty ??
		GWT.log("ERROR: Unimplemented.");
		return null;
	}

	@Override
	public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
		GWT.log("WARNING: setInput() not tested.");
		String xmlStr = "";
		//Convert stream to string. TODO

		// parse the XML document into a DOM
	    Document xmlDoc = XMLParser.parse(xmlStr);
	    this.xmlDoc = xmlDoc;
	    this.xmlStr = xmlStr;
	    treeStack = new ArrayList<>();
	    treeStack.add(new PositionEntry(xmlDoc.getDocumentElement(), 0));
	}

	@Override
	public String getInputEncoding() {
		GWT.log("WARNING: getInputEncoding() Not tested.");
		return this.inputEncoding;
	}

	@Override
	public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
		// TODO Auto-generated method stub
		GWT.log("ERROR: defineEntityReplacementText() not implemented. Dunno what this does.");
	}

	//Returns the numbers of elements in the namespace stack 
	//for the given depth. If namespaces are not enabled, 0 is returned. 
	@Override
	public int getNamespaceCount(int depth) throws XmlPullParserException {
		// TODO Auto-generated method stub
		GWT.log("ERROR: Unimplemented.");
		return 0;
	} 

	/**
	 * Returns the namespace prefix for the given position in the namespace stack. 
	 * Default namespace declaration (xmlns='...') will have null as prefix. 
	 * If the given index is out of range, an exception is thrown. 
	 */
	@Override
	public String getNamespacePrefix(int pos) throws XmlPullParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Returns the namespace URI for the given position in the namespace 
	 * stack If the position is out of range, an exception is thrown. 
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespaceUri(int)
	 */
	@Override
	public String getNamespaceUri(int pos) throws XmlPullParserException {
		GWT.log("WARNING: getNamespaceUri(int pos) Not tested. Probably wrong.");
		//TODO: Check this logic. Probably wrong.
		return getCurrentElement().getAttributes().item(pos).getNamespaceURI();
	}

	//Returns the URI corresponding to the given prefix, depending on current state of the parser. 
	//eg:		<element:name xmlns:prefix="namespaceuri">
	@Override
	public String getNamespace(String prefix) {
		GWT.log("ERROR: getNamespace(String prefix) Not Implemented Properly");
		String currentNamespaceUri = getCurrentElement().getNamespaceURI();
		return currentNamespaceUri;
	}

	@Override
	public int getDepth() {
		GWT.log("WARNING: getDepth() Not tested.");
		return this.currentDepth;
	}

	@Override
	public String getPositionDescription() {
		// TODO Auto-generated method stub
		GWT.log("Error: getPositionDescription Unimplemented.");
		return null;
	}

	@Override
	public int getLineNumber() {
		// TODO Auto-generated method stub
		GWT.log("Error: getLineNumber Unimplemented.");
		return 0;
	}

	@Override
	public int getColumnNumber() {
		// TODO Auto-generated method stub
		GWT.log("Error: getColumnNumber Unimplemented.");
		return 0;
	}

	@Override
	public boolean isWhitespace() throws XmlPullParserException {
		// TODO Auto-generated method stub
		GWT.log("Error: isWhitespace Unimplemented.");
		return false;
	}

	@Override
	public String getText() {
		return getCurrentPosition().getCurrentNode().getNodeValue();		
	}

	@Override
	public char[] getTextCharacters(int[] holderForStartAndLength) {
		// TODO Auto-generated method stub
		GWT.log("Error: getTextCharacters Unimplemented.");
		return null;
	}

	/*
	 * Returns the namespace URI of the current element. 
	 * The default namespace is represented as empty string. 
	 * If namespaces are not enabled, an empty String ("") is always returned. 
	 * The current event must be START_TAG or END_TAG; otherwise, null is returned. (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespace()
	 */
	@Override
	public String getNamespace() {
		GWT.log("WARNING: getNamespace(String prefix) Not Tested.");
		int currentEvent = -1;
		try {
			currentEvent = getEventType();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			GWT.log("getNamespace(): Unable to get event type.");
			return null;
		}
		if (currentEvent != XmlPullParser.START_TAG || 
				currentEvent != XmlPullParser.END_TAG){
			return null;
		}
		if(!FEATURE_PROCESS_NAMESPACES){
			return "";
		}
		return getCurrentElement().getNamespaceURI();
		
	}

	@Override
	public String getName() {
		Element currentElement = getCurrentElement();
		if(currentElement != null){
			return currentElement.getNodeName();
		}
		else{
			return "";
		}
		//return getCurrentElement().getNodeName();
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		if(!FEATURE_PROCESS_NAMESPACES){
			return null;
		}
		try {
			if(getEventType() != XmlPullParser.START_TAG || getEventType() != XmlPullParser.END_TAG){
				return null;
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			GWT.log("Exception in getPrefix()");
		}
		return getCurrentElement().getPrefix();
	}

	@Override
	public boolean isEmptyElementTag() throws XmlPullParserException {
		GWT.log("WARNING: isEmptyElementTag not tested.");
		if (getEventType() != XmlPullParser.START_TAG){
			throw new XmlPullParserException("isEmptyElementTag(): Not a start tag");
		}
		if(getCurrentElement().getAttributes().getLength() == 0){
			return true;
		}else{
			return false;
		}
	}

	//Returns the number of attributes of the current start tag, 
	// or -1 if the current event type is not START_TAG
	@Override
	public int getAttributeCount() {
		GWT.log("WARNING: getAttributeCount() not tested.");
		try {
			if(getEventType() != XmlPullParser.START_TAG){
				return -1;
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getCurrentElement().getAttributes().getLength();
		
	}

	/*Returns the namespace URI of the attribute with the given index (starts from 0). 
	 * Returns an empty string ("") if namespaces are not enabled or the attribute has 
	 * no namespace. Throws an IndexOutOfBoundsException if the index is out of range 
	 * or the current event type is not START_TAG. 
	*/
	@Override
	public String getAttributeNamespace(int index) {
		GWT.log("WARNING: getAttributeNamespace() Not Tested.");
		if(!FEATURE_PROCESS_NAMESPACES){
			return "";
		}
		try {
			if(getEventType() != XmlPullParser.START_TAG){
				throw new IndexOutOfBoundsException("getAttributeNamespace(): Not START_TAG");
			}else{
				return getCurrentElement().getAttributes().item(index).getNamespaceURI();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			GWT.log("getAttributeNamespace() : Exception getting event type.");
			return null;
		}
	}

	@Override
	public String getAttributeName(int index) {
		GWT.log("getAttributeName() not tested.");
		if(!FEATURE_PROCESS_NAMESPACES){
			return "";
		}
		NamedNodeMap currentElementAttributes = getCurrentElement().getAttributes();
		String nodeName = currentElementAttributes.item(index).getNodeName();
		// TODO Auto-generated method stub
		return nodeName;
	}

	@Override
	public String getAttributePrefix(int index) {
		GWT.log("getAttributePrefix() not tested.");
		// Throws an IndexOutOfBoundsException if the index is out of range or current event type is not START_TAG.
		try {
			if(getEventType() != XmlPullParser.START_TAG){
				throw new IndexOutOfBoundsException("getAttributePrefix: no a START_TAG");
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getCurrentElement().getAttributes().item(index).getPrefix();
		
	}

	//Returns the type of the specified attribute If parser is non-validating it MUST return CDATA. 
	//wut?
	@Override
	public String getAttributeType(int index) {
		GWT.log("ERROR: getAttributeType() : Not implemented.");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAttributeDefault(int index) {
		GWT.log("ERROR: isAttributeDefault() : Not implemented.");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAttributeValue(int index) {
		GWT.log("WARNING: getAttributeValue() Not tested.");
		//Gets the index'th attribute value for this element
		NamedNodeMap currentElementNodeMap = getCurrentElement().getAttributes();
		Node currentItem = currentElementNodeMap.item(index);
		String currentAttributeValueAtIndexValue = currentItem.getNodeValue();
		return currentAttributeValueAtIndexValue;
	}

	@Override
	public String getAttributeValue(String namespace, String name) {
		GWT.log("WARNING: getAttributeValue() not tested");
		return getCurrentElement().getAttribute(name);
	}

	@Override
	public int getEventType() throws XmlPullParserException {
		if(treeStack.isEmpty())
			return XmlPullParser.END_DOCUMENT;
		
		PositionEntry currentPosition = this.treeStack.get(treeStack.size()-1);
		return currentPosition.getEventType();
	}

	@Override
	public int next() throws XmlPullParserException, IOException {
		if(!docStarted) {
			docStarted = true;
			return XmlPullParser.START_DOCUMENT;
		}else if(!rootElStarted) {
			rootElStarted = true;
			//this.currentDepth++;
			return XmlPullParser.START_TAG;
		}else if(!rootElFirstChildStarted) {
			rootElFirstChildStarted = true;
			this.currentDepth++;
			return XmlPullParser.START_TAG;
		}
		
		PositionEntry currentPosition = this.treeStack.get(treeStack.size()-1);
		Element currentEl = currentPosition.element;
		
		if(currentPosition.getEventType() == XmlPullParser.START_TAG) {
			PositionEntry posStackEntry = new PositionEntry(
					(Element)currentEl.getChildNodes().item(currentPosition.eventIndex), 0);
			currentPosition.eventIndex++;
			treeStack.add(posStackEntry);
			this.currentDepth++;
		}else if(currentPosition.eventIndex == currentEl.getChildNodes().getLength()){
			treeStack.remove(treeStack.size()-1);
			this.currentDepth--;
			if(treeStack.isEmpty()){
				//end of document
				return XmlPullParser.END_DOCUMENT;	
			}		
		}else {
			currentPosition.eventIndex++;
		}
		
		PositionEntry nextPosition = this.treeStack.get(treeStack.size()-1);
		
		return nextPosition.getEventType();
	}

	@Override
	public int nextToken() throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		GWT.log("ERROR: nextToken() not implemented");
		return 0;
	}

	@Override
	public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
		GWT.log("WARNING: require not tested completely");
		if (type != getEventType()
		  || (namespace != null &&  !namespace.equals( getNamespace () ) )
		  || (name != null &&  !name.equals( getName() ) ) ){
		     throw new XmlPullParserException( "expected "+ TYPES[ type ]+getPositionDescription());
		}

	}

	//If current event is START_TAG then if next element is TEXT 
	//then element content is returned or if next event is END_TAG 
	//then empty string is returned, otherwise exception is thrown. 
	//After calling this function successfully parser will be 
	//positioned on END_TAG. 
	@Override
	public String nextText() throws XmlPullParserException, IOException {
		GWT.log("WARNING: nextText() Not tested.");
		
		if(getEventType() == XmlPullParser.END_TAG){
			return "";
		}
		if (getEventType() != XmlPullParser.START_TAG){
			throw new XmlPullParserException("nextText(): evt not START_TAG");
		}else{
			int childLen = getCurrentElement().getChildNodes().getLength();
			if(childLen == 1){
				if(getCurrentElement().getChildNodes().item(0).getNodeType() == Node.TEXT_NODE){
					String text = getCurrentElement().getChildNodes().item(0).getNodeValue();
					this.next();
					if(getEventType() != XmlPullParser.END_TAG){
						throw new XmlPullParserException("nextText(): got text, went next but still not end tag.");
					}
					return text;
				}
			}else{
				throw new XmlPullParserException("nextText(): multiple nodes found for nextText()");
			}
			
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int nextTag() throws XmlPullParserException, IOException {
		GWT.log("WARNING: nextTag() not tested.");
		int eventType = next();
		if(eventType == TEXT &&  isWhitespace()) {   // skip whitespace
	   		eventType = next();
   		}
   		if (eventType != START_TAG &&  eventType != END_TAG) {
	   		throw new XmlPullParserException("expected start or end tag", this, null);
   		}
   		return eventType;

	}
	
	protected PositionEntry getCurrentPosition() {
		return treeStack.get(treeStack.size()-1);
	}
	
	protected Element getCurrentElement() {
		if(docStarted && !rootElFirstChildStarted) {
			Element treeStackElement = this.treeStack.get(0).element;
			return treeStackElement;
			//return this.treeStack.get(0).element;
		}else {
			PositionEntry currentPosition = getCurrentPosition();
			return currentPosition.getCurrentElement();
		}
	}

	/* ERROR : Reader not part of GWT. ignoring.

	@Override
	public void setInput(Reader arg0) throws XmlPullParserException {
		// TODO Auto-generated method stub
		GWT.log("WARNING: setInput(Reader arg0) NOT IMPLEMENTED BECAUSE READER NOT PART OF GWT JRE EMULATION!");
	}
	*/

}
