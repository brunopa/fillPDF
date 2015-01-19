package service;

import com.ning.http.util.Base64;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.util.PDFTextStripper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class fillPDF {
    Boolean _disableFileds = false;

    public fillPDF(Boolean disable) {
        _disableFileds = disable;
    }

    public fillPDF() {
        _disableFileds = false;
    }

    public String decode(String entrada) throws UnsupportedEncodingException {
        byte[] bit = Base64.decode(entrada);
        return new String(bit, "UTF-8");
    }

    public OutputStream mesclarDecode(String objeto, String urlPDF) throws IOException, JSONException, COSVisitorException {
        objeto = decode(objeto);
        urlPDF = decode(urlPDF);

        return mesclar(objeto, urlPDF);
    }

    public OutputStream mesclarJsonPdf(String json, String urlPDF) throws IOException, JSONException, COSVisitorException {
        json = decode(json);
        urlPDF = decode(urlPDF);

        URL pdf = new URL(urlPDF);
        InputStream stream = pdf.openStream();
        return mesclar(new JSONObject(json), stream);
    }


    public OutputStream mesclar(String urlObjeto, String urlPDF) throws IOException, JSONException, COSVisitorException {
        URL pdf = new URL(urlPDF);
        InputStream stream = pdf.openStream();

        String objeto = leUrl(urlObjeto);
        JSONObject obj = new JSONObject(objeto);
        return mesclar(obj, stream);
    }


    public OutputStream mesclar(JSONObject objeto, InputStream PDF) throws IOException, COSVisitorException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDDocument pdfDocument = PDDocument.load(PDF);
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();

        List fields = acroForm.getFields();
        Iterator fieldsIter = fields.iterator();

        while (fieldsIter.hasNext()) {
            PDField field = (PDField) fieldsIter.next();
            processFields(field, objeto);
        }

        pdfDocument.save(bos);
        pdfDocument.close();
        return bos;
    }


    private void processFields(PDField field, JSONObject values) throws IOException {
        List kids = field.getKids();
        if (kids != null) {
            String chave = field.getPartialName();
            if (values.has(chave)) {
                String value = values.getString(chave);
                field.setValue(value);
                field.setReadonly(_disableFileds);
            }

            Iterator kidsIter = kids.iterator();
            while (kidsIter.hasNext()) {
                Object pdfObj = kidsIter.next();
                if (pdfObj instanceof PDField) {
                    PDField kid = (PDField) pdfObj;
                    processFields(kid, values);
                }
            }
        } else {

            try {
                field.setReadonly(_disableFileds);
                String chave = field.getPartialName();

                if (values.has(chave)) {
                    String value = values.getString(chave);
                    field.setValue(value);
                    field.setReadonly(_disableFileds);
                }
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    private String leUrl(String urlObjeto) throws IOException {
        URL url = new URL(urlObjeto);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        String line;
        String retorno = "";

        while ((line = reader.readLine()) != null) {
            retorno += line;
        }

        reader.close();

        retorno = retorno.trim();
        if (retorno.charAt(0) != '{')
            retorno = retorno.substring(1);

        return retorno;
    }

}


