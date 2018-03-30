/*
 * $Id: XSLTransformer.java 2345 2010-07-31 14:30:28Z andrewinkler $
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformiert ein XML Dokument mittels eines XSL Stylesheet in ein
 * Zieldokument. Ausgangsdokument, Stylesheet und Ausgabe liegen im
 * Filesystem.<br/> Das zugrunde liegende Stylesheet wird solange
 * wiederverwendet, bis es neu gesetzt wird.
 * 
 * @version $LastChangedRevision: 2345 $ $LastChangedDate: 2010-07-31 16:30:28 +0200 (Sa, 31 Jul 2010) $
 * @author by Andre Winkler, $LastChangedBy: andrewinkler $
 */
public class XSLTransformer {

    /** Der private Logger der Klasse. */
    private final Logger log = LoggerFactory.getLogger(XSLTransformer.class);

    // -- parameter -----------------------------------------------------------

    /** Parameter für den XSL Transformer. */
    private Map<String, Object> params = new HashMap<String, Object>();

    /**
     * Setzt einen neuen Parameter ein.
     * 
     * @param key Der Schlüssel.
     * @param value Der Wert.
     */
    public final void addParameter(final String key, final Object value) {
        params.put(key, value);
    }

    /**
     * Setzt die Parameterliste zurück.
     */
    public final void resetParams() {
        params.clear();
    }

    /**
     * Liefert ein nicht modifizierbare Map der Parameter-Map.
     * 
     * @return Ein nicht modifizierbare Map aller Parameter.
     */
    private Map<String, Object> getParams() {
        return params;
    }

    /**
     * Initialisiert den Transformer mit den Parametern.
     * 
     * @param _transe Der zu initialisierende Transformer.
     */
    private void addParams(final Transformer _transe) {
        for (String key : getParams().keySet()) {
            _transe.setParameter(key, getParams().get(key));
        }
    }

    // -- style ---------------------------------------------------------------

    /** Die Stylesheet-Datei als String. */
    private String style;

    /** Das Stylesheet als InputStream. */
    private InputStream styleStream;

    /** Das Stylesheet als File. */
    private File styleFile;

    /** Das Stylesheet verändert? */
    private boolean styleModified = false;

    /**
     * Liefert das Stylesheet als InputStream.
     * 
     * @return Ein InputStream.
     * @throws IOException Stylesheet konnte nicht gelesen werden.
     */
    public final InputStream getStyle() throws IOException {
        InputStream is = null;

        if (style != null) {
            if (log.isDebugEnabled()) {
                StringBuilder buf =
                        new StringBuilder("Load xslt by string: '").append(
                            getStyle()).append("'.");
                log.debug(buf.toString());
            }
            is = new FileInputStream(style);
        } else if (styleFile != null) {
            is = new FileInputStream(styleFile);
        } else if (styleStream != null) {
            is = styleStream;
        }

        return is;
    }

    /**
     * Setzt das Stylesheet.
     * 
     * @param value Das Stylesheet.
     */
    public final void setStyle(final String value) {
        style = value;
        styleModified = true;

        styleFile = null;
        styleStream = null;
    }

    /**
     * Setzt das Stylesheet.
     * 
     * @param value Das Stylesheet als InputStream.
     */
    public final void setStyle(final InputStream value) {
        styleStream = value;
        styleModified = true;

        style = null;
        styleFile = null;
    }

    /**
     * Setzt das Stylesheet.
     * 
     * @param value Das Stylesheet als File.
     */
    public final void setStyle(final File value) {
        styleFile = value;
        styleModified = true;

        style = null;
        styleStream = null;
    }

    // -- source --------------------------------------------------------------

    /** Die Eingabedatei. */
    private File source;

    /**
     * Liefert die Source.
     * 
     * @return Die Source.
     */
    public final File getSource() {
        return source;
    }

    /**
     * Setzt die Source.
     * 
     * @param value Die Source.
     */
    public final void setSource(final String value) {
        source = new File(value);
    }

    /**
     * Setzt das Source-File.
     *
     * @param value Die Source.
     */
    public final void setSource(final File value) {
        source = value;
    }

    // -- target --------------------------------------------------------------

    /** Die Ausgabedatei. */
    private File target;

    /**
     * Liefert die Ausgabedatei.
     * 
     * @return Die Ausgabedatei.
     */
    public final File getTarget() {
        return target;
    }

    /**
     * Setzt die Ausgabedatei.
     * 
     * @param value Die Ausgabedatei.
     */
    public final void setTarget(final String value) {
        target = new File(value);
    }

    /**
     * Setzt die Ausgabedatei.
     * 
     * @param value Die Ausgabedatei.
     */
    public final void setTarget(final File value) {
        target = value;
    }

    // -- uriResolver ---------------------------------------------------------

    /** Ein URIResolver. */
    private URIResolver uriResolver;

    /**
     * Liefert einen URIResolver.
     * 
     * @return Ein URIResolver.
     */
    public final URIResolver getUriResolver() {
        return uriResolver;
    }

