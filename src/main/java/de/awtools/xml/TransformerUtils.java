/*
 * $Id: TransformerUtils.java 2345 2010-07-31 14:30:28Z andrewinkler $
 * ============================================================================
 * Project awtools-xml
 * Copyright (c) 2000-2010 by Andre Winkler. All rights reserved.
 * ============================================================================
 *          GNU LESSER GENERAL PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.awtools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Einige Utils Methoden für das verarbeiten von XML.
 * 
 * @version $LastChangedRevision: 2345 $ $LastChangedDate: 2010-07-31 16:30:28 +0200 (Sa, 31 Jul 2010) $
 * @author by Andre Winkler, $LastChangedBy: andrewinkler $
 */
public final class TransformerUtils {

    /** Der private Logger der Klasse. */
    private static final Logger log = LoggerFactory.getLogger(TransformerUtils.class);

    /** Utlity Klasse. */
    protected TransformerUtils() {
    }

    /**
     * Transformiert ein dom4j Dokument anhand eines XSLT-Eingabestroms. Geht
     * was schief, wird eine <strong>UnhandledException</strong> geworfen.
     *
     * @param out Hier hin erfolgt die Ausgabe der Transformation.
     * @param xsltStream Ein XSLT Stylesheet als InputStream.
     * @param dom4jDocument Ein dom4j Dokument.
     */
    public static void transform(final OutputStream out,
        final InputStream xsltStream, final Document dom4jDocument) {

        // Den Transformer einladen ...
        Transformer transformer = TransformerUtils.getTransformer(xsltStream);

        // Das Dokument durchstylen ...
        TransformerUtils.transform(out, transformer, dom4jDocument);
    }

    /**
     * Eine <code>InputStream</code> mit XSL Daten.
     *
     * @param xsltStream <code>InputStream</code> mit XSL Daten.
     * @return Ein <code>Transformer</code>
     */
    public static Transformer getTransformer(final InputStream xsltStream) {
        return getTransformer(xsltStream, null);
    }

    /**
     * Eine <code>InputStream</code> mit XSL Daten.
     *
     * @param xsltStream <code>InputStream</code> mit XSL Daten.
     * @param resolver Ein <code>URIResolver</code>. Darf <code>null</code>
     *  sein.
     * @return Ein <code>Transformer</code>
     */
    public static Transformer getTransformer(final InputStream xsltStream,
        final URIResolver resolver) {

        Validate.notNull(xsltStream, "xsltStream not set");

        Source source = new StreamSource(xsltStream);
        TransformerFactory factory = TransformerFactory.newInstance();
        if (resolver != null) {
            factory.setURIResolver(resolver);
        }

        try {
            return factory.newTransformer(source);
        } catch (TransformerConfigurationException ex) {
            log.debug("Fehler:", ex);
            throw new UnhandledException(ex);
        }
    }

    /**
     * Transformiert ein dom4j Dokument anhand eines Transfomators in einen
     * OutputStream.
     *
     * @param out Der zu verwendende Ausgabestrom.
     * @param transe Ein Transformator.
     * @param dom4jDocument Ein dom4j Dokument.
     */
    public static void transform(final OutputStream out,
        final Transformer transe, final Document dom4jDocument) {

        Validate.notNull(out, "out not set");
        Validate.notNull(transe, "transe not set");
        Validate.notNull(dom4jDocument, "dom4jDocument not set");

        DocumentSource ds = new DocumentSource(dom4jDocument.getDocument());
        Result result = new StreamResult(out);

        try {
            transe.transform(ds, result);
        } catch (TransformerException ex) {
            log.debug("Catched an TransformerException", ex);
            throw new UnhandledException(ex);
        }
    }

    /**
     * Schreibt ein dom4j Dokument in eine Datei.
     *
     * @param document Ein dom4j Dokument.
     * @param file Die zu schreibende Datei.
     * @param encoding Das Encoding.
     * @throws UnhandledException Da ging was schief.
     */
    public static void toFile(final Document document, final File file,
        final String encoding) throws UnhandledException {

        XMLWriter writer = null;
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(encoding);
            writer = new XMLWriter(new FileWriter(file), format);
            writer.write(document);
        } catch (IOException ex) {
            log.debug("Fehler: ", ex);
            throw new UnhandledException(ex);
        } finally {
            XMLUtils.close(writer);
        }
    }

}
