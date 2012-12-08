/*
 * Created on 20.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.jdom.Element;
import org.jdom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

/**
 * <p>
 * This static class contains toolkit method for soundbuses.
 * </p>
 * 
 * @author jonas.reese
 */
public class SoundbusToolkit {

    /**
     * Deserializes soundbus elements from the given <code>InputStream</code> to the
     * given <code>Soundbus</code>.
     * @param is The input stream.
     * @param soundbus The soundbus. Elements will be added and wired.
     * @param nodes A <code>List</code> to which added nodes shall be added. May be <code>null</code>
     * if the added nodes shall not be added to an extra list.
     * @throws IOException If an I/O error occurred while deserializing.
     * @throws SAXException If the given input stream contains invalid XML data.
     * @throws IllegalSoundbusDescriptionException If the given input stream contains invalid soundbus data.
     */
    @SuppressWarnings("unchecked")
    public static void deserializeSoundbusElements( InputStream is, Soundbus soundbus, List<SbNode> nodes )
    throws IOException, SAXException, IllegalSoundbusDescriptionException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse( is );
            Map<String,Object[]> idMap = new HashMap<String,Object[]>();
            Node soundbusNode = XPathAPI.selectSingleNode( doc, "soundbus" );
            Node tempoNode = soundbusNode.getAttributes().getNamedItem( "tempo" );
            if (tempoNode != null) {
                try {
                    soundbus.setTempo( Float.parseFloat( tempoNode.getNodeValue() ) );
                } catch (NumberFormatException nfex) {
                    nfex.printStackTrace();
                }
            }
            NodeIterator propertiesIter = XPathAPI.selectNodeIterator( soundbusNode, "properties/property" );
            if (propertiesIter != null) {
                for (Node prop = propertiesIter.nextNode(); prop != null; prop = propertiesIter.nextNode()) {
                    String propName = prop.getAttributes().getNamedItem( "name" ).getNodeValue();
                    String propValue = prop.getTextContent();
                    soundbus.putClientProperty( propName, propValue );
                }
            }
            NodeIterator nodesIter = XPathAPI.selectNodeIterator( soundbusNode, "nodes/node" );
            for (Node n = nodesIter.nextNode(); n != null; n = nodesIter.nextNode()) {
                NamedNodeMap nodeAttributes = n.getAttributes();
                String type = nodeAttributes.getNamedItem( "type" ).getNodeValue();
                String name = nodeAttributes.getNamedItem( "name" ).getNodeValue();
                String id = nodeAttributes.getNamedItem( "id" ).getNodeValue();
                NodeIterator parametersIter = XPathAPI.selectNodeIterator( n, "parameters/parameter" );
                Map<String,String> paramMap = new HashMap<String,String>();
                for (Node param = parametersIter.nextNode(); param != null; param = parametersIter.nextNode()) {
                    String paramName = param.getAttributes().getNamedItem( "name" ).getNodeValue();
                    String paramValue = param.getTextContent();
                    paramMap.put( paramName, paramValue );
                }
                List<PlugDescriptor> plugDescriptors = new ArrayList<PlugDescriptor>();
                NodeIterator inputsIter = XPathAPI.selectNodeIterator( n, "inputs/input" );
                for (Node inNode = inputsIter.nextNode(); inNode != null; inNode = inputsIter.nextNode()) {
                    NamedNodeMap nm = inNode.getAttributes();
                    String inType = nm.getNamedItem( "type" ).getNodeValue();
                    String inName = nm.getNamedItem( "name" ).getNodeValue();
                    String inId = nm.getNamedItem( "id" ).getNodeValue();
                    plugDescriptors.add( new PlugDescriptor( true, inType, inName, inId ) );
                }
                NodeIterator outputsIter = XPathAPI.selectNodeIterator( n, "outputs/output" );
                for (Node inNode = outputsIter.nextNode(); inNode != null; inNode = outputsIter.nextNode()) {
                    NamedNodeMap nm = inNode.getAttributes();
                    String outType = nm.getNamedItem( "type" ).getNodeValue();
                    String outName = nm.getNamedItem( "name" ).getNodeValue();
                    String outId = nm.getNamedItem( "id" ).getNodeValue();
                    plugDescriptors.add( new PlugDescriptor( false, outType, outName, outId ) );
                }
                propertiesIter = XPathAPI.selectNodeIterator( n, "properties/property" );
                HashMap<String, String> clientProperties = new HashMap<String, String>();
                for (Node prop = propertiesIter.nextNode(); prop != null; prop = propertiesIter.nextNode()) {
                    String propName = prop.getAttributes().getNamedItem( "name" ).getNodeValue();
                    String propValue = prop.getTextContent();
                    clientProperties.put( propName, propValue );
                }
                
                SbNode newNode = soundbus.addNode( type, paramMap, clientProperties, plugDescriptors );
                if (newNode != null) {
                    idMap.put( id, new Object[] { newNode, plugDescriptors } );
                    newNode.setName( name );
                    if (nodes != null) {
                        nodes.add( newNode );
                    }
                } // if (newNode != null)
            } // for (node declarations)
            