    /**
     * Setzt den URIResolver.
     * 
     * @param value Ein URIResolver.
     */
    public final void setUriResolver(final URIResolver value) {
        uriResolver = value;
    }

    // -- encoding ------------------------------------------------------------

    /** Encoding. */
    private String encoding;

    /**
     * Liefert das Encoding.
     * 
     * @return Das Encoding.
     */
    public final String getEncoding() {
        return encoding;
    }

    /**
     * Setzt das Encoding.
     * 
     * @param value Das Encoding.
     */
    public final void setEncoding(final String value) {
        encoding = value;
    }

    // -- method --------------------------------------------------------------

    /** Ausgabeformat: html, xml oder text. */
    private String method;

    /**
     * Liefert das Ausgabeformat: html, xml oder text.
     * 
     * @return Das Ausgabeformat.
     */
    public final String getMethod() {
        return method;
    }

    /**
     * Setzt das Ausgabeformat: html, xml oder text.
     * 
     * @param value Das Ausgabeformat.
     */
    public final void setMethod(final String value) {
        method = value;
    }

    // -- omitXmlDeclaration --------------------------------------------------

    /** yes or no. XML Deklaration für generierte Datei? */
    private String omitXmlDeclaration;

    /**
     * Liefert OmitXmlDeclaration.
     * 
     * @return omitXmlDeclaration.
     */
    public final String getOmitXmlDeclaration() {
        return omitXmlDeclaration;
    }

    /**
     * Setzt das OmitXmlDeclaration
     * 
     * @param value omitXmlDeclaration.
     */
    public final void setOmitXmlDeclaration(final String value) {
        omitXmlDeclaration = value;
    }

    // ------------------------------------------------------------------------

    /** Das Stylesheet in compilierter Form. */
    private Transformer transe;

    /**
     * Liefert den Transformer.
     * 
     * @return Ein <code>Transformer</code>
     */
    private final Transformer getTransformer() {
        if ((transe == null) || (styleModified)) {
            InputStream xslt = null;
            try {
                xslt = getStyle();
                transe =
                        TransformerUtils.getTransformer(xslt, getUriResolver());

                transe.setOutputProperty(OutputKeys.ENCODING, getEncoding());
                transe.setOutputProperty(OutputKeys.METHOD, getMethod());
                transe.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    getOmitXmlDeclaration());

                styleModified = false;
            } catch (IOException ex) {
                log.debug("IOException", ex);
                throw new RuntimeException(ex);
            } finally {
                IOUtils.closeQuietly(xslt);
            }
        }
        return transe;
    }

    /**
     * Startet die Transformation eines XML Files.
     */
    public final void run() {
        Validate.notNull(getSource());
        Validate.notNull(getTarget());
        Validate.isTrue(!StringUtils.isBlank(getEncoding()));
        Validate.isTrue(!StringUtils.isBlank(getMethod()));
        Validate.isTrue(!StringUtils.isBlank(getOmitXmlDeclaration()));

        SAXReader reader = new SAXReader();
        try {
            cleanTargetFile(getTarget());

            if (!getTarget().canWrite()) {
                final String MSG = "Can not write to target file!";
                log.debug(MSG);
                throw new IllegalArgumentException(MSG);
            }

            if (!getSource().canRead()) {
                final String MSG = "Can not read source file!";
                log.debug(MSG);
                throw new IllegalArgumentException(MSG);
            }

            style(reader.read(getSource()), getTarget());
        } catch (MalformedURLException ex) {
            log.debug("MalformedURLException with file '" + getSource() + "'.",
                ex);
            throw new RuntimeException("MalformedURLException with file '"
                + getSource() + "'.", ex);
        } catch (DocumentException ex) {
            log.debug("DocumentException with file '" + getSource() + "'.", ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            log.debug("Can not delete or create target file '" + getTarget()
                + "'!", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Existiert das Target-File bereits, so wird dieses gelöscht und dann
     * neu angelegt.
     *
     * @param targetFile Target-Datei.
     * @throws IOException Die Datei konnte nicht gelöscht oder angelegt werden.
     */
    private void cleanTargetFile(final File targetFile) throws IOException {

        if (targetFile.exists()) {
            FileUtils.forceDelete(targetFile);
        }
        if (!targetFile.createNewFile()) {
            log.debug("Can not create target file '" + getTarget() + "'!");
        }
    }

    /**
     * Startet die Transformation eines dom4j Dokuments.
     * 
     * @param doc Ein dom4j Dokument.
     * @param targetFile Die Ausgabedatei.
     */
    private void style(final Document doc, final File targetFile) {
        FileOutputStream fous = null;
        try {
            fous = new FileOutputStream(targetFile);
            Transformer _transe = getTransformer();
            addParams(_transe);
            TransformerUtils.transform(fous, _transe, doc);
        } catch (FileNotFoundException ex) {
            log.debug("FileNotFoundException:", ex);
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(fous);
        }
    }

}
