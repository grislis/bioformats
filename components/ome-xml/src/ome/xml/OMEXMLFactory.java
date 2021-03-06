//
// OMEXMLFactory.java
//

/*
 * ome.xml.OMEXMLFactory
 *
 *-----------------------------------------------------------------------------
 *
 *  Copyright (C) 2007-2008 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee,
 *      University of Wisconsin-Madison
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *-----------------------------------------------------------------------------
 */

/*-----------------------------------------------------------------------------
 *
 * Written by:    Curtis Rueden <ctrueden@wisc.edu>
 *
 *-----------------------------------------------------------------------------
 */

package ome.xml;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * OMEXMLFactory is a factory for creating OME-XML node hierarchies.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/ome-xml/src/ome/xml/OMEXMLFactory.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/ome-xml/src/ome/xml/OMEXMLFactory.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public final class OMEXMLFactory {

  // -- Constants --

  /** Latest OME-XML version namespace. */
  public static final String LATEST_VERSION = "2012-06";

  /** Basic skeleton for an OME-XML root node with modern schema. */
  protected static final String SKELETON =
    "<?xml version=\"1.0\"?>\n" +
    "<OME xmlns=\"http://www.openmicroscopy.org/Schemas/OME/VERSION\" " +
    "xmlns:Bin=\"http://www.openmicroscopy.org/Schemas/BinaryFile/VERSION\" " +
    "xmlns:CA=\"http://www.openmicroscopy.org/Schemas/CA/VERSION\" " +
    "xmlns:STD=\"http://www.openmicroscopy.org/Schemas/STD/VERSION\" " +
    "xmlns:SPW=\"http://www.openmicroscopy.org/Schemas/SPW/VERSION\" " +
    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
    "xsi:schemaLocation=\"" +
    "http://www.openmicroscopy.org/Schemas/OME/VERSION " +
    "http://www.openmicroscopy.org/Schemas/OME/VERSION/ome.xsd " +
    "http://www.openmicroscopy.org/Schemas/BinaryFile/VERSION " +
    "http://www.openmicroscopy.org/Schemas/BinaryFile/VERSION/BinaryFile.xsd " +
    "http://www.openmicroscopy.org/Schemas/CA/VERSION " +
    "http://www.openmicroscopy.org/Schemas/CA/VERSION/CA.xsd " +
    "http://www.openmicroscopy.org/Schemas/SPW/VERSION " +
    "http://www.openmicroscopy.org/Schemas/SPW/VERSION/SPW.xsd " +
    "http://www.openmicroscopy.org/Schemas/STD/VERSION " +
    "http://www.openmicroscopy.org/Schemas/STD/VERSION/STD.xsd\"/>";
 
  /** Basic skeleton for an OME-XML root node with 2003-FC legacy schema. */
  protected static final String LEGACY_SKELETON =
    "<?xml version=\"1.0\"?>\n" +
    "<OME xmlns=\"http://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd\" " +
    "xmlns:STD=\"http://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd\" " +
    "xmlns:CA=\"http://www.openmicroscopy.org/XMLschemas/CA/RC1/CA.xsd\" " +
    "xmlns:Bin=\"http://www.openmicroscopy.org/XMLschemas/BinaryFile/RC1/" +
    "BinaryFile.xsd\" " +
    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
    "xsi:schemaLocation = \"" +
    "http://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/CA/RC1/CA.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/CA/RC1/CA.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/CA/RC1/BinaryFile.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/CA/RC1/BinaryFile.xsd\"/>";

  // -- Constructor --

  private OMEXMLFactory() { }

  // -- Static OMEXMLFactory API methods --

  /** Constructs a new, empty OME-XML root node of the latest schema version. */
  public static OMEXMLNode newOMENode()
    throws ParserConfigurationException, SAXException, IOException
  {
    return newOMENode(LATEST_VERSION);
  }

  /**
   * Constructs a new, empty OME-XML root node of the given schema version.
   * @param version The schema version (e.g., 2007-06) to use for the OME-XML.
   */
  public static OMEXMLNode newOMENode(String version)
    throws ParserConfigurationException, SAXException, IOException
  {
    String xml = null;
    if (version == null) version = LATEST_VERSION;
    if (version.equals("2003-FC")) xml = LEGACY_SKELETON; // legacy schema
    else xml = SKELETON.replaceAll("VERSION", version); // modern schema
    return newOMENodeFromSource(xml);
  }

  /** Constructs an OME-XML root node from the given file on disk. */
  public static OMEXMLNode newOMENodeFromSource(File file)
    throws ParserConfigurationException, SAXException, IOException
  {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    return newOMENodeFromSource(parseOME(file));
  }

  /** Constructs an OME-XML root node from the given XML string. */
  public static OMEXMLNode newOMENodeFromSource(String xml)
    throws ParserConfigurationException, SAXException, IOException
  {
    if (xml == null) {
      throw new IllegalArgumentException("XML string must not be null");
    }
    return newOMENodeFromSource(parseOME(xml));
  }

  /** Constructs an OME-XML root node from the given DOM tree. */
  public static OMEXMLNode newOMENodeFromSource(Document doc) {
    final String legacy = "http://www.openmicroscopy.org/XMLschemas/";
    final String modern = "http://www.openmicroscopy.org/Schemas/OME/";

    Element el = doc.getDocumentElement();
    String version = null;

    // parse schema version from xmlns attribute
    String xmlns = DOMUtil.getAttribute("xmlns:ome", el);
    if (xmlns == null) xmlns = DOMUtil.getAttribute("xmlns", el);
    if (xmlns == null) {
      throw new IllegalArgumentException(
        "Document does not contain an xmlns:ome or xmlns attribute");
    }
    xmlns = xmlns.trim();
    if (xmlns.startsWith(legacy)) {
      // legacy schema
      version = "2003fc";
    }
    else if (xmlns.startsWith(modern)) {
      // modern schema
      int len = modern.length();
      int slash = modern.indexOf("/", len);
      if (slash < 0) slash = xmlns.length();
      version = xmlns.substring(len, slash).replaceAll("\\W", "");
    }
    else {
      throw new IllegalArgumentException(
        "Document has unknown schema: " + xmlns);
    }

    Object o = null;
    try {
      Class c = Class.forName("ome.xml.r" + version + ".ome.OMENode");
      Constructor con = c.getConstructor(new Class[] {Element.class});
      o = con.newInstance(new Object[] {el});
    }
    catch (ClassNotFoundException exc) { }
    catch (NoClassDefFoundError err) { }
    catch (NoSuchMethodException exc) { }
    catch (InstantiationException exc) { }
    catch (IllegalAccessException exc) { }
    catch (InvocationTargetException exc) { }
    if (o == null || !(o instanceof OMEXMLNode)) {
      throw new IllegalArgumentException(
        "Unsupported schema version: " + version);
    }

    return (OMEXMLNode) o;
  }

  // -- Utility methods --

  /** Parses a DOM from the given OME-XML file on disk. */
  public static Document parseOME(File file)
    throws ParserConfigurationException, SAXException, IOException
  {
    InputStream is = new FileInputStream(file);
    Document doc = parseOME(is);
    is.close();
    return doc;
  }

  /** Parses a DOM from the given OME-XML string. */
  public static Document parseOME(String xml)
    throws ParserConfigurationException, SAXException, IOException
  {
    byte[] bytes = xml.getBytes();
    InputStream is = new ByteArrayInputStream(bytes);
    Document doc = parseOME(is);
    is.close();
    return doc;
  }

  /** Parses a DOM from the given OME-XML input stream. */
  public static Document parseOME(InputStream is)
    throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilder db = DOMUtil.DOC_FACT.newDocumentBuilder();
    return db.parse(is);
  }

}