            // wire soundbus
            nodesIter = XPathAPI.selectNodeIterator( doc, "soundbus/connections/connection" );
            for (Node n = nodesIter.nextNode(); n != null; n = nodesIter.nextNode()) {
    
                Node inputNode = XPathAPI.selectSingleNode( n, "input" );
                Node outputNode = XPathAPI.selectSingleNode( n, "output" );
                NamedNodeMap inNm = inputNode.getAttributes();
                NamedNodeMap outNm = outputNode.getAttributes();
    
                String inNodeId = inNm.getNamedItem( "nodeId" ).getNodeValue();
                String inId = inNm.getNamedItem( "inputId" ).getNodeValue();
                String outNodeId = outNm.getNamedItem( "nodeId" ).getNodeValue();
                String outId = outNm.getNamedItem( "outputId" ).getNodeValue();
                
                Object[] in = idMap.get( inNodeId );
                Object[] out = idMap.get( outNodeId );
                if (in != null && out != null) {
                    SbNode inNode = (SbNode) in[0];
                    List<PlugDescriptor> inPds = (List<PlugDescriptor>) in[1];
                    
                    SbNode outNode = (SbNode) out[0];
                    List<PlugDescriptor> outPds = (List<PlugDescriptor>) out[1];
                    
                    SbOutput[] outputs = outNode.getOutputs();
                    SbInput[] inputs = inNode.getInputs();
                    
                    // create maps mapping inputType->Queue<SbInput> and outputType->Queue<SbOutput>
                    Map<String,Map<String, SbInput>> inputMap = new HashMap<String,Map<String, SbInput>>();
                    Map<String,Map<String, SbOutput>> outputMap = new HashMap<String,Map<String, SbOutput>>();
                    
                    for (int i = 0; i < inputs.length; i++) {
                        String type = getTypeForInOut( inputs[i] );
                        Map<String, SbInput> queue = inputMap.get( type );
                        if (queue == null) {
                            queue = new HashMap<String, SbInput>();
                            inputMap.put( type, queue );
                        }
                        if (inputs[i].getConnectedOutput() == null) {
                            queue.put( inputs[i].getInputId(), inputs[i] );
                        }
                    }
                    for (int i = 0; i < outputs.length; i++) {
                        String type = getTypeForInOut( outputs[i] );
                        Map<String, SbOutput> queue = outputMap.get( type );
                        if (queue == null) {
                            queue = new HashMap<String, SbOutput>();
                            outputMap.put( type, queue );
                        }
                        if (outputs[i].getConnectedInput() == null) {
                            queue.put( outputs[i].getOutputId(), outputs[i] );
                        }
                    }
                    
                    // find input plug
                    SbInput sbInput = null;
                    for (PlugDescriptor pd : inPds) {
                        if (pd.getId().equals( inId )) {
                            Map<String, SbInput> queue = inputMap.get( pd.getType() );
                            if (queue == null) {
                                throw new IllegalSoundbusDescriptionException(
                                        "No connector of type " + pd.getType() +
                                        " found on " + inNode.getName() );
                            }
                            String key = pd.getId();
                            sbInput = queue.get(key);
                            if (sbInput == null && queue.size() > 0) {
                                key = queue.keySet().iterator().next();
                                sbInput = queue.get(key);
                            }
                            if (sbInput == null) {
                                throw new IllegalSoundbusDescriptionException(
                                        "Too few connectors of type " + pd.getType() +
                                        " available on " + inNode.getName() );
                            }
                            queue.remove(key);
                            break;
                        }
                    }
                    // find output plug
                    SbOutput sbOutput = null;
                    for (PlugDescriptor pd : outPds) {
                        if (pd.getId().equals( outId )) {
                            Map<String, SbOutput> queue = outputMap.get( pd.getType() );
                            if (queue == null) {
                                throw new IllegalSoundbusDescriptionException(
                                        "No connector of type " + pd.getType() +
                                        " found on " + outNode.getName() );
                            }
                            String key = pd.getId();
                            sbOutput = queue.get(key);
                            if (sbOutput == null && queue.size() > 0) {
                                key = queue.keySet().iterator().next();
                                sbOutput = queue.get(key);
                            }
                            if (sbOutput == null) {
                                throw new IllegalSoundbusDescriptionException(
                                        "Too few connectors of type " + pd.getType() +
                                        " available on " + outNode.getName() );
                            }
                            queue.remove(key);
                            break;
                        }
                    }
                    
                    // check if input/output was resolved properly
                    if (sbInput == null) {
                        throw new IllegalSoundbusDescriptionException(
                                "Input connector with ID " + inId + " is not defined" );
                    }
                    if (sbOutput == null) {
                        throw new IllegalSoundbusDescriptionException(
                                "Output connector with ID " + outId + " is not defined" );
                    }
                    sbInput.connect( sbOutput );
                    sbOutput.connect( sbInput );
                }
            } // for (connections)
        } catch (ParserConfigurationException pex) {
            pex.printStackTrace();
            throw new IllegalSoundbusDescriptionException( pex );
        } catch (TransformerException tex) {
            tex.printStackTrace();
            throw new IllegalSoundbusDescriptionException( tex );
        } catch (UnknownNodeTypeException untex) {
            untex.printStackTrace();
            throw new IllegalSoundbusDescriptionException( untex );
        } catch (IllegalStateException isex) {
            isex.printStackTrace();
            throw new IllegalSoundbusDescriptionException( isex );
        } catch (CannotConnectException ccex) {
            ccex.printStackTrace();
            throw new IllegalSoundbusDescriptionException( ccex );
        }
    }
    
    /**
     * Serializes the given Soundbus nodes to the given XML root <code>Element</code>.
     * Please note that only nodes and no general soundbus properties will be serialized
     * by this method.
     * @param sbNodes The nodes to be serialized to the given <code>Element</code>.
     * @param root The root <code>Element</code>.
     * @throws IOException
     */
    public static void serializeSoundbusNodes( SbNode[] sbNodes, Element root ) throws IOException {
        Element nodes = new Element( "nodes" );
        Element connections = new Element( "connections" );
        try {
            for (int i = 0; i < sbNodes.length; i++) {
                Element node = new Element( "node" );
                node.setAttribute( "type", sbNodes[i].getType() );
                node.setAttribute( "name", sbNodes[i].getName() );
                node.setAttribute( "id", Long.toHexString( sbNodes[i].hashCode() ) );
                
                // parameters
                Map<String,String> parameterMap = sbNodes[i].getParameters();
                if (parameterMap != null && !parameterMap.isEmpty()) {
                    Element parameters = new Element( "parameters" );
                    for (String key : parameterMap.keySet()) {
                        Element elem = new Element( "parameter" );
                        elem.setAttribute( "name", key );
                        elem.setContent( new Text( parameterMap.get( key ) ) );
                        parameters.addContent( elem );
                    }
                    node.addContent( parameters );
                }
                
                // client properties
                Map<String,String> propertiesMap = sbNodes[i].getClientProperties();
                if (propertiesMap != null && !propertiesMap.isEmpty()) {
                    Element parameters = new Element( "properties" );
                    for (String key : propertiesMap.keySet()) {
                        Element elem = new Element( "property" );
                        elem.setAttribute( "name", key );
                        elem.setContent( new Text( propertiesMap.get( key ) ) );
                        parameters.addContent( elem );
                    }
                    node.addContent( parameters );
                }
                
                // inputs
                SbInput[] inputs = sbNodes[i].getInputs();
                if (inputs != null && inputs.length > 0) {
                    Element inputsElem = new Element( "inputs" );
                    for (int j = 0; j < inputs.length; j++) {
                        Element elem = new Element( "input" );
                        String id = inputs[j].getInputId();
                        elem.setAttribute( "type", getTypeForInOut( inputs[j] ) );
                        elem.setAttribute( "name", inputs[j].getName() );
                        elem.setAttribute( "id", id );
                        inputsElem.addContent( elem );
                        
                        // add connection if one exists
                        SbOutput output = inputs[j].getConnectedOutput();
                        if (output != null) {
                            Element connection = new Element( "connection" );
                            Element connectionInput = new Element( "input" );
                            connectionInput.setAttribute( "nodeId", Long.toHexString( sbNodes[i].hashCode() ) );
                            connectionInput.setAttribute( "inputId", id );
                            Element connectionOutput = new Element( "output" );
                            connectionOutput.setAttribute(
                                    "nodeId", Long.toHexString( output.getSbNode().hashCode() ) );
                            connectionOutput.setAttribute( "outputId", output.getOutputId() );
                            connection.addContent( connectionInput );
                            connection.addContent( connectionOutput );
                            connections.addContent( connection );
                        }
                    }
                    node.addContent( inputsElem );
                }
                
                // outputs
                SbOutput[] outputs = sbNodes[i].getOutputs();
                if (outputs != null && outputs.length > 0) {
                    Element outputsElem = new Element( "outputs" );
                    for (int j = 0; j < outputs.length; j++) {
                        Element elem = new Element( "output" );
                        elem.setAttribute( "type", getTypeForInOut( outputs[j] ) );
                        elem.setAttribute( "name", outputs[j].getName() );
                        elem.setAttribute( "id", outputs[j].getOutputId() );
                        outputsElem.addContent( elem );
                    }
                    node.addContent( outputsElem );
                }
                
                nodes.addContent( node );
            } // for (serialize nodes)
            root.addContent( nodes );

            // serialize connections tree that has been created
            // in nodes serialization loop, but only if not empty
            List<?> connectionsContent = connections.getContent();
            if (connectionsContent != null && !connectionsContent.isEmpty()) {
                root.addContent( connections );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException( ex.getMessage() );
        }
    }

    
    private static String getTypeForInOut( Object inOut ) {
        if (inOut instanceof SbMidiInput ||
                inOut instanceof SbMidiOutput) {
            return "midi";
        }
        if (inOut instanceof SbAudioInput ||
                inOut instanceof SbAudioOutput) {
            return "audio";
        }
        return "unknown";
    }
}
